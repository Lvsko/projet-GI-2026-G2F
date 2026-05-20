package exit.model.enums;

/**
 * Represents the movement behavior of an agent towards others.
 */
public enum AgentBehavior {
    /** Agent lets others pass first. */
    COOPERATIVE,
    /** Agent always takes priority over others. */
    PRIORITY,
    /** Agent follows the nearest agent going in the same direction. */
    FOLLOWER
}
