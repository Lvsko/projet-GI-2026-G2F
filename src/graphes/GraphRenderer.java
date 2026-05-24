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

        edges.add(new Edge(a, b, 2, true));
        edges.add(new Edge(b, c, 5, false));
        edges.add(new Edge(c, d, 3, true));
        edges.add(new Edge(d, a, 4, false));
        
    }

    public void drawGraph() {

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dessiner les arêtes
        gc.setStroke(Color.BLACK);


        for (Edge edge : edges) {

            // pour l'épaisseur de la ligne
            gc.setLineWidth(edge.width);

            double x1 = edge.from.x + 60;
            double y1 = edge.from.y + 30;

            double x2 = edge.to.x + 60;
            double y2 = edge.to.y + 30;

            // ligne principale
            gc.strokeLine(x1, y1, x2, y2);

            // si la flèche est dirigée
            if(edge.directed) {

                double angle = Math.atan2(y2 - y1, x2 - x1);

                double arrowLength = 15;

                // position de la pointe de la flèche avant le rectangle
                double arrowX = x2 - 30 * Math.cos(angle);
                double arrowY = y2 - 30 * Math.sin(angle);

                double xArrow1 = arrowX - arrowLength * Math.cos(angle - Math.PI / 6);
                double yArrow1 = arrowY - arrowLength * Math.sin(angle - Math.PI / 6);

                double xArrow2 = arrowX - arrowLength * Math.cos(angle + Math.PI / 6);
                double yArrow2 = arrowY - arrowLength * Math.sin(angle + Math.PI / 6);

                gc.strokeLine(arrowX, arrowY, xArrow1, yArrow1);
                gc.strokeLine(arrowX, arrowY, xArrow2, yArrow2);
            }
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
