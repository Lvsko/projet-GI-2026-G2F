package exit.view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Main JavaFX view of the EXIT application.
 * @author Leonardo
 */

public class MainView extends Application {

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(800, 600);

        GraphView renderer = new GraphView(canvas);
        renderer.drawGraph();

        Pane root = new Pane(canvas);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
