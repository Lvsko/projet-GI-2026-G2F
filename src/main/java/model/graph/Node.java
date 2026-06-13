package model.graph;

import model.agent.Agent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node (room, corridor, exit) in the building graph.
 *
 * @author Yoni
 */
public class Node implements Serializable {

    private String id;
    private String name;
    private double x;
    private double y;
    private int maxCapacity;
    private NodeStatus status;
    private NodeType type;
    private float attractiveness;
    private List<Agent> agents;

    /**
     * Constructs a Node with the given properties.
     *
     * @param id             unique identifier of the node
     * @param name           display name of the node
     * @param x              X coordinate on the canvas
     * @param y              Y coordinate on the canvas
     * @param maxCapacity    maximum number of agents the node can hold
     * @param status         accessibility status of the node
     * @param type           type of the node (ROOM, CORRIDOR, EXIT, STAIRCASE)
     * @param attractiveness attractiveness factor influencing agent pathfinding
     */
    public Node(String id, String name, double x, double y, int maxCapacity,
                NodeStatus status, NodeType type, float attractiveness) {
        this.id             = id;
        this.name           = name;
        this.x              = x;
        this.y              = y;
        this.maxCapacity    = maxCapacity;
        this.status         = status;
        this.type           = type;
        this.attractiveness = attractiveness;
        this.agents         = new ArrayList<>();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /** @return the unique identifier of this node */
    public String getId() { return id; }

    /** @return the display name of this node */
    public String getName() { return name; }

    /** @return the X coordinate of this node on the canvas */
    public double getX() { return x; }

    /** @param x the new X coordinate */
    public void setX(double x) { this.x = x; }

    /** @return the Y coordinate of this node on the canvas */
    public double getY() { return y; }

    /** @param y the new Y coordinate */
    public void setY(double y) { this.y = y; }

    /** @return the maximum number of agents this node can hold */
    public int getMaxCapacity() { return maxCapacity; }

    /** @return the accessibility status of this node */
    public NodeStatus getStatus() { return status; }

    /** @param status the new accessibility status */
    public void setStatus(NodeStatus status) { this.status = status; }

    /** @return the type of this node */
    public NodeType getType() { return type; }

    /** @return the attractiveness factor of this node */
    public float getAttractiveness() { return attractiveness; }

    /** @return the list of agents currently in this node */
    public List<Agent> getAgents() { return agents; }

    /** @return the number of agents currently in this node */
    public int getOccupancy() { return agents.size(); }

    // ── Agent management ──────────────────────────────────────────────────────

    /**
     * Adds an agent to this node.
     *
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) { agents.add(agent); }

    /**
     * Removes an agent from this node.
     *
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) { agents.remove(agent); }

    @Override
    public String toString() {
        return "Node{id='" + id + "', name='" + name + "', type=" + type + ", status=" + status + "}";
    }
}
