package model.node;

/**
 * Represents the accessibility status of a node.
 * @author Yoni
 */
public enum NodeStatus {
    /** Node is accessible and open. */
    OPEN,
    /** Node is blocked (fire door, smoke, debris...). */
    BLOCKED
}
