package com.spiceflow.backend.workflow;

import com.spiceflow.backend.audit.AuditEntry;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowEngineTest {

    private WorkflowEngine workflowEngine;
    private WorkflowContext context;

    @BeforeEach
    void setUp() {
        workflowEngine = new WorkflowEngine();
        context = WorkflowContext.of(100L, 1L, "PO-2026-0001");
    }

    enum TestState implements WorkflowState {
        DRAFT,
        SUBMITTED,
        APPROVED
    }

    record TestEvent(
            String aggregateId,
            DomainEventType eventType,
            String correlationId,
            Instant timestamp,
            Long tenantId
    ) implements DomainEvent {
        @Override public String getAggregateId() { return aggregateId; }
        @Override public DomainEventType getEventType() { return eventType; }
        @Override public String getCorrelationId() { return correlationId; }
        @Override public Instant getTimestamp() { return timestamp; }
        @Override public Long getTenantId() { return tenantId; }
    }

    static class TestAggregate implements WorkflowAggregate<TestState> {
        private String id = "TEST-AGG-1";
        private TestState state = TestState.DRAFT;

        @Override
        public String getAggregateId() { return id; }

        @Override
        public TestState getWorkflowState() { return state; }

        @Override
        public List<DomainEvent> transitionTo(TestState targetState, WorkflowContext context) {
            this.state = targetState;
            List<DomainEvent> events = new ArrayList<>();
            events.add(new TestEvent(id, DomainEventType.PURCHASE_ORDER_SUBMITTED, context.correlationId(), context.timestamp(), context.tenantId()));
            return events;
        }
    }

    record SubmitCommand(String comment) implements WorkflowCommand<TestAggregate, TestState> {
        @Override public String getCommandName() { return "SubmitTestCommand"; }
        @Override public TestState getTargetState() { return TestState.SUBMITTED; }
        @Override public String getComment() { return comment; }

        @Override
        public void validate(TestAggregate aggregate, WorkflowContext context) {
            if (aggregate.getWorkflowState() != TestState.DRAFT) {
                throw new BusinessRuleViolationException("Aggregate must be in DRAFT state to submit");
            }
        }
    }

    @Test
    @DisplayName("execute() performs stateless transition and returns immutable result without side effects inside aggregate")
    void execute_successfulTransition_returnsResultWithEventsAndAudit() {
        TestAggregate aggregate = new TestAggregate();
        SubmitCommand command = new SubmitCommand("Submitting for review");

        WorkflowResult<TestAggregate> result = workflowEngine.execute(command, aggregate, context);

        assertThat(result.updatedAggregate().getWorkflowState()).isEqualTo(TestState.SUBMITTED);
        assertThat(result.events()).hasSize(1);
        assertThat(result.events().get(0).getEventType()).isEqualTo(DomainEventType.PURCHASE_ORDER_SUBMITTED);
        assertThat(result.events().get(0).getCorrelationId()).isEqualTo("PO-2026-0001");

        AuditEntry auditEntry = result.auditEntry();
        assertThat(auditEntry.getTenantId()).isEqualTo(1L);
        assertThat(auditEntry.getUserId()).isEqualTo(100L);
        assertThat(auditEntry.getCorrelationId()).isEqualTo("PO-2026-0001");
        assertThat(auditEntry.getCommandName()).isEqualTo("SubmitTestCommand");
        assertThat(auditEntry.getFromState()).isEqualTo("DRAFT");
        assertThat(auditEntry.getToState()).isEqualTo("SUBMITTED");
        assertThat(auditEntry.getComment()).isEqualTo("Submitting for review");
    }

    @Test
    @DisplayName("execute() throws exception when self-validating command fails prerequisites")
    void execute_invalidPrerequisites_throwsBusinessRuleViolationException() {
        TestAggregate aggregate = new TestAggregate();
        aggregate.state = TestState.APPROVED; // already approved
        SubmitCommand command = new SubmitCommand("Try submitting again");

        assertThatThrownBy(() -> workflowEngine.execute(command, aggregate, context))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("must be in DRAFT state");
    }
}
