package simulation;

import model.graph.Graph;
import model.graph.Node;
import model.graph.Edge;
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

    private static final float PANIC_PROBABILITY = 0.20f;
    
    private final java.util.Random random = new java.util.Random();
    private Graph graph;
    private List<Agent> agents;
    private Statistics statistics;
    private boolean running;
    private int currentTick;
    private Map<Agent, Integer> tickCounters;
    private Map<Agent, Integer> stuckCounters;
    private Pathfinder pathfinder;
    private List<Agent> exitingAgents;

    /**
     * Initializes a simulation engine bound to a specific environment graph.
     * @param graph simulation environment graph
     */
    public SimulationEngine(Graph graph) {
        this.graph        = graph;
        this.agents       = new ArrayList<>();
        this.statistics   = new Statistics();
        this.running      = false;
        this.currentTick  = 0;
        this.tickCounters = new HashMap<>();
        this.stuckCounters = new HashMap<>();
        this.pathfinder   = new Pathfinder();
        this.exitingAgents = new ArrayList<>();
    }

    /** Starts the simulation engine by setting its running state to true */
    public void start() { running = true; }

    /** Pauses the simulation engine by setting its running state to false */
    public void pause() { running = false; }

    /** Resets the simulation engine to its initial state */
    public void reset() {
        running      = false;
        currentTick  = 0;
        agents.clear();
        tickCounters.clear();
        stuckCounters.clear();
    }

    /**
     * Advances the simulation by one tick.
     * This method does not check whether the simulation is running. It is the caller's
     * responsibility (e.g., UI loop or controller) to decide when stepping is allowed.
     */
    public void step() {
        currentTick++;

        // Retirer les agents qui ont attendu 1 tick à l'EXIT
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
        exitingAgents.addAll(evacuatedAgents);

        propagatePanic();

        statistics.update(currentTick, (ArrayList<Agent>) agents, graph);
    }

    /**
     * Adds an agent to the simulation engine.
     * @param agent the agent to add to the simulation
     */
    public void addAgent(Agent agent) {
        if (agent.getDestinationNode() == null) {
            agent.setDestinationNode(pathfinder.chooseBestExit(agent, graph));
        }
        if (agent.getCurrentNode() != null && !agent.getCurrentNode().getAgents().contains(agent)) {
              agent.getCurrentNode().getAgents().add(agent);
        }
        agents.add(agent);
        tickCounters.put(agent, 0);
        stuckCounters.put(agent, 0);
    }

    /**
     * Removes an agent to the simulation engine.
     * @param agent the agent to remove to the simulation
     */
    public void removeAgent(Agent agent) {
        if (agent.getCurrentNode() != null) agent.getCurrentNode().removeAgent(agent);
        agents.remove(agent);
        exitingAgents.remove(agent);
        tickCounters.remove(agent);
        stuckCounters.remove(agent);
    }

    public Graph getGraph()              { return graph; }
    public List<Agent> getAgents()       { return agents; }
    public List<Agent> getExitingAgents() { return exitingAgents; }
    public Statistics getStatistics()    { return statistics; }
    public boolean isRunning()           { return running; }
    public int getCurrentTick()          { return currentTick; }

    /**
     * Updates the movement state of an agent during a single simulation tick.
     * @param agent the agent to update
     * @return true if the agent reached an exit and should be removed from the simulation
     */
    private boolean moveAgent(Agent agent) {
        int counter = tickCounters.get(agent);
        tickCounters.put(agent, counter + 1);

        boolean canMove;
        switch (agent.getType()) {
            case CHILD: canMove = (counter % 10) != 2 && (counter % 10) != 5 && (counter % 10) != 9; break;
            case PMR:   canMove = (counter % 2) == 0; break;
            default:    canMove = true;
        }
        if (canMove) {
            if (agent.isInTransit()) {
                // Agent en transit dans un couloir
                Edge edge = agent.getCurrentEdge();
                Node destination = edge.getSource().equals(agent.getPreviousNode())
                    ? edge.getTarget()
                    : edge.getSource();

                arriveAt(agent, destination);

                if (destination.getType() == model.graph.NodeType.EXIT) {
                    destination.removeAgent(agent);
                    return true;
                }
            } else if (!agent.getCurrentPath().isEmpty()) {
                // Agent dans un nœud, en attente d'entrer dans l'arête suivante
                Node nextNode = agent.getCurrentPath().get(0);
                Edge edge     = findEdge(agent.getCurrentNode(), nextNode);

                if (edge != null) {
                    if (isEdgeAvailable(edge) && moveToEdge(agent, edge)) {
                        statistics.recordAgentPassedEdge(edge);
                        agent.getCurrentPath().remove(0);
                        stuckCounters.put(agent, 0);

                        Node destination = edge.getSource().equals(agent.getPreviousNode())
                            ? edge.getTarget()
                            : edge.getSource();

                        if (destination.getType() == model.graph.NodeType.EXIT) {
                            arriveAt(agent, destination);
                            statistics.recordAgentPassedNode(destination);
                            destination.removeAgent(agent);
                            return true;
                        }
                    } else {
                        // Arête pleine → incrémenter le compteur de blocage
                        int stuckTicks = stuckCounters.getOrDefault(agent, 0) + 1;
                        stuckCounters.put(agent, stuckTicks);

                        if (stuckTicks >= 3) {
                            List<Node> newPath = pathfinder.dijkstraTime(
                                agent.getCurrentNode(),
                                agent.getDestinationNode(),
                                graph
                            );
                            if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
                            agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
                            stuckCounters.put(agent, 0);
                        }
                    }
                }

            } else {
                // Aucun chemin disponible — réessayer à chaque tick.
                // Couvre : arête/nœud supprimé puis nouveau chemin créé,
                //          ou graphe temporairement déconnecté.
                int stuckTicks = stuckCounters.getOrDefault(agent, 0) + 1;
                stuckCounters.put(agent, stuckTicks);

                if (stuckTicks >= 1) {
                    List<Node> newPath = pathfinder.dijkstraTime(
                        agent.getCurrentNode(),
                        agent.getDestinationNode(),
                        graph
                    );
                    if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
                    agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
                    stuckCounters.put(agent, 0);
                }
            }
        }
        return false;
    }

    /**
     * Finds an edge connecting two nodes in the graph.
     * @param from the source node
     * @param to the target node
     * @return the edge connecting the two nodes
     */
    private Edge findEdge(Node from, Node to) {
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().equals(from) && edge.getTarget().equals(to)) return edge;
            if (!edge.isDirected() && edge.getTarget().equals(from) && edge.getSource().equals(to)) return edge;
        }
        return null;
    }

    /**
     * Propagates panic between agents located on the same node.
     */
    private void propagatePanic() {
        Map<Node, List<Agent>> agentsOnNodes = new HashMap<>();
        for (Agent agent : agents) {
            if (!agent.isInTransit() && agent.getCurrentNode() != null) {
                agentsOnNodes.computeIfAbsent(agent.getCurrentNode(), k -> new ArrayList<>()).add(agent);
            }
        }
        for (List<Agent> nodeAgents : agentsOnNodes.values()) {
            if (nodeAgents.size() < 2) continue;
            boolean hasPanicked = nodeAgents.stream().anyMatch(a -> a.getState() == AgentState.PANICKED);
            if (hasPanicked) {
                for (Agent a : nodeAgents) {
                    if (a.getState() == AgentState.CALM && random.nextFloat() < PANIC_PROBABILITY) {
                        a.setState(AgentState.PANICKED);
                    }
                }
            }
        }
    }

    /**
     * Moves the agent to a neighboring edge.
     * @param agent agent to move
     * @param edge edge where the agent is going
     * @return true if the move was successful, false if the edge is full
     */
    private boolean moveToEdge(Agent agent, Edge edge) {
        if (!isEdgeAvailable(edge)) {
            return false;
        }
        if (agent.getCurrentNode() != null) {
            agent.setPreviousNode(agent.getCurrentNode());
            agent.getCurrentNode().getAgents().remove(agent);
            agent.setCurrentNode(null);
        }
        agent.setCurrentEdge(edge);
        edge.getAgents().add(agent);
        return true;
    }

    /**
     * Arrive on a node, end of transit on the edge.
     * @param agent agent who arrives
     * @param node arrival node
     */
    private void arriveAt(Agent agent, Node node) {
        if (agent.getCurrentEdge() != null) {
            agent.getCurrentEdge().getAgents().remove(agent);
            agent.setCurrentEdge(null);
        }
        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().getAgents().remove(agent);
        }
        agent.setCurrentNode(node);
        node.getAgents().add(agent);
    }
    
    private boolean isEdgeAvailable(Edge edge) {
        return edge.getAgents().size() < edge.getWidth();
    }

}
