package simulation;

import model.Graph;
import model.Edge;
import model.node.Node;
import model.node.NodeStatus;
import model.node.NodeType;

import java.util.*;
/**
 * Provides pathfinding algorithms (Dijkstra) for agent navigation.
 * @author Lassina
 */
public class Pathfinder {
    // TODO - Lassina
    public List<Node> dijkstraDistance(Node start, Node destination, Graph graph) {

        // 1. The distance map: Associates a node with its shortest distance from the start
        Map<Node, Float> distances = new HashMap<>();

        // 2. The route map: Associates a node with its predecessor (where we came from)
        Map<Node, Node> previousNodes = new HashMap<>();

        // 3. The list of nodes we still need to explore
        List<Node> unvisitedNodes = new ArrayList<>();

        // --- INITIALIZATION ---
        // We iterate through all nodes in the graph to prepare them
        for (Node node : graph.getNodes()) {
            distances.put(node, Float.MAX_VALUE); // Set to "infinity" by default
            unvisitedNodes.add(node);             // Add them to the waiting list
        }

        // The only distance we know at the beginning is the start node
        distances.put(start, 0f);

        // We continue as long as there are nodes left to visit
        while (!unvisitedNodes.isEmpty()) {

            Node currentNode = null;
            float smallestDistance = Float.MAX_VALUE;

            for (Node node : unvisitedNodes){
                if(distances.get(node) <= smallestDistance){
                    currentNode = node;
                    smallestDistance = distances.get(node);
                }
            }

            // 2.b If the closest node has an "infinite" distance, it means we are trapped.
            // The remaining nodes cannot be reached (e.g., blocked by walls).
            if (smallestDistance == Float.MAX_VALUE) {
                break; // Stop the loop
            }

            // 2.c We mark the node as visited by removing it from the list
            unvisitedNodes.remove(currentNode);

            // 2.d If we just reached our destination, we don't need to search further!
            if (currentNode.equals(destination)) {
                break; // Stop the loop
            }

            // We get all the connected nodes (neighbors) of our current node
            List<Node> neighbors = graph.getNeighbors(currentNode);

            for (Node neighbor : neighbors) {

                // 3.a If the neighbor is already visited, we skip it
                if (!unvisitedNodes.contains(neighbor)) {
                    continue;
                }

                // 3.b Check for obstacles! If the node is BLOCKED, agents can't go there
                if (neighbor.getStatus() == NodeStatus.BLOCKED) {
                    continue;
                }

                // 3.c Find the edge connecting 'currentNode' and 'neighbor' to get the distance
                Edge connectingEdge = findConnectingEdge(graph, currentNode, neighbor);

                // Calculate the new path distance
                float newDistance = distances.get(currentNode) + connectingEdge.getDistance();

                // 3.d The crucial test: Is this new route faster than the old one?
                if (newDistance < distances.get(neighbor)) {
                    // Update the distance map with the better time
                    distances.put(neighbor, newDistance);

                    // Update the route map: to get to 'neighbor', we now come from 'currentNode'
                    previousNodes.put(neighbor, currentNode);
                }
            }
        }

        List<Node> finalPath = new ArrayList<>();
        Node currentStep = destination;

        // 4.a Safety check: Did we actually find a path?
        // If the destination has no predecessor (and it's not the start node itself),
        // it means the destination is completely blocked off.
        if (previousNodes.get(currentStep) == null && !currentStep.equals(start)) {
            return finalPath; // Return an empty list (meaning: "Agent is trapped!")
        }

        // 4.b Backtracking: We trace our steps backwards
        finalPath.add(currentStep); // Add the destination first

        while (previousNodes.containsKey(currentStep)) {
            currentStep = previousNodes.get(currentStep); // Look at where we came from
            finalPath.add(currentStep);                   // Add it to the path
        }

        // 4.c Reverse the list!
        // Right now the path is [Destination, Node C, Node B, Start]
        // We need it to be [Start, Node B, Node C, Destination] for the agents to walk it.
        Collections.reverse(finalPath);

        return finalPath;
    }

    public List<Node> dijkstraTime(Node start, Node destination, Graph graph) {


        Map<Node, Float> times = new HashMap<>();
        Map<Node, Node> previousNodes = new HashMap<>();
        List<Node> unvisitedNodes = new ArrayList<>();

        for (Node node : graph.getNodes()) {
            times.put(node, Float.MAX_VALUE);
            unvisitedNodes.add(node);
        }
        times.put(start, 0f); // Time to reach start is 0

        // --- STEP 2: FIND CLOSEST NODE (FASTEST CURRENT NODE) ---
        while (!unvisitedNodes.isEmpty()) {

            Node currentNode = null;
            float smallestTime = Float.MAX_VALUE;

            for (Node node : unvisitedNodes) {
                if (times.get(node) <= smallestTime) {
                    smallestTime = times.get(node);
                    currentNode = node;
                }
            }

            if (smallestTime == Float.MAX_VALUE) break;
            unvisitedNodes.remove(currentNode);
            if (currentNode.equals(destination)) break;

            // --- STEP 3: UPDATE NEIGHBORS WITH CONGESTION FORMULA ---
            List<Node> neighbors = graph.getNeighbors(currentNode);

            for (Node neighbor : neighbors) {
                if (!unvisitedNodes.contains(neighbor)) continue;
                if (neighbor.getStatus() == NodeStatus.BLOCKED) continue;

                Edge connectingEdge = findConnectingEdge(graph, currentNode, neighbor);
                if (connectingEdge == null) continue;


                // Formula from ticket: weight = distance / (speedModifier * (1 - occupancyRatio))
                float distance = connectingEdge.getDistance();
                float speedModifier = connectingEdge.getSpeedModifier(); // assuming getter exists
                float occupancyRatio = (float) connectingEdge.getOccupancy() / connectingEdge.getWidth(); // assuming getter exists
                float denominator = speedModifier * (1.0f - occupancyRatio);
                if (denominator <= 0) denominator = 0.01f;
                float timeCost = distance / denominator;
                
                // Time cost calculation targeting the congestion factor
               

                float newTime = times.get(currentNode) + timeCost;

                if (newTime < times.get(neighbor)) {
                    times.put(neighbor, newTime);
                    previousNodes.put(neighbor, currentNode);
                }
            }
        }


        List<Node> finalPath = new ArrayList<>();
        Node currentStep = destination;

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

    protected Edge findConnectingEdge(Graph graph, Node currentNode, Node neighbor) {

        // We look at every edge in the graph
        // (Assuming you added a getEdges() method to the Graph class)
        for (Edge edge : graph.getEdges()){
            Node source = edge.getSource();
            Node target = edge.getTarget();

            // Case 1: The edge goes exactly from our current node to the neighbor
            boolean isDirectPath = source.equals(currentNode) && target.equals(neighbor);

            // Case 2: The edge goes from the neighbor to our current node,
            // BUT we can only use it if it's a two-way street (!directed)
            boolean isReversePath = source.equals(neighbor) && target.equals(currentNode) && !edge.isDirected();

            // If either case is true, we found our connecting edge!
            if (isDirectPath || isReversePath) {
                return edge;
            }
        }
        // This theoretically shouldn't happen if getNeighbors() is coded correctly,
        // but it's good practice to have a fallback.
        return null;
    }


}
