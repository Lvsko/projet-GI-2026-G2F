package exit.model;

import exit.model.enums.AgentBehavior;
import exit.model.enums.AgentState;
import exit.model.enums.AgentType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a person evacuating the building.
 * @author Clement
 */
public class Agent {

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
    public Agent(String id, Node startNode, Node destinationNode, float maxSpeed, AgentState state, AgentBehavior behavior, AgentType type, float densityTolerance ) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifier can't be empty");
        }
        numberAgent++;
        this.id = id;
        this.currentNode = startNode;
        this.currentEdge = null;
		this.destinationNode = destinationNode;
		this.maxSpeed = maxSpeed;
        this.state = state;
        this.behavior = behavior;
        this.type = type;
        this.densityTolerance = densityTolerance;
        if (startNode != null) {
            startNode.addAgent(this);
        }
    }
    
    /**
     * Creates an agent with an identifier "agentN" and a starting node.
     * @param startNode   initial node of the agent
     */
    public Agent(Node startNode, Node destinationNode) {
    	this("agent"+numberAgent, startNode, destinationNode, 1.0f, AgentState.CALM, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f);
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

    
    public Node getDestinationNode() {
		return destinationNode;
	}
	public float getMaxSpeed() { return maxSpeed; }
    public AgentState getState() { return state; }
    public void setState(AgentState state) { this.state = state; }
    public AgentBehavior getBehavior() { return behavior; }
    public AgentType getType() { return type; }
    public float getDensityTolerance() { return densityTolerance; }
	public List<Node> getCurrentPath() { return currentPath; }
	public void setCurrentPath(List<Node> path) { this.currentPath = path; }
	
	

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
