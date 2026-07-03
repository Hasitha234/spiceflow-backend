package com.spiceflow.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/** Standard error object contained within ApiResponse. */

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuppressWarnings("NullAway.Init")
public class ErrorResponse {
  private String code;
  private String message;
  private List<FieldError> details;

  /** Represents a single field-level validation error */
  @Getter
  @Builder
  public static class FieldError {
    private String field;
    private String message;
  }
}

