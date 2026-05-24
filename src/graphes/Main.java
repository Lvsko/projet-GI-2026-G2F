import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {

        Canvas canvas = new Canvas(800, 600);

        GraphRenderer renderer = new GraphRenderer(canvas);
        renderer.drawGraph();

        Pane root = new Pane(canvas);

        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("Graph Renderer");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}