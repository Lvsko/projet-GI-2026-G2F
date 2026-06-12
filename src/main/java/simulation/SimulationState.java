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

	/**
 	 * Creates a snapshot of the current simulation state.
 	 * @param graph the simulation graph at the time of the snapshot
 	 * @param agents the list of agents currently in the simulation
 	 * @param currentTick the current simulation tick
	 */
	public SimulationState(Graph graph, List<Agent> agents, int currentTick) {
	    this.graph = graph;
	    this.agents = agents;
	    this.currentTick = currentTick;
	}

	/**
  	 * Serializes and saves the current object to a file.
 	 * @param path the file path where the object should be saved
	 */
	public void save(String path) throws IOException {
    	try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
        	oos.writeObject(this);
    	}
	}

	/**
 	 * Loads a previously saved simulation state from a file.
	 * @param path the file path from which the simulation state is loaded
	 * @return the deserialized SimulationState object
 	 */
	public static SimulationState load(String path) throws IOException, ClassNotFoundException {
    	try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
        	return (SimulationState) ois.readObject();
    	}
	}
	
	public Graph getGraph() { return graph; }
	public List<Agent> getAgents() { return agents; }
	public int getCurrentTick() { return currentTick; }
}
