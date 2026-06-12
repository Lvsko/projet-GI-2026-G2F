package view;

import model.Graph;
import model.node.Node;
import model.Edge;
import model.agent.Agent;
import model.agent.AgentBehavior;
import model.agent.AgentState;
import model.agent.AgentType;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import javafx.scene.input.KeyCode;

/**
 * JavaFX view for manually configuring a building graph before launching the simulation.
 * Allows adding nodes, edges and agents, saving/loading plans, generating random graphs,
 * and undoing/redoing changes via Ctrl+Z / Ctrl+Y.
 * @author Ruben
 */
public class ConfigView {

    private Graph graph;
    private GraphView preview;
    private Canvas previewCanvas;
    private List<Agent> agents = new ArrayList<>();
    private final ArrayDeque<Graph> undoStack = new ArrayDeque<>();
    private final ArrayDeque<Graph> redoStack = new ArrayDeque<>();

    public ConfigView() {
        this.graph = new Graph();
    }

    /**
     * Creates a deep copy of the given graph via serialization.
     * Used to snapshot graph state for undo/redo.
     * @param g the graph to copy
     * @return a fully independent copy of {@code g}, or {@code g} itself if serialization fails
     */
    private Graph deepCopy(Graph g) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            new ObjectOutputStream(bos).writeObject(g);
            return (Graph) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        } catch (Exception e) {
            return g;
        }
    }

    /**
     * Generates {@code n} random nodes and connects them into a random spanning tree,
     * guaranteeing graph connectivity. Node types alternate between ROOM and CORRIDOR,
     * with the last node always being an EXIT. Saves state for undo before modifying the graph.
     * @param n the number of nodes to generate 
     */
    private void generateRandom(int n) {
        saveState();
        Random rand = new Random();
        List<Node> newNodes = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String id = "RN" + (graph.getNodes().size() + i);
            double x = 50 + rand.nextDouble() * 500;
            double y = 50 + rand.nextDouble() * 400;

            
            NodeType type;
            if (i == n - 1) {
                type = NodeType.EXIT;
            }
            else if (i % 2 == 0){
                type = NodeType.ROOM;
            }
            else {
                type = NodeType.CORRIDOR;
            }

            Node node = new Node(id, id, x, y, 10, NodeStatus.OPEN, type, 1.0f);
            newNodes.add(node);
            graph.addNode(node);
        }

        //Chains all nodes to guarantee connexity and add random connexions between nodes 
        for (int i = 1; i < newNodes.size(); i++) {
            String edgeId = "RE" + (graph.getEdges().size());
            Node target = newNodes.get(i);
            Node source = newNodes.get(rand.nextInt(i)); // rand within previous nodes
            graph.addEdge(new Edge(edgeId, source, target, 5, 1.0f, 1.0f, false));
        }



        refreshPreview();
    }

    /** Saves the current graph state onto the undo stack and clears the redo stack. */
    private void saveState() {
        undoStack.push(deepCopy(graph));
        redoStack.clear();
    }

    /** Restores the previous graph state from the undo stack and updates the preview. */
    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(deepCopy(graph));
            graph = undoStack.pop();
            refreshPreview();
        }
    }

    /** Reapplies the last undone graph state from the redo stack and updates the preview. */
    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(deepCopy(graph));
            graph = redoStack.pop();
            refreshPreview();
        }
    }

    /**
     * Applies dark theme styling to a ComboBox, including the selected item cell.
     * @param <T> the type of items in the combo box
     * @param combo the ComboBox to style
     */
    private <T> void darkCombo(ComboBox<T> combo) {
        combo.setStyle("-fx-background-color: #303030; -fx-border-color: #616161; -fx-border-radius: 4;");
        combo.setButtonCell(new ListCell<T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: #e0e0e0; -fx-background-color: #303030;");
                }
            }
        });
    }

    /**
     * Builds and displays the configuration window.
     * The window contains sections for adding nodes, edges, agents,
     * saving/loading plans, random generation, and undo/redo controls.
     * Ctrl+Z and Ctrl+Y are bound to undo and redo respectively.
     * @param stage the JavaFX stage to display the view on
     */
    public void start(Stage stage) {
        stage.setTitle("Configure Graph");

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

        // UNDO / REDO
        Button undoBtn = new Button("Undo"); undoBtn.setStyle(btnSecondary);
        Button redoBtn = new Button("Redo"); redoBtn.setStyle(btnSecondary);
        undoBtn.setOnAction(e -> undo());
        redoBtn.setOnAction(e -> redo());
        HBox undoRedoBox = new HBox(10, undoBtn, redoBtn);

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
        darkCombo(nodeType);

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

                saveState(); //Loaded if undo is pressed
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

                saveState(); //Loaded if undo is pressed
                graph.addEdge(new Edge(id, source, target, width, distance, 1.0f, directed));
                edgeStatus.setText("Edge '" + id + "' added.");
                edgeId.clear(); edgeSource.clear(); edgeTarget.clear(); edgeWidth.clear(); edgeDistance.clear();
                refreshPreview();
            } catch (NumberFormatException ex) {
                edgeStatus.setText("Error: width and distance must be numbers.");
            }
        });

        // ADD AGENTS
        Label agentTitle = new Label("Add Agents");
        agentTitle.setStyle(titleStyle);
        GridPane agentGrid = new GridPane();
        agentGrid.setHgap(10); agentGrid.setVgap(8);

        ComboBox<Node> agentNode = new ComboBox<>();
        agentNode.setPromptText("Select start node");
        agentNode.setOnShowing(e -> agentNode.getItems().setAll(graph.getNodes()));
        darkCombo(agentNode);

        TextField agentCount = new TextField();
        agentCount.setPromptText("Number of agents (e.g. 3)");
        agentCount.setStyle(fieldStyle);

        ComboBox<AgentState> agentState = new ComboBox<>();
        agentState.getItems().addAll(AgentState.values());
        agentState.setValue(AgentState.CALM);
        darkCombo(agentState);

        ComboBox<AgentType> agentType = new ComboBox<>();
        agentType.getItems().addAll(AgentType.values());
        agentType.setValue(AgentType.ADULT);
        darkCombo(agentType);

        ComboBox<AgentBehavior> agentBehavior = new ComboBox<>();
        agentBehavior.getItems().addAll(AgentBehavior.values());
        agentBehavior.setValue(AgentBehavior.COOPERATIVE);
        darkCombo(agentBehavior);

        Label lAN = new Label("Start Node:"); lAN.setStyle(labelStyle);
        Label lAC = new Label("Count:");      lAC.setStyle(labelStyle);
        Label lAS = new Label("State:");      lAS.setStyle(labelStyle);
        Label lAT = new Label("Type:");       lAT.setStyle(labelStyle);
        Label lAB = new Label("Behavior:");   lAB.setStyle(labelStyle);

        agentGrid.add(lAN, 0, 0); agentGrid.add(agentNode,     1, 0);
        agentGrid.add(lAC, 0, 1); agentGrid.add(agentCount,    1, 1);
        agentGrid.add(lAS, 0, 2); agentGrid.add(agentState,    1, 2);
        agentGrid.add(lAT, 0, 3); agentGrid.add(agentType,     1, 3);
        agentGrid.add(lAB, 0, 4); agentGrid.add(agentBehavior, 1, 4);

        Label agentStatus = new Label(); agentStatus.setStyle(labelStyle);
        Button addAgentBtn = new Button("Add Agents"); addAgentBtn.setStyle(btnPrimary);
        addAgentBtn.setOnAction(e -> {
            try {
                Node startNode = agentNode.getValue();
                int count = Integer.parseInt(agentCount.getText().trim());
                if (startNode == null || count <= 0) {
                    agentStatus.setText("Error: select a node and enter a valid count.");
                    return;
                }
                AgentState state = agentState.getValue();
                AgentType type = agentType.getValue();
                AgentBehavior behavior = agentBehavior.getValue();
                for (int i = 0; i < count; i++) {
                    agents.add(new Agent("agent_" + agents.size(), startNode, 1.0f, state, behavior, type, 0.5f, graph));
                }
                agentStatus.setText(count + " agent(s) added on '" + startNode.getId() + "'.");
                agentCount.clear();
            } catch (NumberFormatException ex) {
                agentStatus.setText("Error: count must be a number.");
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

        // RANDOM GENERATION
        Button randomBtn = new Button("Random Generation"); randomBtn.setStyle(btnSecondary);
        randomBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("5");
            dialog.setTitle("Random Generation");
            dialog.setHeaderText("Generate random nodes and edges");
            dialog.setContentText("Number of nodes:");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(val -> {
                try {
                    int n = Integer.parseInt(val.trim());
                    if (n <= 0) return;
                    generateRandom(n);
                } catch (NumberFormatException ex) {
                    // entrée invalide, on ignore
                }
            });
        });

        // LAUNCH
        Button launchBtn = new Button("▶ Lancer la simulation");
        launchBtn.setStyle(btnPrimary + "-fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 13;");
        launchBtn.setOnAction(e -> {
            new MainView(graph, agents, "config").start(stage);
        });

        // RETOUR
        Button retourBtn = new Button("← Retour"); retourBtn.setStyle(btnSecondary);
        retourBtn.setOnAction(e -> {
            new HomeView().start(stage);
        });

        form.getChildren().addAll(
                undoRedoBox,
                new Separator(),
                nodeTitle, nodeGrid, addNodeBtn, nodeStatus,
                new Separator(),
                edgeTitle, edgeGrid, addEdgeBtn, edgeStatus,
                new Separator(),
                agentTitle, agentGrid, addAgentBtn, agentStatus,
                new Separator(),
                saveLoadTitle, saveLoadButtons, saveLoadStatus,
                new Separator(),
                randomBtn,
                new Separator(),
                launchBtn, retourBtn
        );

        // --- PREVIEW ---
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

        Scene scene = new Scene(mainLayout);
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Z) undo();
            if (e.isControlDown() && e.getCode() == KeyCode.Y) redo();
        });

        preview.drawGraph();
    }

    /** Redraws the graph preview canvas with the current graph state. */
    private void refreshPreview() {
        preview = new GraphView(previewCanvas, graph);
        preview.drawGraph();
    }

    /**
     * Returns the graph configured by the user.
     * @return the current graph
     */
    public Graph getGraph() { return graph; }
}
