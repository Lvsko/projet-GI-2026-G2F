package simulation;

import model.Graph;
import model.agent.Agent;
import java.io.*;
import java.util.List;
/**
 * Handles saving and loading the simulation state to a binary file.
 * @author Ruben
 */
public class SimulationState implements Serializable {
	private Graph graph;
	private List<Agent> agents;
	private int currentTick;
	
	public SimulationState(Graph graph, List<Agent> agents, int currentTick) {
	    this.graph = graph;
	    this.agents = agents;
	    this.currentTick = currentTick;
	}
	
	public void save(String path) throws IOException {
    	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
        	oos.writeObject(this);
    	}
	}
	
	public static SimulationState load(String path) throws IOException, ClassNotFoundException {
    	try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
        	return (SimulationState) ois.readObject();
    	}
	}
	
	public Graph getGraph() { return graph; }
	public List<Agent> getAgents() { return agents; }
	public int getCurrentTick() { return currentTick; }
}
