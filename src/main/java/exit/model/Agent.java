package exit.model;

import java.util.Objects;

/**
 * Represents a person evacuating the building.
 * @author Clement
 */
public class Agent {

    private final String id;
    private Node currentNode;
    private static int numberAgent = 0;

    /**
     * Creates an agent with an identifier and a starting node.
     * @param id          unique identifier of the agent
     * @param startNode   initial node of the agent
     */
    public Agent(String id, Node startNode) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Identifier can't be empty");
        }
        numberAgent++;
        this.id = id;
        this.currentNode = startNode;
    }
    
    /**
     * Creates an agent with an identifier "agentN" and a starting node.
     * @param startNode   initial node of the agent
     */
    public Agent(Node startNode) {
    	this("agent"+numberAgent, startNode);
    }
    
    /** 
     * @return the agent's identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the agent's current node
     */
    public Node getCurrentNode() {
        return currentNode;
    }

    /**
     * Moves the agent to a neighboring node, the verification of constraints (valid edges) is the responsibility of the graph.
     * @param targetNode destination node
     */
    public void moveTo(Node targetNode) {
    	if (this.currentNode != null) {
            this.currentNode.removeAgent(this); // leaves the old node
        this.currentNode = targetNode;
        targetNode.addAgent(this); // enter into the new node
    }

    @Override
    public String toString() {
        return "Agent{id='" + id + "', currentNode=" + currentNode + "}";
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
