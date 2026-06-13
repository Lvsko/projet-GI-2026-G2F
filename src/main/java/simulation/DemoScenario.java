package simulation;
import java.util.ArrayList;
import java.util.List;

import model.graph.Graph;
import model.graph.Edge;
import model.graph.Node;
import model.graph.NodeType;
import model.graph.NodeStatus;

import model.agent.Agent;
import model.agent.AgentState;
import model.agent.AgentBehavior;
import model.agent.AgentType;


/**
 * Provides predefined simulation scenarios for the demo mode.
 * Includes best case, average case and worst case scenarios.
 * @author Clement
 */
public class DemoScenario {

    private static Graph lastGraph;

    public static Graph getLastGraph() { return lastGraph; }

    /**
     * Builds a predefined simulation graph used for evacuation scenarios.
     * @return initialized graph containing nodes and edges of the scenario
     */
    public static Graph createGraph() {
        Graph g = new Graph();
        // Floor 2
        Node n1  = new Node("N1",  "Room A",     30,  30,  20, NodeStatus.OPEN, NodeType.ROOM,      1.0f);
        Node n2  = new Node("N2",  "Corridor 1", 200, 30,  10, NodeStatus.OPEN, NodeType.CORRIDOR,  1.0f);
        Node n3  = new Node("N3",  "Room B",     370, 30,  18, NodeStatus.OPEN, NodeType.ROOM,      1.0f);
        Node n4  = new Node("N4",  "Room C",     540, 30,  15, NodeStatus.OPEN, NodeType.ROOM,      1.0f);
        // Floor 1
        Node n5  = new Node("N5",  "Room D",     30,  200, 15, NodeStatus.OPEN, NodeType.ROOM,      1.0f);
        Node n6  = new Node("N6",  "Staircase",  540, 200,  8, NodeStatus.OPEN, NodeType.STAIRCASE, 1.0f);
        Node n7  = new Node("N7",  "Corridor 2", 130, 350, 10, NodeStatus.OPEN, NodeType.CORRIDOR,  1.0f);
        Node n8  = new Node("N8",  "Corridor 3", 400, 350, 10, NodeStatus.OPEN, NodeType.CORRIDOR,  1.0f);
        // Exits
        Node n9  = new Node("N9",  "Exit 1",     80,  460, 50, NodeStatus.OPEN, NodeType.EXIT,      1.0f);
        Node n10 = new Node("N10", "Exit 2",     400, 460, 50, NodeStatus.OPEN, NodeType.EXIT,      1.0f);

        g.addNode(n1); g.addNode(n2); g.addNode(n3); g.addNode(n4);
        g.addNode(n5); g.addNode(n6); g.addNode(n7); g.addNode(n8);
        g.addNode(n9); g.addNode(n10);

        g.addEdge(new Edge("E1",  n1,  n2,  5, 1.0f, 1.0f, false)); // Room A → Corridor 1
        g.addEdge(new Edge("E2",  n2,  n3,  4, 1.0f, 1.0f, false)); // Corridor 1 → Room B
        g.addEdge(new Edge("E3",  n3,  n4,  5, 1.0f, 1.0f, false)); // Room B → Room C
        g.addEdge(new Edge("E4",  n2,  n5,  3, 1.0f, 1.0f, false)); // Corridor 1 → Room D (étroit)
        g.addEdge(new Edge("E5",  n4,  n6,  3, 1.0f, 1.0f, false)); // Room C → Staircase (étroit)
        g.addEdge(new Edge("E6",  n5,  n7,  6, 1.0f, 1.0f, false)); // Room D → Corridor 2
        g.addEdge(new Edge("E7",  n6,  n8,  4, 1.0f, 1.0f, false)); // Staircase → Corridor 3
        g.addEdge(new Edge("E8",  n3,  n8,  5, 1.0f, 1.0f, false)); // Room B → Corridor 3
        g.addEdge(new Edge("E9",  n7,  n9,  2, 1.0f, 1.0f, false)); // Corridor 2 → Exit 1 (goulot)
        g.addEdge(new Edge("E10", n8,  n10, 3, 1.0f, 1.0f, false)); // Corridor 3 → Exit 2
        g.addEdge(new Edge("E11", n7,  n8,  4, 1.0f, 1.0f, false)); // Corridor 2 ↔ Corridor 3
        return g;
    }
    
    /**
     * Best case : all agents are CALM, all exits open, nodes filled at 50%.
     * @return list of agents in the generated scenario
     */
    public static List<Agent> bestCase() {
        Graph g = createGraph();
        lastGraph = g;
        return createAgents(g, 0.2f, AgentState.CALM, null, null);
    }

    /**
     * Average case : mix CALM/INJURED, one edge reduced, nodes filled at 50%.
     * @return a list of agents initialized for the average-case scenario
     */
    public static List<Agent> averageCase() {
        Graph g = createGraph();
        // Narrow corridor to Exit 1 to w=1 — visible bottleneck
        for (Edge e : g.getEdges()) {
            if (e.getId().equals("E9")) {
                e.setWidth(1);
                break;
            }
        }
        lastGraph = g;
        return createAgents(g, 0.55f, null, null, null);  // null = alternates CALM/INJURED
    }

    /**
     * Worst case : all agents PANICKED, one exit blocked, nodes filled at 70%.
     * @return a list of agents initialized for the worst-case scenario
     */
    public static List<Agent> worstCase() {
        Graph g = createGraph();
        // Block Exit 2 entirely
        for (Node n : g.getNodes()) {
            if (n.getId().equals("N10")) {
                n.setStatus(NodeStatus.BLOCKED);
                break;
            }
        }
        // Also narrow Exit 1 corridor to w=1 — total chaos
        for (Edge e : g.getEdges()) {
            if (e.getId().equals("E9")) {
                e.setWidth(1);
                break;
            }
        }
        lastGraph = g;
        return createAgents(g, 0.85f, AgentState.PANICKED, null, null);
    }


    /**
     * Fills non-EXIT nodes at the given ratio and creates agents.
     * @param g the graph in which agents are created
     * @param fillRatio proportion of each node's capacity used to determine agent count (0.0 to 1.0)
     * @param state fixed state for all agents, or {@code null} to alternate CALM/INJURED
     * @param behavior behavior assigned to agents (currently not used in implementation)
     * @param type physical type assigned to agents (currently not used in implementation)
     * @return list of agents placed on the graph
     */
    private static List<Agent> createAgents(Graph g, float fillRatio, AgentState state, AgentBehavior behavior, AgentType type) {
        List<Agent> agents = new ArrayList<>();
        int index = 0;

        for (Node node : g.getNodes()) {
            if (node.getType() == NodeType.EXIT) continue;

            int count = (int) (node.getMaxCapacity() * fillRatio);

            for (int i = 0; i < count; i++) {
                AgentState agentState;
                if (state != null) {
                    agentState = state;
                } else {
                    if (index % 2 == 0) {
                        agentState = AgentState.CALM;  
                    } else {
                        agentState = AgentState.INJURED; 
                    }
                }
                agents.add(new Agent("agent_" + node.getId() + "_" + i, node, 1.0f, agentState, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f));
                index++;
            }
        }
        return agents;
    }
    
}
