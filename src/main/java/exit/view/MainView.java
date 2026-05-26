package exit.view;
import exit.model.Agent;
import exit.simulation.SimulationEngine;
import javafx.animation.AnimationTimer;
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

        SimulationEngine engine = new SimulationEngine(renderer.getGraph());
        for (Agent agent : renderer.getAgents()) {
            engine.addAgent(agent);
        }
        engine.start();

        AnimationTimer timer = new AnimationTimer() {
            private long lastTick = 0;
            @Override
            public void handle(long now) {
                if (now - lastTick >= 800_000_000L) {
                    engine.step();
                    renderer.drawGraph();
                    lastTick = now;
                }
            }
        };
        timer.start();

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
