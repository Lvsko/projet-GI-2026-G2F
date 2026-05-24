import exit.model.Agent;
import exit.model.Graph;
import exit.model.Node;
import exit.model.Edge;
import exit.model.enums.NodeStatus;
import exit.model.enums.NodeType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/**
 * Interactive command-line menu for the EXIT simulation.
 * Handles user input and manages the simulation state.
 * @author Ruben
 */
public class Menu {

    private Graph graph;
    private List<Agent> agents;
    private Scanner scanner;

    /**
     * Creates a new Menu and initializes all components.
     */
    public Menu() {
        this.graph = new Graph();
        this.agents = new ArrayList<Agent>();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Starts the main menu loop.
     * Displays options and processes user input until the user chooses to exit.
     */
    public void start() {
        int choice = 0;
        while (choice != 6) {
            displayMenu();
            choice = scanner.nextInt();
            switch (choice) {
                case 1: createTestGraph(); break;
                case 2: displayGraph(); break;
                case 3: placeAgent(); break;
                case 4: nextTick(); break;
                case 5: displayStats(); break;
                case 6: System.out.println("Goodbye !"); break;
                default: System.out.println("Invalid choice"); break;
            }
        }
    }

    /**
     * Displays the main menu options.
     */
    private void displayMenu() {
        System.out.println("=== EXIT Simulation ===");
        System.out.println("1 : Create test graph");
        System.out.println("2 : Display graph");
        System.out.println("3 : Place an agent");
        System.out.println("4 : Go to next tick");
        System.out.println("5 : Display stats");
        System.out.println("6 : Exit menu");
    }

    /**
     * Creates a hardcoded test graph representing a simple building layout.
     * Contains 5 rooms and 1 exit connected by corridors.
     */
    private void createTestGraph() {
        this.graph = new Graph();
        Node r1 = new Node("r1", "Room 1", 10, 20, 10, NodeStatus.OPEN, NodeType.ROOM, 0.5f);
        this.graph.addNode(r1);
        Node r2 = new Node("r2", "Room 2", 10, 30, 10, NodeStatus.OPEN, NodeType.ROOM, 0.5f);
        this.graph.addNode(r2);
        Node r3 = new Node("r3", "Room 3", 20, 30, 10, NodeStatus.OPEN, NodeType.ROOM, 0.5f);
        this.graph.addNode(r3);
        Node r4 = new Node("r4", "Room 4", 20, 15, 10, NodeStatus.OPEN, NodeType.ROOM, 0.5f);
        this.graph.addNode(r4);
        Node r5 = new Node("r5", "Room 5", 30, 30, 10, NodeStatus.OPEN, NodeType.ROOM, 0.5f);
        this.graph.addNode(r5);
        Node ex1 = new Node("ex1", "Exit 1", 30, 40, 10, NodeStatus.OPEN, NodeType.EXIT, 0.5f);
        this.graph.addNode(ex1);

        this.graph.addEdge(new Edge("e1", r1, r2, 5, 1.0f, 1.0f, false));
        this.graph.addEdge(new Edge("e2", r1, r3, 5, 1.0f, 1.0f, false));
        this.graph.addEdge(new Edge("e3", r3, r4, 5, 1.0f, 1.0f, false));
        this.graph.addEdge(new Edge("e4", r4, r5, 5, 1.0f, 1.0f, false));
        this.graph.addEdge(new Edge("e5", r2, r3, 5, 1.0f, 1.0f, false));
        this.graph.addEdge(new Edge("e6", r4, ex1, 2, 1.0f, 1.0f, false));
    }

    /**
     * Displays all nodes and edges of the current graph.
     */
    private void displayGraph() {
        System.out.println("-----Nodes-----");
        for (Node n : this.graph.getNodes()) {
            System.out.println(n);
        }
        System.out.println("-----Edges-----");
        for (Edge e : this.graph.getEdges()) {
            System.out.println(e);
        }
    }

    /**
     * Places an agent in the graph based on user input.
     * The agent's destination is automatically set to the exit node.
     */
    private void placeAgent() {
        System.out.println("-----Nodes-----");
        for (Node n : this.graph.getNodes()) {
            System.out.println(n);
        }
        System.out.println("Select the starting node's ID");
        String id = scanner.next();

        Node startNode = this.graph.getNode(id);
        if (startNode == null) {
            System.out.println("Node not found.");
            return;
        }
        Node destination = this.graph.getNode("ex1");
        if (destination == null) {
            System.out.println("No exit found in the graph.");
            return;
        }
        Agent agent = new Agent(startNode, destination);
        this.agents.add(agent);
        System.out.println("Agent " + agent.getId() + " placed in " + startNode.getName());
    }

    /**
     * Advances the simulation by one tick.
     * Not yet available, waiting for SimulationEngine implementation.
     */
    private void nextTick() {
        System.out.println("Simulation engine not available yet.");
    }

    /**
     * Displays evacuation statistics.
     * Not yet available, waiting for Statistics implementation.
     */
    private void displayStats() {
        System.out.println("Stats not available yet.");
    }
}