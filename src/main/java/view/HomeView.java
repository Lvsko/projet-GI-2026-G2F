package view;

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
 * Home screen of the EXIT application.
 * @author Yoni
 */
public class HomeView extends Application {

    @Override
    public void start(Stage stage) {
        // Title
        Label title = new Label("EXIT");
        title.setFont(Font.font("Georgia", FontWeight.BOLD, 68));
        title.setTextFill(Color.web("#2E7D32"));

        Label subtitle = new Label("SIMULATION D'ÉVACUATION");
        subtitle.setFont(Font.font("Arial", 13));
        subtitle.setTextFill(Color.web("#bdbdbd"));

        VBox titleBox = new VBox(8, title, subtitle);
        titleBox.setAlignment(Pos.CENTER);

        // Demo button
        Label demoTitle = new Label("Démo");
        demoTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        demoTitle.setTextFill(Color.WHITE);

        Label demoSub = new Label("3 scénarios possibles");
        demoSub.setFont(Font.font("Arial", 12));
        demoSub.setTextFill(Color.web("#a5d6a7"));

        VBox demoBox = new VBox(5, demoTitle, demoSub);
        demoBox.setAlignment(Pos.CENTER);

        Button demoButton = new Button();
        demoButton.setGraphic(demoBox);
        demoButton.setPrefSize(170, 70);
        demoButton.setStyle(
            "-fx-background-color: #2E7D32;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        demoButton.setOnAction(e -> {
            stage.close();
            Stage selectorStage = new Stage();
            new ScenarioSelectorView().start(selectorStage);
        });
        // Config button
        Label configTitle = new Label("Configuration");
        configTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 17));
        configTitle.setTextFill(Color.web("#e0e0e0"));

        Label configSub = new Label("Personnaliser le bâtiment");
        configSub.setFont(Font.font("Arial", 12));
        configSub.setTextFill(Color.web("#9e9e9e"));

        VBox configBox = new VBox(5, configTitle, configSub);
        configBox.setAlignment(Pos.CENTER);

        Button configButton = new Button();
        configButton.setGraphic(configBox);
        configButton.setPrefSize(170, 70);
        configButton.setStyle(
            "-fx-background-color: #303030;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #616161;" +
            "-fx-border-radius: 8;" +
            "-fx-cursor: hand;"
        );
        configButton.setOnAction(e -> {
            stage.close();
            Stage configStage = new Stage();
            new ConfigView().start(configStage);
        });

        HBox buttons = new HBox(24, demoButton, configButton);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(40, titleBox, buttons);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #424242;");

        Scene scene = new Scene(root, 600, 350);
        stage.setTitle("EXIT");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
