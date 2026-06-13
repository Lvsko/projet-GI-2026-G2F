package controller;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Edge;
import model.Graph;
import model.agent.Agent;
import model.node.Node;
import model.node.NodeStatus;
import model.node.NodeType;
import simulation.Pathfinder;
import simulation.SimulationEngine;
import simulation.SimulationState;
import view.GraphView;
import view.ResultView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Controller linking the view and the simulation engine.
 * Centralises all logic previously scattered across ConfigView, GraphView and MainView:
 * graph edition, agent management, save/load, random generation, and the animation loop.
 *
 * @author Clement
 */
public class SimulationController {

    private Graph graph;
    private SimulationEngine engine;
    private GraphView renderer;
    private AnimationTimer timer;
    private Stage stage;

    private long[] tickInterval    = { 1_000_000_000L };
    private int[]  ticksNoProgress = { 0 };
    private int[]  lastEvacuated   = { 0 };

    // ── Constructor ───────────────────────────────────────────────────────────

    /**
     * Creates a SimulationController for the given graph and stage.
     *
     * @param graph the building graph to control
     * @param stage the JavaFX stage used for file dialogs and scene transitions
     */
    public SimulationController(Graph graph, Stage stage) {
        this.graph = graph;
        this.stage = stage;
    }

    // ── Graph edition ─────────────────────────────────────────────────────────

    /**
     * Creates and adds a node to the graph.
     *
     * @param id             unique identifier of the node
     * @param name           display name of the node
     * @param x              X coordinate of the node on the canvas
     * @param y              Y coordinate of the node on the canvas
     * @param capacity       maximum number of agents the node can hold
     * @param status         accessibility status of the node
     * @param type           type of the node (ROOM, CORRIDOR, EXIT, STAIRCASE)
     * @param attractiveness attractiveness factor influencing agent pathfinding
     * @return the created {@link Node} instance
     */
    public Node addNode(String id, String name, double x, double y,
                        int capacity, NodeStatus status, NodeType type, float attractiveness) {
        Node node = new Node(id, name, x, y, capacity, status, type, attractiveness);
        graph.addNode(node);
        return node;
    }

    /**
     * Creates and adds an edge to the graph.
     *
     * @param id            unique identifier of the edge
     * @param source        source node of the edge
     * @param target        target node of the edge
     * @param width         maximum number of agents that can traverse the edge simultaneously
     * @param distance      physical length of the edge, used in pathfinding cost calculation
     * @param speedModifier base speed modifier applied to agents traversing this edge
     * @param directed      {@code true} if the edge is one-way, {@code false} if bidirectional
     * @return the created {@link Edge} instance
     */
    public Edge addEdge(String id, Node source, Node target,
                        int width, float distance, float speedModifier, boolean directed) {
        Edge edge = new Edge(id, source, target, width, distance, speedModifier, directed);
        graph.addEdge(edge);
        return edge;
    }

    /**
     * Removes a node from the graph and reroutes any agents currently on it or
     * on one of its incident edges.
     *
     * @param id the unique identifier of the node to remove
     */
    public void removeNode(String id) {
        Node node = graph.getNode(id);
        if (node == null) return;

        List<Node> neighbors = graph.getNeighbors(node);
        Pathfinder pf = new Pathfinder();
        List<Agent> allAgents = engine != null ? engine.getAgents() : new ArrayList<>();

        for (Agent agent : new ArrayList<>(allAgents)) {
            if (node.equals(agent.getCurrentNode())) {
                if (!neighbors.isEmpty()) {
                    Node fallback = neighbors.get(0);
                    node.removeAgent(agent);
                    placeAgentOnNode(agent, fallback);
                    rerouteAgent(agent, fallback, pf);
                } else {
                    if (engine != null) engine.removeAgent(agent);
                }
            }
        }

        List<Edge> edgesToRemove = new ArrayList<>();
        for (Edge edge : graph.getEdges()) {
            if (edge.getSource().equals(node) || edge.getTarget().equals(node)) {
                edgesToRemove.add(edge);
            }
        }
        for (Edge edge : edgesToRemove) {
            Node fallback = edge.getSource().equals(node) ? edge.getTarget() : edge.getSource();
            for (Agent agent : new ArrayList<>(edge.getAgents())) {
                placeAgentOnNode(agent, fallback);
                rerouteAgent(agent, fallback, pf);
            }
            graph.removeEdge(edge.getId());
        }

        graph.removeNode(id);
    }

    /**
     * Removes an edge from the graph and reroutes agents that were on it or
     * whose planned path used it.
     *
     * @param id the unique identifier of the edge to remove
     */
    public void removeEdge(String id) {
        Edge edge = graph.getEdge(id);
        if (edge == null) return;

        Pathfinder pf  = new Pathfinder();
        Node src = edge.getSource();
        Node tgt = edge.getTarget();

        List<Agent> allAgents = engine != null ? engine.getAgents() : new ArrayList<>();
        List<Agent> agentsToReroute = new ArrayList<>();
        for (Agent agent : allAgents) {
            if (agent.getCurrentEdge() == edge || pathUsesEdge(agent, src, tgt)) {
                agentsToReroute.add(agent);
            }
        }

        for (Agent agent : new ArrayList<>(edge.getAgents())) {
            placeAgentOnNode(agent, src);
        }

        graph.removeEdge(id);

        for (Agent agent : agentsToReroute) {
            Node from = agent.getCurrentNode();
            if (from != null) rerouteAgent(agent, from, pf);
        }
    }

    /**
     * Removes an agent from the simulation or, if the engine is not running,
     * detaches it from its current node and edge.
     *
     * @param agent the agent to remove
     */
    public void removeAgent(Agent agent) {
        if (engine != null) {
            engine.removeAgent(agent);
        } else {
            if (agent.getCurrentNode() != null) agent.getCurrentNode().removeAgent(agent);
            if (agent.getCurrentEdge() != null) agent.getCurrentEdge().removeAgent(agent);
        }
    }

    // ── Save / Load ───────────────────────────────────────────────────────────

    /**
     * Saves the current graph to a {@code .exit} file chosen by the user.
     */
    public void savePlan() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Plan");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT Plan", "*.exit"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try {
            new SimulationState(graph, new ArrayList<>(), 0).save(file.getAbsolutePath());
        } catch (IOException e) {
            showError("Save error", e.getMessage());
        }
    }

    /**
     * Loads a graph from a {@code .exit} file chosen by the user.
     *
     * @return the loaded {@link Graph}, or {@code null} if loading failed or was cancelled
     */
    public Graph loadPlan() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load Plan");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT Plan", "*.exit"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return null;
        try {
            SimulationState state = SimulationState.load(file.getAbsolutePath());
            this.graph = state.getGraph();
            return this.graph;
        } catch (Exception e) {
            showError("Load error", e.getMessage());
            return null;
        }
    }

    // ── Random generation ─────────────────────────────────────────────────────

    /**
     * Generates {@code n} random nodes connected into a spanning tree.
     * The last node is always of type EXIT.
     *
     * @param n number of nodes to generate
     */
    public void generateRandom(int n) {
        Random rand = new Random();
        List<Node> newNodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String id    = "RN" + (graph.getNodes().size() + i);
            double x     = 50 + rand.nextDouble() * 500;
            double y     = 50 + rand.nextDouble() * 400;
            NodeType type;
            if      (i == n - 1) type = NodeType.EXIT;
            else if (i % 2 == 0) type = NodeType.ROOM;
            else                 type = NodeType.CORRIDOR;
            Node node = new Node(id, id, x, y, 10, NodeStatus.OPEN, type, 1.0f);
            newNodes.add(node);
            graph.addNode(node);
        }
        for (int i = 1; i < newNodes.size(); i++) {
            String edgeId = "RE" + (graph.getEdges().size());
            Node target   = newNodes.get(i);
            Node source   = newNodes.get(rand.nextInt(i));
            graph.addEdge(new Edge(edgeId, source, target, 5, 1.0f, 1.0f, false));
        }
    }

    // ── Simulation lifecycle ──────────────────────────────────────────────────

    /**
     * Validates the graph and agent list, then initialises the engine.
     * Shows an error dialog if validation fails.
     *
     * @param agents the list of agents to add to the simulation
     */
    public void launchSimulation(List<Agent> agents) {
        boolean hasExit = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.EXIT);
        if (!hasExit) {
            showError("No EXIT node", "Add at least one node of type EXIT before launching.");
            return;
        }
        if (agents == null || agents.isEmpty()) {
            showError("No agents", "Add at least one agent before launching.");
            return;
        }
        engine = new SimulationEngine(graph);
        for (Agent agent : agents) engine.addAgent(agent);
        engine.start();
    }

    /** Starts or resumes the simulation engine. */
    public void start() {
        if (engine != null) engine.start();
    }

    /** Pauses the simulation engine. */
    public void pause() {
        if (engine != null) engine.pause();
    }

    /** Advances the simulation by exactly one tick. */
    public void step() {
        if (engine != null) engine.step();
    }

    /** Resets the simulation engine to its initial state. */
    public void reset() {
        if (engine != null) engine.reset();
    }

    /**
     * Sets the tick interval from a speed value expressed in ticks per second.
     *
     * @param speed desired speed in ticks per second (e.g. 1.0 to 10.0)
     */
    public void setSpeed(double speed) {
        tickInterval[0] = (long)(1_000_000_000L / speed);
    }

    /**
     * Starts the JavaFX {@link AnimationTimer} loop.
     *
     * @param renderer      the {@link GraphView} to redraw on each tick
     * @param onStatsUpdate callback invoked after each tick to refresh stats labels;
     *                      may be {@code null}
     */
    public void startTimer(GraphView renderer, Runnable onStatsUpdate) {
        this.renderer = renderer;
        timer = new AnimationTimer() {
            private long lastTick = 0;

            @Override
            public void handle(long now) {
                if (!engine.isRunning()) return;
                if (now - lastTick < tickInterval[0]) return;

                engine.step();
                renderer.drawGraph();
                lastTick = now;
                if (onStatsUpdate != null) onStatsUpdate.run();

                int evacCount = engine.getStatistics().getEvacuatedCount();
                if (evacCount > lastEvacuated[0]) {
                    lastEvacuated[0]   = evacCount;
                    ticksNoProgress[0] = 0;
                } else {
                    ticksNoProgress[0]++;
                }
                if (ticksNoProgress[0] >= 100 && !engine.getAgents().isEmpty()) {
                    engine.pause();
                    this.stop();
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Simulation stuck");
                    alert.setHeaderText("100 ticks with no progress");
                    alert.setContentText("Some agents cannot reach an EXIT node.");
                    alert.showAndWait();
                    stage.setScene(new ResultView().createScene(stage, engine));
                    return;
                }
                if (engine.getAgents().isEmpty() && engine.getExitingAgents().isEmpty()) {
                    engine.pause();
                    this.stop();
                    stage.setScene(new ResultView().createScene(stage, engine));
                }
            }
        };
        timer.start();
    }

    /** Stops the {@link AnimationTimer}. */
    public void stopTimer() {
        if (timer != null) timer.stop();
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    /**
     * Returns the graph currently managed by this controller.
     *
     * @return the current {@link Graph}
     */
    public Graph getGraph() { return graph; }

    /**
     * Returns the simulation engine, or {@code null} if not yet initialised.
     *
     * @return the current {@link SimulationEngine}
     */
    public SimulationEngine getEngine() { return engine; }

    /**
     * Updates the JavaFX stage used for dialogs and scene transitions.
     *
     * @param stage the new {@link Stage}
     */
    public void setStage(Stage stage) { this.stage = stage; }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Recalculates and assigns a new path for the given agent from the specified node.
     *
     * @param agent the agent to reroute
     * @param from  the node from which to recalculate the path
     * @param pf    the {@link Pathfinder} instance to use
     */
    private void rerouteAgent(Agent agent, Node from, Pathfinder pf) {
        if (from == null || agent.getDestinationNode() == null) return;
        List<Node> newPath = pf.dijkstraTime(from, agent.getDestinationNode(), graph);
        if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
        agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
    }

    /**
     * Checks whether an agent's current planned path uses a given edge (in either direction).
     *
     * @param agent the agent whose path is being analysed
     * @param src   one endpoint of the edge
     * @param tgt   the other endpoint of the edge
     * @return {@code true} if the agent's path uses the edge
     */
    private boolean pathUsesEdge(Agent agent, Node src, Node tgt) {
        List<Node> path = agent.getCurrentPath();
        if (path == null || path.isEmpty()) return false;
        Node current = agent.getCurrentNode();
        if (current != null && !path.isEmpty()) {
            Node next = path.get(0);
            if ((current.equals(src) && next.equals(tgt)) ||
                (current.equals(tgt) && next.equals(src))) return true;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            if ((path.get(i).equals(src) && path.get(i + 1).equals(tgt)) ||
                (path.get(i).equals(tgt) && path.get(i + 1).equals(src))) return true;
        }
        return false;
    }

    /**
     * Detaches an agent from its current edge and node, then places it on the target node.
     *
     * @param agent the agent to relocate
     * @param node  the node on which to place the agent
     */
    private void placeAgentOnNode(Agent agent, Node node) {
        if (agent.getCurrentEdge() != null) {
            agent.getCurrentEdge().getAgents().remove(agent);
            agent.setCurrentEdge(null);
        }
        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().getAgents().remove(agent);
        }
        agent.setCurrentNode(node);
        node.getAgents().add(agent);
    }

    /**
     * Displays a JavaFX error alert dialog.
     *
     * @param header  short description of the error type
     * @param content detailed error message
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
