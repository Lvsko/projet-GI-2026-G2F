package model.agent;

import model.graph.Edge;
import model.graph.Node;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a person evacuating the building.
 *
 * @author Clement
 */
public class Agent implements Serializable {

    private static final long serialVersionUID = 1L;
    private static int numberAgent = 0;

    private final String id;
    private Node currentNode;
    private Edge currentEdge;
    private Node destinationNode;
    private Node previousNode;
    private float maxSpeed;
    private AgentState state;
    private AgentBehavior behavior;
    private AgentType type;
    private float densityTolerance;
    private List<Node> currentPath = new ArrayList<>();

    // ── Constructors ──────────────────────────────────────────────────────────

    /**
     * Creates an agent with a full set of properties.
     *
     * @param id               unique identifier of the agent
     * @param startNode        initial node of the agent (may be {@code null})
     * @param maxSpeed         maximum movement speed (must be &gt; 0)
     * @param state            initial psychological state
     * @param behavior         movement behavior
     * @param type             physical profile
     * @param densityTolerance tolerance to crowded areas (0.0–1.0)
     */
    public Agent(String id, Node startNode, float maxSpeed,
                 AgentState state, AgentBehavior behavior, AgentType type,
                 float densityTolerance) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifier can't be empty");
        }
        Objects.requireNonNull(state,    "State cannot be null");
        Objects.requireNonNull(behavior, "Behavior cannot be null");
        Objects.requireNonNull(type,     "Type cannot be null");
        if (maxSpeed <= 0) {
            throw new IllegalArgumentException("Max speed must be positive");
        }
        if (densityTolerance < 0 || densityTolerance > 1) {
            throw new IllegalArgumentException("Density tolerance must be between 0 and 1");
        }
        numberAgent++;
        this.id               = id;
        this.currentNode      = startNode;
        this.currentEdge      = null;
        this.maxSpeed         = maxSpeed;
        this.state            = state;
        this.behavior         = behavior;
        this.type             = type;
        this.densityTolerance = densityTolerance;
        this.destinationNode  = null;
        // Node registration intentionally omitted — caller is responsible
        // for calling startNode.addAgent(this) via the controller/engine.
    }

    /**
     * Creates a default agent (CALM, COOPERATIVE, ADULT) with auto-generated ID.
     *
     * @param startNode initial node of the agent
     */
    public Agent(Node startNode) {
        this("agent" + numberAgent, startNode, 1.0f,
             AgentState.CALM, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f);
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /** @return the unique identifier of this agent */
    public String getId() { return id; }

    /** @return the node the agent is currently on, or {@code null} if in transit */
    public Node getCurrentNode() { return currentNode; }

    /** @param currentNode the node to place the agent on */
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }

    /** @return the edge the agent is currently traversing, or {@code null} if on a node */
    public Edge getCurrentEdge() { return currentEdge; }

    /** @param currentEdge the edge the agent is traversing */
    public void setCurrentEdge(Edge currentEdge) { this.currentEdge = currentEdge; }

    /** @return the previous node the agent was on */
    public Node getPreviousNode() { return previousNode; }

    /** @param previousNode the node the agent was previously on */
    public void setPreviousNode(Node previousNode) { this.previousNode = previousNode; }

    /** @return the target destination node of this agent */
    public Node getDestinationNode() { return destinationNode; }

    /** @param destinationNode the destination node to assign */
    public void setDestinationNode(Node destinationNode) { this.destinationNode = destinationNode; }

    /** @return the maximum movement speed of this agent */
    public float getMaxSpeed() { return maxSpeed; }

    /** @return the current psychological state of this agent */
    public AgentState getState() { return state; }

    /** @param state the new psychological state */
    public void setState(AgentState state) { this.state = state; }

    /** @return the movement behavior of this agent */
    public AgentBehavior getBehavior() { return behavior; }

    /** @return the physical profile of this agent */
    public AgentType getType() { return type; }

    /** @return the density tolerance of this agent (0.0–1.0) */
    public float getDensityTolerance() { return densityTolerance; }

    /** @return the current planned path of this agent as an ordered list of nodes */
    public List<Node> getCurrentPath() { return currentPath; }

    /** @param path the new planned path */
    public void setCurrentPath(List<Node> path) { this.currentPath = path; }

    // ── Derived state ─────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if the agent is currently traversing an edge.
     *
     * @return {@code true} if the agent is in transit
     */
    public boolean isInTransit() { return currentEdge != null; }

    // ── Object overrides ──────────────────────────────────────────────────────

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
        return Objects.equals(id, ((Agent) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}
