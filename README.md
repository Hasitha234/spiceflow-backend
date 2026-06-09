# SpiceFlow — Backend

Spring Boot REST API for SpiceFlow, a business management system for spice sellers.

## Tech Stack

- Java 21
- Spring Boot 3.x
- PostgreSQL 16
- Flyway (DB migrations)
- Spring Security + JWT
- JUnit 5 + Testcontainers

## Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16 (or Docker)

## Getting Started

```bash
# Clone the repo
git clone git@github.com:YOUR_USERNAME/spiceflow-backend.git

# Run PostgreSQL via Docker
docker run --name spiceflow-db \
  -e POSTGRES_DB=spiceflow \
  -e POSTGRES_USER=spiceflow_user \
  -e POSTGRES_PASSWORD=spiceflow_pass \
  -p 5432:5432 -d postgres:16

# Run the app
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

API docs available at: http://localhost:8080/swagger-ui.html


