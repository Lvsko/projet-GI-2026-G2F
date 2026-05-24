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

    	Node a = new Node(100, 100, "ROOM A", "ROOM");
    	Node d = new Node(300, 100, "CORRIDOR B", "CORRIDOR");
    	Node b = new Node(100, 250, "EXIT C", "EXIT");
    	Node c = new Node(300, 250, "STAIR D", "STAIRCASE");

        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
        nodes.add(d);

        edges.add(new Edge(a, b));
        edges.add(new Edge(b, c));
        edges.add(new Edge(c, d));
        edges.add(new Edge(d, a));
        
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
        for (Node node : nodes) {

            switch(node.type) {

                case "ROOM":
                    gc.setFill(Color.LIGHTBLUE);
                    break;
                case "CORRIDOR":
                	gc.setFill(Color.LIGHTGRAY);
                	break;

                case "EXIT":
                    gc.setFill(Color.LIGHTGREEN);
                    break;

                case "STAIRCASE":
                    gc.setFill(Color.ORANGE);
                    break;
                default:
                	gc.setFill(Color.BLACK);
            }


            // Rectangle principal
            gc.fillRect(node.x, node.y, 120, 60);

            // Bordure noire
            gc.setStroke(Color.BLACK);
            gc.strokeRect(node.x, node.y, 120, 60);

            // Texte du noeud
            gc.setFill(Color.WHITE);
            gc.fillText(node.label, node.x + 20, node.y + 35);
        }
        }
    }
