# Support & Troubleshooting

Common commands, API testing, and troubleshooting guide.

---

## Common Commands

### C# EPMC-csharp

**Build & Run:**

```bash
cd EPMC-csharp

# Restore NuGet packages
dotnet restore

# Build the project
dotnet build

# Run the service
dotnet run

# Run with auto-restart on file changes
dotnet watch run

# Run release build
dotnet run --configuration Release
```

**Testing:**

```bash
# All tests
dotnet test

# Tests in specific file
dotnet test --filter "GrantValidator"

# Verbose output with test names
dotnet test --verbosity detailed

# No rebuild (just run)
dotnet test --no-build
```

**Cleaning:**

```bash
# Remove build artifacts
dotnet clean

# Remove build + NuGet cache
dotnet clean && rm -r bin obj
```

### Java EPMC-Java

**Build & Run:**

```bash
cd EPMC-java

# Download dependencies
mvn dependency:resolve

# Build and package
mvn clean package

# Run the JAR
java -jar target/epmc-java-1.0.0.jar

# Run with Maven plugin
mvn spring-boot:run

# Run with debug output
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```

**Testing:**

```bash
# All tests
mvn test

# Specific test class
mvn test -Dtest=GrantValidatorTest

# Tests matching pattern
mvn test -Dtest=*ValidatorTest

# Skip tests during build
mvn clean package -DskipTests
```

**Cleaning:**

```bash
# Remove build artifacts
mvn clean

# Remove build + local Maven cache
mvn clean && rm -rf ~/.m2/repository
```

---

## API Testing

### Using curl

**Basic request (default limits):**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"
```

**Custom limits:**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Manchester&limit=5&publicationsPerGrant=3"
```

**Pretty print response:**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge" | jq .
```

**Save response to file:**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge" > response.json
```

**Validation error (empty institution):**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution="
```

**Validation error (limit > 10):**

```bash
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge&limit=11"
```

**Check response headers:**

```bash
curl -i "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"
```

### Using Swagger UI

**C# EPMC-csharp:**

1. Start the service: `dotnet run`
2. Open http://localhost:8080/swagger
3. Expand "PortfolioController" section
4. Click "Get /api/portfolio/institution-portfolio"
5. Enter parameters and click "Try it out"

**Java EPMC-Java:**

1. Start the service: `mvn spring-boot:run`
2. Open http://localhost:8080/swagger-ui.html
3. Expand "PortfolioController" section
4. Click "GET /api/portfolio/institution-portfolio"
5. Enter parameters and click "Try it out"

### Using Postman

1. Import the API endpoint: `http://localhost:8080/api/portfolio/institution-portfolio`
2. Set method to GET
3. Add query parameters:
   - `institution`: Cambridge
   - `limit`: 5 (optional)
   - `publicationsPerGrant`: 3 (optional)
4. Send request
5. View response in formatted JSON

### Using VS Code REST Client

Create a file `test.http`:

```http
### Get portfolio with defaults
GET http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge

### Get portfolio with custom limits
GET http://localhost:8080/api/portfolio/institution-portfolio?institution=Manchester&limit=5&publicationsPerGrant=3

### Validation error: empty institution
GET http://localhost:8080/api/portfolio/institution-portfolio?institution=

### Validation error: limit exceeds max
GET http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge&limit=11
```

Then install the REST Client extension and click "Send Request" above each test.

---

## Troubleshooting

### Service Won't Start

**Symptom**: Port 8080 is already in use

```bash
# Find process using port 8080
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080

# Kill the process (Windows)
taskkill /PID <PID> /F

# Kill the process (Linux/Mac)
kill -9 <PID>

# Or use a different port
# C#
dotnet run --urls "http://+:8081"

# Java
java -jar target/epmc-java-1.0.0.jar --server.port=8081
```

### API Returns 500 Error

**Symptom**: Unexpected server error

```bash
# Check service logs
# C#: Look in console output for exception details
# Java: Check console output for stack trace

# Enable debug logging
# C#
set LOGGING__LOGLEVEL__DEFAULT=Debug
dotnet run

# Java
export LOGGING_LEVEL_COM_EXAMPLE=DEBUG
mvn spring-boot:run
```

### Tests Fail with "Connection Refused"

**Symptom**: Test tries to connect to external API

This should NOT happen if using mocks correctly. Verify:
1. Tests use mock API clients, not real HttpClient calls
2. Mock implementations return hardcoded data
3. No real network calls in unit tests

```bash
# Run tests with verbose output to see what's happening
# C#
dotnet test --verbosity detailed

# Java
mvn test -X
```

### Validation Fails Unexpectedly

**Symptom**: "Institution name is required" even when provided

Verify:
1. Institution name is not empty or null
2. Institution name is ≤255 characters
3. Query parameter name is exactly `institution` (case-sensitive in Java)

**Debug:**

```bash
# Test with curl and check the exact request
curl -v "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"

# Check query parameters are being received
# (Look at console logs in service)
```

### Slow Response Times

**Symptom**: Portfolio requests take >5 seconds

Possible causes:
1. External APIs are slow (check in real deployment, not local mocks)
2. Too many concurrent requests overwhelming system
3. Network latency
4. System resource constraints (CPU, memory)

**Debug:**

```bash
# Check concurrency setting
# Default is 10, can be increased in config

# Monitor system resources while running
# Windows: Task Manager
# Linux: top, htop
# Mac: Activity Monitor

# Increase timeout if external APIs are naturally slow
set Portfolio__PerCallTimeoutSeconds=15
```

### Mock Data Doesn't Match Real APIs

**Symptom**: Tests pass but real API integration fails

This is expected! Mock implementations are intentionally simplified for testing.

**When integrating real APIs:**
1. Use dependency injection to swap implementations
2. Test with real API credentials in staging
3. Validate response schemas match domain models
4. Add retry logic for flaky networks
5. Implement caching for performance
6. Add correlation IDs for tracing
7. Monitor external API response times

See [DECISION_LOG.md](./DECISION_LOG.md) for more details on when/how to integrate real APIs.

---

## Debugging

### VS Code Debugger (C#)

1. Set breakpoints by clicking line numbers
2. Press F5 or go to Run → Start Debugging
3. Service starts in debug mode
4. Breakpoints pause execution
5. Inspect variables in Debug Console
6. Continue with F5 or Step Over (F10)

### IntelliJ/VS Code Debugger (Java)

1. Set breakpoints by clicking line numbers
2. Run `mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"`
3. In IDE, go to Run → Attach to Process (port 5005)
4. Debugger connects
5. Breakpoints pause execution
6. Inspect variables in Debug Console
7. Continue execution

### Request Tracing

Add logging to see request flow:

```bash
# C#: Enable Information-level logs
set LOGGING__LOGLEVEL__DEFAULT=Information
dotnet run

# Java: Enable debug logging
export LOGGING_LEVEL_COM_EXAMPLE=DEBUG
mvn spring-boot:run
```

Look for these log lines:
1. "Portfolio request for: Cambridge" — Request received
2. "Fetching portfolio for institution: Cambridge" — Service started
3. "Fetching grants for institution: Cambridge" — Calling grant API
4. "Fetching publications for grant: G001" — Fetching publications
5. "Aggregating portfolio..." — Building response

---

## Getting Help

### Check Documentation

- [README.md](./README.md) — Architecture overview
- [QUICKSTART.md](./QUICKSTART.md) — Getting started
- [DECISION_LOG.md](./DECISION_LOG.md) — Design decisions
- [DEPLOYMENT.md](./DEPLOYMENT.md) — Configuration & deployment
- [TESTING.md](./TESTING.md) — Test guide
- [SOLID_PRINCIPLES.md](./SOLID_PRINCIPLES.md) — SOLID patterns
- [TECH_DEBT.md](./TECH_DEBT.md) — Known limitations

### Review Logs

Both implementations log to console with timestamps:

```
[HH:MM:SS] [LEVEL] [Component] Message
```

**Log levels:**
- `ERROR`: Critical failures (requires action)
- `WARNING`: Unexpected but recovered (monitor)
- `INFORMATION`: Normal operations (diagnostic)
- `DEBUG`: Detailed diagnostic info (development only)

### Check Recent Changes

```bash
# View git log
git log --oneline -10

# View recent file changes
git status

# View specific file history
git log --oneline -- EPMC-csharp/Program.cs
```

---

## Next Steps

- **Quick start**: [QUICKSTART.md](./QUICKSTART.md)
- **Architecture**: [README.md](./README.md)
- **Decisions**: [DECISION_LOG.md](./DECISION_LOG.md)
- **Testing**: [TESTING.md](./TESTING.md)
