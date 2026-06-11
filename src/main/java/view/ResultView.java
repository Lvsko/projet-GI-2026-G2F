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
        analysisBox.setStyle("-fx-background-color: #303030;
