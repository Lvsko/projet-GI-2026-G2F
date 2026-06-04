package simulation;

import java.util.ArrayList;
import java.util.List;
import model.agent.Agent;
import model.node.Node;
import model.Edge;

/**
 * Tracks and computes evacuation statistics during the simulation.
 * @author Ruben
 */
public class Statistics {
    private int evacuatedCount;
    private int totalTicks;
    private List<Edge> bottlenecks;

    public Statistics() {
        this.evacuatedCount = 0;
        this.totalTicks = 0;
        this.bottlenecks = new ArrayList<Edge>();
    }

    public void update(int ticks, ArrayList<Agent> agents) {
        totalTicks = ticks;
    }

    /** Increments the evacuated count when an agent reaches the EXIT */
    public void incrementEvacuated() {
        evacuatedCount++;
    }

    public int getEvacuatedCount() { return evacuatedCount; }
    public int getTotalTicks() { return totalTicks; }
    public List<Edge> getBottlenecks() { return bottlenecks; }

    @Override
    public String toString() {
        return "Statistics [evacuatedCount=" + evacuatedCount + ", totalTicks=" + totalTicks + ", bottlenecks=" + bottlenecks + "]";
    }
}
