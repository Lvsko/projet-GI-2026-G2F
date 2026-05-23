package exit.simulation;

import exit.model.Agent;
import exit.model.Graph;
import exit.stats.Statistics;
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
        // TODO - appeler agent.update() pour chaque agent
        for (Agent agent : agents) {
            // TODO - Yoni : logique de déplacement
        }
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
