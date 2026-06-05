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
import javafx.scene.canvas.Canvas;
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
    private GraphView preview;
    private Canvas previewCanvas;

    public ConfigView() {
        this.graph = new Graph();
    }

    public void start(Stage stage) {
        stage.setTitle("Configure Graph");

        // --- FORMULAIRE (gauche) ---
        String labelStyle = "-fx-text-fill: #e0e0e0; -fx-font-family: Arial;";
        String titleStyle = "-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 14;";
        String fieldStyle = "-fx-background-color: #303030; -fx-text-fill: #e0e0e0; -fx-border-color: #616161; -fx-border-radius: 4;";
        String btnPrimary = "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14 6 14;";
        String btnSecondary = "-fx-background-color: #424242; -fx-text-fill: #e0e0e0; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: #616161; -fx-border-radius: 6; -fx-padding: 6 14 6 14;";
        String btnDanger = "-fx-background-color: #7B1F1F; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14 6 14;";

        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        form.setPrefWidth(420);
        form.setStyle("-fx-background-color: #424242;");

        // ADD NODE
        Label nodeTitle = new Label("Add Node");
        nodeTitle.setStyle(titleStyle);
        GridPane nodeGrid = new GridPane();
        nodeGrid.setHgap(10); nodeGrid.setVgap(8);
        TextField nodeId = new TextField(); nodeId.setPromptText("ID (e.g. N1)");
        TextField nodeName = new TextField(); nodeName.setPromptText("Name (e.g. Room A)");
        TextField nodeCapacity = new TextField(); nodeCapacity.setPromptText("Capacity (e.g. 10)");
        TextField nodeX = new TextField(); nodeX.setPromptText("X position");
        TextField nodeY = new TextField(); nodeY.setPromptText("Y position");
        ComboBox<NodeType> nodeType = new ComboBox<>();
        nodeType.getItems().addAll(NodeType.values());
        nodeType.setValue(NodeType.ROOM);
        nodeId.setStyle(fieldStyle); nodeName.setStyle(fieldStyle);
        nodeCapacity.setStyle(fieldStyle); nodeX.setStyle(fieldStyle); nodeY.setStyle(fieldStyle);
        nodeType.setStyle(fieldStyle);

        Label lId1 = new Label("ID:"); lId1.setStyle(labelStyle);
        Label lName = new Label("Name:"); lName.setStyle(labelStyle);
        Label lType = new Label("Type:"); lType.setStyle(labelStyle);
        Label lCap = new Label("Capacity:"); lCap.setStyle(labelStyle);
        Label lX = new Label("X:"); lX.setStyle(labelStyle);
        Label lY = new Label("Y:"); lY.setStyle(labelStyle);
        nodeGrid.add(lId1, 0, 0); nodeGrid.add(nodeId, 1, 0);
        nodeGrid.add(lName, 0, 1); nodeGrid.add(nodeName, 1, 1);
        nodeGrid.add(lType, 0, 2); nodeGrid.add(nodeType, 1, 2);
        nodeGrid.add(lCap, 0, 3);  nodeGrid.add(nodeCapacity, 1, 3);
        nodeGrid.add(lX, 0, 4);    nodeGrid.add(nodeX, 1, 4);
        nodeGrid.add(lY, 0, 5);    nodeGrid.add(nodeY, 1, 5);
        Label nodeStatus = new Label(); nodeStatus.setStyle(labelStyle);
        Button addNodeBtn = new Button("Add Node"); addNodeBtn.setStyle(btnPrimary);
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
                refreshPreview();
            } catch (NumberFormatException ex) {
                nodeStatus.setText("Error: capacity, X and Y must be numbers.");
            }
        });

        // ADD EDGE
        Label edgeTitle = new Label("Add Edge");
        edgeTitle.setStyle(titleStyle);
        GridPane edgeGrid = new GridPane();
        edgeGrid.setHgap(10); edgeGrid.setVgap(8);
        TextField edgeId = new TextField(); edgeId.setPromptText("ID (e.g. E1)");
        TextField edgeSource = new TextField(); edgeSource.setPromptText("Source node ID");
        TextField edgeTarget = new TextField(); edgeTarget.setPromptText("Target node ID");
        TextField edgeWidth = new TextField(); edgeWidth.setPromptText("Width (e.g. 5)");
        TextField edgeDistance = new TextField(); edgeDistance.setPromptText("Distance (e.g. 1.0)");
        CheckBox edgeDirected = new CheckBox("Directed");
        edgeId.setStyle(fieldStyle); edgeSource.setStyle(fieldStyle); edgeTarget.setStyle(fieldStyle);
        edgeWidth.setStyle(fieldStyle); edgeDistance.setStyle(fieldStyle);
        edgeDirected.setStyle(labelStyle);

        Label lId2 = new Label("ID:"); lId2.setStyle(labelStyle);
        Label lSrc = new Label("Source:"); lSrc.setStyle(labelStyle);
        Label lTgt = new Label("Target:"); lTgt.setStyle(labelStyle);
        Label lW = new Label("Width:"); lW.setStyle(labelStyle);
        Label lDist = new Label("Distance:"); lDist.setStyle(labelStyle);
        edgeGrid.add(lId2, 0, 0); edgeGrid.add(edgeId, 1, 0);
        edgeGrid.add(lSrc, 0, 1); edgeGrid.add(edgeSource, 1, 1);
        edgeGrid.add(lTgt, 0, 2); edgeGrid.add(edgeTarget, 1, 2);
        edgeGrid.add(lW, 0, 3);   edgeGrid.add(edgeWidth, 1, 3);
        edgeGrid.add(lDist, 0, 4); edgeGrid.add(edgeDistance, 1, 4);
        edgeGrid.add(edgeDirected, 1, 5);
        Label edgeStatus = new Label(); edgeStatus.setStyle(labelStyle);
        Button addEdgeBtn = new Button("Add Edge"); addEdgeBtn.setStyle(btnPrimary);
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
                refreshPreview();
            } catch (NumberFormatException ex) {
                edgeStatus.setText("Error: width and distance must be numbers.");
            }
        });

        // SAVE / LOAD
        Label saveLoadTitle = new Label("Save / Load Plan");
        saveLoadTitle.setStyle(titleStyle);
        Label saveLoadStatus = new Label(); saveLoadStatus.setStyle(labelStyle);
        Button saveBtn = new Button("💾 Save Plan"); saveBtn.setStyle(btnSecondary);
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
        Button loadBtn = new Button("📂 Load Plan"); loadBtn.setStyle(btnSecondary);
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
                    refreshPreview();
                } catch (Exception ex) {
                    saveLoadStatus.setText("Error loading: " + ex.getMessage());
                }
            }
        });
        HBox saveLoadButtons = new HBox(10, saveBtn, loadBtn);

        // LAUNCH
        Button launchBtn = new Button("▶ Lancer la simulation");
        launchBtn.setStyle(btnPrimary + "-fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 13;");
        launchBtn.setOnAction(e -> {
            stage.close();
            Stage simStage = new Stage();
            new MainView(graph, new ArrayList<Agent>(), "config").start(simStage);
        });

        // RETOUR
        Button retourBtn = new Button("← Retour"); retourBtn.setStyle(btnSecondary);
        retourBtn.setOnAction(e -> {
            stage.close();
            Stage homeStage = new Stage();
            new HomeView().start(homeStage);
        });

        form.getChildren().addAll(
            nodeTitle, nodeGrid, addNodeBtn, nodeStatus,
            new Separator(),
            edgeTitle, edgeGrid, addEdgeBtn, edgeStatus,
            new Separator(),
            saveLoadTitle, saveLoadButtons, saveLoadStatus,
            new Separator(),
            launchBtn, retourBtn
        );

        // --- PREVIEW (droite) ---
        previewCanvas = new Canvas(600, 500);
        preview = new GraphView(previewCanvas, graph);

        VBox previewBox = new VBox(8);
        previewBox.setPadding(new Insets(15));
        previewBox.setStyle("-fx-background-color: #424242;");
        Label previewTitle = new Label("Aperçu du graphe");
        previewTitle.setStyle("-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 14;");
        previewBox.getChildren().addAll(previewTitle, previewCanvas);

        ScrollPane scrollForm = new ScrollPane(form);
        scrollForm.setStyle("-fx-background-color: #424242; -fx-background: #424242;");

        HBox mainLayout = new HBox(10, scrollForm, previewBox);
        mainLayout.setStyle("-fx-background-color: #424242;");

        Scene scene = new Scene(mainLayout, 1060, 720);
        stage.setScene(scene);
        stage.show();

        // Dessiner le graphe initial (vide)
        preview.drawGraph();
    }

    /** Recrée le GraphView avec le graphe courant et redessine */
    private void refreshPreview() {
        preview = new GraphView(previewCanvas, graph);
        preview.drawGraph();
    }

    public Graph getGraph() { return graph; }
}
