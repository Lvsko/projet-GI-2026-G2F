package model;

import model.node.Node;
import model.agent.Agent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a corridor between two nodes in the building graph.
 *
 * @author Yoni
 */
public class Edge implements Serializable {

    private String id;
    private Node source;
    private Node target;
    private int width;
    private float distance;
    private float speedModifier;
    private boolean directed;
    private List<Agent> agents;

    /**
     * Constructs an Edge between two nodes.
     *
     * @param id            unique identifier of the edge
     * @param source        source node
     * @param target        target node
     * @param width         maximum number of agents that can traverse simultaneously
     * @param distance      physical length of the edge
     * @param speedModifier speed modifier applied to agents traversing this edge
     * @param directed      {@code true} if the edge is one-way
     */
    public Edge(String id, Node source, Node target, int width, float distance, float speedModifier, boolean directed) {
        this.id            = id;
        this.source        = source;
        this.target        = target;
        this.width         = width;
        this.distance      = distance;
        this.speedModifier = speedModifier;
        this.directed      = directed;
        this.agents        = new ArrayList<>();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /** @return the unique identifier of this edge */
    public String getId() { return id; }

    /** @return the source node of this edge */
    public Node getSource() { return source; }

    /** @return the target node of this edge */
    public Node getTarget() { return target; }

    /** @return the maximum number of agents that can traverse this edge simultaneously */
    public int getWidth() { return width; }

    /** @param width the new maximum traversal width */
    public void setWidth(int width) { this.width = width; }

    /** @return the physical length of this edge */
    public float getDistance() { return distance; }

    /** @return the speed modifier applied to agents traversing this edge */
    public float getSpeedModifier() { return speedModifier; }

    /** @return {@code true} if this edge is one-way */
    public boolean isDirected() { return directed; }

    /** @return the list of agents currently on this edge */
    public List<Agent> getAgents() { return agents; }

    /** @return the number of agents currently on this edge */
    public int getOccupancy() { return agents.size(); }

    // ── Agent management ──────────────────────────────────────────────────────

    /**
     * Adds an agent to this edge.
     *
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) { agents.add(agent); }

    /**
     * Removes an agent from this edge.
     *
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) { agents.remove(agent); }

    @Override
    public String toString() {
        return "Edge{id='" + id + "', from='" + source.getId() + "', to='" + target.getId() + "', width=" + width + "}";
    }
}
