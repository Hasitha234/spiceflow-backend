package com.spiceflow.backend.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * Immutable append-only audit timeline projection entry for operational workflows.
 * Treated as an execution projection, not primary storage logic influencing business decisions.
 */
@Entity
@Table(name = "audit_entries")
public class AuditEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Nullable Long id;

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    @Column(name = "correlation_id", nullable = false, updatable = false, length = 100)
    private String correlationId;

    @Column(name = "command_name", nullable = false, updatable = false, length = 100)
    private String commandName;

    @Column(name = "from_state", nullable = false, updatable = false, length = 50)
    private String fromState;

    @Column(name = "to_state", nullable = false, updatable = false, length = 50)
    private String toState;

    @Column(name = "comment", updatable = false, length = 1000)
    private @Nullable String comment;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private Instant timestamp;

    protected AuditEntry() {
        // JPA default constructor
        this.tenantId = 0L;
        this.userId = 0L;
        this.correlationId = "";
        this.commandName = "";
        this.fromState = "";
        this.toState = "";
        this.timestamp = Instant.EPOCH;
    }

    private AuditEntry(Builder builder) {
        this.tenantId = Objects.requireNonNull(builder.tenantId, "tenantId must not be null");
        this.userId = Objects.requireNonNull(builder.userId, "userId must not be null");
        this.correlationId = Objects.requireNonNull(builder.correlationId, "correlationId must not be null");
        this.commandName = Objects.requireNonNull(builder.commandName, "commandName must not be null");
        this.fromState = Objects.requireNonNull(builder.fromState, "fromState must not be null");
        this.toState = Objects.requireNonNull(builder.toState, "toState must not be null");
        this.comment = builder.comment;
        this.timestamp = Objects.requireNonNull(builder.timestamp, "timestamp must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public @Nullable Long getId() { return id; }
    public Long getTenantId() { return tenantId; }
    public Long getUserId() { return userId; }
    public String getCorrelationId() { return correlationId; }
    public String getCommandName() { return commandName; }
    public String getFromState() { return fromState; }
    public String getToState() { return toState; }
    public @Nullable String getComment() { return comment; }
    public Instant getTimestamp() { return timestamp; }

    public static class Builder {
        private @Nullable Long tenantId;
        private @Nullable Long userId;
        private @Nullable String correlationId;
        private @Nullable String commandName;
        private @Nullable String fromState;
        private @Nullable String toState;
        private @Nullable String comment;
        private @Nullable Instant timestamp;

        public Builder tenantId(Long tenantId) { this.tenantId = tenantId; return this; }
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder correlationId(String correlationId) { this.correlationId = correlationId; return this; }
        public Builder commandName(String commandName) { this.commandName = commandName; return this; }
        public Builder fromState(String fromState) { this.fromState = fromState; return this; }
        public Builder toState(String toState) { this.toState = toState; return this; }
        public Builder comment(@Nullable String comment) { this.comment = comment; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }

        public AuditEntry build() {
            return new AuditEntry(this);
        }
    }
}
