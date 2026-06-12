package model.agent;

import model.agent.AgentBehavior;
import model.agent.AgentState;
import model.agent.AgentType;
import model.node.NodeType;
import model.Edge;
import model.Graph;
import model.node.Node;
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
		this.destinationNode = chooseBestExit(graph);
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
            this.previousNode = this.currentNode;
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
        // Leave the current edge
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

	/**
     * Automatically chooses the best output according to the agent's state.
     * - CALM / INJURED → optimal exit via dijkstraTime 
     * - PANICKED → nearest exit via dijkstraDistance 
     * @param graph     graph of the building
     * @return the best exit
     */
    private Node chooseBestExit(Graph graph) {
        Pathfinder pathfinder = new Pathfinder();
        Node bestExit = null;
        float bestScore = Float.MAX_VALUE;
        List<Node> bestPath = new ArrayList<>();

        // We get all exits directly from the graph
        for (Node exit : graph.getNodesByType(NodeType.EXIT)) {

            // 1. Generate path based on psychological state (Original logic)
            List<Node> path = state == AgentState.PANICKED
                    ? pathfinder.dijkstraDistance(currentNode, exit, graph)
                    : pathfinder.dijkstraTime(currentNode, exit, graph);

            if (path == null || path.isEmpty()) continue; // inaccessible exit

            // 2. KAN-37: Calculate the congestionFactor (sum of occupancies)
            float congestionFactor = 0f;

            for (int i = 0; i < path.size() - 1; i++) {
                Node u = path.get(i);
                Node v = path.get(i + 1);

                Edge edge = pathfinder.findConnectingEdge(graph, u, v);
                if (edge != null) {
                    congestionFactor += edge.getOccupancy();
                }
            }
            // 3. Apply the formula from the Jira ticket
            float score = path.size() + congestionFactor;

            // 4. Keep the lowest score
            if (score < bestScore) {
                bestScore = score;
                bestExit = exit;
                // Create a copy to avoid reference manipulation issues
                bestPath = new ArrayList<>(path);
            }
        }
        // 5. Remove the starting node so the agent doesn't stay in place
        if (!bestPath.isEmpty()) {
            bestPath.remove(0);
        }
        this.currentPath = bestPath;
        return bestExit;
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
