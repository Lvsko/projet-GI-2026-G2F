package view;

import model.Graph;
import model.agent.Agent;
import simulation.DemoScenario;
import java.util.List;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Scenario selection screen for the demo mode.
 * @author Yoni
 */
public class ScenarioSelectorView extends Application {

    /**
     * Initializes and displays the scenario selection screen for demo mode.
     * @param stage the primary JavaFX stage used to display the scene
     */
    @Override
    public void start(Stage stage) {
        // Title
        Label title = new Label("CHOISIR UN SCÉNARIO");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("MODE DÉMONSTRATION");
        subtitle.setFont(Font.font("Arial", 12));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(6, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // Best Case
        Label bestTitle = new Label("Best Case");
        bestTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        bestTitle.setTextFill(Color.WHITE);
        Label bestSub = new Label("Tous calmes, sorties ouvertes");
        bestSub.setFont(Font.font("Arial", 11));
        bestSub.setTextFill(Color.web("#a5d6a7"));
        VBox bestBox = new VBox(5, bestTitle, bestSub);
        bestBox.setAlignment(Pos.CENTER);
        Button bestButton = new Button();
        bestButton.setGraphic(bestBox);
        bestButton.setPrefSize(150, 70);
        bestButton.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        bestButton.setOnAction(e -> {
            stage.close();
            List<Agent> agents = DemoScenario.bestCase();
            Graph g = DemoScenario.getLastGraph();
            Stage simStage = new Stage();
            new MainView(g, agents, "demo").start(simStage);
        });

        // Average Case
        Label avgTitle = new Label("Average Case");
        avgTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        avgTitle.setTextFill(Color.WHITE);
        Label avgSub = new Label("Mix calmes / blessés");
        avgSub.setFont(Font.font("Arial", 11));
        avgSub.setTextFill(Color.web("#ffcc80"));
        VBox avgBox = new VBox(5, avgTitle, avgSub);
        avgBox.setAlignment(Pos.CENTER);
        Button avgButton = new Button();
        avgButton.setGraphic(avgBox);
        avgButton.setPrefSize(150, 70);
        avgButton.setStyle(
            "-fx-background-color: #E65100;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        avgButton.setOnAction(e -> {
            stage.close();
            List<Agent> agents = DemoScenario.averageCase();
            Graph g = DemoScenario.getLastGraph();
            Stage simStage = new Stage();
            new MainView(g, agents, "demo").start(simStage);
        });

        // Worst Case
        Label worstTitle = new Label("Worst Case");
        worstTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        worstTitle.setTextFill(Color.WHITE);
        Label worstSub = new Label("Tous paniqués, sortie bloquée");
        worstSub.setFont(Font.font("Arial", 11));
        worstSub.setTextFill(Color.web("#ef9a9a"));
        VBox worstBox = new VBox(5, worstTitle, worstSub);
        worstBox.setAlignment(Pos.CENTER);
        Button worstButton = new Button();
        worstButton.setGraphic(worstBox);
        worstButton.setPrefSize(150, 70);
        worstButton.setStyle(
            "-fx-background-color: #B71C1C;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        worstButton.setOnAction(e -> {
            stage.close();
            List<Agent> agents = DemoScenario.worstCase();
            Graph g = DemoScenario.getLastGraph();
            Stage simStage = new Stage();
            new MainView(g, agents, "demo").start(simStage);
        });
        HBox buttons = new HBox(20, bestButton, avgButton, worstButton);
        buttons.setAlignment(Pos.CENTER);

        Button retourButton = new Button("← Retour");
        retourButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #bdbdbd; -fx-cursor: hand;");
        retourButton.setOnAction(e -> {
            stage.close();
            Stage homeStage = new Stage();
            new HomeView().start(homeStage);
        });
        
        VBox root = new VBox(24, titleBox, buttons, retourButton);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");

        Scene scene = new Scene(root, 600, 340);
        stage.setTitle("EXIT — Démo");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Launches the JavaFX application for scenario selection.
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}
