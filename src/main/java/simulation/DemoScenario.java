package simulation;
import java.util.ArrayList;
import java.util.List;

import model.Graph;
import model.Edge;
import model.node.Node;
import model.node.NodeType;
import model.node.NodeStatus;

import model.agent.Agent;
import model.agent.AgentState;
import model.agent.AgentBehavior;
import model.agent.AgentType;

import model.Graph;
import model.node.Node;
import model.node.NodeType;
import model.node.NodeStatus;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentState;
import model.agent.AgentBehavior;
import model.agent.AgentType;
import java.util.ArrayList;
import java.util.List;
/**
 * Provides predefined simulation scenarios for the demo mode.
 * Includes best case, average case and worst case scenarios.
 * @author Clement
 */
public class DemoScenario {

    /**
     * Best case : all agents are CALM, all exits open, nodes filled at 50%.
     */
    public static List<Agent> bestCase(Graph g) {
        return createAgents(g, 0.5f, AgentState.CALM, null, null);
    }

    /**
     * Average case : mix CALM/INJURED, one edge reduced, nodes filled at 50%.
     */
    public static List<Agent> averageCase(Graph g, Edge reducedEdge) {
        reducedEdge.setWidth(1);
        return createAgents(g, 0.5f, null, null, null);  // null = alternates CALM/INJURED
    }

    /**
     * Worst case : all agents PANICKED, one exit blocked, nodes filled at 70%.
     */
    public static List<Agent> worstCase(Graph g, Node blockedExit) {
        blockedExit.setStatus(NodeStatus.BLOCKED);
        return createAgents(g, 0.7f, AgentState.PANICKED, null, null);
    }

    /**
     * Fills non-EXIT nodes at the given ratio and creates agents.
     * @param state if null, alternates CALM/INJURED
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
                agents.add(new Agent("agent_" + node.getId() + "_" + i, node, 1.0f, agentState, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f, g));
                index++;
            }
        }
        return agents;
    }
}
