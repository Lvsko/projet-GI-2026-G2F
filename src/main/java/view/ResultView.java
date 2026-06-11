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
import model.Graph;
import model.node.Node;
import model.Edge;
import java.io.*;
import java.util.Map;

/**
 * Displays the results of a simulation with heatmap and run comparison.
 * @author Leonardo
 */
public class ResultView {

    public Scene createScene(Stage stage, SimulationEngine engine) {
        Statistics stats = engine.getStatistics();
        Graph graph = engine.getGraph();

        // --- Titre ---
        Label title = new Label("RÉSULTATS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("SIMULATION D'ÉVACUATION");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // --- Heatmap ---
        Label heatmapTitle = new Label("CARTE DE CONGESTION");
        heatmapTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        heatmapTitle.setTextFill(Color.web("#2E7D32"));

        Canvas heatmapCanvas = new Canvas(520, 280);
        drawHeatmap(heatmapCanvas, graph, stats);

        Label lgGreen  = legendLabel("● Fluide",      "#4CAF50");
        Label lgOrange = legendLabel("● Congestionné","#FF9800");
        Label lgRed    = legendLabel("● Saturé",      "#F44336");
        HBox legend = new HBox(16, lgGreen, lgOrange, lgRed);

        VBox heatmapBox = new VBox(8, heatmapTitle, heatmapCanvas, legend);
        heatmapBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 16;");
        heatmapBox.setMaxWidth(560);

        // --- Stats ---
        Label ticksLabel     = statLabel("⏱ Temps d'évacuation", stats.getTotalTicks() + " ticks");
        Label evacuatedLabel = statLabel("👥 Agents évacués",     String.valueOf(stats.getEvacuatedCount()));
        Label avgTimeLabel   = statLabel("⌀ Temps moyen",        String.format("%.1f", stats.getAverageEvacuationTime()) + " ticks");

        VBox statsBox = new VBox(16, ticksLabel, evacuatedLabel, avgTimeLabel);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle("-fx-background-color: #303030; -fx-background-radius: 10; -fx-padding: 24;");
        statsBox.setMaxWidth(400);

        // --- Analyse ---
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

        // --- Boutons ---
        Button saveButton = new Button("💾 Sauvegarder ce run");
        saveButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        saveButton.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        saveButton.setOnAction(e -> saveStats(stage, stats));

        Button compareButton = new Button("📊 Comparer avec un run précédent");
        compareButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        compareButton.setStyle("-fx-background-color: #6A1B9A; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        compareButton.setOnAction(e -> {
            Statistics previous = loadStats(stage);
            if (previous != null) showComparison(previous, stats);
        });

        Button retourButton = new Button("← Retour à l'accueil");
        retourButton.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        retourButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 12 30 12 30;");
        retourButton.setOnAction(e -> {
            stage.close();
            Stage homeStage = new Stage();
            new HomeView().start(homeStage);
        });

        // BUG CORRIGÉ : buttons est bien ajouté au root (pas retourButton seul)
        HBox buttons = new HBox(15, saveButton, compareButton, retourButton);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(24, titleBox, heatmapBox, statsBox, analysisBox, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");
        root.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setStyle("-fx-background-color: #424242; -fx-background: #424242;");
        scroll.setFitToWidth(true);

        return new Scene(scroll, 640, 720);
    }

    /** Draws the congestion heatmap on the given canvas */
    private void drawHeatmap(Canvas canvas, Graph graph, Statistics stats) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.web("#1e1e1e"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        if (graph.getNodes().isEmpty()) {
            gc.setFill(Color.web("#9e9e9e"));
            gc.fillText("Aucun nœud à afficher", 20, 40);
            return;
        }

        Map<Edge, Integer> edgeSat  = stats.getEdgeSaturationTicks();
        Map<Node, Integer> nodePeak = stats.getNodePeakOccupancy();

        // Calcul des bornes pour mise à l'échelle automatique
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

        // Dessin des arêtes
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

        // Dessin des nœuds
        for (Node node : graph.getNodes()) {
            int peak  = nodePeak.getOrDefault(node, 0);
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

    private void saveStats(Stage stage, Statistics stats) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sauvegarder le run");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier EXIT", "*.exit"));
        fc.setInitialFileName("run.exit");
        File file = fc.showSaveDialog(stage);
        if (file == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(stats);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    private Statistics loadStats(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Charger un run précédent");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier EXIT", "*.exit"));
        File file = fc.showOpenDialog(stage);
        if (file == null) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Statistics) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur chargement : " + e.getMessage());
            return null;
        }
    }

    private void showComparison(Statistics run1, Statistics run2) {
        Label title = new Label("COMPARAISON DES RUNS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2E7D32"));

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #303030; -fx-background-radius: 10;");

        grid.add(headerLabel("MÉTRIQUE"), 0, 0);
        grid.add(headerLabel("RUN 1"),    1, 0);
        grid.add(headerLabel("RUN 2"),    2, 0);
        grid.add(new Separator(), 0, 1);
        grid.add(new Separator(), 1, 1);
        grid.add(new Separator(), 2, 1);

        grid.add(cellLabel("⏱ Ticks"),         0, 2);
        grid.add(cellLabel(run1.getTotalTicks() + " ticks"), 1, 2);
        grid.add(compareLabel(run2.getTotalTicks() + " ticks",
                run1.getTotalTicks() > run2.getTotalTicks()), 2, 2);

        grid.add(cellLabel("👥 Évacués"),       0, 3);
        grid.add(cellLabel(String.valueOf(run1.getEvacuatedCount())), 1, 3);
        grid.add(compareLabel(String.valueOf(run2.getEvacuatedCount()),
                run1.getEvacuatedCount() < run2.getEvacuatedCount()), 2, 3);

        grid.add(cellLabel("⌀ Temps moyen"),   0, 4);
        grid.add(cellLabel(String.format("%.1f", run1.getAverageEvacuationTime()) + " ticks"), 1, 4);
        grid.add(compareLabel(String.format("%.1f", run2.getAverageEvacuationTime()) + " ticks",
                run1.getAverageEvacuationTime() > run2.getAverageEvacuationTime()), 2, 4);

        grid.add(cellLabel("🚧 Goulots"),       0, 5);
        grid.add(cellLabel(run1.getBottlenecks().size() + " arêtes"), 1, 5);
        grid.add(compareLabel(run2.getBottlenecks().size() + " arêtes",
                run1.getBottlenecks().size() > run2.getBottlenecks().size()), 2, 5);

        Button closeButton = new Button("Fermer");
        closeButton.setStyle("-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;");
        Stage compStage = new Stage();
        closeButton.setOnAction(e -> compStage.close());

        VBox root = new VBox(20, title, grid, closeButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");
        root.setPadding(new Insets(30));
        compStage.setTitle("Comparaison");
        compStage.setScene(new Scene(root, 550, 420));
        compStage.show();
    }

    private Label compareLabel(String val2, boolean run1Better) {
        Label l = new Label(val2 + (run1Better ? "  ✗" : "  ✓"));
        l.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        l.setTextFill(run1Better ? Color.web("#F44336") : Color.web("#4CAF50"));
        return l;
    }

    private Label headerLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 15));
        l.setTextFill(Color.web("#2E7D32"));
        return l;
    }

    private Label cellLabel(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 14));
        l.setTextFill(Color.web("#e0e0e0"));
        return l;
    }

    private Label statLabel(String key, String value) {
        Label l = new Label(key + " : " + value);
        l.setFont(Font.font("Arial", 15));
        l.setTextFill(Color.web("#e0e0e0"));
        return l;
    }

    private Label legendLabel(String text, String color) {
        Label l = new Label(text);
        l.setFont(Font.font("Arial", 12));
        l.setTextFill(Color.web(color));
        return l;
    }
}
