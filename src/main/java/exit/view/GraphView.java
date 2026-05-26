package exit.view;

import exit.model.Graph;
import exit.model.Node;
import exit.model.Edge;
import exit.model.Agent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import exit.model.enums.NodeStatus;
import exit.model.enums.NodeType;


import java.util.ArrayList;
import java.util.List;

/**
 * Handles the 2D rendering of the graph, agents and nodes.
 * @author Leonardo
 */
public class GraphView {

    private Canvas canvas;
    private GraphicsContext gc;

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();

    public GraphView(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();

        createSampleGraph();
    }

    private void createSampleGraph() {

    	Node a = new Node(
    		    "N1",
    		    "ROOM",
    		    100,
    		    100,
    		    10,
    		    NodeStatus.OPEN,
    		    NodeType.ROOM,
    		    1.0f
    		);

    		Node d = new Node(
    		    "N2",
    		    "CORRIDOR",
    		    300,
    		    100,
    		    10,
    		    NodeStatus.OPEN,
    		    NodeType.CORRIDOR,
    		    1.0f
    		);

    		Node b = new Node(
    		    "N3",
    		    "EXIT",
    		    100,
    		    250,
    		    10,
    		    NodeStatus.OPEN,
    		    NodeType.EXIT,
    		    1.0f
    		);

    		Node c = new Node(
    		    "N4",
    		    "STAIR",
    		    300,
    		    250,
    		    10,
    		    NodeStatus.OPEN,
    		    NodeType.STAIRCASE,
    		    1.0f
    		);

        nodes.add(a);
        nodes.add(b);
        nodes.add(c);
        nodes.add(d);

        edges.add(new Edge(
        	    "E1",
        	    a,
        	    b,
        	    2,
        	    1.0f,
        	    1.0f,
        	    true
        	));

        	edges.add(new Edge(
        	    "E2",
        	    b,
        	    c,
        	    5,
        	    1.0f,
        	    1.0f,
        	    false
        	));

        	edges.add(new Edge(
        	    "E3",
        	    c,
        	    d,
        	    3,
        	    1.0f,
        	    1.0f,
        	    true
        	));

        	edges.add(new Edge(
        	    "E4",
        	    d,
        	    a,
        	    4,
        	    1.0f,
        	    1.0f,
        	    false
        	));
    }

    public void drawGraph() {

        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dessiner les arêtes
        gc.setStroke(Color.BLACK);


        for (Edge edge : edges) {

            // pour l'épaisseur de la ligne
            gc.setLineWidth(edge.getWidth());

            double x1 = edge.getSource().getX() + 60;
            double y1 = edge.getSource().getY() + 30;

            double x2 = edge.getTarget().getX() + 60;
            double y2 = edge.getTarget().getY() + 30;

            // ligne principale
            gc.strokeLine(x1, y1, x2, y2);

            // si la flèche est dirigée
            if(edge.isDirected()) {

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

        	switch(node.getType()) {

                case ROOM:
                    gc.setFill(Color.LIGHTBLUE);
                    break;
                case CORRIDOR:
                	gc.setFill(Color.LIGHTGRAY);
                	break;

                case EXIT:
                    gc.setFill(Color.LIGHTGREEN);
                    break;

                case STAIRCASE:
                    gc.setFill(Color.ORANGE);
                    break;
                default:
                	gc.setFill(Color.BLACK);
            }


            // Rectangle principal
        	gc.fillRect(node.getX(), node.getY(), 120, 60);

            // Bordure noire
            gc.setStroke(Color.BLACK);
            gc.strokeRect(node.getX(), node.getY(), 120, 60);

            // Texte du noeud
            gc.setFill(Color.BLACK);
            gc.fillText(
            	    node.getName(),
            	    node.getX() + 20,
            	    node.getY() + 35
            	);
        }
        }
    }

