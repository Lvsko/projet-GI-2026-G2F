package simulation;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentState;
import simulation.Statistics;
import java.util.ArrayList;
import java.util.List;

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

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.statistics = new Statistics();
        this.running = false;
        this.currentTick = 0;
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
    }

    /** Advances the simulation by one tick */
    public void step() {
        currentTick++;
        for (Agent agent : agents) {
            moveAgent(agent);
        }
        statistics.update(currentTick, (ArrayList<Agent>) agents);
    }
    /** Moves an agent one step forward */
    private void moveAgent(Agent agent) {
        if (agent.isInTransit()) {
            // agent is in an edge, make it arrive at the target node
            agent.arriveAt(agent.getCurrentEdge().getTarget());
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
    }

    /** Removes an agent from the simulation */
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
    public Statistics getStatistics() { return statistics; }
    public boolean isRunning() { return running; }
    public int getCurrentTick() { return currentTick; }
}
