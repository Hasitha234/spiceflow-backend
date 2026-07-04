package com.spiceflow.backend.workflow;

import com.spiceflow.backend.audit.AuditEntry;
import com.spiceflow.backend.events.DomainEvent;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Stateless execution engine for operational workflow state transitions.
 * Pure execution logic without stateful orchestration, session holding, or transaction boundary entanglement.
 */
@Component
public class WorkflowEngine {

    /**
     * Executes a workflow command against a target aggregate within the provided context.
     * Enforces Rule 15 copy-on-write immutability: returns a new aggregate instance without mutating input.
     *
     * @param command   The intention and prerequisites
     * @param aggregate The target aggregate to transition
     * @param context   The execution context (user, tenant, correlationId, timestamp)
     * @param <T>       The aggregate type
     * @param <S>       The workflow state type
     * @return Immutable WorkflowResult containing updated aggregate, domain events, and audit entry
     */
    public <T extends WorkflowAggregate<T, S>, S extends WorkflowState> WorkflowResult<T> execute(
            WorkflowCommand<T, S> command,
            T aggregate,
            WorkflowContext context) {

        // 1. Validate intention and prerequisites against aggregate and context
        command.validate(aggregate, context);

        // 2. Capture initial state
        S fromState = aggregate.getWorkflowState();
        S toState = command.getTargetState();

        // 3. Perform transition on aggregate (returns copy-on-write updated aggregate and generated domain events)
        WorkflowTransitionOutput<T> output = aggregate.transitionTo(toState, context);
        T updatedAggregate = output.updatedAggregate();
        List<DomainEvent> events = output.events();

        // 4. Create immutable audit timeline projection entry
        AuditEntry auditEntry = AuditEntry.builder()
                .tenantId(context.tenantId())
                .userId(context.userId())
                .correlationId(context.correlationId())
                .aggregateId(aggregate.getAggregateId())
                .commandName(command.getCommandName())
                .fromState(fromState.name())
                .toState(toState.name())
                .comment(command.getComment())
                .timestamp(context.timestamp())
                .build();

        // 5. Return immutable result (events are emitted outside aggregate by service/engine caller)
        return new WorkflowResult<>(updatedAggregate, events, auditEntry);
    }
}
