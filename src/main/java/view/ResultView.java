package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

import java.io.*;


/**
 * Displays the results of a simulation.
 * @author Leonardo
 */
public class ResultView {

    public Scene createScene(Stage stage, SimulationEngine engine) {
        Statistics stats = engine.getStatistics();

        Label title = new Label("RÉSULTATS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("SIMULATION D'ÉVACUATION");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        Label ticksLabel = statLabel("⏱ Temps d'évacuation", stats.getTotalTicks() + " ticks");
        Label evacuatedLabel = statLabel("👥 Agents évacués", String.valueOf(stats.getEvacuatedCount()));
        Label avgTimeLabel   = statLabel("⌀ Temps moyen", String.format("%.1f", stats.getAverageEvacuationTime()) + " ticks");  

        VBox statsBox = new VBox(16, ticksLabel, evacuatedLabel, avgTimeLabel);
        statsBox.setAlignment(Pos.CENTER_LEFT);
        statsBox.setStyle(
            "-fx-background-color: #303030;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 24;"
        );
        statsBox.setMaxWidth(400);

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
        analysisBox.setStyle(
            "-fx-background-color: #303030;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 24;"
        );
        analysisBox.setMaxWidth(400);

        Button saveButton = new Button("💾 Sauvegarder ce run");
        saveButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        saveButton.setStyle(
            "-fx-background-color: #1565C0;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20 10 20;"
        );
        saveButton.setOnAction(e -> saveStats(stage, stats));

        Button compareButton = new Button("📊 Comparer avec un run précédent");
        compareButton.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        compareButton.setStyle(
            "-fx-background-color: #6A1B9A;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20 10 20;"
        );
        compareButton.setOnAction(e -> {
            Statistics previous = loadStats(stage);
            if (previous != null) {
                showComparison(stage, previous, stats);
            }
        });

        Button retourButton = new Button("← Retour à l'accueil");
        retourButton.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        retourButton.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 12 30 12 30;"
        );
        retourButton.setOnAction(e -> {
            stage.close();
            Stage homeStage = new Stage();
            new HomeView().start(homeStage);
        });

        HBox buttons = new HBox(15, saveButton, compareButton, retourButton);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(30, titleBox, statsBox, analysisBox, retourButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");
        root.setPadding(new javafx.geometry.Insets(30));

        return new Scene(root, 600, 600);
    }

    /** Saves statistics to a .exit file */
    private void saveStats(Stage stage, Statistics stats) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder le run");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier EXIT", "*.exit"));
        fileChooser.setInitialFileName("run.exit");
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) return;
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(stats);
        } catch (IOException e) {
            System.err.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    /** Loads statistics from a .exit file */
    private Statistics loadStats(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Charger un run précédent");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier EXIT", "*.exit"));
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) return null;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Statistics) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur chargement : " + e.getMessage());
            return null;
        }
    }

    /** Shows a comparison table between two runs */
    private void showComparison(Stage stage, Statistics run1, Statistics run2) {
        Label title = new Label("COMPARAISON DES RUNS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#2E7D32"));

        // Tableau comparatif
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));
        grid.setStyle("-fx-background-color: #303030; -fx-background-radius: 10;");

        // En-têtes
        grid.add(headerLabel("MÉTRIQUE"), 0, 0);
        grid.add(headerLabel("RUN 1"), 1, 0);
        grid.add(headerLabel("RUN 2"), 2, 0);
        grid.add(new Separator(), 0, 1);
        grid.add(new Separator(), 1, 1);
        grid.add(new Separator(), 2, 1);

        // Lignes
        grid.add(statLabel("⏱ Ticks", ""), 0, 2);
        grid.add(statLabel("", run1.getTotalTicks() + " ticks"), 1, 2);
        grid.add(compareLabel(run1.getTotalTicks() + " ticks",
                              run2.getTotalTicks() + " ticks",
                              run1.getTotalTicks() > run2.getTotalTicks()), 2, 2);

        grid.add(statLabel("👥 Évacués", ""), 0, 3);
        grid.add(statLabel("", String.valueOf(run1.getEvacuatedCount())), 1, 3);
        grid.add(compareLabel(String.valueOf(run1.getEvacuatedCount()),
                              String.valueOf(run2.getEvacuatedCount()),
                              run1.getEvacuatedCount() < run2.getEvacuatedCount()), 2, 3);

        grid.add(statLabel("⌀ Temps moyen", ""), 0, 4);
        grid.add(statLabel("", String.format("%.1f", run1.getAverageEvacuationTime()) + " ticks"), 1, 4);
        grid.add(compareLabel(
                              String.format("%.1f", run1.getAverageEvacuationTime()) + " ticks",
                              String.format("%.1f", run2.getAverageEvacuationTime()) + " ticks",
                              run1.getAverageEvacuationTime() > run2.getAverageEvacuationTime()), 2, 4);

        grid.add(statLabel("🚧 Goulots", ""), 0, 5);
        grid.add(statLabel("", run1.getBottlenecks().size() + " arêtes"), 1, 5);
        grid.add(compareLabel(run1.getBottlenecks().size() + " arêtes",
                              run2.getBottlenecks().size() + " arêtes",
                              run1.getBottlenecks().size() > run2.getBottlenecks().size()), 2, 5);

        Button closeButton = new Button("Fermer");
        closeButton.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 10 20 10 20;"
        );

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

    /**
     * Returns a colored label for run2 value.
     * Green if run2 is better, red if worse.
     * @param val1     run1 value (display only)
     * @param val2     run2 value
     * @param run1Better true if run1 is better than run2
     */
    private Label compareLabel(String val1, String val2, boolean run1Better) {
        Label l = new Label(val2 + (run1Better ? " ✗" : " ✓"));
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


    private Label statLabel(String key, String value) {
        Label label = new Label(key + " : " + value);
        label.setFont(Font.font("Arial", 15));
        label.setTextFill(Color.web("#e0e0e0"));
        return label;
    }
}
