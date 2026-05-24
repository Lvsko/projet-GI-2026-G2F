package exit.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Main structure of the building, contains all nodes and edges.
 * @author Yoni
 */
public class Graph {

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
    public void removeNode(String id) {
        Node toRemove = getNode(id);
        if (toRemove == null) return;
        List<Edge> toDelete = new ArrayList<>();
        for (Edge e : edges) {
            if (e.getSource().getId().equals(id) || e.getTarget().getId().equals(id)) {
                toDelete.add(e);
            }
        }
        edges.removeAll(toDelete);
        nodes.remove(toRemove);
    }

    /** Removes an edge and moves its agents back to the source node */
    public void removeEdge(String id) {
        Edge toRemove = getEdge(id);
        if (toRemove == null) return;
        for (Agent a : toRemove.getAgents()) {
            toRemove.getSource().addAgent(a);
        }
        edges.remove(toRemove);
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

    public List<Node> getNodes() { return nodes; }
    public List<Edge> getEdges() { return edges; }
}
