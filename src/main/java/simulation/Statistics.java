package simulation;

import java.util.ArrayList;
import java.util.List;

import model.agent.Agent;
import model.node.Node;
import model.Edge;

/**
 * Tracks and computes evacuation statistics during the simulation.
 * 
 * @author Ruben
 */
public class Statistics {
	private int evacuatedCount;
	private int totalTicks;
	private List<Edge> bottlenecks;

	public Statistics() {
		this.evacuatedCount = 0;
		this.totalTicks = 0;
		this.bottlenecks = new ArrayList<Edge>();
	}

	/**
	 * Updates the simulation statistics.
	 * 
	 * @param ticks  current tick count
	 * @param agents list of agents in the simulation
	 */
	public void update(int ticks, ArrayList<Agent> agents) {
		totalTicks = ticks;
		evacuatedCount = 0;
		for (Agent a : agents) {
			if (a.getCurrentNode() != null && a.getCurrentNode().equals(a.getDestinationNode())) {
				this.evacuatedCount += 1;
			}
		}
	}

	/**
	 * @return the number of evacuated agents
	 */
	public int getEvacuatedCount() {
		return evacuatedCount;
	}

	/**
	 * @return the total number of ticks elapsed
	 */
	public int getTotalTicks() {
		return totalTicks;
	}

	/**
	 * @return the list of congested edges
	 */
	public List<Edge> getBottlenecks() {
		return bottlenecks;
	}

	@Override
	public String toString() {
		return "Statistics [evacuatedCount=" + evacuatedCount + ", totalTicks=" + totalTicks + ", bottlenecks="
				+ bottlenecks + "]";
	}
	
	
}
