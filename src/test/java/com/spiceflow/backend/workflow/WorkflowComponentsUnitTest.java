package com.spiceflow.backend.workflow;

import com.spiceflow.backend.common.exception.BusinessRuleViolationException;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WorkflowComponentsUnitTest {

    enum TestState implements WorkflowState {
        DRAFT,
        SUBMITTED,
        APPROVED
    }

    static class DummyAggregate implements WorkflowAggregate<DummyAggregate, TestState> {
        private TestState state = TestState.DRAFT;

        @Override public String getAggregateId() { return "DUMMY-1"; }
        @Override public TestState getWorkflowState() { return state; }
        @Override public WorkflowTransitionOutput<DummyAggregate> transitionTo(TestState targetState, WorkflowContext context) {
            return new WorkflowTransitionOutput<>(this, java.util.List.of());
        }
    }

    static class DummyValidator implements WorkflowValidator<DummyAggregate, TestState> {
        @Override
        public Set<WorkflowTransition<TestState>> getAllowedTransitions() {
            return Set.of(WorkflowTransition.of(TestState.DRAFT, TestState.SUBMITTED));
        }
    }

    @Test
    @DisplayName("WorkflowTransition record and WorkflowValidator default validateTransition")
    void testTransitionAndValidator() {
        WorkflowTransition<TestState> transition = WorkflowTransition.of(TestState.DRAFT, TestState.SUBMITTED);
        assertThat(transition.fromState()).isEqualTo(TestState.DRAFT);
        assertThat(transition.toState()).isEqualTo(TestState.SUBMITTED);

        DummyValidator validator = new DummyValidator();
        DummyAggregate aggregate = new DummyAggregate();

        // Valid transition should pass without throwing
        validator.validateTransition(aggregate, TestState.SUBMITTED);

        // Invalid transition should throw BusinessRuleViolationException
        assertThatThrownBy(() -> validator.validateTransition(aggregate, TestState.APPROVED))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Invalid workflow transition from 'DRAFT' to 'APPROVED'");
    }
}
