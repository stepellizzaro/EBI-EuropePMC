# EPMC: C# Implementation (ASP.NET Core 8)

[![Status](https://img.shields.io/badge/status-production--ready-brightgreen)]()
[![Tests](https://img.shields.io/badge/tests-32%2F32%20passed-brightgreen)]()
[![Coverage](https://img.shields.io/badge/coverage-~75%25-blue)]()

**EPMC** (Enterprise Portfolio Management & Correlation) — C# ASP.NET Core 8 implementation.

For shared documentation, architecture, and API details, see [../README.md](../README.md).

---

## Quick Start

### Run Locally

```bash
cd EPMC-csharp

# Build and run
dotnet restore
dotnet build
dotnet run

# Service at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger
```

### Run with Debugger (VS Code)

```bash
code .
# Press F5 to start debugging
```

### Run with Docker

```bash
docker-compose up
# Service at http://localhost:8080
```

---

## Documentation

- **[Setup Guide](./docs/SETUP.md)** — .NET 8 prerequisites, debugging, Docker
- **[API Reference](../README.md#api-documentation)** — Endpoint specifications
- **[Architecture](../README.md#architecture)** — Request flow, domain model, concurrency
- **[GLOSSARY](../GLOSSARY.md)** — Domain terminology

---

## Testing

```bash
# All tests
dotnet test

# Specific test class
dotnet test --filter "GrantValidatorTests"

# Verbose output
dotnet test --verbosity detailed
```

**Test Results**: 32/32 Passed (25 unit + 7 integration), ~75% coverage

---

## Project Structure

```
EPMC-csharp/
├── docs/
│   └── SETUP.md                     # Setup guide for this project
├── Controllers/                      # HTTP endpoints
├── Services/                         # Business logic
├── Models/                           # Domain models
├── Exceptions/                       # Custom exceptions
├── Middleware/                       # Cross-cutting concerns
├── Config/                           # Configuration
├── Tests/                            # Unit & integration tests
├── Program.cs                        # Entry point & DI setup
├── appsettings.json                  # Configuration
├── Dockerfile                        # Container build
├── docker-compose.yml                # Local development
├── EPMC.http                         # REST client test requests
└── README.md                         # This file
```

---

## Configuration

**Key Settings** (from `appsettings.json`):

```json
{
  "Portfolio": {
    "MaxGrants": 10,
    "MaxPublicationsPerGrant": 5,
    "MaxConcurrentLookups": 10,
    "PerCallTimeoutSeconds": 5,
    "OverallTimeoutSeconds": 30
  }
}
```

**Override with Environment Variables**:

```bash
set ASPNETCORE_ENVIRONMENT=Production
set Portfolio__MaxConcurrentLookups=20
dotnet run
```

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
- [Java Implementation](../EPMC-java/) — Java/Spring Boot version

---

## Troubleshooting

See [Setup Guide: Troubleshooting](./docs/SETUP.md#troubleshooting)

Common issues:
- Port 8080 already in use → See setup guide
- .NET SDK not found → Reinstall from https://dotnet.microsoft.com/download
- Tests failing → Run `dotnet clean` then rebuild

---

## Implementation Highlights

Resilience: Partial success on failures (isolate & continue pattern)  
Performance: SemaphoreSlim bounded concurrency (10 concurrent lookups)  
Testability: Dependency injection + mock API clients  
Operability: Configuration-driven, environment variable overrides  
Quality: 32 tests, ~75% coverage, SOLID principles applied

---

See [Project README](../README.md) for complete documentation.
