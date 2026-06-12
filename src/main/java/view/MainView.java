package view;

import model.Graph;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentState;
import simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    // Global stats labels
    private Label lblTick     = new Label("—");
    private Label lblAgents   = new Label("—");
    private Label lblEvacues  = new Label("—");
    private Label lblTaux     = new Label("—");
    private Label lblPaniques = new Label("—");
    private Label lblBlesses  = new Label("—");

    // Selection info panel
    private VBox selectionPanel = new VBox(4);

    public MainView() {}

    public MainView(Graph graph, List<Agent> agents) {
        this.graph  = graph;
        this.agents = agents;
        this.source = "demo";
    }

    public MainView(Graph graph, List<Agent> agents, String source) {
        this.graph  = graph;
        this.agents = agents;
        this.source = source;
    }

    // ── Visual helpers ────────────────────────────────────────────────

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

    private Label sectionHeader(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setTextFill(Color.web("#90CAF9"));
        lbl.setMaxWidth(Double.MAX_VALUE);
        lbl.setStyle("-fx-background-color: #2a2a3a; -fx-padding: 4 8 4 8;");
        return lbl;
    }

    private HBox statRow(String key, Label valueLabel) {
        Label keyLbl = new Label(key);
        keyLbl.setFont(Font.font("Arial", 10));
        keyLbl.setTextFill(Color.web("#AAAAAA"));
        keyLbl.setMinWidth(120);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        valueLabel.setTextFill(Color.WHITE);
        HBox row = new HBox(6, keyLbl, valueLabel);
        row.setPadding(new Insets(1, 8, 1, 8));
        return row;
    }

    private Label infoLine(String key, String value) {
        Label lbl = new Label("• " + key + " : " + value);
        lbl.setFont(Font.font("Arial", 10));
        lbl.setTextFill(Color.web("#DDDDDD"));
        lbl.setPadding(new Insets(1, 8, 1, 12));
        lbl.setWrapText(true);
        lbl.setMaxWidth(255);
        return lbl;
    }

    // ── Update selection panel ────────────────────────────────────────

    /**
     * Refreshes the bottom half of the stats bar based on the currently
     * selected element in the GraphView.
     * @param renderer the GraphView whose current selection is read
     */
    private void updateSelectionPanel(GraphView renderer) {
        selectionPanel.getChildren().clear();

        if (renderer.getSelectedAgent() != null) {
            Agent a = renderer.getSelectedAgent();
            String shortId = a.getId().length() > 10
                ? a.getId().substring(0, 10) + "…" : a.getId();
            selectionPanel.getChildren().addAll(
                sectionHeader("Agent sélectionné"),
                infoLine("ID",           shortId),
                infoLine("Type",         a.getType()     != null ? a.getType().name()     : "—"),
                infoLine("État",         a.getState()    != null ? a.getState().name()    : "—"),
                infoLine("Comportement", a.getBehavior() != null ? a.getBehavior().name() : "—"),
                infoLine("Nœud courant", a.getCurrentNode()     != null ? a.getCurrentNode().getName()     : "en transit"),
                infoLine("Destination",  a.getDestinationNode() != null ? a.getDestinationNode().getName() : "—"),
                infoLine("Chemin restant", a.getCurrentPath() != null
                    ? a.getCurrentPath().size() + " nœud(s)" : "—")
            );

        } else if (renderer.getSelectedNode() != null) {
            model.node.Node n = renderer.getSelectedNode();
            selectionPanel.getChildren().addAll(
                sectionHeader("Nœud sélectionné"),
                infoLine("ID",          n.getId()),
                infoLine("Nom",         n.getName()),
                infoLine("Type",        n.getType()   != null ? n.getType().name()   : "—"),
                infoLine("Statut",      n.getStatus() != null ? n.getStatus().name() : "—"),
                infoLine("Capacité max", String.valueOf(n.getMaxCapacity())),
                infoLine("Occupation",  (n.getAgents() != null ? n.getAgents().size() : 0) + " agent(s)"),
                infoLine("Attractivité", String.format("%.2f", n.getAttractiveness()))
            );

        } else if (renderer.getSelectedEdge() != null) {
            Edge e = renderer.getSelectedEdge();
            selectionPanel.getChildren().addAll(
                sectionHeader("Arête sélectionnée"),
                infoLine("ID",           e.getId()),
                infoLine("Source",       e.getSource().getName()),
                infoLine("Cible",        e.getTarget().getName()),
                infoLine("Largeur",      e.getWidth() + " agent(s)"),
                infoLine("Distance",     String.format("%.1f", e.getDistance())),
                infoLine("Vitesse eff.", String.format("%.2f", e.getEffectiveSpeed())),
                infoLine("Occupation",   (e.getAgents() != null ? e.getAgents().size() : 0) + " agent(s)"),
                infoLine("Sens unique",  e.isDirected() ? "Oui" : "Non")
            );

        } else {
            Label none = new Label("Aucune sélection");
            none.setFont(Font.font("Arial", 10));
            none.setTextFill(Color.web("#555555"));
            none.setPadding(new Insets(8, 8, 4, 12));
            selectionPanel.getChildren().add(none);
        }
    }

    // ── Update global stats ───────────────────────────────────────────

    /**
     * Refreshes the global statistics labels from the current engine state.
     * @param engine the running simulation engine
     */
    private void updateGlobalStats(SimulationEngine engine) {
        List<Agent> living  = engine.getAgents();
        int evacues = engine.getStatistics().getEvacuatedCount();
        int total   = living.size() + engine.getExitingAgents().size() + evacues;
        long panics  = living.stream().filter(a -> a.getState() == AgentState.PANICKED).count();
        long injured = living.stream().filter(a -> a.getState() == AgentState.INJURED).count();
        double taux  = total > 0 ? (evacues * 100.0 / total) : 0;

        lblTick.setText(String.valueOf(engine.getCurrentTick()));
        lblAgents.setText(String.valueOf(living.size()));
        lblEvacues.setText(String.valueOf(evacues));
        lblTaux.setText(String.format("%.0f%%", taux));
        lblPaniques.setText(String.valueOf(panics));
        lblBlesses.setText(String.valueOf(injured));
    }

    // ── Build stats bar ───────────────────────────────────────────────

    /**
     * Builds the right-side vertical stats bar containing global statistics
     * on the top half and the selected element info on the bottom half.
     * @return the constructed VBox stats bar
     */
    private VBox buildStatsBar() {
        VBox bar = new VBox(0);
        bar.setPrefWidth(270);
        bar.setMinWidth(240);
        bar.setMaxWidth(270);
        bar.setStyle("-fx-background-color: #1e1e2e;");

        // Top half : global stats
        VBox statsSection = new VBox(3);
        statsSection.setPadding(new Insets(0, 0, 8, 0));
        statsSection.getChildren().addAll(
            sectionHeader("Statistiques globales"),
            statRow("Tick :",             lblTick),
            statRow("Agents actifs :",    lblAgents),
            statRow("Évacués :",          lblEvacues),
            statRow("Taux évacuation :",  lblTaux),
            statRow("En panique :",       lblPaniques),
            statRow("Blessés :",          lblBlesses)
        );

        Separator sep = new Separator(Orientation.HORIZONTAL);
        sep.setPadding(new Insets(4, 0, 4, 0));

        // Bottom half : selected element
        Label none = new Label("Aucune sélection");
        none.setFont(Font.font("Arial", 10));
        none.setTextFill(Color.web("#555555"));
        none.setPadding(new Insets(8, 8, 4, 12));
        selectionPanel.getChildren().add(none);

        bar.getChildren().addAll(statsSection, sep, selectionPanel);
        return bar;
    }

    // ── start ─────────────────────────────────────────────────────────

    @Override
    public void start(Stage stage) {
        Canvas canvas = new Canvas(800, 560);

        GraphView renderer = new GraphView(canvas, graph);
        renderer.drawGraph();

        SimulationEngine engine = new SimulationEngine(graph);
        for (Agent agent : agents) {
            engine.addAgent(agent);
        }
        renderer.setEngine(engine);

        // Wire selection callback → update bottom panel
        renderer.setOnSelectionChanged(() -> updateSelectionPanel(renderer));

        engine.start();

        long[] tickInterval    = { 200_000_000L }; // 5 ticks/s default
        int[]  ticksNoProgress = { 0 };
        int[]  lastEvacuated   = { 0 };

        AnimationTimer timer = new AnimationTimer() {
            private long lastTick = 0;
            @Override
            public void handle(long now) {
                if (!engine.isRunning()) return;
                if (now - lastTick < tickInterval[0]) return;
                engine.step();
                renderer.drawGraph();
                lastTick = now;
                updateGlobalStats(engine);

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

        stage.setOnCloseRequest(e -> timer.stop());

        // ── Toolbar ──
        Button pauseButton = styledButton("⏸ Pause", "#2E7D32");
        pauseButton.setOnAction(e -> {
            if (engine.isRunning()) {
                engine.pause();
                pauseButton.setText("▶ Resume");
            } else {
                engine.start();
                pauseButton.setText("⏸ Pause");
            }
        });

        Button stepButton = styledButton("⏭ Step", "#1565C0");
        stepButton.setOnAction(e -> {
            if (!engine.isRunning()) {
                engine.step();
                renderer.drawGraph();
                updateGlobalStats(engine);
            }
        });

        Slider speedSlider = new Slider(1, 10, 5);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(3);
        speedSlider.setPrefWidth(110);
        speedSlider.valueProperty().addListener((obs, oldVal, newVal) ->
            tickInterval[0] = (long)(1_000_000_000L / newVal.doubleValue())
        );
        Label speedLabel = new Label("Vitesse");
        speedLabel.setTextFill(Color.WHITE);
        speedLabel.setFont(Font.font("Arial", 10));

        Button spawnButton   = styledButton("+ Agent",    "#424242");
        Button addNodeButton = styledButton("+ Node",     "#424242");
        Button addEdgeButton = styledButton("+ Edge",     "#424242");
        Button removeButton  = styledButton("✕ Supprimer","#7B1F1F");
        Button retourButton  = styledButton("← Retour",   "#303030");

        spawnButton.setOnAction(e   -> renderer.spawnAgentAtRoom());
        addNodeButton.setOnAction(e -> renderer.addRoomNode());
        addEdgeButton.setOnAction(e -> renderer.startAddEdge());
        removeButton.setOnAction(e  -> renderer.removeSelectedNode());
        retourButton.setOnAction(e  -> {
            timer.stop();
            engine.pause();
            stage.close();
            Stage prev = new Stage();
            if ("demo".equals(source)) new ScenarioSelectorView().start(prev);
            else                        new HomeView().start(prev);
        });

        HBox toolbar = new HBox(8,
            pauseButton, stepButton,
            new Separator(Orientation.VERTICAL),
            speedLabel, speedSlider,
            new Separator(Orientation.VERTICAL),
            spawnButton, addNodeButton, addEdgeButton, removeButton,
            new Separator(Orientation.VERTICAL),
            retourButton
        );
        toolbar.setPadding(new Insets(8, 10, 8, 10));
        toolbar.setStyle("-fx-background-color: #303030;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // ── Two-column layout ──
        VBox canvasContainer = new VBox(toolbar, canvas);
        canvasContainer.setStyle("-fx-background-color: #424242;");
        HBox.setHgrow(canvasContainer, Priority.ALWAYS);

        HBox root = new HBox(canvasContainer, buildStatsBar());
        root.setStyle("-fx-background-color: #1e1e2e;");

        Scene scene = new Scene(root, 1100, 610);
        stage.setTitle("EXIT — Simulation");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) { launch(); }
}
