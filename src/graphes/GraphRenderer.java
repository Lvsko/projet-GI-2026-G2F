import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GraphRenderer {

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public GraphRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        createSampleGraph();
    }

    private void createSampleGraph() {

        Node a = new Node(100, 100, "A");
        Node b = new Node(300, 150, "B");
        Node c = new Node(200, 300, "C");

        nodes.add(a);
        nodes.add(b);
        nodes.add(c);

        edges.add(new Edge(a, b));
        edges.add(new Edge(b, c));
        edges.add(new Edge(c, a));
    }

    public void drawGraph() {

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dessiner les arêtes
        gc.setStroke(Color.BLACK);

        for (Edge edge : edges) {
            gc.strokeLine(
                    edge.from.x,
                    edge.from.y,
                    edge.to.x,
                    edge.to.y
            );
        }

        // Dessiner les nœuds
        gc.setFill(Color.LIGHTBLUE);

        for (Node node : nodes) {

            gc.fillOval(node.x - 20, node.y - 20, 40, 40);

            gc.setFill(Color.BLACK);
            gc.fillText(node.label, node.x - 5, node.y + 5);

            gc.setFill(Color.LIGHTBLUE);
        }
    }
}
