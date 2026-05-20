package exit.model.enums;
/**
 * Represents the psychological state of an agent during evacuation.
 */
public enum AgentState {
    /** Agent is calm and follows optimal path. */
    CALM,
    /** Agent is panicked and takes the nearest exit. */
    PANICKED,
    /** Agent is injured and moves very slowly. */
    INJURED
}
