package exit.view;

import exit.model.Graph;
import exit.model.Node;
import exit.model.Edge;
import exit.model.enums.NodeStatus;
import exit.model.enums.NodeType;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Configuration form for manually adding nodes and edges to the graph.
 * Allows the user to configure a building layout before launching the simulation.
 * @author Ruben
 */
public class ConfigView {

    private Graph graph;
    private GraphView graphView;

    /**
     * Creates a new ConfigView linked to an existing graph and renderer.
     * @param graph     the graph to configure
     * @param graphView the renderer to refresh after each modification
     */
    public ConfigView(Graph graph, GraphView graphView) {
        this.graph = graph;
        this.graphView = graphView;
    }

    /**
     * Opens the configuration window.
     */
    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Configure Graph");

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        // --- ADD NODE SECTION ---
        Label nodeTitle = new Label("Add Node");
        nodeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        GridPane nodeGrid = new GridPane();
        nodeGrid.setHgap(10);
        nodeGrid.setVgap(8);

        TextField nodeId = new TextField();
        nodeId.setPromptText("ID (e.g. N8)");

        TextField nodeName = new TextField();
        nodeName.setPromptText("Name (e.g. Room D)");

        TextField nodeCapacity = new TextField();
        nodeCapacity.setPromptText("Capacity (e.g. 10)");

        TextField nodeX = new TextField();
        nodeX.setPromptText("X position");

        TextField nodeY = new TextField();
        nodeY.setPromptText("Y position");

        ComboBox<NodeType> nodeType = new ComboBox<>();
        nodeType.getItems().addAll(NodeType.values());
        nodeType.setValue(NodeType.ROOM);

        nodeGrid.add(new Label("ID:"), 0, 0);
        nodeGrid.add(nodeId, 1, 0);
        nodeGrid.add(new Label("Name:"), 0, 1);
        nodeGrid.add(nodeName, 1, 1);
        nodeGrid.add(new Label("Type:"), 0, 2);
        nodeGrid.add(nodeType, 1, 2);
        nodeGrid.add(new Label("Capacity:"), 0, 3);
        nodeGrid.add(nodeCapacity, 1, 3);
        nodeGrid.add(new Label("X:"), 0, 4);
        nodeGrid.add(nodeX, 1, 4);
        nodeGrid.add(new Label("Y:"), 0, 5);
        nodeGrid.add(nodeY, 1, 5);

        Label nodeStatus = new Label();

        Button addNodeBtn = new Button("Add Node");
        addNodeBtn.setOnAction(e -> {
            try {
                String id = nodeId.getText().trim();
                String name = nodeName.getText().trim();
                int capacity = Integer.parseInt(nodeCapacity.getText().trim());
                double x = Double.parseDouble(nodeX.getText().trim());
                double y = Double.parseDouble(nodeY.getText().trim());
                NodeType type = nodeType.getValue();

                if (id.isEmpty() || name.isEmpty()) {
                    nodeStatus.setText("Error: ID and name are required.");
                    return;
                }
                if (graph.getNode(id) != null) {
                    nodeStatus.setText("Error: Node ID already exists.");
                    return;
                }

                Node node = new Node(id, name, x, y, capacity, NodeStatus.OPEN, type, 1.0f);
                graph.addNode(node);
                graphView.getNodes().add(node);
                graphView.drawGraph();
                nodeStatus.setText("Node '" + name + "' added.");
                nodeId.clear(); nodeName.clear(); nodeCapacity.clear();
                nodeX.clear(); nodeY.clear();
            } catch (NumberFormatException ex) {
                nodeStatus.setText("Error: capacity, X and Y must be numbers.");
            }
        });

        // --- ADD EDGE SECTION ---
        Label edgeTitle = new Label("Add Edge");
        edgeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        GridPane edgeGrid = new GridPane();
        edgeGrid.setHgap(10);
        edgeGrid.setVgap(8);

        TextField edgeId = new TextField();
        edgeId.setPromptText("ID (e.g. E8)");

        TextField edgeSource = new TextField();
        edgeSource.setPromptText("Source node ID");

        TextField edgeTarget = new TextField();
        edgeTarget.setPromptText("Target node ID");

        TextField edgeWidth = new TextField();
        edgeWidth.setPromptText("Width (e.g. 5)");

        TextField edgeDistance = new TextField();
        edgeDistance.setPromptText("Distance (e.g. 1.0)");

        CheckBox edgeDirected = new CheckBox("Directed");

        edgeGrid.add(new Label("ID:"), 0, 0);
        edgeGrid.add(edgeId, 1, 0);
        edgeGrid.add(new Label("Source:"), 0, 1);
        edgeGrid.add(edgeSource, 1, 1);
        edgeGrid.add(new Label("Target:"), 0, 2);
        edgeGrid.add(edgeTarget, 1, 2);
        edgeGrid.add(new Label("Width:"), 0, 3);
        edgeGrid.add(edgeWidth, 1, 3);
        edgeGrid.add(new Label("Distance:"), 0, 4);
        edgeGrid.add(edgeDistance, 1, 4);
        edgeGrid.add(edgeDirected, 1, 5);

        Label edgeStatus = new Label();

        Button addEdgeBtn = new Button("Add Edge");
        addEdgeBtn.setOnAction(e -> {
            try {
                String id = edgeId.getText().trim();
                String sourceId = edgeSource.getText().trim();
                String targetId = edgeTarget.getText().trim();
                int width = Integer.parseInt(edgeWidth.getText().trim());
                float distance = Float.parseFloat(edgeDistance.getText().trim());
                boolean directed = edgeDirected.isSelected();

                Node source = graph.getNode(sourceId);
                Node target = graph.getNode(targetId);

                if (source == null || target == null) {
                    edgeStatus.setText("Error: source or target node not found.");
                    return;
                }

                Edge edge = new Edge(id, source, target, width, distance, 1.0f, directed);
                graph.addEdge(edge);
                graphView.getEdges().add(edge);
                graphView.drawGraph();
                edgeStatus.setText("Edge '" + id + "' added.");
                edgeId.clear(); edgeSource.clear(); edgeTarget.clear();
                edgeWidth.clear(); edgeDistance.clear();
            } catch (NumberFormatException ex) {
                edgeStatus.setText("Error: width and distance must be numbers.");
            }
        });

        // --- LAUNCH SIMULATION BUTTON ---
        Button launchBtn = new Button("Launch Simulation");
        launchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        launchBtn.setOnAction(e -> {
            stage.close();
        });

        root.getChildren().addAll(
                nodeTitle, nodeGrid, addNodeBtn, nodeStatus,
                new Separator(),
                edgeTitle, edgeGrid, addEdgeBtn, edgeStatus,
                new Separator(),
                launchBtn
        );

        Scene scene = new Scene(root, 400, 650);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @return the configured graph
     */
    public Graph getGraph() {
        return graph;
    }
}