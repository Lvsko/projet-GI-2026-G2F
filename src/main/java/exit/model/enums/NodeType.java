package exit.model.enums;

/**
 * Represents the type of a node in the building graph.
 * @author Yoni
 */
public enum NodeType {
    /** A standard room or office. */
    ROOM,
    /** A corridor connecting rooms. */
    CORRIDOR,
    /** An emergency exit point. */
    EXIT,
    /** A staircase between floors. */
    STAIRCASE
}
