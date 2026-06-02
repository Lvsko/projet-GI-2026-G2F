package simulation;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentType;
import model.agent.AgentState;
import simulation.Statistics;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controls the simulation loop and manages agent movements at each tick.
 * @author Yoni
 */
public class SimulationEngine {

    private Graph graph;
    private List<Agent> agents;
    private Statistics statistics;
    private boolean running;
    private int currentTick;
    private Map<Agent, Integer> tickCounters;

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.statistics = new Statistics();
        this.running = false;
        this.currentTick = 0;
        this.tickCounters = new HashMap<>();
    }

    /** Starts the simulation */
    public void start() {
        running = true;
    }

    /** Pauses the simulation */
    public void pause() {
        running = false;
    }

    /** Resets the simulation to its initial state */
    public void reset() {
        running = false;
        currentTick = 0;
        agents.clear();
        tickCounters.clear();
    }

    /** Advances the simulation by one tick */
    public void step() {
        currentTick++;

        List<Agent> evacuatedAgents = new ArrayList<>();

        for (Agent agent : agents) {
            if (moveAgent(agent)) {
                evacuatedAgents.add(agent);
            }
        }

        for (Agent agent : evacuatedAgents) {
            removeAgent(agent);
        }

        statistics.update(currentTick, (ArrayList<Agent>) agents);
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
                    return true;
                }

            } else if (!agent.getCurrentPath().isEmpty()) {
                // agent is in a node, find the next edge
            	
                Node nextNode = agent.getCurrentPath().get(0);
                Edge edge = findEdge(agent.getCurrentNode(), nextNode);
                if (edge != null && edge.isAvailable()) {
                    if (agent.moveToEdge(edge)) {
                        agent.getCurrentPath().remove(0);
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


    /** Adds an agent to the simulation */
    public void addAgent(Agent agent) {
        agents.add(agent);
        tickCounters.put(agent, 0);
    }

    /** Removes an agent from the simulation */
    public void removeAgent(Agent agent) {

        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().removeAgent(agent);
        }

        agents.remove(agent);
        tickCounters.remove(agent);
    }

    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
    public Statistics getStatistics() { return statistics; }
    public boolean isRunning() { return running; }
    public int getCurrentTick() { return currentTick; }
}
