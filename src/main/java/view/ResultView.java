package view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import simulation.SimulationEngine;

/**
 * Displays the results of a simulation.
 * Shows evacuation time and evacuated agents.
 * @author Leonardo
 */
public class ResultView {

    public Scene createScene(Stage stage, SimulationEngine engine) {

        Pane root = new Pane();

        Label title = new Label("Simulation Results");
        title.setLayoutX(300);
        title.setLayoutY(20);

        Label ticksLabel = new Label(
                "Temps d'evacuation : "
                + engine.getStatistics().getTotalTicks()
                + " ticks");
        ticksLabel.setLayoutX(50);
        ticksLabel.setLayoutY(80);

        Label evacuatedLabel = new Label(
                "Agents evacues : "
                + engine.getStatistics().getEvacuatedCount());
        evacuatedLabel.setLayoutX(50);
        evacuatedLabel.setLayoutY(120);

        Button retourButton = new Button("Retour");
            retourButton.setLayoutX(50);
            retourButton.setLayoutY(500);
            retourButton.setOnAction(e -> {
                stage.close();
                Stage homeStage = new Stage();
                new HomeView().start(homeStage);
        });

        root.getChildren().addAll(
                title,
                ticksLabel,
                evacuatedLabel,
                retourButton
        );

        return new Scene(root, 800, 600);
    }
}
