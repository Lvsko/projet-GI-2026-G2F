package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.node.Node;
import model.node.NodeType;
import model.Edge;

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

    /** 
     * Adds a node to the graph 
     * @param node the node to add to the graph
     */
    public void addNode(Node node) {
        nodes.add(node);
    }

    /** 
     * Adds an edge to the graph 
     * @param edge to add to the graph
     */
    public void addEdge(Edge edge) {
        edges.add(edge);
    }

    /** 
     * Returns the list of neighbors of a given node 
     * @param node the reference node
     * @return list of adjacent nodes
     */
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

    /** 
     * @param id the identifier of the node to search for
     * @return a node by its id, null if not found 
     */
    public Node getNode(String id) {
        for (Node n : nodes) {
            if (n.getId().equals(id)) return n;
        }
        return null;
    }

    /** 
     * @param id the identifier of the edge to search for
     * @return an edge by its id, null if not found 
     */
    public Edge getEdge(String id) {
        for (Edge e : edges) {
            if (e.getId().equals(id)) return e;
        }
        return null;
    }

    /** 
     * @param type the type of nodes to search for
     * @return a list of all nodes of a given type 
     */
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
