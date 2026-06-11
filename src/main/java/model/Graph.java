package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.node.Node;
import model.node.NodeType;
import model.Edge;
import model.agent.Agent;

/**
 * Main structure of the building, contains all nodes and edges.
 * @author Yoni
 */
public class Graph implements Serializable {

    private List<Node> nodes;
    private List<Edge> edges;

    public Graph() {
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
    }

    /** Adds a node to the graph */
    public void addNode(Node node) {
        nodes.add(node);
    }

    /** Adds an edge to the graph */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /** Removes a node and all its connected edges from the graph */
    public List<Agent> removeNode(String id) {
        List<Agent> lostAgents = new ArrayList<>();
        Node toRemove = getNode(id);

        if (toRemove == null) return lostAgents;

        // RELOCATE AGENTS ON THE NODE
        List<Agent> agentsOnNode = new ArrayList<>(toRemove.getAgents());
        List<Node> neighbors = getNeighbors(toRemove);

        if (!neighbors.isEmpty()) {
            // Salvage operation: move everyone to the first available neighbor
            Node safeHaven = neighbors.get(0);
            for (Agent a : agentsOnNode) {
                a.arriveAt(safeHaven);
                // Note: The agent will need to recalculate its path at the next tick!
            }
        } else {
            // No neighbors available, agents fall into the void
            lostAgents.addAll(agentsOnNode);
        }

        // DELETE CONNECTED EDGES AND SALVAGE THEIR AGENTS
        List<Edge> toDelete = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getSource().getId().equals(id) || e.getTarget().getId().equals(id)) {
                toDelete.add(e);
            }
        }

        for (Edge e : toDelete) {
            List<Agent> agentsOnEdge = new ArrayList<>(e.getAgents());

            for (Agent a : agentsOnEdge) {
                // If the edge's source is the node we are currently deleting,
                // sending them to the source is fatal. We send them to the target instead.
                Node fallbackNode = e.getSource().getId().equals(id) ? e.getTarget() : e.getSource();

                // Double check: if the fallback is ALSO being deleted (e.g. isolated graph segment),
                // the agent is lost.
                if (fallbackNode.getId().equals(id)) {
                    lostAgents.add(a);
                } else {
                    a.arriveAt(fallbackNode);
                }
            }
            edges.remove(e);
        }

        // FINAL CLEANUP
        nodes.remove(toRemove);

        return lostAgents; // We return the casualties to the SimulationEngine
    }

    /** Removes an edge and moves its agents back to the source node */
    public List<Agent> removeEdge(String id) {
        List<Agent> relocatedAgents = new ArrayList<>();
        Edge toRemove = getEdge(id);

        if (toRemove == null) return relocatedAgents;

        // We make a copy of the list to avoid ConcurrentModificationException
        // when agents are removed from the edge during iteration
        List<Agent> agentsOnEdge = new ArrayList<>(toRemove.getAgents());

        for (Agent a : agentsOnEdge) {
            // Relocate the agent to the source node using its internal state method
            a.arriveAt(toRemove.getSource());
            relocatedAgents.add(a);
        }

        edges.remove(toRemove);
        return relocatedAgents;
    }

    /** Returns the list of neighbors of a given node */
    public List<Node> getNeighbors(Node node) {
        List<Node> neighbors = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getSource().getId().equals(node.getId())) {
                neighbors.add(e.getTarget());
            } else if (!e.isDirected() && e.getTarget().getId().equals(node.getId())) {
                neighbors.add(e.getSource());
            }
        }
        return neighbors;
    }

    /** Returns a node by its id, null if not found */
    public Node getNode(String id) {
        for (Node n : nodes) {
            if (n.getId().equals(id)) return n;
        }
        return null;
    }

    /** Returns an edge by its id, null if not found */
    public Edge getEdge(String id) {
        for (Edge e : edges) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    /** Returns all nodes of a given type */
    public List<Node> getNodesByType(NodeType type) {
        List<Node> result = new ArrayList<>();
        for (Node n : nodes) {
            if (n.getType() == type) {
                result.add(n);
            }
        }
        return result;
    }
        
    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
