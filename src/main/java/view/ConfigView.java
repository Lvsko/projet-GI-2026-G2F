package view;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import model.node.NodeStatus;
import model.node.NodeType;
import simulation.SimulationState;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;

/**
 * Configuration form for manually adding nodes and edges to the graph.
 * @author Ruben
 */
public class ConfigView {

    private Graph graph;

    public ConfigView() {
        this.graph = new Graph();
    }

    public void start(Stage stage) {
        stage.setTitle("Configure Graph");

        VBox root = new VBox(15);
        root.setPadding(new Insets(15));

        // --- ADD NODE SECTION ---
        Label nodeTitle = new Label("Add Node");
        nodeTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        GridPane nodeGrid = new GridPane();
        nodeGrid.setHgap(10);
        nodeGrid.setVgap(8);
        TextField nodeId = new TextField(); nodeId.setPromptText("ID (e.g. N1)");
        TextField nodeName = new TextField(); nodeName.setPromptText("Name (e.g. Room A)");
        TextField nodeCapacity = new TextField(); nodeCapacity.setPromptText("Capacity (e.g. 10)");
        TextField nodeX = new TextField(); nodeX.setPromptText("X position");
        TextField nodeY = new TextField(); nodeY.setPromptText("Y position");
        ComboBox<NodeType> nodeType = new ComboBox<>();
        nodeType.getItems().addAll(NodeType.values());
        nodeType.setValue(NodeType.ROOM);
        nodeGrid.add(new Label("ID:"), 0, 0);       nodeGrid.add(nodeId, 1, 0);
        nodeGrid.add(new Label("Name:"), 0, 1);     nodeGrid.add(nodeName, 1, 1);
        nodeGrid.add(new Label("Type:"), 0, 2);     nodeGrid.add(nodeType, 1, 2);
        nodeGrid.add(new Label("Capacity:"), 0, 3); nodeGrid.add(nodeCapacity, 1, 3);
        nodeGrid.add(new Label("X:"), 0, 4);        nodeGrid.add(nodeX, 1, 4);
        nodeGrid.add(new Label("Y:"), 0, 5);        nodeGrid.add(nodeY, 1, 5);
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
                if (id.isEmpty() || name.isEmpty()) { nodeStatus.setText("Error: ID and name are required."); return; }
                if (graph.getNode(id) != null) { nodeStatus.setText("Error: Node ID already exists."); return; }
                graph.addNode(new Node(id, name, x, y, capacity, NodeStatus.OPEN, type, 1.0f));
                nodeStatus.setText("Node '" + name + "' added.");
                nodeId.clear(); nodeName.clear(); nodeCapacity.clear(); nodeX.clear(); nodeY.clear();
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
        TextField edgeId = new TextField(); edgeId.setPromptText("ID (e.g. E1)");
        TextField edgeSource = new TextField(); edgeSource.setPromptText("Source node ID");
        TextField edgeTarget = new TextField(); edgeTarget.setPromptText("Target node ID");
        TextField edgeWidth = new TextField(); edgeWidth.setPromptText("Width (e.g. 5)");
        TextField edgeDistance = new TextField(); edgeDistance.setPromptText("Distance (e.g. 1.0)");
        CheckBox edgeDirected = new CheckBox("Directed");
        edgeGrid.add(new Label("ID:"), 0, 0);       edgeGrid.add(edgeId, 1, 0);
        edgeGrid.add(new Label("Source:"), 0, 1);   edgeGrid.add(edgeSource, 1, 1);
        edgeGrid.add(new Label("Target:"), 0, 2);   edgeGrid.add(edgeTarget, 1, 2);
        edgeGrid.add(new Label("Width:"), 0, 3);    edgeGrid.add(edgeWidth, 1, 3);
        edgeGrid.add(new Label("Distance:"), 0, 4); edgeGrid.add(edgeDistance, 1, 4);
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
                if (source == null || target == null) { edgeStatus.setText("Error: source or target node not found."); return; }
                graph.addEdge(new Edge(id, source, target, width, distance, 1.0f, directed));
                edgeStatus.setText("Edge '" + id + "' added.");
                edgeId.clear(); edgeSource.clear(); edgeTarget.clear(); edgeWidth.clear(); edgeDistance.clear();
            } catch (NumberFormatException ex) {
                edgeStatus.setText("Error: width and distance must be numbers.");
            }
        });

        // --- SAVE / LOAD SECTION ---
        Label saveLoadTitle = new Label("Save / Load Plan");
        saveLoadTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label saveLoadStatus = new Label();
        Button saveBtn = new Button("Save Plan");
        saveBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Save Plan");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT Plan", "*.exit"));
            File file = fc.showSaveDialog(stage);
            if (file != null) {
                try {
                    new SimulationState(graph, new ArrayList<>(), 0).save(file.getAbsolutePath());
                    saveLoadStatus.setText("Plan saved.");
                } catch (Exception ex) {
                    saveLoadStatus.setText("Error saving: " + ex.getMessage());
                }
            }
        });
        Button loadBtn = new Button("Load Plan");
        loadBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Load Plan");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXIT Plan", "*.exit"));
            File file = fc.showOpenDialog(stage);
            if (file != null) {
                try {
                    SimulationState state = SimulationState.load(file.getAbsolutePath());
                    graph = state.getGraph();
                    saveLoadStatus.setText("Plan loaded.");
                } catch (Exception ex) {
                    saveLoadStatus.setText("Error loading: " + ex.getMessage());
                }
            }
        });
        HBox saveLoadButtons = new HBox(10, saveBtn, loadBtn);

        // --- LAUNCH BUTTON ---
        Button launchBtn = new Button("Launch Simulation");
        launchBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        launchBtn.setOnAction(e -> {
            stage.close();
            Stage simStage = new Stage();
            new MainView(graph, new ArrayList<Agent>(), "config").start(simStage);
        });

        // --- RETOUR BUTTON ---
        Button retourBtn = new Button("← Retour");
        retourBtn.setOnAction(e -> {
            stage.close();
            Stage homeStage = new Stage();
            new HomeView().start(homeStage);
        });

        root.getChildren().addAll(
                nodeTitle, nodeGrid, addNodeBtn, nodeStatus,
                new Separator(),
                edgeTitle, edgeGrid, addEdgeBtn, edgeStatus,
                new Separator(),
                saveLoadTitle, saveLoadButtons, saveLoadStatus,
                new Separator(),
                launchBtn, retourBtn
        );

        Scene scene = new Scene(new ScrollPane(root), 420, 700);
        stage.setScene(scene);
        stage.show();
    }

    public Graph getGraph() { return graph; }
}
