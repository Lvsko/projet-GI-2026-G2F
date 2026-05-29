package model.agent;

import model.agent.AgentBehavior;
import model.agent.AgentState;
import model.agent.AgentType;
import model.node.NodeType;
import model.Edge;
import model.Graph;
import model.Node;
import simulation.Pathfinder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Represents a person evacuating the building.
 * @author Clement
 */
public class Agent implements Serializable {

    private final String id;
    private Node currentNode;
    private Edge currentEdge;
    private Node destinationNode;
    private float maxSpeed;
    private AgentState state;
    private AgentBehavior behavior;
    private AgentType type;
    private float densityTolerance;
	private List<Node> currentPath = new ArrayList<>();
    
    private static int numberAgent = 0;

    /**
     * Creates an agent with an identifier and a starting node.
     * @param id          unique identifier of the agent
     * @param startNode   initial node of the agent
	 * @param destinationNode target exit node
     * @param maxSpeed maximum speed of the agent
     * @param state initial psychological state
     * @param behavior movement behavior
     * @param type physical profile
     * @param densityTolerance tolerance to crowded areas
     */
    public Agent(String id, Node startNode, float maxSpeed, AgentState state, AgentBehavior behavior, AgentType type, float densityTolerance, Graph graph) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifier can't be empty");
        }
        numberAgent++;
        this.id = id;
        this.currentNode = startNode;
        this.currentEdge = null;
		this.maxSpeed = maxSpeed;
        this.state = state;
        this.behavior = behavior;
        this.type = type;
        this.densityTolerance = densityTolerance;
		this.destinationNode = chooseBestExit(graph);
        if (startNode != null) {
            startNode.addAgent(this);
        }
    }
    
    /**
     * Creates an agent with an identifier "agentN" and a starting node.
     * @param startNode   initial node of the agent
     */

    public Agent(Node startNode, Graph graph) {
        this(
            "agent" + numberAgent,
            startNode,
            1.0f,
            AgentState.CALM,
            AgentBehavior.COOPERATIVE,
            AgentType.ADULT,
            0.5f,
            graph
        );
    }

    
    /** 
     * @return the agent's identifier
     */
    public String getId() { return id; }
    /**
     * @return the agent's current node
     */
    public Node getCurrentNode() { return currentNode; }
    /**
     * @return the agent's current edge
     */
    public Edge getCurrentEdge() { return currentEdge; }

    
    public Node getDestinationNode() {	return destinationNode;	}
	public void setDestinationNode(Node destinationNode) { this.destinationNode = destinationNode; }
	public float getMaxSpeed() { return maxSpeed; }
    public AgentState getState() { return state; }
    public void setState(AgentState state) { this.state = state; }
    public AgentBehavior getBehavior() { return behavior; }
    public AgentType getType() { return type; }
    public float getDensityTolerance() { return densityTolerance; }
	public List<Node> getCurrentPath() { return currentPath; }
	public void setCurrentPath(List<Node> path) { this.currentPath = path; }
	
	    /**
     * Automatically chooses the best output according to the agent's state.
     * - CALM / INJURED    → optimal exit via dijkstraTime 
     * - PANICKED → nearest exit via dijkstraDistance 
     * @param graph     graph of the building
     * @return the best exit
     */
    private Node chooseBestExit(Graph graph) {
    	Pathfinder pathfinder = new Pathfinder();
    	Node bestExit = null;
    	float bestScore = Float.MAX_VALUE;
		List<Node> bestPath = new ArrayList<>();
		
    	for (Node exit :  graph.getNodesByType(NodeType.EXIT)) {
        	List<Node> path = state == AgentState.PANICKED
            	? pathfinder.dijkstraDistance(currentNode, exit, graph)
            	: pathfinder.dijkstraTime(currentNode, exit, graph);

        	if (path.isEmpty()) continue; // inaccessible exit

        	float score = path.size();
        	if (score < bestScore) {
            	bestScore = score;
            	bestExit = exit;
				bestPath = path;
        	}
    	}
		if (!bestPath.isEmpty()) {
        	bestPath.remove(0);
		}
		this.currentPath = bestPath;
    	return bestExit;
	}

	/**
     * Moves the agent to a neighboring edge.
     * @param edge edge where the agent is going
     * @return true if the move was successful, false if the edge is full
     */
    public boolean moveToEdge(Edge edge) {
        if (!edge.isAvailable()) {
            return false;
        }
        if (this.currentNode != null) {
            this.currentNode.removeAgent(this);
            this.currentNode = null;
        }
        this.currentEdge = edge;
        edge.addAgent(this);
        return true;
    }

    /**
     * Arrive on a node, end of transit on the edge.
     * @param node arrival node
     */
    public void arriveAt(Node node) {
        // Quitte l'arête actuelle
        if (this.currentEdge != null) {
            this.currentEdge.removeAgent(this);
            this.currentEdge = null;
        }
        this.currentNode = node;
        node.addAgent(this);
    }

    /** 
     * @return true if the agent is on an edge
     */
    public boolean isInTransit() {
        return (this.currentEdge != null);
    }

    @Override
    public String toString() {
        if (isInTransit()) {
            return "Agent{id='" + id + "', edge='" + currentEdge.getId() + "'}";
        }
        return "Agent{id='" + id + "', node='" + (currentNode != null ? currentNode.getId() : "none") + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent)) return false;
        Agent agent = (Agent) o;
        return Objects.equals(id, agent.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
