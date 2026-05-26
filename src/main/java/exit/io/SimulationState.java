package exit.io;

import exit.model.Agent;
import exit.model.Graph;
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
	    FileOutputStream fos = new FileOutputStream(path);
	    ObjectOutputStream oos = new ObjectOutputStream(fos);
	    oos.writeObject(this);
	    oos.close();
	}
	
	public static SimulationState load(String path) throws IOException, ClassNotFoundException {
	    FileInputStream fis = new FileInputStream(path);
	    ObjectInputStream ois = new ObjectInputStream(fis);
	    SimulationState state = (SimulationState) ois.readObject();
	    ois.close();
	    return state;
	}
	
	public Graph getGraph() { return graph; }
	public List<Agent> getAgents() { return agents; }
	public int getCurrentTick() { return currentTick; }
}
