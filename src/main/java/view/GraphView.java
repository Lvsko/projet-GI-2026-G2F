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
    private SimulationEngine engine;
    private int nodeCounter;

    public GraphView(Canvas canvas, Graph graph) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
        this.graph = graph;
        this.nodes = new ArrayList<>(graph.getNodes());
        this.edges = new ArrayList<>(graph.getEdges());
        this.nodeCounter = nodes.size();
        
        canvas.setOnMouseClicked(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();
            for (Node node : nodes) {
                if (
                    mouseX >= node.getX()
                    && mouseX <= node.getX() + 120
                    && mouseY >= node.getY()
                    && mouseY <= node.getY() + 60
                ) {
                    selectedNode = node;
                    drawGraph();
                    System.out.println("Selected node: " + node.getName());
                }
            }
        });
       
    }

    public void setEngine(SimulationEngine engine) {
        this.engine = engine;
        this.agents = new ArrayList<>(engine.getAgents());
    }

    /** Spawns a new agent at the first available room node */
    public void spawnAgentAtRoom() {
        for (Node node : nodes) {
            if (node.getType() == NodeType.ROOM) {
                Agent agent = new Agent(node, graph);
                agents.add(agent);
                if (engine != null) engine.addAgent(agent);
                drawGraph();
                return;
            }
        }
    }

    /** Adds a new room node connected to the first corridor found */
    public void addRoomNode() {
        Node connectTo = null;
        for (Node n : nodes) {
            if (n.getType() == NodeType.CORRIDOR) {
                connectTo = n;
                break;
            }
        }
        if (connectTo == null && !nodes.isEmpty()) {
            connectTo = nodes.get(0);
        }
        if (connectTo == null) return;

        String id = "N" + nodeCounter;
        double x = 50 + ((nodeCounter - 8) % 3) * 200;
        double y = 420;
        Node newNode = new Node(id, "Room " + nodeCounter, x, y, 10, NodeStatus.OPEN, NodeType.ROOM, 1.0f);
        Edge newEdge = new Edge("E" + (edges.size() + 1), newNode, connectTo, 5, 1.0f, 1.0f, false);
        nodes.add(newNode);
        edges.add(newEdge);
        graph.addNode(newNode);
        graph.addEdge(newEdge);
        nodeCounter++;
        drawGraph();
    }

    /** Removes the currently selected node */
    public void removeSelectedNode() {
        if (selectedNode == null) return;
        // Remove agents on this node from simulation
        List<Agent> toRemove = new ArrayList<>();
        for (Agent a : agents) {
            if (selectedNode.equals(a.getCurrentNode())) {
                toRemove.add(a);
                if (engine != null) engine.removeAgent(a);
            }
        }
        agents.removeAll(toRemove);
        // Remove connected edges
        edges.removeIf(e -> e.getSource().equals(selectedNode) || e.getTarget().equals(selectedNode));
        // Remove from graph and nodes list
        graph.removeNode(selectedNode.getId());
        nodes.remove(selectedNode);
        selectedNode = null;
        drawGraph();
    }

    public void drawGraph() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Draw edges — color based on capacity
        for (Edge edge : edges) {
            if (edge.getOccupancy() >= edge.getWidth()) {
                gc.setStroke(Color.RED); // full
            } else if (edge.getOccupancy() > 0) {
                gc.setStroke(Color.ORANGE); // partially full
            } else {
                gc.setStroke(Color.BLACK); // empty
            }
            gc.setLineWidth(2);
            double x1 = edge.getSource().getX() + 60;
            double y1 = edge.getSource().getY() + 30;
            double x2 = edge.getTarget().getX() + 60;
            double y2 = edge.getTarget().getY() + 30;
            gc.strokeLine(x1, y1, x2, y2);
            if(edge.isDirected()) {
                double angle = Math.atan2(y2 - y1, x2 - x1);
                double arrowLength = 15;
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

        // Draw nodes
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
            gc.fillRect(node.getX(), node.getY(), 120, 60);
            if (node == selectedNode) {
                gc.setStroke(Color.RED);
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.BLACK);
                gc.setLineWidth(1);
            }
            gc.strokeRect(node.getX(), node.getY(), 120, 60);
            // Node label
            gc.setFill(Color.BLACK);
            gc.fillText(node.getName(), node.getX() + 5, node.getY() + 20);
            // Agent queue count
            int occupancy = node.getOccupancy();
            if (occupancy > 0) {
                gc.setFill(occupancy > 1 ? Color.RED : Color.DARKGREEN);
                gc.fillText("agents: " + occupancy, node.getX() + 5, node.getY() + 50);
            }
        }

        // Draw agents — offset circles so multiple agents are visible
        List<Agent> agentsToDraw =
                (engine != null) ? engine.getAgents() : agents;

        for (Agent agent : agentsToDraw) {
            Node node = agent.getCurrentNode();
            if (node != null) {
                switch(agent.getState()) {
                    case CALM:
                        gc.setFill(Color.GREEN);
                        break;
                    case PANICKED:
                        gc.setFill(Color.RED);
                        break;
                    case INJURED:
                        gc.setFill(Color.YELLOW);
                        break;
                    default:
                        gc.setFill(Color.BLACK);
                }
                List<Agent> nodeAgents = node.getAgents();
                int index = nodeAgents.indexOf(agent);
                double agentX = node.getX() + 10 + index * 20;
                double agentY = node.getY() + 30;
                gc.fillOval(agentX, agentY, 15, 15);
            }
        }

        // Draw selected node info panel
        if (selectedNode != null) {
            gc.setFill(Color.BLACK);
            gc.fillText("Selected Node", 620, 80);
            gc.fillText("ID: " + selectedNode.getId(), 620, 110);
            gc.fillText("Type: " + selectedNode.getType(), 620, 140);
            gc.fillText("Capacity: " + selectedNode.getMaxCapacity(), 620, 170);
            gc.fillText("Agents: " + selectedNode.getOccupancy(), 620, 200);
        }
    }
    
    public void removeAgent(Agent agent) {
        agents.remove(agent);
    }
    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
    
}
