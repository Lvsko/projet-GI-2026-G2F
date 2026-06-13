package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import simulation.SimulationEngine;
import simulation.Statistics;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Edge;
import java.io.*;
import java.util.Map;

/**
 * Displays the results of a simulation with heatmap and run comparison.
 *
 * @author Leonardo
 */
public class ResultView {

    /**
     * Creates the result scene displaying simulation statistics,
     * heatmap visualization, and comparison tools.
     * The scene uses a ScrollPane so all content remains accessible
     * regardless of window height, without forcing a fixed window size (KAN-39).
     *
     * @param stage  the primary stage used for navigation
     * @param engine the simulation engine containing results
     * @return the constructed JavaFX scene
     */
    public Scene createScene(Stage stage, SimulationEngine engine) {
        Statistics stats = engine.getStatistics();
        Graph graph = engine.getGraph();

        // ── Title ────────────────────────────────────────────────────────────────
        Label title = new Label("RESULTS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("EVACUATION SIMULATION");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // ── Heatmap ──────────────────────────────────────────────────────────────
        Label heatmapTitle = new Label("CONGESTION MAP");
        heatmapTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        heatmapTitle.setTextFill(Color.web("#2E7D32"));

        Canvas heatmapCanvas = new Canvas(520, 280);
        drawHeatmap(heatmapCanvas, graph, stats);

        Label lgGreen  = legendLabel("● Fluid",       "#4CAF50");
        Label lgOrange = legendLabel("● Congested", "#FF9800");
        Label lgRed    = legendLabel("● Saturated",       "#F44336");
        HBox legend = new HBox(16, lgGreen, lgOrange, lgRed);

        VBox heatmapBox = new VBox(8, heatmapTitle, heatmapCanvas, legend);
        heatmapBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 16;");
        heatmapBox.setMaxWidth(560);

        // ── Statistics ───────────────────────────────────────────────────────────
        Label ticksLabel     = statLabel("⏱ Evacuation Time", stats.getTotalTicks() + " ticks");
        Label evacuatedLabel = statLabel("👥 Evacuated Agents",     String.valueOf(stats.getEvacuatedCount()));
        Label avgTimeLabel   = statLabel("⌀ Average Time",
                String.format("%.1f", stats.getAverageEvacuationTime()) + " ticks");

        VBox statsBox = new VBox(16, ticksLabel, evacuatedLabel, avgTimeLabel);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 24;");
        statsBox.setMaxWidth(400);

        // ── Analysis ─────────────────────────────────────────────────────────────
        Label analysisTitle = new Label("ANALYSE");
        analysisTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        analysisTitle.setTextFill(Color.web("#2E7D32"));

        VBox analysisBox = new VBox(8, analysisTitle, new Separator());
        for (String line : stats.generateAnalysis()) {
            Label l = new Label("• " + line);
            l.setFont(Font.font("Arial", 14));
            l.setTextFill(Color.web("#e0e0e0"));
            l.setWrapText(true);
            analysisBox.getChildren().add(l);
        }
        analysisBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 24;");
        analysisBox.setMaxWidth(400);

        // ── Buttons ──────────────────────────────────────────────────────────────
        Button saveButton = new Button("💾 Save this run");
        saveButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        saveButton.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        saveButton.setOnAction(e -> saveStats(stage, stats));

        Button compareButton = new Button("📊 Compare with a previous run");
        compareButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        compareButton.setStyle("-fx-background-color: #6A1B9A; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        compareButton.setOnAction(e -> {
            Statistics previous = loadStats(stage);
            if (previous != null) showComparison(previous, stats);
        });

        Button retourButton = new Button("← Back to home");
        retourButton.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        retourButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12 30 12 30;");
        // Reuse the same stage to avoid window size jump on navigation (KAN-39)
        retourButton.setOnAction(e -> new HomeView().start(stage));

        HBox buttons = new HBox(15, saveButton, compareButton, retourButton);
        buttons.setAlignment(Pos.CENTER);

        // ── Root layout ──────────────────────────────────────────────────────────
        VBox root = new VBox(24, titleBox, heatmapBox, statsBox, analysisBox, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");
        root.setPadding(new Insets(30));

        // ScrollPane so all content is reachable on small screens,
        // without imposing a fixed scene size (KAN-39)
        ScrollPane scroll = new ScrollPane(root);
        scroll.setStyle("-fx-background-color: #424242; -fx-background: #424242;");
        scroll.setFitToWidth(true);
        return new Scene(scroll, stage.getWidth(), stage.getHeight());
    }

    /**
     * Draws the congestion heatmap on the given canvas.
     * Nodes and edges are colored based on their peak occupancy and saturation.
     *
     * @param canvas the canvas on which the heatmap is rendered
     * @param graph  the graph structure of the simulation
     * @param stats  the simulation statistics used for visualization
     */
    private void drawHeatmap(Canvas canvas, Graph graph, Statistics stats) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (graph.getNodes().isEmpty()) {
            gc.setFill(Color.web("#9e9e9e"));
            gc.fillText("No nodes to display", 20, 40);
            return;
        }

        Map<Edge, Integer> edgeSat  = stats.getEdgeSaturationTicks();
        Map<Node, Integer> nodePeak = stats.getNodePeakOccupancy();

        // Compute bounds for automatic scaling
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Node n : graph.getNodes()) {
            minX = Math.min(minX, n.getX());
            minY = Math.min(minY, n.getY());
            maxX = Math.max(maxX, n.getX() + 120);
            maxY = Math.max(maxY, n.getY() + 60);
        }

        double margin = 20;
        double scaleX = (canvas.getWidth()  - 2 * margin) / Math.max(maxX - minX, 1);
        double scaleY = (canvas.getHeight() - 2 * margin) / Math.max(maxY - minY, 1);
        double scale  = Math.min(scaleX, scaleY);
        double offX   = margin - minX * scale;
        double offY   = margin - minY * scale;

        // Draw edges colored by saturation level
        for (Edge edge : graph.getEdges()) {
            int sat = edgeSat.getOrDefault(edge, 0);
            if      (sat == 0) gc.setStroke(Color.web("#4CAF50"));
            else if (sat < 5)  gc.setStroke(Color.web("#FF9800"));
            else               gc.setStroke(Color.web("#F44336"));
            gc.setLineWidth(Math.max(2, 3 * scale));
            double x1 = edge.getSource().getX() * scale + offX + 60 * scale;
            double y1 = edge.getSource().getY() * scale + offY + 30 * scale;
            double x2 = edge.getTarget().getX() * scale + offX + 60 * scale;
            double y2 = edge.getTarget().getY() * scale + offY + 30 * scale;
            gc.strokeLine(x1, y1, x2, y2);
        }

        // Draw nodes colored by peak occupancy ratio
        for (Node node : graph.getNodes()) {
            int peak    = nodePeak.getOrDefault(node, 0);
            double ratio = node.getMaxCapacity() > 0 ? (double) peak / node.getMaxCapacity() : 0;
            Color fill;
            if      (ratio <= 0.5) fill = Color.web("#4CAF50");
            else if (ratio <= 1.0) fill = Color.web("#FF9800");
            else                   fill = Color.web("#F44336");

            double nx = node.getX() * scale + offX;
            double ny = node.getY() * scale + offY;
            double w  = 120 * scale;
            double h  = 60  * scale;

            gc.setFill(fill);
            gc.fillRoundRect(nx, ny, w, h, 6, 6);
            gc.setStroke(Color.web("#212121"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(nx, ny, w, h, 6, 6);

            double fontSize = Math.max(9, Math.min(13, 12 * scale));
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", fontSize));
            gc.fillText(node.getName(), nx + 4, ny + h * 0.45);
            gc.setFont(Font.font("Arial", Math.max(8, fontSize - 2)));
            gc.fillText("pic: " + peak + "/" + node.getMaxCapacity(), nx + 4, ny + h * 0.78);
        }
    }

    /**
     * Saves the current simulation statistics to a file using Java serialization.
     *
     * @param stage the stage used to display the file chooser dialog
     * @param stats the statistics object to serialize and save
     */
    private void saveStats(Stage stage, Statistics stats) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save this run");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT File", "*.exit"));
        fc.setInitialFileName("run.exit");
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(stats);
        } catch (IOException e) {
            System.err.println("Save error: " + e.getMessage());
        }
    }

    /**
     * Loads previously saved simulation statistics from a file chosen by the user.
     *
     * @param stage the stage used to display the file chooser dialog
     * @return the deserialized {@link Statistics} object, or {@code null} if loading
     *         fails or the dialog is cancelled
     */
    private Statistics loadStats(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Load previous run");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT File", "*.exit"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Statistics) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Load error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Opens a separate window displaying a side-by-side comparison of two simulation runs.
     *
     * @param run1 the reference (previous) simulation run
     * @param run2 the current simulation run to compare against
     */
    private void showComparison(Statistics run1, Statistics run2) {
        Label title = new Label("RUN COMPARISON");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2E7D32"));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #303030; -fx-background-radius: 10;");

        grid.add(headerLabel("METRIC"), 0, 0);
        grid.add(headerLabel("RUN 1"),    1, 0);
        grid.add(headerLabel("RUN 2"),    2, 0);
        grid.add(new Separator(), 0, 1);
        grid.add(new Separator(), 1, 1);
        grid.add(new Separator(), 2, 1);

        grid.add(cellLabel("⏱ Ticks"), 0, 2);
        grid.add(cellLabel(run1.getTotalTicks() + " ticks"), 1, 2);
        grid.add(compareLabel(run2.getTotalTicks() + " ticks",
                run1.getTotalTicks() > run2.getTotalTicks()), 2, 2);

        grid.add(cellLabel("👥 Evacuated"), 0, 3);
        grid.add(cellLabel(String.valueOf(run1.getEvacuatedCount())), 1, 3);
        grid.add(compareLabel(String.valueOf(run2.getEvacuatedCount()),
                run1.getEvacuatedCount() < run2.getEvacuatedCount()), 2, 3);

        grid.add(cellLabel("⌀ Average Time"), 0, 4);
        grid.add(cellLabel(String.format("%.1f", run1.getAverageEvacuationTime()) + " ticks"), 1, 4);
        grid.add(compareLabel(String.format("%.1f", run2.getAverageEvacuationTime()) + " ticks",
                run1.getAverageEvacuationTime() > run2.getAverageEvacuationTime()), 2, 4);

        grid.add(cellLabel("🚧 Bottlenecks"), 0, 5);
        grid.add(cellLabel(run1.getBottlenecks().size() + " edges"), 1, 5);
        grid.add(compareLabel(run2.getBottlenecks().size() + " edges",
                run1.getBottlenecks().size() > run2.getBottlenecks().size()), 2, 5);

        Button closeButton = new Button("Close");
        closeButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");

        Stage compStage = new Stage();
        closeButton.setOnAction(e -> compStage.close());

        VBox root = new VBox(20, title, grid, closeButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");
        root.setPadding(new Insets(30));

        compStage.setTitle("Comparison");
        compStage.setScene(new Scene(root, 550, 420));
        compStage.show();
    }

    /**
     * Creates a colored label displaying a run-2 metric value with a visual indicator
     * showing whether run 2 is better (✓) or worse (✗) than run 1 for that metric.
     *
     * @param val2       the formatted value of run 2
     * @param run1Better {@code true} if run 1 performs better than run 2 for this metric
     * @return a styled JavaFX {@link Label} representing the comparison result
     */
    private Label compareLabel(String val2, boolean run1Better) {
        Label l = new Label(val2 + (run1Better ? "  ✗" : "  ✓"));
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(run1Better ? Color.web("#F44336") : Color.web("#4CAF50"));
        return l;
    }

    /**
     * Creates a bold header label used in the comparison table.
     *
     * @param text the header text to display
     * @return a styled JavaFX {@link Label}
     */
    private Label headerLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        l.setTextFill(Color.web("#2E7D32"));
        return l;
    }

    /**
     * Creates a standard cell label for displaying metric values in the comparison table.
     *
     * @param text the text content of the cell
     * @return a styled JavaFX {@link Label}
     */
    private Label cellLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 14));
        l.setTextFill(Color.web("#e0e0e0"));
        return l;
    }

    /**
     * Creates a formatted statistic label combining a metric name and its value.
     *
     * @param key   the name of the statistic
     * @param value the value associated with the statistic
     * @return a styled JavaFX {@link Label}
     */
    private Label statLabel(String key, String value) {
        Label l = new Label(key + " : " + value);
        l.setFont(Font.font("Arial", 15));
        l.setTextFill(Color.web("#e0e0e0"));
        return l;
    }

    /**
     * Creates a legend entry label for the heatmap color key.
     *
     * @param text  the legend description
     * @param color the hex color string used for the label text
     * @return a styled JavaFX {@link Label} representing a legend entry
     */
    private Label legendLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(color));
        return l;
    }
}
