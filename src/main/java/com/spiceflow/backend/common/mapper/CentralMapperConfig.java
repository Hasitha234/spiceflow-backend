package com.spiceflow.backend.common.mapper;

import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

/**
 * Centralized MapStruct configuration used by all mappers in the application.
 * 
 * Enforces Spring component model for dependency injection and raises a compile-time
 * error if any target properties are unmapped.
 */
@MapperConfig(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface CentralMapperConfig {
}
