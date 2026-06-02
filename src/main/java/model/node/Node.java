package model.node;
import model.agent.Agent;
import model.agent.AgentState;
import model.node.NodeStatus;
import model.node.NodeType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a node (room, corridor, exit) in the building graph.
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

    /** Returns the number of agents in this node */
    public int getOccupancy() {
        return agents.size();
    }

    /** Returns true if there are more agents than the max capacity */
    public boolean isOverloaded() {
        return agents.size() > maxCapacity;
    }

    /** Adds an agent to this node */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    /** Removes an agent from this node*/
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getMaxCapacity() { return maxCapacity; }
    public NodeStatus getStatus() { return status; }
    public void setStatus(NodeStatus status) { this.status = status; }
    public NodeType getType() { return type; }
    public float getAttractiveness() { return attractiveness; }
    public List<Agent> getAgents() { return agents; }

    @Override
    public String toString() {
    return "Node{id='" + id + "', name='" + name + "', type=" + type + ", status=" + status + "}";
    }
    
}
