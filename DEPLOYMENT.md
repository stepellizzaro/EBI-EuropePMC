# Deployment & Configuration

Deploy EPMC to local, cloud, or Docker environments.

---

## Configuration

Both implementations use configuration-driven settings for environment-specific tuning.

### C# EPMC-csharp (appsettings.json)

```json
{
  "Portfolio": {
    "MaxGrants": 10,
    "MaxPublicationsPerGrant": 5,
    "PerCallTimeoutSeconds": 5,
    "OverallTimeoutSeconds": 30,
    "MaxConcurrentLookups": 10
  },
  "ExternalApis": {
    "GristApiBaseUrl": "https://api.grist.example.com",
    "ArticlesApiBaseUrl": "https://api.europepmc.org"
  }
}
```

**Override with environment variables:**

```bash
set ASPNETCORE_ENVIRONMENT=Production
set Portfolio__MaxConcurrentLookups=20
set Portfolio__PerCallTimeoutSeconds=10
set ExternalApis__GristApiBaseUrl=https://prod-api.grist.example.com
dotnet run
```

**Common settings:**
- `Portfolio__MaxGrants` — Maximum grants per request (1-10, default 10)
- `Portfolio__MaxPublicationsPerGrant` — Maximum publications per grant (1-5, default 5)
- `Portfolio__PerCallTimeoutSeconds` — Timeout per API call (default 5)
- `Portfolio__OverallTimeoutSeconds` — Timeout for entire request (default 30)
- `Portfolio__MaxConcurrentLookups` — Concurrent publication fetches (default 10; tune per environment)

### Java EPMC-Java (application.yml)

```yaml
portfolio:
  max-grants: 10
  max-publications-per-grant: 5
  per-call-timeout-seconds: 5
  overall-timeout-seconds: 30
  max-concurrent-lookups: 10

external-apis:
  grist-api-base-url: https://api.grist.example.com
  articles-api-base-url: https://api.europepmc.org
```

**Override with environment variables:**

```bash
export SPRING_PROFILES_ACTIVE=production
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20
export PORTFOLIO_PER_CALL_TIMEOUT_SECONDS=10
export EXTERNAL_APIS_GRIST_API_BASE_URL=https://prod-api.grist.example.com
java -jar target/epmc-java-1.0.0.jar
```

**Common settings:**
- `portfolio.max-grants` — Maximum grants per request (1-10, default 10)
- `portfolio.max-publications-per-grant` — Maximum publications per grant (1-5, default 5)
- `portfolio.per-call-timeout-seconds` — Timeout per API call (default 5)
- `portfolio.overall-timeout-seconds` — Timeout for entire request (default 30)
- `portfolio.max-concurrent-lookups` — Concurrent publication fetches (default 10; tune per environment)

---

## Local Development

### C# EPMC-csharp

```bash
cd EPMC-csharp
dotnet run
# Service at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger
```

### Java EPMC-Java

```bash
cd EPMC-java
mvn spring-boot:run
# Service at http://localhost:8080
# Swagger UI at http://localhost:8080/swagger-ui.html
```

---

## Docker Deployment

### C# EPMC-csharp

**Build image:**

```bash
cd EPMC-csharp
docker build -t epmc:latest .
```

**Run container:**

```bash
docker run -p 8080:8080 \
  -e "Portfolio__MaxConcurrentLookups=20" \
  -e "ExternalApis__GristApiBaseUrl=https://prod-api.grist.example.com" \
  epmc:latest
# Service at http://localhost:8080
```

**Using docker-compose:**

```bash
cd EPMC-csharp
docker-compose up
# Service at http://localhost:8080
```

### Java EPMC-Java

**Build image:**

```bash
cd EPMC-java
docker build -t epmc-java:latest .
```

**Run container:**

```bash
docker run -p 8080:8080 \
  -e "PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20" \
  -e "EXTERNAL_APIS_GRIST_API_BASE_URL=https://prod-api.grist.example.com" \
  epmc-java:latest
# Service at http://localhost:8080
```

**Using docker-compose:**

```bash
cd EPMC-java
docker-compose up
# Service at http://localhost:8080
```

---

## Environment-Specific Tuning

### Development
- Low concurrency (5-10)
- Shorter timeouts acceptable (3-5 seconds)
- Mock APIs used
- Detailed logging enabled

```bash
# C#
set ASPNETCORE_ENVIRONMENT=Development
set Portfolio__MaxConcurrentLookups=5

# Java
export SPRING_PROFILES_ACTIVE=development
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=5
```

### Staging / Pre-production
- Medium concurrency (10-15)
- Real API timeouts validated (5-10 seconds)
- Verbose logging for debugging
- Performance tested with real data

```bash
# C#
set ASPNETCORE_ENVIRONMENT=Staging
set Portfolio__MaxConcurrentLookups=15

# Java
export SPRING_PROFILES_ACTIVE=staging
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=15
```

### Production
- High concurrency (15-20+)
- Optimized timeouts (5-10 seconds)
- Structured logging for monitoring
- Caching enabled (if implemented)
- Rate limiting enforced

```bash
# C#
set ASPNETCORE_ENVIRONMENT=Production
set Portfolio__MaxConcurrentLookups=20
set Portfolio__PerCallTimeoutSeconds=10

# Java
export SPRING_PROFILES_ACTIVE=production
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20
export PORTFOLIO_PER_CALL_TIMEOUT_SECONDS=10
```

---

## Performance Tuning

### MaxConcurrentLookups

Controls how many publication lookups run in parallel.

- **Too low (e.g., 1-3)**: Slow for large portfolios, but minimal API strain
- **Optimal (10-20)**: Balances speed with API saturation prevention
- **Too high (e.g., 100+)**: Fast but risks overwhelming external APIs

**Recommendation**: Start at 10, monitor external API response times, adjust upward if still fast.

### Timeouts

- **PerCallTimeoutSeconds**: Individual HTTP call timeout (5-10s recommended)
- **OverallTimeoutSeconds**: Entire request timeout (20-30s recommended)

If timeouts are frequently exceeded, check:
1. External API response times (use monitoring/APM)
2. Network latency
3. Concurrent load on external APIs
4. Local resource constraints (CPU, memory)

---

## Next Steps

- **Get started**: See [QUICKSTART.md](./QUICKSTART.md) for setup
- **Understand architecture**: See [README.md](./README.md)
- **Run tests**: See [TESTING.md](./TESTING.md)
- **Review decisions**: See [DECISION_LOG.md](./DECISION_LOG.md)
