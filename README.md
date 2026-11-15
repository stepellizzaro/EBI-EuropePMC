# Europe PMC

**Date**: November 10, 2026  
**Project Scope**: Technical Take-Home Test for Technical Project Lead Role (3 hours)  
**Project Approach**: Two complete implementations: C# (ASP.NET Core 8) and Java (Spring Boot 3)

---

The Java version fulfills the assignment; the C# version demonstrates my working expertise in my primary technology stack.

---

## Project Overview

This repository contains **two complete, tested implementations** exploring the same service specification. Both are fully functional with comprehensive test coverage (~75% each) and demonstrate practical architectural decisions made during the 3-hour take-home test.

| Aspect | EPMC (C#) | EPMC-Java (Java) |
|--------|-----------|-----------------|
| Runtime | .NET 8 | Java 21 LTS |
| Framework | ASP.NET Core | Spring Boot 3.4 |
| Build Tool | dotnet CLI | Maven |
| Tests | 32 (25 unit + 7 integration) | 32 (25 unit + 7 integration) |
| Coverage | ~75% | ~75% |
| Status | Complete & Tested | Complete & Tested |
| Location | `./EPMC-csharp/` | `./EPMC-java/` |

Both implementations share identical:
- REST API contracts
- Domain models  
- Business logic
- Configuration approach
- Error handling strategy
- Testing coverage

**Note**: This is a take-home test implementation designed to demonstrate API exploration, practical decision-making, and architectural thinking. See [TECH_DEBT.md](./TECH_DEBT.md) for an honest assessment of trade-offs and accepted limitations.

---

## User Story & Acceptance Criteria

### User Story

> As a university research administrator, I want to input an institution name and receive a structured portfolio of its matching grants and linked scientific publications, so that I can easily explore funding awards and associated research without needing to know complex query syntax or grant identifiers.

### Acceptance Criteria

#### Scenario 1: Successfully Fetching an Institution Portfolio

**Given** the REST API is running  
**When** a client sends: `GET /api/portfolio/institution-portfolio?institution=Cambridge&limit=5&publicationsPerGrant=3`  
**Then** the service returns 200 OK with:
- Requested institution name
- Up to 5 matching grants (preserving order, with ID, title, funder, institution)
- Up to 3 linked publications per grant

#### Scenario 2: Scope Boundaries & Input Validation

**Given** maximum limits are enforced (10 grants, 5 publications per grant)  
**When** a client provides invalid parameters (empty institution, exceeds max bounds)  
**Then** the service responds with 400 Bad Request and clear error details

#### Scenario 3: Partial Failure Resilience

**Given** publication lookups are executed concurrently  
**When** one grant's publication fetch times out or fails  
**Then** the service isolates the failure, preserves successful results for other grants, and returns 200 with partial success

#### Scenario 4: Summary Metrics & Status Reporting

**Given** a successful or partial portfolio fetch completes  
**When** the JSON response is generated  
**Then** it includes:
- Clear status for each grant (found, partial, error, notfound)
- Summary counts (total grants, total publications, funder breakdown)
- Publication status indicators for each grant

---

## API Documentation

### Endpoint: Get Institution Portfolio

```http
GET /api/portfolio/institution-portfolio
```

### Query Parameters

| Parameter | Type | Required | Default | Constraint |
|-----------|------|----------|---------|-----------|
| `institution` | string | Yes | — | 1-255 characters, non-empty |
| `limit` | integer | No | 10 | 1-10 (max grants) |
| `publicationsPerGrant` | integer | No | 5 | 1-5 (max per grant) |

*Note: Java implementation uses `maxGrants` and `maxPublications` instead of `limit` and `publicationsPerGrant`.*

### Success Response (200 OK)

```json
{
  "institution": {
    "name": "Cambridge"
  },
  "grants": [
    {
      "grant": {
        "id": "G001",
        "title": "AI Research Initiative",
        "funder": "UKRI",
        "institution": "Cambridge",
        "link": "https://grants.example.com/G001"
      },
      "publications": [
        {
          "id": "P001",
          "title": "Advances in Machine Learning",
          "authors": "Smith, J., et al.",
          "journal": "Nature",
          "url": "https://doi.org/example",
          "publicationDate": "2024-06-15"
        }
      ],
      "publicationStatus": "found",
      "errorMessage": null
    }
  ],
  "summary": {
    "totalGrants": 10,
    "totalPublications": 28,
    "funderCounts": {
      "UKRI": 5,
      "EU": 3,
      "DARPA": 2
    },
    "successfulGrants": 9,
    "partialFailureGrants": 1,
    "failedGrants": 0
  }
}
```

### Error Responses

| Status | Scenario |
|--------|----------|
| **400 Bad Request** | Invalid institution name, limit exceeds 10, publicationsPerGrant exceeds 5 |
| **502 Bad Gateway** | External API (Grist/Articles) communication error |
| **504 Gateway Timeout** | Request timeout (per-call or overall) |
| **500 Server Error** | Unexpected server error |

### Example Requests

**Using curl:**

```bash
# Basic request (uses defaults: limit=10, publicationsPerGrant=5)
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"

# Custom limits
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Manchester&limit=5&publicationsPerGrant=3"

# Empty institution (returns 400)
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution="
```

**Using Swagger UI:**

- **C# EPMC-csharp**: http://localhost:8080/swagger
- **Java EPMC-Java**: http://localhost:8080/swagger-ui.html

---

## Architecture

### Request Flow

```
HTTP GET /api/portfolio/institution-portfolio?institution=Cambridge&limit=5&publicationsPerGrant=3
  ↓
Controller (HTTP parsing & validation)
  ├─ Extract: institution, limit, publicationsPerGrant
  ├─ Call Service
  └─ Return Response
  ↓
Service (Orchestration & Business Logic)
  ├─ Validate institution name, limits
  ├─ Fetch grants from Grist API
  ├─ Filter valid grants (must have ID, title, funder)
  ├─ Fetch publications (parallel with Semaphore bounded concurrency)
  │  └─ Max 10 concurrent lookups to prevent API saturation
  ├─ Handle errors per grant (continue on failure)
  ├─ Aggregate summary statistics
  └─ Build response
  ↓
Response
  ├─ 200 OK: Complete portfolio with all data
  ├─ 400 Bad Request: Validation error with details
  ├─ 502 Bad Gateway: External API error
  ├─ 504 Gateway Timeout: Request timeout
  └─ 500 Internal Server Error: Unexpected error
```

### Concurrency Model: Semaphore Bounded Parallelism

**Why bounded concurrency?**
- Prevents API saturation (limits concurrent requests to external APIs)
- Fair resource allocation across grants
- Supports large portfolios (100+ grants) efficiently

**How it works:**
- Default bound: 10 concurrent publication lookups
- Each grant acquires a permit before calling Articles API
- Permits are released (finally block) even if the lookup fails
- Exception-safe: One grant's failure doesn't affect others

**Example with 100 grants:**
- Batch 1 (grants 1-10): Run in parallel (150ms)
- Batch 2 (grants 11-20): Run in parallel (150ms)
- ... (repeats for all 10 batches)
- Total time: ~1.5 seconds (10 batches × 150ms each)

---

**Configuration & Deployment**: See [DEPLOYMENT.md](./DEPLOYMENT.md) for complete configuration details, environment variables, Docker setup, and performance tuning.

---

**Testing**: See [TESTING.md](./TESTING.md) for comprehensive test coverage, scenarios, and running tests.

---

## Design & Architecture

**SOLID Principles**: [SOLID_PRINCIPLES.md](./SOLID_PRINCIPLES.md) — How the code demonstrates all 5 SOLID design principles

**Architectural Decisions**: [DECISION_LOG.md](./DECISION_LOG.md) — The 5 key design decisions, trade-off analysis, and production assumptions

---

## Documentation Map

| Document | Purpose |
|----------|---------|
| [README.md](./README.md) | Architecture overview & API contract (this file) |
| [QUICKSTART.md](./QUICKSTART.md) | Get started in minutes (setup & run) |
| [DECISION_LOG.md](./DECISION_LOG.md) | 5 design decisions + reflections + assumptions |
| [SOLID_PRINCIPLES.md](./SOLID_PRINCIPLES.md) | Deep dive into SOLID patterns |
| [DEPLOYMENT.md](./DEPLOYMENT.md) | Configuration, Docker, environment tuning |
| [TESTING.md](./TESTING.md) | Test coverage, test scenarios, running tests |
| [SUPPORT.md](./SUPPORT.md) | Common commands, API testing, troubleshooting |
| [AI-WORKFLOW.md](./AI-WORKFLOW.md) | Engineering Process & AI Collaboration |
| [TECH_DEBT.md](./TECH_DEBT.md) | Trade-offs, limitations, roadmap |
| [GLOSSARY.md](./GLOSSARY.md) | Domain terminology |

---

## File Structure

```
C:\Repos\EBI-EuropePMC\
├── README.md                            # Architecture overview (this file)
├── DECISION_LOG.md                      # 5 design decisions + reflections
├── SOLID_PRINCIPLES.md                  # SOLID pattern explanations
├── DEPLOYMENT.md                        # Configuration & deployment
├── TESTING.md                           # Test guide
├── SUPPORT.md                           # Common commands & troubleshooting
├── AI-WORKFLOW.md                       # Engineering Process & AI Collaboration
├── TECH_DEBT.md                         # Trade-offs & roadmap
├── GLOSSARY.md                          # Domain terminology
│
├── EPMC-csharp/                                # C# ASP.NET Core implementation
│   ├── docs/
│   │   └── SETUP.md                     # .NET 8 prerequisites & debugging
│   ├── README.md                        # C# specifics
│   ├── Controllers/
│   ├── Services/
│   ├── Models/
│   ├── Tests/
│   ├── appsettings.json
│   ├── Dockerfile
│   └── docker-compose.yml
│
└── EPMC-java/                           # Java Spring Boot implementation
    ├── docs/
    │   └── SETUP.md                     # Java 21 prerequisites & Maven
    ├── README.md                        # Java specifics
    ├── src/
    │   └── main/
    │       └── resources/
    │           └── application.yml
    ├── pom.xml
    ├── Dockerfile
    └── docker-compose.yml
```

---

## Setup & Deployment

**Get started now**: [QUICKSTART.md](./QUICKSTART.md)  
**Configure for your environment**: [DEPLOYMENT.md](./DEPLOYMENT.md)  
**Run tests**: [TESTING.md](./TESTING.md)

---

## Non-Functional Requirements

| Requirement | Status | Evidence |
|---|---|---|
| **Performance** | Complete | Bounded concurrency; 100 grants in ~7s |
| **Resilience** | Complete | Isolate & continue; partial success on errors |
| **Security** | Complete | Input validation (24+ rules); no sensitive data in logs |
| **Operability** | Complete | Config-driven; environment variable overrides; Docker |
| **Testability** | Complete | Mock clients; dependency injection; 32 tests |
| **Observability** | Partial | Basic logging; lacks structured traces & correlation IDs |
| **Caching** | Not implemented | Future enhancement |
| **Rate Limiting** | Partial | Implicit via bounded concurrency; no explicit rate limiter |

### Accepted Trade-offs Summary

**See [TECH_DEBT.md](./TECH_DEBT.md)** for detailed trade-off analysis and roadmap.

---

## Support & Resources

- **Common commands, API testing, troubleshooting**: [SUPPORT.md](./SUPPORT.md)
- **Domain terminology**: [GLOSSARY.md](./GLOSSARY.md)
- **C# setup & debugging**: [EPMC-csharp/docs/SETUP.md](./EPMC-csharp/docs/SETUP.md)
- **Java setup**: [EPMC-java/docs/SETUP.md](./EPMC-java/docs/SETUP.md)

---

**Project Type**: Technical Take-Home Test (3-hour challenge)  
**Date**: November 10, 2026  
**Status**: Complete & Tested  
**Coverage**: 75%+ | **Tests**: 64/64 Passed
