package exit.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a corridor between two nodes in the building graph.
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

    /** Returns the number of agents currently in this edge */
    public int getOccupancy() {
        return agents.size();
    }

    /** Returns true if the edge can still accept more agents */
    public boolean isAvailable() {
        return agents.size() < width;
    }

    /** Adds an agent to this edge */
    public void addAgent(Agent agent) {
        agents.add(agent);
    }

    /** Removes an agent from this edge */
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }

    public String getId() { return id; }
    public Node getSource() { return source; }
    public Node getTarget() { return target; }
    public int getWidth() { return width; }
    public float getDistance() { return distance; }
    public float getSpeedModifier() { return speedModifier; }
    public boolean isDirected() { return directed; }
    public List<Agent> getAgents() { return agents; }

    @Override
    public String toString() {
    return "Edge{id='" + id + "', from='" + source.getId() + "', to='" + target.getId() + "', width=" + width + "}";
    }


}
