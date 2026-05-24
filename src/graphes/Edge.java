public class Edge {

    Node from;
    Node to;
    
    double width;
    boolean directed;

    public Edge(Node from, Node to, double width, boolean directed) {
        this.from = from;
        this.to = to;
        
        this.width = width;
        this.directed = directed;
    }
}