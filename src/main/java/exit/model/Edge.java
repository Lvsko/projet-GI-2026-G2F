package exit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a corridor (edge) connecting two nodes in the building graph.
 * @author Yoni
 */
public class Edge {

    /** Unique identifier of the edge. */
    private String id;

    /** The node where this edge starts. */
    private Node source;

    /** The node where this edge ends. */
    private Node target;

    /** Maximum number of agents allowed simultaneously in this edge. */
    private int width;

    /** Distance between the two nodes. */
    private float distance;

    /** Speed modifier applied to agents crossing this edge. */
    private float speedModifier;

    /** Whether this edge can only be used in one direction. */
    private boolean directed;

    /** List of agents currently in this edge. */
    private List<Agent> agents;

    /**
     * Constructs an Edge with all its properties.
     * @param id unique identifier
     * @param source starting node
     * @param target ending node
     * @param width maximum agents simultaneously
     * @param distance length of the corridor
     * @param speedModifier speed factor (1.0 = normal, less = slower)
     * @param directed true if one-way only
     */
    public Edge(String id, Node source, Node target, int width, float distance, float speedModifier, boolean directed) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.width = width;
        this.distance = distance;
        this.speedModifier = speedModifier;
        this.directed = directed;
        this.agents = new ArrayList<>();
    }

    /**
     * Returns the number of agents currently in this edge.
     * @return current occupancy
     */
    public int getOccupancy() {
        return agents.size();
    }

    /**
     * Checks if this edge can accept more agents.
     * @return true if there is space available
     */
    public boolean isAvailable() {
        return agents.size() < width;
    }

    /**
     * Adds an agent to this edge.
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    /**
     * Removes an agent from this edge.
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    /**
     * Returns the unique identifier of this edge.
     * @return edge id
     */
    public String getId() { return id; }

    /**
     * Returns the source node of this edge.
     * @return source node
     */
    public Node getSource() { return source; }

    /**
     * Returns the target node of this edge.
     * @return target node
     */
    public Node getTarget() { return target; }

    /**
     * Returns the maximum number of agents allowed in this edge.
     * @return width
     */
    public int getWidth() { return width; }

    /**
     * Returns the distance of this edge.
     * @return distance
     */
    public float getDistance() { return distance; }

    /**
     * Returns the speed modifier of this edge.
     * @return speed modifier
     */
    public float getSpeedModifier() { return speedModifier; }

    /**
     * Returns whether this edge is directed.
     * @return true if one-way
     */
    public boolean isDirected() { return directed; }

    /**
     * Returns the list of agents in this edge.
     * @return list of agents
     */
    public List<Agent> getAgents() { return agents; }
}
