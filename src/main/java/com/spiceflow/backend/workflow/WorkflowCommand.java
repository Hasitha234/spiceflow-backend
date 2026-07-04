package com.spiceflow.backend.workflow;

import org.jspecify.annotations.Nullable;

/**
 * Represents an immutable intention to transition an aggregate in an operational workflow.
 * Commands define intent and prerequisites, while behavior and state mutation reside in WorkflowEngine.
 *
 * @param <T> The aggregate type being transitioned
 * @param <S> The workflow state type
 */
public interface WorkflowCommand<T, S extends WorkflowState> {
    /**
     * Unique name of the command (e.g., "SubmitPurchaseOrder").
     */
    String getCommandName();

    /**
     * The target workflow state this command intends to reach.
     */
    S getTargetState();

    /**
     * Optional user comment or note accompanying the command.
     */
    @Nullable
    String getComment();

    /**
     * Validates whether this command can be executed against the given aggregate in the current context.
     * Throws BusinessRuleViolationException or IllegalStateException if prerequisites are not met.
     *
     * @param aggregate The target aggregate
     * @param context   The execution context
     */
    void validate(T aggregate, WorkflowContext context);

    /**
     * Non-throwing check if the command can be executed.
     *
     * @param aggregate The target aggregate
     * @param context   The execution context
     * @return true if prerequisites are satisfied
     */
    default boolean canExecute(T aggregate, WorkflowContext context) {
        try {
            validate(aggregate, context);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
