package simulation;

import model.Graph;
import model.Edge;
import model.node.Node;
import model.node.NodeStatus;
import java.util.*;

/**
 * Provides pathfinding algorithms (Dijkstra) for agent navigation.
 * @author Lassina
 */
public class Pathfinder {

    /**
     * Finds the shortest path between two nodes using Dijkstra's algorithm with edge distance as cost function.
     * @param start starting node of the path
     * @param destination target node
     * @param graph graph used for navigation
     * @return ordered list of nodes representing the shortest path
     */
    public List<Node> dijkstraDistance(Node start, Node destination, Graph graph) {
        if (start == null || destination == null) return new ArrayList<>(); // #1 null guard

        Map<Node, Float> distances     = new HashMap<>();
        Map<Node, Node>  previousNodes = new HashMap<>();
        List<Node>       unvisitedNodes = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            distances.put(node, Float.MAX_VALUE);
            unvisitedNodes.add(node);
        }
        distances.put(start, 0f);

        while (!unvisitedNodes.isEmpty()) {
            Node  currentNode      = null;
            float smallestDistance = Float.MAX_VALUE;
            for (Node node : unvisitedNodes) {
                if (distances.get(node) <= smallestDistance) {
                    currentNode      = node;
                    smallestDistance = distances.get(node);
                }
            }
            if (smallestDistance == Float.MAX_VALUE) break;
            unvisitedNodes.remove(currentNode);
            if (currentNode.equals(destination)) break;

            for (Node neighbor : graph.getNeighbors(currentNode)) {
                if (!unvisitedNodes.contains(neighbor)) continue;
                if (neighbor.getStatus() == NodeStatus.BLOCKED) continue;
                Edge connectingEdge = findConnectingEdge(graph, currentNode, neighbor);
                if (connectingEdge == null) continue;
                float newDistance = distances.get(currentNode) + connectingEdge.getDistance();
                if (newDistance < distances.get(neighbor)) {
                    distances.put(neighbor, newDistance);
                    previousNodes.put(neighbor, currentNode);
                }
            }
        }
        List<Node> finalPath   = new ArrayList<>();
        Node       currentStep = destination;
        if (previousNodes.get(currentStep) == null && !currentStep.equals(start)) {
            return finalPath;
        }
        finalPath.add(currentStep);
        while (previousNodes.containsKey(currentStep)) {
            currentStep = previousNodes.get(currentStep);
            finalPath.add(currentStep);
        }
        Collections.reverse(finalPath);
        return finalPath;
    }

    /**
     * Computes the optimal path based on travel time using a modified Dijkstra algorithm.
     * @param start starting node
     * @param destination target node
     * @param graph navigation graph
     * @return ordered list of nodes representing the fastest route
     */
    public List<Node> dijkstraTime(Node start, Node destination, Graph graph) {
        if (start == null || destination == null) return new ArrayList<>(); // #1 null guard

        Map<Node, Float> times         = new HashMap<>();
        Map<Node, Node>  previousNodes = new HashMap<>();
        List<Node>       unvisitedNodes = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            times.put(node, Float.MAX_VALUE);
            unvisitedNodes.add(node);
        }
        times.put(start, 0f);

        while (!unvisitedNodes.isEmpty()) {
            Node  currentNode   = null;
            float smallestTime  = Float.MAX_VALUE;
            for (Node node : unvisitedNodes) {
                if (times.get(node) <= smallestTime) {
                    smallestTime = times.get(node);
                    currentNode  = node;
                }
            }
            if (smallestTime == Float.MAX_VALUE) break;
            unvisitedNodes.remove(currentNode);
            if (currentNode.equals(destination)) break;

            for (Node neighbor : graph.getNeighbors(currentNode)) {
                if (!unvisitedNodes.contains(neighbor)) continue;
                if (neighbor.getStatus() == NodeStatus.BLOCKED) continue;
                Edge connectingEdge = findConnectingEdge(graph, currentNode, neighbor);
                if (connectingEdge == null) continue;

                float distance       = connectingEdge.getDistance();
                float speedModifier  = connectingEdge.getEffectiveSpeed();
                float occupancyRatio = (float) connectingEdge.getOccupancy() / connectingEdge.getWidth();
                float denominator    = speedModifier * (1.0f - occupancyRatio);
                if (denominator <= 0 || Float.isNaN(denominator)) denominator = 0.01f; // guard NaN (#6)
                float timeCost = distance / denominator;

                float newTime = times.get(currentNode) + timeCost;
                if (newTime < times.get(neighbor)) {
                    times.put(neighbor, newTime);
                    previousNodes.put(neighbor, currentNode);
                }
            }
        }
        List<Node> finalPath   = new ArrayList<>();
        Node       currentStep = destination;
        if (previousNodes.get(currentStep) == null && !currentStep.equals(start)) {
            return finalPath;
        }
        finalPath.add(currentStep);
        while (previousNodes.containsKey(currentStep)) {
            currentStep = previousNodes.get(currentStep);
            finalPath.add(currentStep);
        }
        Collections.reverse(finalPath);
        return finalPath;
    }

    /**
     * Retrieves the edge connecting two nodes, considering directionality.
     * @param graph graph containing nodes and edges
     * @param currentNode first node
     * @param neighbor second node
     * @return the connecting edge, or null if no edge exists between the nodes
     */
    public Edge findConnectingEdge(Graph graph, Node currentNode, Node neighbor) {
        for (Edge edge : graph.getEdges()) {
            boolean isDirectPath  = edge.getSource().equals(currentNode) && edge.getTarget().equals(neighbor);
            boolean isReversePath = edge.getSource().equals(neighbor)    && edge.getTarget().equals(currentNode) && !edge.isDirected();
            if (isDirectPath || isReversePath) return edge;
        }
        return null;
    }
}
