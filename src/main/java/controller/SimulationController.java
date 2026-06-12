package controller;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Edge;
import model.Graph;
import model.agent.Agent;
import model.agent.AgentBehavior;
import model.agent.AgentState;
import model.agent.AgentType;
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
 * Handles all logic previously scattered in ConfigView, GraphView and MainView.
 * @author Clement
 */
public class SimulationController {

    private Graph graph;
    private SimulationEngine engine;
    private GraphView renderer;
    private AnimationTimer timer;
    private Stage stage;

    // Tick management
    private long[] tickInterval    = { 200_000_000L };
    private int[]  ticksNoProgress = { 0 };
    private int[]  lastEvacuated   = { 0 };

    /**
     * Creates a SimulationController for the given graph and stage.
     * @param graph the building graph to control
     * @param stage the JavaFX stage used for file dialogs and scene transitions
     */
    public SimulationController(Graph graph, Stage stage) {
        this.graph  = graph;
        this.stage  = stage;
    }

    // Graph edition (from ConfigView) 

    /**
     * Creates and adds a node to the graph.
     * @param id             unique identifier of the node
     * @param name           display name of the node
     * @param x              X coordinate of the node on the canvas
     * @param y              Y coordinate of the node on the canvas
     * @param capacity       maximum number of agents the node can hold
     * @param status         accessibility status of the node
     * @param type           type of the node (ROOM, CORRIDOR, EXIT, STAIRCASE)
     * @param attractiveness attractiveness factor influencing agent pathfinding
     * @return the created Node instance
     */
    public Node addNode(String id, String name, double x, double y,
                        int capacity, NodeStatus status, NodeType type, float attractiveness) {
        Node node = new Node(id, name, x, y, capacity, status, type, attractiveness);
        graph.addNode(node);
        return node;
    }

    /**
     * Creates and adds an edge to the graph.
     * @param id            unique identifier of the edge
     * @param source        source node of the edge
     * @param target        target node of the edge
     * @param width         maximum number of agents that can traverse the edge simultaneously
     * @param distance      physical length of the edge, used in pathfinding cost calculation
     * @param speedModifier base speed modifier applied to agents traversing this edge
     * @param directed      true if the edge is one-way, false if bidirectional
     * @return the created Edge instance
     */
    public Edge addEdge(String id, Node source, Node target,
                        int width, float distance, float speedModifier, boolean directed) {
        Edge edge = new Edge(id, source, target, width, distance, speedModifier, directed);
        graph.addEdge(edge);
        return edge;
    }

    /**
     * Removes a node and reroutes agents if needed.
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
                    agent.arriveAt(fallback);
                    rerouteAgent(agent, fallback, pf);
                } else {
                    if (engine != null) engine.removeAgent(agent);
                }
            }
        }
        graph.removeNode(id);
    }

    /**
     * Removes an edge and reroutes agents on it or using it.
     * @param id the unique identifier of the edge to remove
     */
    public void removeEdge(String id) {
        Edge edge = graph.getEdge(id);
        if (edge == null) return;

            Pathfinder pf = new Pathfinder();
        Node src = edge.getSource();
        Node tgt = edge.getTarget();
        List<Agent> allAgents = engine != null ? engine.getAgents() : new ArrayList<>();

        List<Agent> agentsToReroute = new ArrayList<>();
         for (Agent agent : allAgents) {
            if (agent.getCurrentEdge() == edge || pathUsesEdge(agent, src, tgt)) {
                agentsToReroute.add(agent);
            }
        }
        graph.removeEdge(id);

        for (Agent agent : agentsToReroute) {
            Node from = agent.getCurrentNode();
            if (from != null) {
                rerouteAgent(agent, from, pf);
            }
        }
    }

    /**
     * Removes an agent
     * @param agent the agent  to remove
     */
    public void removeAgent(Agent agent) {
        if (engine != null) {
            engine.removeAgent(agent);
         } else {
            if (agent.getCurrentNode() != null) {
                agent.getCurrentNode().removeAgent(agent);
            }
            if (agent.getCurrentEdge() != null) {
                agent.getCurrentEdge().removeAgent(agent);
            }    
        }
    }

    // Save / Load (from ConfigView) 

    /**
     * Saves the current graph to a .exit file.
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
            showError("Erreur sauvegarde", e.getMessage());
        }
    }

    /**
     * Loads a graph from a .exit file.
     * @return the loaded graph, or null if loading failed
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
            showError("Erreur chargement", e.getMessage());
            return null;
        }
    }

    // Random generation (from ConfigView)

    /**
     * Generates n random nodes connected into a spanning tree.
     * Last node is always EXIT.
     * @param n number of nodes to generate (2-100)
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

    // Simulation lifecycle (from MainView) 

    /**
     * Initializes the engine with the given agents and starts the simulation.
     * @param agents the list of agents to add to the simulation
     */
    public void launchSimulation(List<Agent> agents) {
        boolean hasExit = graph.getNodes().stream()
            .anyMatch(n -> n.getType() == NodeType.EXIT);
        if (!hasExit) {
            showError("Aucun nœud EXIT", "Ajoutez au moins un nœud de type EXIT avant de lancer la simulation.");
            return;
        }
        if (agents == null || agents.isEmpty()) {
            showError("Aucun agent", "Ajoutez au moins un agent avant de lancer la simulation.");
            return;
        }
        engine = new SimulationEngine(graph);
        for (Agent agent : agents) {
            engine.addAgent(agent);
        }
        engine.start();
    }

    /** Starts or resumes the simulation. */
    public void start() {
        if (engine != null) engine.start();
    }

    /** Pauses the simulation. */
    public void pause() {
        if (engine != null) engine.pause();
    }

    /** Advances the simulation by one tick. */
    public void step() {
        if (engine != null) engine.step();
    }

    /** Resets the simulation. */
    public void reset() {
        if (engine != null) engine.reset();
    }

    /**
     * Sets the tick interval from a speed value (1–10).
     * @param speed ticks per second
     */
    public void setSpeed(double speed) {
        tickInterval[0] = (long)(1_000_000_000L / speed);
    }

    /**
     * Starts the AnimationTimer loop.
     * Transitions to ResultView when all agents are evacuated or simulation is stuck.
     * @param renderer the GraphView to redraw each tick
     * @param onStatsUpdate callback to refresh stats labels
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

                // Timeout : 100 ticks without evacuation progress
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
                    alert.setTitle("Simulation bloquée");
                    alert.setHeaderText("100 ticks sans progression");
                    alert.setContentText("Des agents ne peuvent pas atteindre une sortie.");
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

    /** Stops the AnimationTimer. */
    public void stopTimer() {
        if (timer != null) timer.stop();
    }

    // Pathfinding helpers (from GraphView)

    /**
     * Reroutes an agent from a given node toward its destination.
     * @param agent the agent to reroute
     * @param from  the node from which to recalculate the path
     * @param pf    the Pathfinder instance to use
     */
    private void rerouteAgent(Agent agent, Node from, Pathfinder pf) {
        if (from == null || agent.getDestinationNode() == null) return;
        List<Node> newPath = pf.dijkstraTime(from, agent.getDestinationNode(), graph);
        if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
        agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
    }

    /**
     * Checks whether an agent's path uses a given edge.
     * @param agent the agent whose path is being analyzed
     * @param src   one endpoint of the edge
     * @param tgt   the other endpoint of the edge
     * @return true if the agent's path uses the edge, false otherwise
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

    // Helpers

    /**
     * Displays an error alert dialog.
     * @param header  the alert header text describing the error type
     * @param content the alert content text giving details about the error
     */
    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Getters

    public Graph getGraph()            { return graph; }
    public SimulationEngine getEngine(){ return engine; }
    public void setStage(Stage stage)  { this.stage = stage; }
}
