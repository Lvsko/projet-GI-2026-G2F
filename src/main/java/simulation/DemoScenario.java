package simulation;

/**
 * Provides predefined simulation scenarios for the demo mode.
 * Includes best case, average case and worst case scenarios.
 * @author Clement
 */
public class DemoScenario {
    
    /**
     * Best case, all agents are CALM, all exits are open.
     * @param g the building graph
     * @return a list of agents ready for the simulation
     */
    public static List<Agent> bestCase(Graph g) {
        List<Agent> agents = new ArrayList<>();

        for (Node node : g.getNodes()) {
            if (!node.getAgents().isEmpty()) {
                agents.add(new Agent("best_" + node.getId(), node, 1.0f,
                    AgentState.CALM, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f, g));
            }
        }
        return agents;
    }

    /**
     * Average case : mix of CALM and INJURED agents, one exit has reduced capacity.
     * @param g the building graph
     * @return a list of agents ready for the simulation
     */
    public static List<Agent> averageCase(Graph g, Node reducedExit) {
        reducedExit.setStatus(NodeStatus.REDUCED);

        List<Agent> agents = new ArrayList<>();
        List<Node> nodes = g.getNodes();

        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            if (!node.getAgents().isEmpty()) {
                AgentState state = (i % 2 == 0) ? AgentState.CALM : AgentState.INJURED;
                agents.add(new Agent("avg_" + node.getId(), node, 1.0f,
                    state, AgentBehavior.COOPERATIVE, AgentType.ADULT, 0.5f, g));
            }
        }
        return agents;
    }

    /**
     * Worst case : all agents are PANICKED, one exit is blocked.
     * @param g the building graph
     * @param blockedExit the exit node to block
     * @return a list of agents ready for the simulation
     */
    public static List<Agent> worstCase(Graph g, Node blockedExit) {
        blockedExit.setStatus(NodeStatus.BLOCKED);

        List<Agent> agents = new ArrayList<>();

        for (Node node : g.getNodes()) {
            if (!node.getAgents().isEmpty()) {
                agents.add(new Agent("worst_" + node.getId(), node, 1.0f,
                    AgentState.PANICKED, AgentBehavior.PRIORITY, AgentType.ADULT, 0.5f, g));
            }
        }
        return agents;
    }
}
