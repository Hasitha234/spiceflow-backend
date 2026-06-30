package com.spiceflow.backend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.info.Contact;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "SpiceFlow API", 
        version = "v1.0.0", 
        description = "SaaS Backend API",
        contact = @Contact(name = "Spice Flow Engineering", email = "engineering@spiceflow.com")
    ),
    // This tells Swagger that ALL endpoints require this security scheme by default
    security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    scheme = "bearer",
    description = "Paste your JWT Access Token here. You do NOT need to type 'Bearer ' before it."
)
public class OpenApiConfig {
    // Empty class - we just need the annotations to configure Springdoc OpenAPI
}
