package view;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import model.agent.AgentState;
import model.node.NodeStatus;
import model.node.NodeType;
import simulation.SimulationEngine;
import simulation.Pathfinder;
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
    private double lastClickX = 100;
    private double lastClickY = 100;
    private double zoom = 1.0;
    private double offsetX = 0;
    private double offsetY = 0;
    private double dragStartX;
    private double dragStartY;
    private boolean draggingCanvas = false;
    private boolean draggingNode = false;
    private Node draggedNode = null;
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
            double mouseX = toWorldX(event.getX());
            double mouseY = toWorldY(event.getY());
            lastClickX = mouseX;
            lastClickY = mouseY;

            if (connectMode) {
                for (Node node : nodes) {
                    if (hitNode(node, mouseX, mouseY) && !node.equals(connectSource)) {
                        String edgeId = "E" + (edges.size() + 1);
                        Stage popup = new Stage();
                        popup.initModality(Modality.APPLICATION_MODAL);
                        popup.setTitle("Nouvelle arête");
                        TextField widthField    = new TextField("5");
                        TextField distanceField = new TextField("1.0");
                        TextField speedField    = new TextField("1.0");
                        ComboBox<Boolean> directedBox = new ComboBox<>();
                        directedBox.getItems().addAll(false, true);
                        directedBox.setValue(false);
                        Label errorLabel = new Label();
                        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
                        Button createButton = new Button("Créer");
                        createButton.setOnAction(e -> {
                            try {
                                int w      = Integer.parseInt(widthField.getText().trim());
                                float dist = Float.parseFloat(distanceField.getText().trim());
                                float spd  = Float.parseFloat(speedField.getText().trim());
                                // Validations #6 #8
                                if (w <= 0)    { errorLabel.setText("La largeur doit être > 0.");  return; }
                                if (dist <= 0) { errorLabel.setText("La distance doit être > 0."); return; }
                                if (spd <= 0)  { errorLabel.setText("La vitesse doit être > 0.");  return; }
                                Edge newEdge = new Edge(edgeId, connectSource, node, w, dist, spd, directedBox.getValue());
                                edges.add(newEdge);
                                graph.addEdge(newEdge);
                                connectMode  = false;
                                connectSource = null;
                                drawGraph();
                                popup.close();
                            } catch (NumberFormatException ex) {
                                errorLabel.setText("Valeurs invalides — entrez des nombres."); // #7
                            }
                        });
                        VBox layout = new VBox(10,
                            new Label("Largeur"),      widthField,
                            new Label("Distance"),     distanceField,
                            new Label("Speed Modifier"), speedField,
                            new Label("Directed"),     directedBox,
                            createButton, errorLabel
                        );
                        layout.setStyle("-fx-padding: 10;");
                        popup.setScene(new Scene(layout, 300, 320));
                        popup.showAndWait();
                        return;
                    }
                }
                connectMode   = false;
                connectSource = null;
                drawGraph();
                return;
            }

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
                        selectedNode  = null;
                        selectedEdge  = null;
                        drawGraph();
                        return;
                    }
                }
            }

            for (Node node : nodes) {
                if (hitNode(node, mouseX, mouseY)) {
                    selectedNode  = node;
                    selectedEdge  = null;
                    selectedAgent = null;
                    drawGraph();
                    return;
                }
            }

            for (Edge edge : edges) {
                if (hitEdge(edge, mouseX, mouseY)) {
                    selectedEdge  = edge;
                    selectedNode  = null;
                    selectedAgent = null;
                    drawGraph();
                    return;
                }
            }

            selectedNode  = null;
            selectedEdge  = null;
            selectedAgent = null;
            drawGraph();
        });

        // Zoom avec clamp pour éviter zoom nul ou négatif (bonus robustesse)
        canvas.setOnScroll(event -> {
            zoom = event.getDeltaY() > 0 ? zoom * 1.1 : zoom / 1.1;
            zoom = Math.max(0.1, Math.min(zoom, 8.0));
            drawGraph();
        });

        canvas.setOnMousePressed(event -> {
            dragStartX = event.getX();
            dragStartY = event.getY();
            double wx = toWorldX(event.getX());
            double wy = toWorldY(event.getY());
            for (Node node : nodes) {
                if (hitNode(node, wx, wy)) {
                    draggedNode   = node;
                    draggingNode  = true;
                    return;
                }
            }
            draggingCanvas = true;
        });

        canvas.setOnMouseDragged(event -> {
            double dx = event.getX() - dragStartX;
            double dy = event.getY() - dragStartY;
            if (draggingNode && draggedNode != null) {
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
            draggingNode   = false;
            draggedNode    = null;
        });
    }

    private double toWorldX(double screenX) { return (screenX - offsetX) / zoom; }
    private double toWorldY(double screenY) { return (screenY - offsetY) / zoom; }

    private boolean hitNode(Node node, double x, double y) {
        return x >= node.getX() && x <= node.getX() + 120
            && y >= node.getY() && y <= node.getY() + 60;
    }

    private boolean hitEdge(Edge edge, double mx, double my) {
        double x1   = edge.getSource().getX() + 60;
        double y1   = edge.getSource().getY() + 30;
        double x2   = edge.getTarget().getX() + 60;
        double y2   = edge.getTarget().getY() + 30;
        double len2 = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
        if (len2 == 0) return false;
        double t  = Math.max(0, Math.min(1, ((mx - x1) * (x2 - x1) + (my - y1) * (y2 - y1)) / len2));
        double px = x1 + t * (x2 - x1);
        double py = y1 + t * (y2 - y1);
        return Math.sqrt((mx - px) * (mx - px) + (my - py) * (my - py)) < 8;
    }

    public void setEngine(SimulationEngine engine) {
        this.engine = engine;
        this.agents = new ArrayList<>(engine.getAgents());
    }

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
        ComboBox<AgentState>    stateBox    = new ComboBox<>();
        ComboBox<AgentBehavior> behaviorBox = new ComboBox<>();
        ComboBox<AgentType>     typeBox     = new ComboBox<>();
        stateBox.getItems().addAll(AgentState.values());    stateBox.setValue(AgentState.CALM);
        behaviorBox.getItems().addAll(AgentBehavior.values()); behaviorBox.setValue(AgentBehavior.COOPERATIVE);
        typeBox.getItems().addAll(AgentType.values());      typeBox.setValue(AgentType.ADULT);
        Button createButton = new Button("Créer");
        createButton.setOnAction(e -> {
            Agent agent = new Agent(
                "agent" + System.currentTimeMillis(), finalTarget, 1.0f,
                stateBox.getValue(), behaviorBox.getValue(), typeBox.getValue(),
                0.5f, graph
            );
            agents.add(agent);
            if (engine != null) engine.addAgent(agent);
            drawGraph();
            popup.close();
        });
        VBox layout = new VBox(10,
            new Label("État"),         stateBox,
            new Label("Comportement"), behaviorBox,
            new Label("Type"),         typeBox,
            createButton
        );
        layout.setStyle("-fx-padding: 10;");
        popup.setScene(new Scene(layout, 250, 250));
        popup.showAndWait();
    }

    public void addRoomNode() {
        String id = "N" + (nodeCounter + 1);
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Nouveau nœud");
        TextField nameField         = new TextField("Room " + (nodeCounter + 1));
        TextField capacityField     = new TextField("10");
        TextField attractivenessField = new TextField("1.0");
        ComboBox<NodeType>   typeBox   = new ComboBox<>();
        ComboBox<NodeStatus> statusBox = new ComboBox<>();
        typeBox.getItems().addAll(NodeType.values());     typeBox.setValue(NodeType.ROOM);
        statusBox.getItems().addAll(NodeStatus.values()); statusBox.setValue(NodeStatus.OPEN);
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ff6b6b;");
        Button createButton = new Button("Créer");
        createButton.setOnAction(e -> {
            try {
                int    capacity      = Integer.parseInt(capacityField.getText().trim());
                float  attractiveness = Float.parseFloat(attractivenessField.getText().trim());
                String name          = nameField.getText().trim();
                if (name.isEmpty())    { errorLabel.setText("Le nom est requis.");           return; }
                if (capacity <= 0)     { errorLabel.setText("La capacité doit être > 0.");   return; } // #11
                if (attractiveness <= 0) { errorLabel.setText("L'attractivité doit être > 0."); return; }
                Node newNode = new Node(
                    id, name, lastClickX - 60, lastClickY - 30,
                    capacity, statusBox.getValue(), typeBox.getValue(), attractiveness
                );
                nodes.add(newNode);
                graph.addNode(newNode);
                nodeCounter++;
                drawGraph();
                popup.close();
            } catch (NumberFormatException ex) {
                errorLabel.setText("Valeurs invalides — entrez des nombres."); // #7
            }
        });
        VBox layout = new VBox(10,
            new Label("Nom"),         nameField,
            new Label("Type"),        typeBox,
            new Label("Statut"),      statusBox,
            new Label("Capacité"),    capacityField,
            new Label("Attractivité"), attractivenessField,
            createButton, errorLabel
        );
        layout.setStyle("-fx-padding: 10;");
        popup.setScene(new Scene(layout, 300, 370));
        popup.showAndWait();
    }

    public void startAddEdge() {
        if (selectedNode == null) return;
        connectMode   = true;
        connectSource = selectedNode;
        drawGraph();
    }

    /**
     * Vérifie si le chemin courant d'un agent emprunte l'arête src→tgt (ou tgt→src).
     */
    private boolean pathUsesEdge(Agent agent, Node src, Node tgt) {
        List<Node> path = agent.getCurrentPath();
        if (path == null || path.isEmpty()) return false;
        Node current = agent.getCurrentNode();
        if (current != null) {
            Node next = path.get(0);
            if ((current.equals(src) && next.equals(tgt)) ||
                (current.equals(tgt) && next.equals(src))) return true;
        }
        for (int i = 0; i < path.size() - 1; i++) {
            if ((path.get(i).equals(src) && path.get(i + 1).equals(tgt)) ||
                (path.get(i).equals(tgt) && path.get(i + 1).equals(src))) return true;
        }
        return false;
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
            Node edgeSource = selectedEdge.getSource();
            Node edgeTarget = selectedEdge.getTarget();

            List<Agent> agentsOnEdge = new ArrayList<>(selectedEdge.getAgents());

            List<Agent> allAgents = (engine != null) ? engine.getAgents() : agents;
            List<Agent> agentsToReroute = new ArrayList<>();
            for (Agent agent : allAgents) {
                if (pathUsesEdge(agent, edgeSource, edgeTarget)) agentsToReroute.add(agent);
            }

            // Supprimer EN PREMIER pour que Dijkstra ne réutilise pas l'arête supprimée
            edges.remove(selectedEdge);
            graph.removeEdge(selectedEdge.getId());

            Pathfinder pf = new Pathfinder();

            for (Agent agent : agentsOnEdge) {
                agent.arriveAt(edgeSource);
                if (agent.getDestinationNode() != null) {
                    List<Node> newPath = pf.dijkstraTime(edgeSource, agent.getDestinationNode(), graph);
                    if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
                    agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
                }
            }

            for (Agent agent : agentsToReroute) {
                Node currentNode = agent.getCurrentNode();
                if (currentNode != null && agent.getDestinationNode() != null) {
                    List<Node> newPath = pf.dijkstraTime(currentNode, agent.getDestinationNode(), graph);
                    if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
                    agent.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
                }
            }

            selectedEdge = null;
            drawGraph();
            return;
        }

        if (selectedNode == null) return;

        // KAN-13 : déplacer les agents vers un voisin avant suppression
        List<Node> neighbors = graph.getNeighbors(selectedNode);
        Pathfinder pf = new Pathfinder();

        for (Agent a : new ArrayList<>(agents)) {
            if (selectedNode.equals(a.getCurrentNode())) {
                if (!neighbors.isEmpty()) {
                    Node fallback = neighbors.get(0);
                    selectedNode.removeAgent(a);
                    a.arriveAt(fallback);
                    if (a.getDestinationNode() != null) {
                        List<Node> newPath = pf.dijkstraTime(fallback, a.getDestinationNode(), graph);
                        if (newPath != null && !newPath.isEmpty()) newPath.remove(0);
                        a.setCurrentPath(newPath != null ? newPath : new ArrayList<>());
                    }
                } else {
                    if (engine != null) engine.removeAgent(a);
                    agents.remove(a);
                }
            }
        }

        edges.removeIf(e -> e.getSource().equals(selectedNode) || e.getTarget().equals(selectedNode));
        graph.removeNode(selectedNode.getId());
        nodes.remove(selectedNode);
        selectedNode = null;
        drawGraph();
    }

    public void drawGraph() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.save();
        gc.translate(offsetX, offsetY);
        gc.scale(zoom, zoom);

        if (connectMode) {
            gc.setFill(Color.DARKBLUE);
            gc.fillText("Mode connexion : cliquez sur le nœud cible (clic dans le vide pour annuler)", 10, 15);
        }

        // Draw edges
        for (Edge edge : edges) {
            if (edge == selectedEdge) {
                gc.setStroke(Color.BLUE);   gc.setLineWidth(3);
            } else if (edge.getOccupancy() >= edge.getWidth()) {
                gc.setStroke(Color.RED);    gc.setLineWidth(2);
            } else if (edge.getOccupancy() > 0) {
                gc.setStroke(Color.ORANGE); gc.setLineWidth(2);
            } else {
                gc.setStroke(Color.BLACK);  gc.setLineWidth(2);
            }
            double x1 = edge.getSource().getX() + 60;
            double y1 = edge.getSource().getY() + 30;
            double x2 = edge.getTarget().getX() + 60;
            double y2 = edge.getTarget().getY() + 30;
            gc.strokeLine(x1, y1, x2, y2);
            gc.setFill(Color.DARKGRAY);
            gc.fillText("w=" + edge.getWidth(), (x1 + x2) / 2 + 4, (y1 + y2) / 2 - 4);
            if (edge.isDirected()) {
                double angle       = Math.atan2(y2 - y1, x2 - x1);
                double arrowLength = 15;
                double arrowX      = x2 - 30 * Math.cos(angle);
                double arrowY      = y2 - 30 * Math.sin(angle);
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
                gc.fillText("agents: " + occupancy, node.getX() + 5, node.getY() + 56);
            }
        }

        // Draw path of selected agent (dashed)
        if (selectedAgent != null && selectedAgent.getCurrentPath() != null
                && !selectedAgent.getCurrentPath().isEmpty()) {
            gc.setLineDashes(10);
            Node start = selectedAgent.getCurrentNode();
            if (start != null) {
                Node previous = start;
                for (Node next : selectedAgent.getCurrentPath()) {
                    double x1 = previous.getX() + 60, y1 = previous.getY() + 30;
                    double x2 = next.getX()     + 60, y2 = next.getY()     + 30;
                    gc.setStroke(Color.WHITE);             gc.setLineWidth(5); gc.strokeLine(x1, y1, x2, y2);
                    gc.setStroke(agentColor(selectedAgent)); gc.setLineWidth(3); gc.strokeLine(x1, y1, x2, y2);
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
            Node node       = entry.getKey();
            List<Agent> nodeAgents = entry.getValue();
            int  count      = nodeAgents.size();
            if (count <= 4) {
                for (int i = 0; i < count; i++) {
                    Agent agent = nodeAgents.get(i);
                    gc.setFill(agentColor(agent));
                    double ax = node.getX() + 10 + i * 22;
                    double ay = node.getY() + 38;
                    gc.fillOval(ax, ay, 15, 15);
                    if (agent == selectedAgent) {
                        gc.setStroke(Color.BLUE); gc.setLineWidth(2);
                        gc.strokeOval(ax, ay, 15, 15);
                    }
                }
            } else {
                long panicked = nodeAgents.stream().filter(a -> a.getState() == AgentState.PANICKED).count();
                long injured  = nodeAgents.stream().filter(a -> a.getState() == AgentState.INJURED).count();
                if      (panicked > count / 2) gc.setFill(Color.RED);
                else if (injured  > count / 2) gc.setFill(Color.YELLOW);
                else                           gc.setFill(Color.GREEN);
                double cx = node.getX() + 60, cy = node.getY() + 42;
                gc.fillOval(cx - 18, cy - 18, 36, 36);
                gc.setFill(Color.BLACK);
                gc.fillText("x" + count, cx - 8, cy + 5);
            }
        }

        // Draw exiting agents
        if (engine != null) {
            for (Agent agent : engine.getExitingAgents()) {
                Node node = agent.getCurrentNode();
                if (node != null) {
                    gc.setFill(Color.WHITE); gc.setStroke(Color.DARKGREEN); gc.setLineWidth(1.5);
                    int index = node.getAgents().size();
                    double ax = node.getX() + 10 + (index % 5) * 22;
                    double ay = node.getY() + 38 + (index / 5) * 20;
                    gc.fillOval(ax, ay, 15, 15);
                    gc.strokeOval(ax, ay, 15, 15);
                }
            }
        }

        gc.restore();

        // Compteur évacués (fixe)
        if (engine != null) {
            gc.setFill(Color.DARKGREEN);
            gc.fillRect(620, 15, 160, 25);
            gc.setFill(Color.WHITE);
            gc.fillText("Évacués : " + engine.getStatistics().getEvacuatedCount(), 630, 32);
        }

        // Panneau info (fixe)
        double px = 620, py = 55;
        if (selectedAgent != null) {
            gc.setFill(Color.BLACK);
            gc.fillText("— Agent sélectionné —", px, py);
            gc.fillText("ID : "    + selectedAgent.getId(),    px, py + 20);
            gc.fillText("État : "  + selectedAgent.getState(), px, py + 40);
            gc.fillText("Type : "  + selectedAgent.getType(),  px, py + 60);
            gc.fillText("Nœud : "  + (selectedAgent.getCurrentNode() != null
                ? selectedAgent.getCurrentNode().getName() : "transit"), px, py + 80);
        } else if (selectedEdge != null) {
            gc.setFill(Color.BLACK);
            gc.fillText("— Arête sélectionnée —",                    px, py);
            gc.fillText("ID : "        + selectedEdge.getId(),        px, py + 20);
            gc.fillText("Source : "    + selectedEdge.getSource().getName(), px, py + 40);
            gc.fillText("Cible : "     + selectedEdge.getTarget().getName(), px, py + 60);
            gc.fillText("Largeur : "   + selectedEdge.getWidth(),     px, py + 80);
            gc.fillText("Occupancy : " + selectedEdge.getOccupancy(), px, py + 100);
        } else if (selectedNode != null) {
            gc.setFill(Color.BLACK);
            gc.fillText("— Nœud sélectionné —",                      px, py);
            gc.fillText("ID : "       + selectedNode.getId(),         px, py + 20);
            gc.fillText("Type : "     + selectedNode.getType(),       px, py + 40);
            gc.fillText("Capacité : " + selectedNode.getMaxCapacity(), px, py + 60);
            gc.fillText("Agents : "   + selectedNode.getOccupancy(),  px, py + 80);
            gc.fillText("(Cliquez 'Add Edge'",  px, py + 100);
            gc.fillText(" pour connecter)",     px, py + 115);
        }
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
    public Graph getGraph()              { return graph; }
    public List<Agent> getAgents()       { return agents; }
}
