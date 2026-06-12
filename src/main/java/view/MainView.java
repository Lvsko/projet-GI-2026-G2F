package view;

import controller.SimulationController;
import model.Graph;
import model.agent.Agent;
import model.agent.AgentState;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import java.util.List;
import simulation.SimulationEngine;

/**
 * Main JavaFX view of the EXIT application.
 * Displays the simulation canvas, a toolbar with controls,
 * and a side panel showing live statistics.
 *
 * @author Leonardo
 */
public class MainView extends Application {

    private Graph graph;
    private List<Agent> agents;
    private String source;

    public MainView() {}

    /**
     * Creates a MainView with the given graph and agents, using "demo" as the source.
     *
     * @param graph  the graph to simulate
     * @param agents the list of agents to place in the simulation
     */
    public MainView(Graph graph, List<Agent> agents) {
        this.graph  = graph;
        this.agents = agents;
        this.source = "demo";
    }

    /**
     * Creates a MainView with the given graph, agents and navigation source.
     *
     * @param graph  the graph to simulate
     * @param agents the list of agents to place in the simulation
     * @param source the identifier of the view that launched this one ("demo" or other)
     */
    public MainView(Graph graph, List<Agent> agents, String source) {
        this.graph  = graph;
        this.agents = agents;
        this.source = source;
    }

    /**
     * Creates a styled button with the given label and background color.
     *
     * @param text the button label
     * @param bg   the CSS background color string
     * @return the configured Button
     */
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


        // ── Canvas — bound to stage size so it fills available space (KAN-39) ──
        Canvas canvas = new Canvas();
        canvas.widthProperty().bind(stage.widthProperty().subtract(250));
        canvas.heightProperty().bind(stage.heightProperty().subtract(100));

        SimulationController controller = new SimulationController(graph, stage);

         GraphView renderer = new GraphView(canvas, graph, controller);
        canvas.widthProperty().addListener((obs, oldVal, newVal) -> renderer.drawGraph());
        canvas.heightProperty().addListener((obs, oldVal, newVal) -> renderer.drawGraph());
        renderer.drawGraph();

        controller.launchSimulation(agents);
        
        renderer.setEngine(controller.getEngine());
        SimulationEngine engine = controller.getEngine();
        
        // ── Stats panel ──────────────────────────────────────────────────────────
        String statStyle  = "-fx-text-fill: #e0e0e0; -fx-font-family: Arial; -fx-font-size: 13;";
        String titleStyle = "-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 15;";

        Label statsTitle     = new Label("STATISTIQUES"); statsTitle.setStyle(titleStyle);
        Label tickLabel      = new Label("Tick : 0");
        Label evacuatedLabel = new Label("Évacués : 0");
        Label remainingLabel = new Label("Restants : 0");
        Label calmLabel      = new Label("Calmes : 0");
        Label panickedLabel  = new Label("Paniqués : 0");
        Label injuredLabel   = new Label("Blessés : 0");

        tickLabel.setStyle(statStyle);
        evacuatedLabel.setStyle(statStyle);
        remainingLabel.setStyle(statStyle);
        calmLabel.setStyle(statStyle);
        panickedLabel.setStyle(statStyle);
        injuredLabel.setStyle(statStyle);

        VBox statsPanel = new VBox(12,
            statsTitle, new Separator(),
            tickLabel, evacuatedLabel, remainingLabel,
            new Separator(),
            calmLabel, panickedLabel, injuredLabel
        );
        statsPanel.setPadding(new Insets(15));
        statsPanel.setPrefWidth(200);
        statsPanel.setMinWidth(200);
        statsPanel.setStyle("-fx-background-color: #303030;");

        // ── Speed slider ─────────────────────────────────────────────────────────
        Slider speedSlider = new Slider(1, 10, 1);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(3);
        speedSlider.setSnapToTicks(false);
        speedSlider.setPrefWidth(180);
        speedSlider.setStyle("-fx-control-inner-background: #303030;");

        Label speedLabel = new Label("Vitesse : 1 tick/s");
        speedLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 11;");

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double tps = newVal.doubleValue();
            controller.setSpeed(tps);
            speedLabel.setText(String.format("Vitesse : %.1f tick/s", tps));
        });

        VBox speedBox = new VBox(4, speedLabel, speedSlider);
        speedBox.setAlignment(Pos.CENTER_LEFT);

        // ── Stats updater (shared by timer and step button) ───────────────────────
        Runnable updateStats = () -> {
            long calm     = engine.getAgents().stream().filter(a -> a.getState() == AgentState.CALM).count();
            long panicked = engine.getAgents().stream().filter(a -> a.getState() == AgentState.PANICKED).count();
            long injured  = engine.getAgents().stream().filter(a -> a.getState() == AgentState.INJURED).count();
            tickLabel.setText("Tick : "      + engine.getCurrentTick());
            evacuatedLabel.setText("Évacués : " + engine.getStatistics().getEvacuatedCount());
            remainingLabel.setText("Restants : " + engine.getAgents().size());
            calmLabel.setText("Calmes : "    + calm);
            panickedLabel.setText("Paniqués : " + panicked);
            injuredLabel.setText("Blessés : "   + injured);
        };

        controller.startTimer(renderer, updateStats);
        stage.setOnCloseRequest(e -> controller.stopTimer());


        // ── Buttons ───────────────────────────────────────────────────────────────
        Button pauseButton = styledButton("▶ Démarrer", "#2E7D32");
        pauseButton.setOnAction(e -> {
            if (engine.isRunning()) {
                controller.pause();
                pauseButton.setText("▶ Reprendre");
            } else {
                controller.start();
                pauseButton.setText("⏸ Pause");
            }
        });

        Button stepButton = styledButton("⏭ Pas", "#1565C0");
        stepButton.setOnAction(e -> {
            if (!engine.isRunning()) {
                controller.step();
                renderer.drawGraph();
                updateStats.run();
                if (engine.getAgents().isEmpty() && engine.getExitingAgents().isEmpty()) {
                    controller.stopTimer();
                    stage.setScene(new ResultView().createScene(stage, engine));
                }
            }
        });

        Button endButton = styledButton("⏹ Terminer", "#7B1F1F");
        endButton.setOnAction(e -> {
            controller.pause();
            controller.stopTimer();
            stage.setScene(new ResultView().createScene(stage, engine));
        });

        Button spawnButton   = styledButton("+ Agent",     "#424242");
        Button addNodeButton = styledButton("+ Node",      "#424242");
        Button addEdgeButton = styledButton("+ Edge",      "#424242");
        Button removeButton  = styledButton("✕ Supprimer", "#7B1F1F");
        Button retourButton  = styledButton("← Retour",    "#303030");

        spawnButton.setOnAction(e   -> renderer.spawnAgentAtRoom());
        addNodeButton.setOnAction(e -> renderer.addRoomNode());
        addEdgeButton.setOnAction(e -> renderer.startAddEdge());
        removeButton.setOnAction(e  -> renderer.removeSelected());

        // Reuse the same stage to avoid window size jump on navigation (KAN-39)
        retourButton.setOnAction(e -> {
            controller.pause();
            controller.stopTimer();
            if ("demo".equals(source)) {
                new ScenarioSelectorView().start(stage);
            } else {
                new HomeView().start(stage);
            }
        });

        HBox toolbar = new HBox(10,
            pauseButton, stepButton, endButton,
            new Separator(),
            speedBox,
            new Separator(),
            spawnButton, addNodeButton, addEdgeButton, removeButton,
            new Separator(),
            retourButton
        );
        toolbar.setPadding(new Insets(8, 10, 8, 10));
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setStyle("-fx-background-color: #303030;");

        // ── Layout — StackPane absorbs extra space so stats panel doesn't
        //    stick to the canvas on wide screens (KAN-40) ──────────────────────────
        StackPane canvasWrapper = new StackPane(canvas);
        HBox mainContent = new HBox(canvasWrapper, statsPanel);
        HBox.setHgrow(canvasWrapper, Priority.ALWAYS);

        VBox root = new VBox(toolbar, mainContent);
        root.setStyle("-fx-background-color: #424242;");

        // No fixed scene size — window keeps its current dimensions on navigation (KAN-39)
        Scene scene = new Scene(root);
        stage.setTitle("EXIT — Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
