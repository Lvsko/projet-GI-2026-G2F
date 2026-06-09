package view;
import model.Graph;
import model.node.Node;
import model.node.NodeStatus;
import model.node.NodeType;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentState;
import simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

    private Button styledButton(String text, String bg) {
        Button btn = new Button(text);
        btn.setFont(Font.font("Arial", 12));
        btn.setStyle(
            "-fx-background-color: " + bg + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 6;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 14 6 14;"
        );
        return btn;
    }

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(700, 560);

        GraphView renderer = new GraphView(canvas, graph);
        renderer.drawGraph();

        SimulationEngine engine = new SimulationEngine(graph);
        for (Agent agent : agents) {
            engine.addAgent(agent);
        }
        renderer.setEngine(engine);

        // --- Panneau de statistiques (colonne droite) ---
        String statStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: Arial; -fx-font-size: 13;";
        String titleStyle = "-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 15;";

        Label statsTitle = new Label("STATISTIQUES");
        statsTitle.setStyle(titleStyle);

        Label tickLabel     = new Label("Tick : 0");
        Label evacuatedLabel = new Label("Évacués : 0");
        Label remainingLabel = new Label("Restants : 0");
        Label calmLabel     = new Label("Calmes : 0");
        Label panickedLabel = new Label("Paniqués : 0");
        Label injuredLabel  = new Label("Blessés : 0");

        tickLabel.setStyle(statStyle);
        evacuatedLabel.setStyle(statStyle);
        remainingLabel.setStyle(statStyle);
        calmLabel.setStyle(statStyle);
        panickedLabel.setStyle(statStyle);
        injuredLabel.setStyle(statStyle);

        VBox statsPanel = new VBox(12,
            statsTitle,
            new Separator(),
            tickLabel,
            evacuatedLabel,
            remainingLabel,
            new Separator(),
            calmLabel,
            panickedLabel,
            injuredLabel
        );
        statsPanel.setPadding(new Insets(15));
        statsPanel.setPrefWidth(200);
        statsPanel.setStyle("-fx-background-color: #303030;");

        // --- Timer ---
        AnimationTimer timer = new AnimationTimer() {
            private long lastTick = 0;
            @Override
            public void handle(long now) {
                if (engine.isRunning() && now - lastTick >= 2_000_000_000L) {
                    engine.step();
                    renderer.drawGraph();
                    lastTick = now;

                    // Mise à jour du panneau stats
                    long calm     = engine.getAgents().stream().filter(a -> a.getState() == AgentState.CALM).count();
                    long panicked = engine.getAgents().stream().filter(a -> a.getState() == AgentState.PANICKED).count();
                    long injured  = engine.getAgents().stream().filter(a -> a.getState() == AgentState.INJURED).count();
                    tickLabel.setText("Tick : " + engine.getCurrentTick());
                    evacuatedLabel.setText("Évacués : " + engine.getStatistics().getEvacuatedCount());
                    remainingLabel.setText("Restants : " + engine.getAgents().size());
                    calmLabel.setText("Calmes : " + calm);
                    panickedLabel.setText("Paniqués : " + panicked);
                    injuredLabel.setText("Blessés : " + injured);

                    if (engine.getAgents().isEmpty() && engine.getExitingAgents().isEmpty()) {
                        engine.pause();
                        ResultView resultView = new ResultView();
                        stage.setScene(resultView.createScene(stage, engine));
                    }
                }
            }
        };
        timer.start();

        // --- Boutons ---
        Button pauseButton = styledButton("▶ Démarrer", "#2E7D32");
        pauseButton.setOnAction(e -> {
            if (engine.isRunning()) {
                engine.pause();
                pauseButton.setText("▶ Reprendre");
            } else {
                engine.start();
                pauseButton.setText("⏸ Pause");
            }
        });

        Button endButton = styledButton("⏹ Terminer", "#7B1F1F");
        endButton.setOnAction(e -> {
            engine.pause();
            ResultView resultView = new ResultView();
            stage.setScene(resultView.createScene(stage, engine));
        });

        Button spawnButton = styledButton("+ Agent", "#424242");
        spawnButton.setOnAction(e -> renderer.spawnAgentAtRoom());

        Button addNodeButton = styledButton("+ Node", "#424242");
        addNodeButton.setOnAction(e -> renderer.addRoomNode());

        Button addEdgeButton = styledButton("+ Edge", "#424242");
        addEdgeButton.setOnAction(e -> renderer.startAddEdge());

        Button removeButton = styledButton("✕ Supprimer", "#7B1F1F");
        removeButton.setOnAction(e -> renderer.removeSelectedNode());

        Button retourButton = styledButton("← Retour", "#303030");
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

        HBox toolbar = new HBox(10, pauseButton, endButton, spawnButton, addNodeButton, addEdgeButton, removeButton, retourButton);
        toolbar.setPadding(new Insets(10, 10, 10, 10));
        toolbar.setStyle("-fx-background-color: #303030;");

        HBox mainContent = new HBox(canvas, statsPanel);
        VBox root = new VBox(toolbar, mainContent);
        root.setStyle("-fx-background-color: #424242;");

        Scene scene = new Scene(root, 910, 610);
        stage.setTitle("EXIT — Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
