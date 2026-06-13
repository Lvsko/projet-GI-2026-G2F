package model.agent;

import model.agent.AgentBehavior;
import model.agent.AgentState;
import model.agent.AgentType;
import model.Edge;
import model.Graph;
import model.node.Node;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * Represents a person evacuating the building.
 * @author Clement
 */
public class Agent implements Serializable {

	private static final long serialVersionUID = 1L;
	private static int numberAgent = 0;

    private final String id;
    private Node currentNode;
    private Edge currentEdge;
    private Node destinationNode;
    private float maxSpeed;
    private AgentState state;
    private AgentBehavior behavior;
    private AgentType type;
    private float densityTolerance;
	private Node previousNode;
	private List<Node> currentPath = new ArrayList<>();

    /**
     * Creates an agent with an identifier and a starting node.
     * @param id   unique identifier of the agent
     * @param startNode   initial node of the agent
     * @param maxSpeed maximum speed of the agent
     * @param state initial psychological state
     * @param behavior movement behavior
     * @param type physical profile
     * @param densityTolerance tolerance to crowded areas
	 * @param graph graph of the simulation
     */
    public Agent(String id, Node startNode, float maxSpeed, AgentState state, AgentBehavior behavior, AgentType type, float densityTolerance, Graph graph) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifier can't be empty");
        }
		Objects.requireNonNull(graph, "Graph can't be null");
		Objects.requireNonNull(state, "State cannot be null");
		Objects.requireNonNull(behavior, "Behavior cannot be null");
		Objects.requireNonNull(type, "Type cannot be null");
		if (maxSpeed <= 0) {
    		throw new IllegalArgumentException("Max speed must be positive");
		}
		if (densityTolerance < 0 || densityTolerance > 1) {
    		throw new IllegalArgumentException( "Density tolerance must be between 0 and 1");
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
		this.destinationNode = null;
        if (startNode != null) {
            startNode.addAgent(this);
        }
    }
    
    /**
     * Creates an agent with an identifier "agentN" and a starting node.
     * @param startNode   initial node of the agent
	 * @param graph graph of the simulation
     */
    public Agent(Node startNode, Graph graph) {
        this( "agent" + numberAgent,
            startNode,
            1.0f,
            AgentState.CALM,
            AgentBehavior.COOPERATIVE,
            AgentType.ADULT,
            0.5f,
            graph
        );
    }


    public String getId() { return id; }
    public Node getCurrentNode() { return currentNode; }
    public Edge getCurrentEdge() { return currentEdge; }
	public Node getPreviousNode() {   return previousNode;  }
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
	public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }
	public void setCurrentEdge(Edge currentEdge) { this.currentEdge = currentEdge; }
	public void setPreviousNode(Node previousNode) { this.previousNode = previousNode; }


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
