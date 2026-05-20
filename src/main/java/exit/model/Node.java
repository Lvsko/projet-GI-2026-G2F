package exit.model;

import exit.model.enums.NodeStatus;
import exit.model.enums.NodeType;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node (room, corridor, exit) in the building graph.
 * @author Yoni
 */
public class Node {

    /** Unique identifier of the node. */
    private String id;

    /** Display name of the node. */
    private String name;

    /** Horizontal position on the canvas. */
    private double x;

    /** Vertical position on the canvas. */
    private double y;

    /** Maximum number of agents this node can hold. */
    private int maxCapacity;

    /** Current accessibility status of this node. */
    private NodeStatus status;

    /** Type of this node in the building. */
    private NodeType type;

    /** Attractiveness factor (positive = attractive, negative = repulsive). */
    private float attractiveness;

    /** List of agents currently in this node. */
    private List<Agent> agents;

    /**
     * Constructs a Node with all its properties.
     * @param id unique identifier
     * @param name display name
     * @param x horizontal position
     * @param y vertical position
     * @param maxCapacity maximum agents allowed
     * @param status accessibility status
     * @param type type of node
     * @param attractiveness attractiveness factor
     */
    public Node(String id, String name, double x, double y, int maxCapacity, NodeStatus status, NodeType type, float attractiveness) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.maxCapacity = maxCapacity;
        this.status = status;
        this.type = type;
        this.attractiveness = attractiveness;
        this.agents = new ArrayList<>();
    }

    /**
     * Returns the number of agents currently in this node.
     * @return current occupancy
     */
    public int getOccupancy() {
        return agents.size();
    }

    /**
     * Checks if this node has exceeded its maximum capacity.
     * @return true if overloaded
     */
    public boolean isOverloaded() {
        return agents.size() > maxCapacity;
    }

    /**
     * Adds an agent to this node.
     * @param agent the agent to add
     */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    /**
     * Removes an agent from this node.
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    /**
     * Returns the unique identifier of this node.
     * @return node id
     */
    public String getId() { return id; }

    /**
     * Returns the display name of this node.
     * @return node name
     */
    public String getName() { return name; }

    /**
     * Returns the horizontal position of this node.
     * @return x coordinate
     */
    public double getX() { return x; }

    /**
     * Returns the vertical position of this node.
     * @return y coordinate
     */
    public double getY() { return y; }

    /**
     * Returns the maximum capacity of this node.
     * @return max capacity
     */
    public int getMaxCapacity() { return maxCapacity; }

    /**
     * Returns the status of this node.
     * @return node status
     */
    public NodeStatus getStatus() { return status; }

    /**
     * Sets the status of this node.
     * @param status new status
     */
    public void setStatus(NodeStatus status) { this.status = status; }

    /**
     * Returns the type of this node.
     * @return node type
     */
    public NodeType getType() { return type; }

    /**
     * Returns the attractiveness factor of this node.
     * @return attractiveness
     */
    public float getAttractiveness() { return attractiveness; }

    /**
     * Returns the list of agents currently in this node.
     * @return list of agents
     */
    public List<Agent> getAgents() { return agents; }
}
