package view;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.agent.AgentState;
import model.node.NodeStatus;
import model.node.NodeType;
import simulation.SimulationEngine;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import simulation.Pathfinder;

import model.agent.AgentBehavior;
import model.agent.AgentType;

/**
 * Handles the 2D rendering of the graph, agents and nodes.
 * @author Leonardo
 */
public class GraphView {
    private Canvas canvas;
    private GraphicsContext gc;
    private Graph graph;
    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private List<Agent> agents = new ArrayList<>();

    private Node selectedNode;
    private Edge selectedEdge;
    private Agent selectedAgent;

    private SimulationEngine engine;
    private int nodeCounter;

    // Position du dernier clic pour placer un nouveau nœud (en coordonnées monde)
    private double lastClickX = 100;
    private double lastClickY = 100;

    // Zoom et déplacement du canvas
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double dragStartX;
    private double dragStartY;
    private boolean draggingCanvas = false;
    private boolean draggingNode = false;
    private Node draggedNode = null;

    // Mode connexion : on attend un deuxième clic pour créer une arête
    private boolean connectMode = false;
    private Node connectSource = null;

    public GraphView(Canvas canvas, Graph graph) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.graph = graph;
        this.nodes = new ArrayList<>(graph.getNodes());
        this.edges = new ArrayList<>(graph.getEdges());
        this.nodeCounter = nodes.size();

        canvas.setOnMouseClicked(event -> {
            // Convertir les coordonnées écran en coordonnées monde
            double mouseX = toWorldX(event.getX());
            double mouseY = toWorldY(event.getY());
            lastClickX = mouseX;
            lastClickY = mouseY;

            // Mode connexion — on cherche le nœud cible
            if (connectMode) {
                for (Node node : nodes) {
                    if (hitNode(node, mouseX, mouseY) && !node.equals(connectSource)) {
                        String edgeId = "E" + (edges.size() + 1);
                        Stage popup = new Stage();
                        popup.initModality(Modality.APPLICATION_MODAL);
                        popup.setTitle("Nouvelle arête");

                        TextField widthField = new TextField("5");
                        TextField distanceField = new TextField("1.0");
                        TextField speedField = new TextField("1.0");

                        ComboBox<Boolean> directedBox = new ComboBox<>();
                        directedBox.getItems().addAll(false, true);
                        directedBox.setValue(false);

                        Button createButton = new Button("Créer");
                        createButton.setOnAction(e -> {

                            Edge newEdge = new Edge(
                                edgeId,
                                connectSource,
                                node,
                                Integer.parseInt(widthField.getText()),
                                Float.parseFloat(distanceField.getText()),
                                Float.parseFloat(speedField.getText()),
                                directedBox.getValue()
                            );

                            edges.add(newEdge);
                            graph.addEdge(newEdge);

                            connectMode = false;
                            connectSource = null;

                            drawGraph();

                            popup.close();
                        });
                        
                        VBox layout = new VBox(
                        	    10,
                        	    new Label("Largeur"),
                        	    widthField,
                        	    new Label("Distance"),
                        	    distanceField,
                        	    new Label("Speed Modifier"),
                        	    speedField,
                        	    new Label("Directed"),
                        	    directedBox,
                        	    createButton
                        	);

                        	layout.setStyle("-fx-padding: 10;");
                        	
                        	Scene scene = new Scene(layout);
                        	popup.setScene(scene);
                        	popup.showAndWait();
                            return;
                    }
                }
                // Clic dans le vide — annuler le mode connexion
                connectMode = false;
                connectSource = null;
                drawGraph();
                return;
            }

            // Priorité 1 : agents
            List<Agent> agentsToDraw = (engine != null) ? engine.getAgents() : agents;
            for (Agent agent : agentsToDraw) {
                Node node = agent.getCurrentNode();
                if (node != null) {
                    List<Agent> nodeAgents = node.getAgents();
                    int index = nodeAgents.indexOf(agent);
                    double ax = node.getX() + 10 + (index % 5) * 22;
                    double ay = node.getY() + 35;
                    if (mouseX >= ax && mouseX <= ax + 15 && mouseY >= ay && mouseY <= ay + 15) {
                        selectedAgent = agent;
                        selectedNode = null;
                        selectedEdge = null;
                        drawGraph();
                        return;
                    }
                }
            }

            // Priorité 2 : nœuds
            for (Node node : nodes) {
                if (hitNode(node, mouseX, mouseY)) {
                    selectedNode = node;
                    selectedEdge = null;
                    selectedAgent = null;
                    drawGraph();
                    return;
                }
            }

            // Priorité 3 : arêtes
            for (Edge edge : edges) {
                if (hitEdge(edge, mouseX, mouseY)) {
                    selectedEdge = edge;
                    selectedNode = null;
                    selectedAgent = null;
                    drawGraph();
                    System.out.println("Selected edge: " + edge.getId());
                    return;
                }
            }

            // Clic dans le vide — désélectionner tout
            selectedNode = null;
            selectedEdge = null;
            selectedAgent = null;
            drawGraph();
        });

        canvas.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                zoom *= 1.1;
            } else {
                zoom /= 1.1;
            }
            drawGraph();
        });

        canvas.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
            // Convertir en coordonnées monde pour tester les nœuds
            double wx = toWorldX(event.getX());
            double wy = toWorldY(event.getY());
            for (Node node : nodes) {
                if (hitNode(node, wx, wy)) {
                    draggedNode = node;
                    draggingNode = true;
                    return;
                }
            }
            draggingCanvas = true;
        });

        canvas.setOnMouseDragged(event -> {
            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;
            if (draggingNode && draggedNode != null) {
                // Diviser par zoom pour que le déplacement soit cohérent
                draggedNode.setX(draggedNode.getX() + dx / zoom);
                draggedNode.setY(draggedNode.getY() + dy / zoom);
            } else if (draggingCanvas) {
                offsetX += dx;
                offsetY += dy;
            }
            dragStartX = event.getX();
            dragStartY = event.getY();
            drawGraph();
        });

        canvas.setOnMouseReleased(event -> {
            draggingCanvas = false;
            draggingNode = false;
            draggedNode = null;
        });
    }

    /** Convertit une coordonnée X écran en coordonnée monde */
    private double toWorldX(double screenX) { return (screenX - offsetX) / zoom; }

    /** Convertit une coordonnée Y écran en coordonnée monde */
    private double toWorldY(double screenY) { return (screenY - offsetY) / zoom; }

    /** Vérifie si un clic touche un nœud */
    private boolean hitNode(Node node, double x, double y) {
        return x >= node.getX() && x <= node.getX() + 120
            && y >= node.getY() && y <= node.getY() + 60;
    }

    /** Vérifie si un clic est proche d'une arête (distance < 8px) */
    private boolean hitEdge(Edge edge, double mx, double my) {
        double x1 = edge.getSource().getX() + 60;
        double y1 = edge.getSource().getY() + 30;
        double x2 = edge.getTarget().getX() + 60;
        double y2 = edge.getTarget().getY() + 30;
        double len2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (len2 == 0) return false;
        double t = Math.max(0, Math.min(1, ((mx - x1) * (x2 - x1) + (my - y1) * (y2 - y1)) / len2));
        double px = x1 + t * (x2 - x1);
        double py = y1 + t * (y2 - y1);
        double dist = Math.sqrt((mx - px) * (mx - px) + (my - py) * (my - py));
        return dist < 8;
    }

    public void setEngine(SimulationEngine engine) {
        this.engine = engine;
        this.agents = new ArrayList<>(engine.getAgents());
    }

    /** Spawns a new agent at the first available room node */
    public void spawnAgentAtRoom() {
        Node target = selectedNode;
        if (target == null) {
            for (Node node : nodes) {
                if (node.getType() == NodeType.ROOM) { target = node; break; }
            }
        }
        if (target == null) return;
        final Node finalTarget = target;
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Nouvel Agent");

        ComboBox<AgentState> stateBox = new ComboBox<>();
        stateBox.getItems().addAll(AgentState.values());
        stateBox.setValue(AgentState.CALM);

        ComboBox<AgentBehavior> behaviorBox = new ComboBox<>();
        behaviorBox.getItems().addAll(AgentBehavior.values());
        behaviorBox.setValue(AgentBehavior.COOPERATIVE);

        ComboBox<AgentType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(AgentType.values());
        typeBox.setValue(AgentType.ADULT);

        Button createButton = new Button("Créer");
        
        createButton.setOnAction(e -> {

            Agent agent = new Agent(
                "agent" + System.currentTimeMillis(),
                finalTarget,
                1.0f,
                stateBox.getValue(),
                behaviorBox.getValue(),
                typeBox.getValue(),
                0.5f,
                graph
            );

            agents.add(agent);

            if (engine != null) {
                engine.addAgent(agent);
            }

            drawGraph();
            popup.close();
        });
        
        VBox layout = new VBox(
        	    10,
        	    new Label("État"),
        	    stateBox,
        	    new Label("Comportement"),
        	    behaviorBox,
        	    new Label("Type"),
        	    typeBox,
        	    createButton
        	);

        	layout.setStyle("-fx-padding: 10;");
        	
        	Scene scene = new Scene(layout);
        	popup.setScene(scene);
        	popup.showAndWait();
    }

    /** Adds a new room node at the last clicked position (world coordinates) */
    public void addRoomNode() {
        String id = "N" + (nodeCounter + 1);
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Nouveau noeud");
        TextField nameField = new TextField("Room " + (nodeCounter + 1));

        ComboBox<NodeType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(NodeType.values());
        typeBox.setValue(NodeType.ROOM);

        ComboBox<NodeStatus> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(NodeStatus.values());
        statusBox.setValue(NodeStatus.OPEN);

        TextField capacityField = new TextField("10");

        TextField attractivenessField = new TextField("1.0");
        Button createButton = new Button("Créer");
        
        createButton.setOnAction(e -> {

            Node newNode = new Node(
                id,
                nameField.getText(),
                lastClickX - 60,
                lastClickY - 30,
                Integer.parseInt(capacityField.getText()),
                statusBox.getValue(),
                typeBox.getValue(),
                Float.parseFloat(attractivenessField.getText())
            );

            nodes.add(newNode);
            graph.addNode(newNode);

            nodeCounter++;

            drawGraph();

            popup.close();
        });
        VBox layout = new VBox(
        	    10,
        	    new Label("Nom"),
        	    nameField,
        	    new Label("Type"),
        	    typeBox,
        	    new Label("Statut"),
        	    statusBox,
        	    new Label("Capacité"),
        	    capacityField,
        	    new Label("Attractivité"),
        	    attractivenessField,
        	    createButton
        	);

        	layout.setStyle("-fx-padding: 10;");
        	
        	Scene scene = new Scene(layout);
        	popup.setScene(scene);
        	popup.showAndWait();
    }

    /** Active le mode connexion pour ajouter une arête depuis le nœud sélectionné */
    public void startAddEdge() {
        if (selectedNode == null) return;
        connectMode = true;
        connectSource = selectedNode;
        drawGraph();
    }

    /** Removes the currently selected element (node, edge or agent) */
    public void removeSelectedNode() {
        if (selectedAgent != null) {
            agents.remove(selectedAgent);
            if (engine != null) engine.removeAgent(selectedAgent);
            selectedAgent = null;
            drawGraph();
            return;
        }
        if (selectedEdge != null) {

            List<Agent> edgeAgents = new ArrayList<>(selectedEdge.getAgents());
            
            edges.remove(selectedEdge);
            graph.removeEdge(selectedEdge.getId());

            for (Agent agent : edgeAgents) {

                Node fallbackNode = selectedEdge.getSource();

                agent.arriveAt(fallbackNode);

                Pathfinder pathfinder = new Pathfinder();

                List<Node> newPath = pathfinder.dijkstraTime(
                    fallbackNode,
                    agent.getDestinationNode(),
                    graph
                );

                if (newPath != null && !newPath.isEmpty()) {
                    newPath.remove(0);
                }

                agent.setCurrentPath(newPath);
            }

            selectedEdge = null;

            drawGraph();
            return;
        }
        if (selectedNode == null) return;
        List<Agent> toRemove = new ArrayList<>();
        for (Agent a : agents) {
            if (selectedNode.equals(a.getCurrentNode())) {
                toRemove.add(a);
                if (engine != null) engine.removeAgent(a);
            }
        }
        agents.removeAll(toRemove);
        edges.removeIf(e -> e.getSource().equals(selectedNode) || e.getTarget().equals(selectedNode));
        graph.removeNode(selectedNode.getId());
        nodes.remove(selectedNode);
        selectedNode = null;
        drawGraph();
    }

    public void drawGraph() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // --- Rendu avec zoom et offset (coordonnées monde) ---
        
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = 0;
        double maxY = 0;

        for (Node n : nodes) {
            if (n.getX() < minX) minX = n.getX();
            if (n.getY() < minY) minY = n.getY();

            if (n.getX() > maxX) maxX = n.getX();
            if (n.getY() > maxY) maxY = n.getY();
        }
        
        double graphWidth = maxX - minX + 120;
        double graphHeight = maxY - minY + 60;

        double centerX = (canvas.getWidth() - graphWidth) / 2;
        double centerY = (canvas.getHeight() - graphHeight) / 2;
        gc.save();
        gc.translate(centerX, centerY);
        gc.scale(zoom, zoom);

        // Mode connexion — afficher un message
        if (connectMode) {
            gc.setFill(Color.DARKBLUE);
            gc.fillText("Mode connexion : cliquez sur le nœud cible (clic dans le vide pour annuler)", 10, 15);
        }

        // Draw edges
        for (Edge edge : edges) {
            if (edge == selectedEdge) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(3);
            } else if (edge.getOccupancy() >= edge.getWidth()) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(2);
            } else if (edge.getOccupancy() > 0) {
                gc.setStroke(Color.ORANGE);
                gc.setLineWidth(2);
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(2);
            }
            double x1 = edge.getSource().getX() + 60;
            double y1 = edge.getSource().getY() + 30;
            double x2 = edge.getTarget().getX() + 60;
            double y2 = edge.getTarget().getY() + 30;
            gc.strokeLine(x1, y1, x2, y2);

            // Afficher la largeur de l'arête
            double mx = (x1 + x2) / 2;
            double my = (y1 + y2) / 2;
            gc.setFill(Color.DARKGRAY);
            gc.fillText("w=" + edge.getWidth(), mx + 4, my - 4);

            if (edge.isDirected()) {
                double angle = Math.atan2(y2 - y1, x2 - x1);
                double arrowLength = 15;
                double arrowX = x2 - 30 * Math.cos(angle);
                double arrowY = y2 - 30 * Math.sin(angle);
                gc.setStroke(edge == selectedEdge ? Color.BLUE : Color.BLACK);
                gc.strokeLine(arrowX, arrowY, arrowX - arrowLength * Math.cos(angle - Math.PI / 6), arrowY - arrowLength * Math.sin(angle - Math.PI / 6));
                gc.strokeLine(arrowX, arrowY, arrowX - arrowLength * Math.cos(angle + Math.PI / 6), arrowY - arrowLength * Math.sin(angle + Math.PI / 6));
            }
        }

        // Draw nodes
        for (Node node : nodes) {
            switch (node.getType()) {
                case ROOM:      gc.setFill(Color.LIGHTBLUE);  break;
                case CORRIDOR:  gc.setFill(Color.LIGHTGRAY);  break;
                case EXIT:      gc.setFill(Color.LIGHTGREEN); break;
                case STAIRCASE: gc.setFill(Color.ORANGE);     break;
                default:        gc.setFill(Color.BLACK);
            }
            gc.fillRect(node.getX(), node.getY(), 120, 60);

            if (node == selectedNode || node == connectSource) {
                gc.setStroke(node == connectSource ? Color.DARKBLUE : Color.RED);
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
            }
            gc.strokeRect(node.getX(), node.getY(), 120, 60);
            gc.setFill(Color.BLACK);
            gc.fillText(node.getName(), node.getX() + 5, node.getY() + 20);
            int occupancy = node.getOccupancy();
            if (occupancy > 0) {
                gc.setFill(occupancy > 3 ? Color.RED : Color.DARKGREEN);
                gc.fillText("agents: " + occupancy, node.getX() + 5, node.getY() + 50);
            }
        }
        
     // Dessin du chemin restant de l'agent en pointillés
        if (selectedAgent != null && selectedAgent.getCurrentPath() != null
                && !selectedAgent.getCurrentPath().isEmpty()) {

            gc.setLineWidth(3);

            gc.setLineDashes(10);

            gc.setStroke(agentColor(selectedAgent));

            Node start = selectedAgent.getCurrentNode();

            if (start != null) {

                Node previous = start;

                for (Node next : selectedAgent.getCurrentPath()) {

                    double x1 = previous.getX() + 60;
                    double y1 = previous.getY() + 30;

                    double x2 = next.getX() + 60;
                    double y2 = next.getY() + 30;
        // Halo blanc
                    gc.setStroke(Color.WHITE);
                    gc.setLineWidth(5);
                    gc.strokeLine(x1, y1, x2, y2);
        // Ligne de couleur de l'agent
                    gc.setStroke(agentColor(selectedAgent));
                    gc.setLineWidth(3);
                    gc.strokeLine(x1, y1, x2, y2);

                    previous = next;
                }
            }

            gc.setLineDashes(null);
        }
        // Draw agents
        List<Agent> agentsToDraw = (engine != null) ? engine.getAgents() : agents;
        java.util.Map<Node, List<Agent>> agentsByNode = new java.util.HashMap<>();
        for (Agent agent : agentsToDraw) {
            Node node = agent.getCurrentNode();
            if (node != null) agentsByNode.computeIfAbsent(node, k -> new ArrayList<>()).add(agent);
        }

        for (java.util.Map.Entry<Node, List<Agent>> entry : agentsByNode.entrySet()) {
            Node node = entry.getKey();
            List<Agent> nodeAgents = entry.getValue();
            int count = nodeAgents.size();

            if (count <= 4) {
                for (int i = 0; i < count; i++) {
                    Agent agent = nodeAgents.get(i);
                    Color fill = agentColor(agent);
                    gc.setFill(fill);
                    double ax = node.getX() + 10 + i * 22;
                    double ay = node.getY() + 35;
                    gc.fillOval(ax, ay, 15, 15);
                    if (agent == selectedAgent) {
                        gc.setStroke(Color.BLUE);
                        gc.setLineWidth(2);
                        gc.strokeOval(ax, ay, 15, 15);
                    }
                }
            } else {
                long panicked = nodeAgents.stream().filter(a -> a.getState() == AgentState.PANICKED).count();
                long injured  = nodeAgents.stream().filter(a -> a.getState() == AgentState.INJURED).count();
                if (panicked > count / 2) gc.setFill(Color.RED);
                else if (injured > count / 2) gc.setFill(Color.YELLOW);
                else gc.setFill(Color.GREEN);
                double cx = node.getX() + 60;
                double cy = node.getY() + 42;
                gc.fillOval(cx - 18, cy - 18, 36, 36);
                gc.setFill(Color.BLACK);
                gc.fillText("x" + count, cx - 8, cy + 5);
            }
        }

        // Draw exiting agents (visible 1 tick at EXIT)
        if (engine != null) {
            for (Agent agent : engine.getExitingAgents()) {
                Node node = agent.getCurrentNode();
                if (node != null) {
                    gc.setFill(Color.WHITE);
                    gc.setStroke(Color.DARKGREEN);
                    gc.setLineWidth(1.5);
                    int index = node.getAgents().size();
                    double ax = node.getX() + 10 + (index % 5) * 22;
                    double ay = node.getY() + 35 + (index / 5) * 20;
                    gc.fillOval(ax, ay, 15, 15);
                    gc.strokeOval(ax, ay, 15, 15);
                }
            }
        }

        gc.restore();
        // --- Fin du rendu monde, retour en coordonnées écran ---

        // Compteur évacués (fixe sur l'écran, indépendant du zoom)
        

        // Panneau info selon sélection (fixe sur l'écran)
        
    }

    private Color agentColor(Agent agent) {
        switch (agent.getState()) {
            case CALM:     return Color.GREEN;
            case PANICKED: return Color.RED;
            case INJURED:  return Color.YELLOW;
            default:       return Color.BLACK;
        }
    }

    public void removeAgent(Agent agent) { agents.remove(agent); }
    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
}
