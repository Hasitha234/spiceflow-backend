package com.spiceflow.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;
import com.spiceflow.backend.common.filter.CorrelationIdFilter;

/**
 * Global API Response Envelope.
 * Guarantees a consistent JSON shape: {status, data, error, traceId, timestamp}
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("NullAway.Init")
public class ApiResponse<T> {

    private String status;
    private T data;
    private Object error;
    private String traceId;
    
    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .status("success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(Object error) {
        return ApiResponse.<T>builder()
                .status("error")
                .error(error)
                .traceId(MDC.get(CorrelationIdFilter.CORRELATION_ID_LOG_VAR_NAME))
                .build();
    }
}

