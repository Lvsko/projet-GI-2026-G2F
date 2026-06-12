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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
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
     * @param g he graph to duplicate
     * @return a deep copy of the graph, or the original graph if copying fails
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
     * Generates n random nodes (2 ≤ n ≤ 100) connected into a spanning tree.
     * Last node is always EXIT. Saves state for undo.
     * @param n the number of nodes to generate
     */
    private void generateRandom(int n) {
        saveState();
        Random rand = new Random();
        List<Node> newNodes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String id  = "RN" + (graph.getNodes().size() + i);
            double x   = 50 + rand.nextDouble() * 500;
            double y   = 50 + rand.nextDouble() * 400;
            NodeType type;
            if      (i == n - 1)  type = NodeType.EXIT;
            else if (i % 2 == 0)  type = NodeType.ROOM;
            else                  type = NodeType.CORRIDOR;
            Node node = new Node(id, id, x, y, 10, NodeStatus.OPEN, type, 1.0f);
            newNodes.add(node);
            graph.addNode(node);
        }
        // Arbre couvrant aléatoire → connexité garantie
        for (int i = 1; i < newNodes.size(); i++) {
            String edgeId = "RE" + (graph.getEdges().size());
            Node target   = newNodes.get(i);
            Node source   = newNodes.get(rand.nextInt(i));
            graph.addEdge(new Edge(edgeId, source, target, 5, 1.0f, 1.0f, false));
        }
        refreshPreview();
    }

    /**
     * Saves the current state of the graph into the undo stack.
     */
    private void saveState() {
        undoStack.push(deepCopy(graph));
        redoStack.clear();
    }

    /**
     * Restores the previous state of the graph from the undo stack.
     */
    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(deepCopy(graph));
            graph = undoStack.pop();
            refreshPreview();
        }
    }

    /**
     * Restores the next state of the graph from the redo stack.
     */
    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(deepCopy(graph));
            graph = redoStack.pop();
            refreshPreview();
        }
    }

    /**
     * Applies a dark theme style to a JavaFX ComboBox.
     * @param combo the ComboBox to style
     * @param <T> the type of items contained in the ComboBox
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
     * Initializes and displays the JavaFX configuration window for building a graph
     * and setting up a simulation.
     * @param stage the primary JavaFX stage used to display the configuration window
     */
    public void start(Stage stage) {
        stage.setTitle("Configure Graph");

        String labelStyle    = "-fx-text-fill: #e0e0e0; -fx-font-family: Arial;";
        String titleStyle    = "-fx-text-fill: #2E7D32; -fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 14;";
        String fieldStyle    = "-fx-background-color: #303030; -fx-text-fill: #e0e0e0; -fx-border-color: #616161; -fx-border-radius: 4;";
        String btnPrimary    = "-fx-background-color: #2E7D32; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-padding: 6 14 6 14;";
        String btnSecondary  = "-fx-background-color: #424242; -fx-text-fill: #e0e0e0; -fx-background-radius: 6; -fx-cursor: hand; -fx-border-color: #616161; -fx-border-radius: 6; -fx-padding: 6 14 6 14;";

        VBox form = new VBox(15);
        form.setPadding(new Insets(15));
        form.setPrefWidth(420);
        form.setStyle("-fx-background-color: #424242;");

        // --- UNDO / REDO ---
        Button undoBtn = new Button("Undo"); undoBtn.setStyle(btnSecondary);
        Button redoBtn = new Button("Redo"); redoBtn.setStyle(btnSecondary);
        undoBtn.setOnAction(e -> undo());
        redoBtn.setOnAction(e -> redo());
        HBox undoRedoBox = new HBox(10, undoBtn, redoBtn);

        // --- ADD NODE ---
        Label nodeTitle = new Label("Add Node");
        nodeTitle.setStyle(titleStyle);
        GridPane nodeGrid = new GridPane();
        nodeGrid.setHgap(10); nodeGrid.setVgap(8);

        TextField nodeId       = new TextField(); nodeId.setPromptText("ID (e.g. N1)");
        TextField nodeName     = new TextField(); nodeName.setPromptText("Name (e.g. Room A)");
        TextField nodeCapacity = new TextField(); nodeCapacity.setPromptText("Capacity (e.g. 10)");
        TextField nodeX        = new TextField(); nodeX.setPromptText("X position");
        TextField nodeY        = new TextField(); nodeY.setPromptText("Y position");
        ComboBox<NodeType> nodeType = new ComboBox<>();
        nodeType.getItems().addAll(NodeType.values());
        nodeType.setValue(NodeType.ROOM);

        nodeId.setStyle(fieldStyle); nodeName.setStyle(fieldStyle);
        nodeCapacity.setStyle(fieldStyle); nodeX.setStyle(fieldStyle); nodeY.setStyle(fieldStyle);
        darkCombo(nodeType);

        Label lId1  = new Label("ID:");       lId1.setStyle(labelStyle);
        Label lName = new Label("Name:");     lName.setStyle(labelStyle);
        Label lType = new Label("Type:");     lType.setStyle(labelStyle);
        Label lCap  = new Label("Capacity:"); lCap.setStyle(labelStyle);
        Label lX    = new Label("X:");        lX.setStyle(labelStyle);
        Label lY    = new Label("Y:");        lY.setStyle(labelStyle);

        nodeGrid.add(lId1,  0, 0); nodeGrid.add(nodeId,       1, 0);
        nodeGrid.add(lName, 0, 1); nodeGrid.add(nodeName,     1, 1);
        nodeGrid.add(lType, 0, 2); nodeGrid.add(nodeType,     1, 2);
        nodeGrid.add(lCap,  0, 3); nodeGrid.add(nodeCapacity, 1, 3);
        nodeGrid.add(lX,    0, 4); nodeGrid.add(nodeX,        1, 4);
        nodeGrid.add(lY,    0, 5); nodeGrid.add(nodeY,        1, 5);

        Label nodeStatus = new Label(); nodeStatus.setStyle(labelStyle);
        Button addNodeBtn = new Button("Add Node"); addNodeBtn.setStyle(btnPrimary);
        addNodeBtn.setOnAction(e -> {
            try {
                String id     = nodeId.getText().trim();
                String name   = nodeName.getText().trim();
                int capacity  = Integer.parseInt(nodeCapacity.getText().trim());
                double x      = Double.parseDouble(nodeX.getText().trim());
                double y      = Double.parseDouble(nodeY.getText().trim());
                NodeType type = nodeType.getValue();

                if (id.isEmpty() || name.isEmpty()) { nodeStatus.setText("Erreur : ID et nom requis.");           return; }
                if (graph.getNode(id) != null)       { nodeStatus.setText("Erreur : ID déjà utilisé.");           return; }
                if (capacity <= 0)                   { nodeStatus.setText("Erreur : capacité doit être > 0.");    return; } // #11

                saveState();
                graph.addNode(new Node(id, name, x, y, capacity, NodeStatus.OPEN, type, 1.0f));
                nodeStatus.setText("Nœud '" + name + "' ajouté.");
                nodeId.clear(); nodeName.clear(); nodeCapacity.clear(); nodeX.clear(); nodeY.clear();
                refreshPreview();
            } catch (NumberFormatException ex) {
                nodeStatus.setText("Erreur : capacité, X et Y doivent être des nombres.");
            }
        });

        // --- ADD EDGE ---
        Label edgeTitle = new Label("Add Edge");
        edgeTitle.setStyle(titleStyle);
        GridPane edgeGrid = new GridPane();
        edgeGrid.setHgap(10); edgeGrid.setVgap(8);

        TextField edgeId       = new TextField(); edgeId.setPromptText("ID (e.g. E1)");
        TextField edgeSource   = new TextField(); edgeSource.setPromptText("Source node ID");
        TextField edgeTarget   = new TextField(); edgeTarget.setPromptText("Target node ID");
        TextField edgeWidth    = new TextField(); edgeWidth.setPromptText("Width (e.g. 5)");
        TextField edgeDistance = new TextField(); edgeDistance.setPromptText("Distance (e.g. 1.0)");
        CheckBox  edgeDirected = new CheckBox("Directed");

        edgeId.setStyle(fieldStyle); edgeSource.setStyle(fieldStyle); edgeTarget.setStyle(fieldStyle);
        edgeWidth.setStyle(fieldStyle); edgeDistance.setStyle(fieldStyle);
        edgeDirected.setStyle(labelStyle);

        Label lId2   = new Label("ID:");       lId2.setStyle(labelStyle);
        Label lSrc   = new Label("Source:");   lSrc.setStyle(labelStyle);
        Label lTgt   = new Label("Target:");   lTgt.setStyle(labelStyle);
        Label lW     = new Label("Width:");    lW.setStyle(labelStyle);
        Label lDist  = new Label("Distance:"); lDist.setStyle(labelStyle);

        edgeGrid.add(lId2,  0, 0); edgeGrid.add(edgeId,       1, 0);
        edgeGrid.add(lSrc,  0, 1); edgeGrid.add(edgeSource,   1, 1);
        edgeGrid.add(lTgt,  0, 2); edgeGrid.add(edgeTarget,   1, 2);
        edgeGrid.add(lW,    0, 3); edgeGrid.add(edgeWidth,    1, 3);
        edgeGrid.add(lDist, 0, 4); edgeGrid.add(edgeDistance, 1, 4);
        edgeGrid.add(edgeDirected, 1, 5);

        Label edgeStatus = new Label(); edgeStatus.setStyle(labelStyle);
        Button addEdgeBtn = new Button("Add Edge"); addEdgeBtn.setStyle(btnPrimary);
        addEdgeBtn.setOnAction(e -> {
            try {
                String id       = edgeId.getText().trim();
                String sourceId = edgeSource.getText().trim();
                String targetId = edgeTarget.getText().trim();
                int    width    = Integer.parseInt(edgeWidth.getText().trim());
                float  distance = Float.parseFloat(edgeDistance.getText().trim());
                boolean directed = edgeDirected.isSelected();

                Node source = graph.getNode(sourceId);
                Node target = graph.getNode(targetId);

                if (source == null || target == null) { edgeStatus.setText("Erreur : nœud source ou cible introuvable."); return; }
                if (source.equals(target))             { edgeStatus.setText("Erreur : source et cible doivent être différents."); return; } // #9
                if (width <= 0)                        { edgeStatus.setText("Erreur : largeur doit être > 0.");    return; }               // #6
                if (distance <= 0)                     { edgeStatus.setText("Erreur : distance doit être > 0.");   return; }               // #8

                saveState();
                graph.addEdge(new Edge(id, source, target, width, distance, 1.0f, directed));
                edgeStatus.setText("Arête '" + id + "' ajoutée.");
                edgeId.clear(); edgeSource.clear(); edgeTarget.clear(); edgeWidth.clear(); edgeDistance.clear();
                refreshPreview();
            } catch (NumberFormatException ex) {
                edgeStatus.setText("Erreur : largeur et distance doivent être des nombres.");
            }
        });

        // --- ADD AGENTS ---
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

        ComboBox<AgentState>    agentState    = new ComboBox<>();
        ComboBox<AgentType>     agentType     = new ComboBox<>();
        ComboBox<AgentBehavior> agentBehavior = new ComboBox<>();
        agentState.getItems().addAll(AgentState.values());       agentState.setValue(AgentState.CALM);
        agentType.getItems().addAll(AgentType.values());         agentType.setValue(AgentType.ADULT);
        agentBehavior.getItems().addAll(AgentBehavior.values()); agentBehavior.setValue(AgentBehavior.COOPERATIVE);
        darkCombo(agentState); darkCombo(agentType); darkCombo(agentBehavior);

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
                int  count     = Integer.parseInt(agentCount.getText().trim());
                if (startNode == null || count <= 0) {
                    agentStatus.setText("Erreur : sélectionnez un nœud et entrez un nombre valide.");
                    return;
                }
                for (int i = 0; i < count; i++) {
                    agents.add(new Agent(
                        "agent_" + agents.size(), startNode, 1.0f,
                        agentState.getValue(), agentBehavior.getValue(), agentType.getValue(),
                        0.5f, graph
                    ));
                }
                agentStatus.setText(count + " agent(s) ajouté(s) sur '" + startNode.getId() + "'.");
                agentCount.clear();
            } catch (NumberFormatException ex) {
                agentStatus.setText("Erreur : le nombre doit être un entier.");
            }
        });

        // --- SAVE / LOAD ---
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
                    saveLoadStatus.setText("Plan sauvegardé.");
                } catch (Exception ex) {
                    saveLoadStatus.setText("Erreur sauvegarde : " + ex.getMessage());
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
                    saveLoadStatus.setText("Plan chargé.");
                    refreshPreview();
                } catch (Exception ex) {
                    saveLoadStatus.setText("Erreur chargement : " + ex.getMessage());
                }
            }
        });
        HBox saveLoadButtons = new HBox(10, saveBtn, loadBtn);

        // --- RANDOM GENERATION (#12 : cap 2–100) ---
        Button randomBtn = new Button("Random Generation"); randomBtn.setStyle(btnSecondary);
        randomBtn.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("5");
            dialog.setTitle("Génération aléatoire");
            dialog.setHeaderText("Générer des nœuds et arêtes aléatoires");
            dialog.setContentText("Nombre de nœuds (2–100) :");
            Optional<String> result = dialog.showAndWait();
            result.ifPresent(val -> {
                try {
                    int n = Integer.parseInt(val.trim());
                    if (n < 2) {
                        new Alert(Alert.AlertType.WARNING, "Minimum 2 nœuds requis.").showAndWait();
                        return;
                    }
                    if (n > 100) {
                        new Alert(Alert.AlertType.WARNING, "Maximum 100 nœuds (limite performances).").showAndWait();
                        return;
                    }
                    generateRandom(n);
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Entrez un nombre entier valide.").showAndWait();
                }
            });
        });

        // --- LAUNCH (#1 : validation EXIT, #3 : validation agents) ---
        Button launchBtn = new Button("▶ Lancer la simulation");
        launchBtn.setStyle(btnPrimary + "-fx-font-family: Georgia; -fx-font-weight: bold; -fx-font-size: 13;");
        launchBtn.setOnAction(e -> {
            // Vérification nœud EXIT (#1)
            boolean hasExit = graph.getNodes().stream()
                .anyMatch(n -> n.getType() == NodeType.EXIT);
            if (!hasExit) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Aucun nœud EXIT");
                alert.setContentText("Ajoutez au moins un nœud de type EXIT avant de lancer la simulation.");
                alert.showAndWait();
                return;
            }
            // Vérification agents (#3)
            if (agents.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText("Aucun agent");
                alert.setContentText("Ajoutez au moins un agent avant de lancer la simulation.");
                alert.showAndWait();
                return;
            }
            stage.close();
            new MainView(graph, agents, "config").start(new Stage());
        });

        // --- RETOUR ---
        Button retourBtn = new Button("← Retour"); retourBtn.setStyle(btnSecondary);
        retourBtn.setOnAction(e -> {
            stage.close();
            new HomeView().start(new Stage());
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
        preview       = new GraphView(previewCanvas, graph);

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

        // Ctrl+Z / Ctrl+Y — ignorés si un TextField a le focus (#Ctrl+Z bug)
        scene.setOnKeyPressed(e -> {
            if (scene.getFocusOwner() instanceof TextField) return;
            if (e.isControlDown() && e.getCode() == KeyCode.Z) undo();
            if (e.isControlDown() && e.getCode() == KeyCode.Y) redo();
        });

        preview.drawGraph();
    }

    /**
     * Refreshes the graphical preview of the graph.
     */
    private void refreshPreview() {
        preview = new GraphView(previewCanvas, graph);
        preview.drawGraph();
    }

    public Graph getGraph() { return graph; }
    
}
