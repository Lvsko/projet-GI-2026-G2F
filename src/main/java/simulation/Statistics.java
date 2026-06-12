package simulation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import model.agent.Agent;
import model.node.Node;
import model.Edge;
import model.Graph;

/**
 * Tracks and computes evacuation statistics during the simulation.
 * 
 * @author Ruben
 */
public class Statistics implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int evacuatedCount;
	private int totalTicks;
	private List<Edge> bottlenecks;

	// Total agents who passed through each edge/node
    private Map<Edge, Integer> edgePassCount;
    private Map<Node, Integer> nodePassCount;

    // Peak occupancy reached for each edge/node
    private Map<Edge, Integer> edgePeakOccupancy;
    private Map<Node, Integer> nodePeakOccupancy;

    // How many ticks each edge was saturated / each node was overloaded
    private Map<Edge, Integer> edgeSaturationTicks;
    private Map<Node, Integer> nodeOverloadTicks;

    // Tick when each agent was evacuated (for average evacuation time)
    private Map<String, Integer> agentEvacuationTicks;

	public Statistics() {
		this.evacuatedCount = 0;
		this.totalTicks = 0;
		this.bottlenecks = new ArrayList<Edge>();
		this.edgePassCount = new HashMap<>();
        this.nodePassCount = new HashMap<>();
        this.edgePeakOccupancy = new HashMap<>();
        this.nodePeakOccupancy = new HashMap<>();
        this.edgeSaturationTicks = new HashMap<>();
        this.nodeOverloadTicks = new HashMap<>();
        this.agentEvacuationTicks = new HashMap<>();
	}

	/**
	 * Updates the simulation statistics.
	 * 
	 * @param ticks  current tick count
	 * @param agents list of agents in the simulation
	 * @param graph  the building graph
	 */
	public void update(int ticks, ArrayList<Agent> agents, Graph graph) {
		totalTicks = ticks;

		// Update peak occupancy
		for (Edge edge : graph.getEdges()) {
            int occ = edge.getOccupancy();
            if (occ > edgePeakOccupancy.getOrDefault(edge, 0)) {
                edgePeakOccupancy.put(edge, occ);
            }
            // Track saturation ticks
            if (occ >= edge.getWidth()) {
                edgeSaturationTicks.put(edge, edgeSaturationTicks.getOrDefault(edge, 0) + 1);
                if (!bottlenecks.contains(edge)) {
                    bottlenecks.add(edge);
                }
            }
        }
		// Update peak occupancy
        for (Node node : graph.getNodes()) {
            int occ = node.getOccupancy();
            if (occ > nodePeakOccupancy.getOrDefault(node, 0)) {
                nodePeakOccupancy.put(node, occ);
            }
            // Track overload ticks
            if (node.isOverloaded()) {
                nodeOverloadTicks.put(node, nodeOverloadTicks.getOrDefault(node, 0) + 1);
            }
        }
		
	}
	
	/** 
	 * Called when an agent passes through a node 
	 * @param node the node that has been passed by an agent
	 */
    public void recordAgentPassedNode(Node node) {
        nodePassCount.put(node, nodePassCount.getOrDefault(node, 0) + 1);
    }

    /** 
	 * Called when an agent passes through an edge
	 * @param edge the edge that has been crossed by an agent
	 */
    public void recordAgentPassedEdge(Edge edge) {
        edgePassCount.put(edge, edgePassCount.getOrDefault(edge, 0) + 1);
    }

    /** 
	 * Called when an agent is evacuated 
	 * @param agentId the identifier of the evacuated agent
	 * @param tick the simulation tick at which the agent exited
	 */
    public void recordAgentEvacuated(String agentId, int tick) {
        agentEvacuationTicks.put(agentId, tick);
        evacuatedCount++;
    }
	

	/** 
	 * @return the average evacuation time across all evacuated agents 
	 */
    public float getAverageEvacuationTime() {
        if (agentEvacuationTicks.isEmpty()) return 0;
        int total = 0;
        for (int tick : agentEvacuationTicks.values()) {
            total += tick;
        }
        return (float) total / agentEvacuationTicks.size();
    }

    /**
     * Generates automatic analysis sentences.
     * @return list of analysis messages
     */
    public List<String> generateAnalysis() {
        List<String> analysis = new ArrayList<>();

        // Edge saturation analysis
        Edge worstEdge = null;
        int worstEdgeTicks = 0;
        for (Map.Entry<Edge, Integer> entry : edgeSaturationTicks.entrySet()) {
            analysis.add("L'arête " + entry.getKey().getId() + " a été saturée pendant " + entry.getValue() + " ticks");
            if (entry.getValue() > worstEdgeTicks) {
                worstEdgeTicks = entry.getValue();
                worstEdge = entry.getKey();
            }
        }
        if (worstEdge != null) {
            analysis.add("→ " + worstEdge.getId() + " était le goulot principal");
        }

        // Node overload analysis
        for (Map.Entry<Node, Integer> entry : nodeOverloadTicks.entrySet()) {
            analysis.add("La salle " + entry.getKey().getName() + " était surchargée pendant " + entry.getValue() + " ticks");
        }

        if (analysis.isEmpty()) {
            analysis.add("Aucun goulot d'étranglement détecté");
        }

        return analysis;
    }

	public int getEvacuatedCount() { return evacuatedCount; }
	public int getTotalTicks() { return totalTicks; }
	public List<Edge> getBottlenecks() { return bottlenecks; }
	public Map<Edge, Integer> getEdgePassCount() { return edgePassCount; }
    public Map<Node, Integer> getNodePassCount() { return nodePassCount; }
    public Map<Edge, Integer> getEdgePeakOccupancy() { return edgePeakOccupancy; }
    public Map<Node, Integer> getNodePeakOccupancy() { return nodePeakOccupancy; }
    public Map<Edge, Integer> getEdgeSaturationTicks() { return edgeSaturationTicks; }
    public Map<Node, Integer> getNodeOverloadTicks() { return nodeOverloadTicks; }

	@Override
	public String toString() {
		return "Statistics [evacuatedCount=" + evacuatedCount + ", totalTicks=" + totalTicks + ", bottlenecks="
				+ bottlenecks + "]";
	}
	
	
}
