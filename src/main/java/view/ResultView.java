package view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import simulation.SimulationEngine;

/**
 * Displays the results of a simulation.
 * @author Leonardo
 */
public class ResultView {

    public Scene createScene(Stage stage, SimulationEngine engine) {

        Label title = new Label("RÉSULTATS");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 48));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("SIMULATION D'ÉVACUATION");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        int ticks = engine.getStatistics().getTotalTicks();
        int evacuated = engine.getStatistics().getEvacuatedCount();

        Label ticksLabel = statLabel("⏱ Temps d'évacuation", ticks + " ticks");
        Label evacuatedLabel = statLabel("👥 Agents évacués", String.valueOf(evacuated));

        VBox statsBox = new VBox(16, ticksLabel, evacuatedLabel);
        statsBox.setAlignment(Pos.CENTER);
        statsBox.setStyle(
            "-fx-background-color: #303030;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 24;"
        );
        statsBox.setMaxWidth(300);

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

        VBox root = new VBox(40, titleBox, statsBox, retourButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");

        return new Scene(root, 600, 450);
    }

    private Label statLabel(String key, String value) {
        Label label = new Label(key + " : " + value);
        label.setFont(Font.font("Arial", 15));
        label.setTextFill(Color.web("#e0e0e0"));
        return label;
    }
}
