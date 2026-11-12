# EPMC: Java Implementation (Spring Boot 3)

[![Status](https://img.shields.io/badge/status-production--ready-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-32%2F32%20passed-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-~75%25-blue)]()
[![Java](https://img.shields.io/badge/java-21--lts-blue)]()
[![Spring Boot](https://img.shields.io/badge/spring%20boot-3.4-green)]()

**EPMC** (Enterprise Portfolio Management & Correlation) — Java Spring Boot 3 implementation.

For shared documentation, architecture, and API details, see [../README.md](../README.md).

---

## Quick Start

### Run Locally

\\\ash
cd EPMC-java

# Build and run
mvn clean package
java -jar target/epmc-java-1.0.0.jar
or
mvn spring-boot:run

# Service at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
\\\

### Run with IDE

Open in IntelliJ IDEA, Eclipse, or VS Code:
- Find `EpmcApplication.java` in `src/main/java/com/example/epmc/`
- Right-click → Run
- Service at http://localhost:8080

### Run with Docker

\\\ash
docker-compose up
# Service at http://localhost:8080
\\\

---

## Documentation

- **[Setup Guide](./docs/SETUP.md)** — Java 21 prerequisites, Maven, Docker
- **[API Reference](../README.md#api-documentation)** — Endpoint specifications
- **[Architecture](../README.md#architecture)** — Request flow, domain model, concurrency
- **[GLOSSARY](../GLOSSARY.md)** — Domain terminology

---

## Testing

\\\ash
# All tests
mvn test

# Specific test class
mvn test -Dtest=GrantValidatorTest

# With coverage report
mvn clean test jacoco:report
# View: target/site/jacoco/index.html
\\\

**Test Results**: 32/32 Passed (25 unit + 7 integration), ~75% coverage

---

## Project Structure

\\\
EPMC-java/
├── docs/
│   └── SETUP.md                     # Setup guide for this project
├── src/main/java/com/example/epmc/
│   ├── config/
│   ├── controllers/                  # HTTP endpoints
│   ├── exceptions/
│   ├── models/                       # Domain models
│   ├── services/                     # Business logic and API clients
│   └── services/interfaces/
├── src/main/resources/
│   ├── application.yml
│   └── logback-spring.xml
├── src/test/java/com/example/epmc/
│   ├── GrantValidatorTest.java
│   └── PortfolioServiceIntegrationTest.java
├── pom.xml                           # Maven configuration
├── Dockerfile                        # Container build
├── docker-compose.yml                # Local development
└── README.md                         # This file
\\\

---

## Configuration

**Key Settings** (from \pplication.yml\):

\\\yaml
portfolio:
  max-grants: 10
  max-publications-per-grant: 5
  max-concurrent-lookups: 10
  per-call-timeout-seconds: 5
  overall-timeout-seconds: 30
\\\

**Override with Environment Variables**:

\\\ash
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20
java -jar target/epmc-java-1.0.0.jar
\\\

See [Setup Guide](./docs/SETUP.md#configuration) for full configuration details.

---

## Test Coverage

| Component | Tests | Coverage |
|---|---|---|
| GrantValidator | 25 unit tests | ~85% |
| PortfolioService | 7 integration tests | ~70% |
| **Total** | **32 tests** | **~75%** |

**All tests passing**

---

## Related Documentation

- [Project Overview](../README.md) — Complete EPMC documentation (shared)
- [Domain Glossary](../GLOSSARY.md) — Domain terminology
- [C# Implementation](../EPMC-csharp/) — C#/ASP.NET Core version

---

## Troubleshooting

See [Setup Guide: Troubleshooting](./docs/SETUP.md#troubleshooting)

Common issues:
- Port 8080 already in use → See setup guide
- Maven not found → Reinstall from https://maven.apache.org
- Java version wrong → Install Java 21 LTS from https://www.oracle.com/java/technologies/downloads/#java21
- Tests failing → Run \mvn clean install\ then retry

---

## Implementation Highlights

Resilience: Partial success on failures (isolate & continue pattern)  
Performance: Semaphore bounded concurrency (10 concurrent lookups)  
Testability: Dependency injection + mock API clients  
Operability: Configuration-driven, environment variable overrides  
Quality: 32 tests, ~75% coverage, SOLID principles applied

---

See [Project README](../README.md) for complete documentation.
