package simulation;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentType;
import model.agent.AgentState;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls the simulation loop and manages agent movements at each tick.
 * @author Yoni
 * @contributor Lassina (KAN-36 & Panic Propagation integration)
 */
public class SimulationEngine {

    private Graph graph;
    private List<Agent> agents;
    private Statistics statistics;
    private boolean running;
    private int currentTick;

    // Counters for speed management (Yoni's logic)
    private Map<Agent, Integer> tickCounters;

    // KAN-36: Counters to track how long agents have been stuck waiting for an edge
    private Map<Agent, Integer> stuckCounters;

    // Pathfinder instance needed for recalculation
    private Pathfinder pathfinder;

    // Agents waiting 1 tick at EXIT before being removed (visual effect)
    private List<Agent> exitingAgents;

    // PANIC PROPAGATION MECHANICS
    private static final float PANIC_PROBABILITY = 0.20f;
    private final java.util.Random random = new java.util.Random();

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.statistics = new Statistics();
        this.running = false;
        this.currentTick = 0;
        this.tickCounters = new HashMap<>();
        this.stuckCounters = new HashMap<>();
        this.pathfinder = new Pathfinder();
        this.exitingAgents = new ArrayList<>();
    }

    /** Starts the simulation */
    public void start() { running = true; }

    /** Pauses the simulation */
    public void pause() { running = false; }

    /** Resets the simulation to its initial state */
    public void reset() {
        running = false;
        currentTick = 0;
        agents.clear();
        tickCounters.clear();
        stuckCounters.clear(); // Reset stuck counters too
    }

    /** Advances the simulation by one tick */
    public void step() {
        if (!running) return;
        currentTick++;

        // Remove agents that waited 1 tick at EXIT
        for (Agent agent : exitingAgents) {
            if (agent.getCurrentNode() != null) {
                agent.getCurrentNode().removeAgent(agent);
            }
            agents.remove(agent);
            tickCounters.remove(agent);
            stuckCounters.remove(agent);
            statistics.recordAgentEvacuated(agent.getId(), currentTick);
        }
        exitingAgents.clear();

        List<Agent> evacuatedAgents = new ArrayList<>();
        for (Agent agent : new ArrayList<>(agents)) {
            if (moveAgent(agent)) {
                evacuatedAgents.add(agent);
            }
        }

        // Move evacuated agents to exitingAgents (visible 1 tick at EXIT)
        exitingAgents.addAll(evacuatedAgents);

        // Propagate panic before updating statistics
        propagatePanic();

        statistics.update(currentTick, (ArrayList<Agent>) agents, graph);
    }

    /** Moves an agent one step forward */
    private boolean moveAgent(Agent agent) {
        int counter = tickCounters.get(agent);
        tickCounters.put(agent, counter + 1);

        boolean canMove;
        switch (agent.getType()) {
            case CHILD: canMove = (counter % 10) != 2 && (counter % 10) != 5 && (counter % 10) != 9; break; // move 7 ticks out of 10
            case PMR:   canMove = (counter % 2) == 0; break;
            default:    canMove = true;
        }

        if (canMove) {
            if (agent.isInTransit()) {
                // Agent is moving inside a corridor (Edge)
                Edge edge = agent.getCurrentEdge();
                Node destination;

                if (edge.getSource().equals(agent.getPreviousNode())) {
                    destination = edge.getTarget();
                } else {
                    destination = edge.getSource();
                }

                agent.arriveAt(destination);

                if (destination.getType() == model.node.NodeType.EXIT) {
                    destination.removeAgent(agent);
                    return true; // Agent evacuated
                }

            } else if (!agent.getCurrentPath().isEmpty()) {
                // Agent is in a node, waiting to enter the next edge
                Node nextNode = agent.getCurrentPath().get(0);
                Edge edge = findEdge(agent.getCurrentNode(), nextNode);

                if (edge != null) {
                    if (edge.isAvailable() && agent.moveToEdge(edge)) {
                        statistics.recordAgentPassedEdge(edge);
                        agent.getCurrentPath().remove(0);
                        stuckCounters.put(agent, 0);
                        // Exit immédiat si la destination est EXIT
                        Node destination;
                        if (edge.getSource().equals(agent.getPreviousNode())) {
                            destination = edge.getTarget();
                        } else {
                            destination = edge.getSource();
                        }
                        if (destination.getType() == model.node.NodeType.EXIT) {
                            agent.arriveAt(destination);
                            statistics.recordAgentPassedNode(destination);
                            destination.removeAgent(agent);
                            return true;
                        }
                    } else {
                        // Edge plein — incrémenter le compteur de blocage
                        int stuckTicks = stuckCounters.getOrDefault(agent, 0) + 1;
                        stuckCounters.put(agent, stuckTicks);
                        // Recalculer le chemin après 3 ticks bloqué
                        if (stuckTicks >= 3) {
                            List<Node> newPath = pathfinder.dijkstraTime(
                                    agent.getCurrentNode(),
                                    agent.getDestinationNode(),
                                    graph
                            );
                            if (newPath != null && !newPath.isEmpty()) {
                                newPath.remove(0);
                            }
                            agent.setCurrentPath(newPath);
                            stuckCounters.put(agent, 0);
                        }
                    }
                }
            }
        }
        return false;
    }

    /** Finds the edge connecting two nodes */
    private Edge findEdge(Node from, Node to) {
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().equals(from) && edge.getTarget().equals(to)) {
                return edge;
            }
            if (!edge.isDirected() && edge.getTarget().equals(from) && edge.getSource().equals(to)) {
                return edge;
            }
        }
        return null;
    }

    /**
     * Spreads panic between agents sharing the same node.
     * Emergent behavior: A calm agent has a 20% chance to become panicked
     * if exposed to a panicked agent on the same node.
     */
    private void propagatePanic() {
        Map<Node, List<Agent>> agentsOnNodes = new HashMap<>();

        // Group agents by their current node (excluding those in transit in corridors)
        for (Agent agent : agents) {
            if (!agent.isInTransit() && agent.getCurrentNode() != null) {
                agentsOnNodes.computeIfAbsent(agent.getCurrentNode(), k -> new ArrayList<>()).add(agent);
            }
        }

        // Evaluate each node individually
        for (List<Agent> nodeAgents : agentsOnNodes.values()) {
            if (nodeAgents.size() < 2) continue; // Optimization

            // Check if there is at least one PANICKED agent on this node
            boolean hasPanickedAgent = false;
            for (Agent a : nodeAgents) {
                if (a.getState() == AgentState.PANICKED) {
                    hasPanickedAgent = true;
                    break;
                }
            }

            // If "infected", roll the dice for CALM agents
            if (hasPanickedAgent) {
                for (Agent a : nodeAgents) {
                    if (a.getState() == AgentState.CALM) {
                        if (random.nextFloat() < PANIC_PROBABILITY) {
                            a.setState(AgentState.PANICKED);
                        }
                    }
                }
            }
        }
    }

    /** Adds an agent to the simulation */
    public void addAgent(Agent agent) {
        agents.add(agent);
        tickCounters.put(agent, 0);
        stuckCounters.put(agent, 0); // Initialize stuck counter
    }

    /** Removes an agent from the simulation */
    public void removeAgent(Agent agent) {
        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().removeAgent(agent);
        }
        agents.remove(agent);
        exitingAgents.remove(agent);
        tickCounters.remove(agent);
        stuckCounters.remove(agent);
    }

    // Getters
    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
    public List<Agent> getExitingAgents() { return exitingAgents; }
    public Statistics getStatistics() { return statistics; }
    public boolean isRunning() { return running; }
    public int getCurrentTick() { return currentTick; }
}