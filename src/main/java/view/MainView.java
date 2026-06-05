package view;

import model.Graph;
import model.node.Node;
import model.node.NodeStatus;
import model.node.NodeType;
import model.Edge;
import model.agent.Agent;
import simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.util.List;
/**
 * Main JavaFX view of the EXIT application.
 * @author Leonardo
 */
public class MainView extends Application {
    
    private Graph graph;
    private List<Agent> agents;
    private String source;

    public MainView() {}

    public MainView(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;
        this.source = "demo";
    }

    public MainView(Graph graph, List<Agent> agents, String source) {
        this.graph = graph;
        this.agents = agents;
        this.source = source;
    }
    
    
    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 560);
        canvas.setLayoutY(50);

        GraphView renderer = new GraphView(canvas, graph);
        renderer.drawGraph();

        SimulationEngine engine = new SimulationEngine(graph);
        for (Agent agent : agents) {
            engine.addAgent(agent);
        }
        renderer.setEngine(engine);
        engine.start();

        AnimationTimer timer = new AnimationTimer() {
            private long lastTick = 0;

            @Override
            public void handle(long now) {
                if (engine.isRunning() && now - lastTick >= 2_000_000_000L) {
                    engine.step();
                    renderer.drawGraph();
                    lastTick = now;

                    if (engine.getAgents().isEmpty()) {
                        engine.pause();
                        ResultView resultView = new ResultView();
                        stage.setScene(resultView.createScene(stage, engine));
                    }
                }
            }
        };
        timer.start();

        // Pause / Resume
        Button pauseButton = new Button("Pause");
        pauseButton.setLayoutX(10);
        pauseButton.setLayoutY(10);
        pauseButton.setOnAction(e -> {
            if (engine.isRunning()) {
                engine.pause();
                pauseButton.setText("Resume");
            } else {
                engine.start();
                pauseButton.setText("Pause");
            }
        });

        // Spawn agent
        Button spawnButton = new Button("Spawn Agent");
        spawnButton.setLayoutX(100);
        spawnButton.setLayoutY(10);
        spawnButton.setOnAction(e -> renderer.spawnAgentAtRoom());

        // Add room node at last clicked position
        Button addNodeButton = new Button("Add Node ici");
        addNodeButton.setLayoutX(210);
        addNodeButton.setLayoutY(10);
        addNodeButton.setOnAction(e -> renderer.addRoomNode());

        // Add edge between selected node and next clicked node
        Button addEdgeButton = new Button("Add Edge");
        addEdgeButton.setLayoutX(310);
        addEdgeButton.setLayoutY(10);
        addEdgeButton.setOnAction(e -> renderer.startAddEdge());

        // Remove selected element (node, edge or agent)
        Button removeNodeButton = new Button("Supprimer");
        removeNodeButton.setLayoutX(390);
        removeNodeButton.setLayoutY(10);
        removeNodeButton.setOnAction(e -> renderer.removeSelectedNode());

        // Retour button
        Button retourButton = new Button("← Retour");
        retourButton.setLayoutX(490);
        retourButton.setLayoutY(10);
        retourButton.setOnAction(e -> {
            engine.pause();
            stage.close();
            Stage prevStage = new Stage();
            if ("demo".equals(source)) {
                new ScenarioSelectorView().start(prevStage);
            } else {
                new HomeView().start(prevStage);
            }
        });

        Pane root = new Pane(canvas, pauseButton, spawnButton, addNodeButton, addEdgeButton, removeNodeButton, retourButton);
        Scene scene = new Scene(root, 800, 610);
        stage.setTitle("EXIT Simulation");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
