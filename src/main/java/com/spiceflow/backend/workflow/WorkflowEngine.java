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
     *
     * @param command   The intention and prerequisites
     * @param aggregate The target aggregate to transition
     * @param context   The execution context (user, tenant, correlationId, timestamp)
     * @param <T>       The aggregate type
     * @param <S>       The workflow state type
     * @return Immutable WorkflowResult containing updated aggregate, domain events, and audit entry
     */
    public <T extends WorkflowAggregate<S>, S extends WorkflowState> WorkflowResult<T> execute(
            WorkflowCommand<T, S> command,
            T aggregate,
            WorkflowContext context) {

        // 1. Validate intention and prerequisites against aggregate and context
        command.validate(aggregate, context);

        // 2. Capture initial state
        S fromState = aggregate.getWorkflowState();
        S toState = command.getTargetState();

        // 3. Perform transition on aggregate (generates domain events)
        List<DomainEvent> events = aggregate.transitionTo(toState, context);

        // 4. Create immutable audit timeline projection entry
        AuditEntry auditEntry = AuditEntry.builder()
                .tenantId(context.tenantId())
                .userId(context.userId())
                .correlationId(context.correlationId())
                .commandName(command.getCommandName())
                .fromState(fromState.name())
                .toState(toState.name())
                .comment(command.getComment())
                .timestamp(context.timestamp())
                .build();

        // 5. Return immutable result (events are emitted outside aggregate by service/engine caller)
        return new WorkflowResult<>(aggregate, events, auditEntry);
    }
}
