package exit.model.enums;
/**
 * Represents the status of a node in the graph.
 */
public enum NodeStatus {
    /** Node is accessible. */
    OPEN, 
    /** Node is blocked (fire door, smoke...). */
    BLOCKED
}
