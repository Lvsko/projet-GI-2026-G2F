package exit.view;
import exit.model.Agent;
import exit.simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.control.Button;
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

        SimulationEngine engine = new SimulationEngine(renderer.getGraph());
        for (Agent agent : renderer.getAgents()) {
            engine.addAgent(agent);
        }
        engine.start();

        AnimationTimer timer = new AnimationTimer() {
            private long lastTick = 0;
            @Override
            public void handle(long now) {
                if (engine.isRunning() && now - lastTick >= 800_000_000L) {
                    engine.step();
                    renderer.drawGraph();
                    lastTick = now;
                }
            }
        };
        timer.start();
        Button pauseButton = new Button("Pause");
        pauseButton.setLayoutX(10);
        pauseButton.setLayoutY(10);
        pauseButton.setOnAction(e -> {
            if (engine.isRunning()) {
                engine.pause();
                pauseButton.setText("Resume");
            } else {
                engine.start();
                pauseButton.setText("Pause");
            }
        });
        Pane root = new Pane(canvas, pauseButton);
        Scene scene = new Scene(root, 800, 600);
        stage.setTitle("Graph");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args) {
        launch();
    }
}
