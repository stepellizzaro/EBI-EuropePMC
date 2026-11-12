# EPMC-Java (Spring Boot 3) Setup Guide

Complete setup instructions for the Java implementation of EPMC using Spring Boot 3 and Maven.

---

## Prerequisites

### System Requirements

- **Windows, macOS, or Linux**
- **Java 21 LTS** (JDK or JRE)
- **Maven 3.9+**
- **Git** (for cloning the repository)
- **Docker & Docker Compose** (optional, for containerized deployment)
- **IntelliJ IDEA, Eclipse, or VS Code** (recommended for development)

### Download Java 21 LTS

#### Windows

1. Go to https://www.oracle.com/java/technologies/downloads/#java21
2. Download **Windows x64 Installer** (for x64 systems)
3. Run the installer and follow prompts
4. Verify installation:

```powershell
java -version
# Should output: Java 21.x.x LTS
javac -version
# Should output: javac 21.x.x
```

#### macOS

```bash
# Using Homebrew
brew install openjdk@21

# Verify
java -version
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt-get update
sudo apt-get install openjdk-21-jdk

# Verify
java -version
```

### Download Maven 3.9+

#### Windows (Manual Installation)

1. Go to https://maven.apache.org/download.cgi
2. Download **apache-maven-3.9.x-bin.zip**
3. Extract to `C:\Program Files\Maven` (or your preferred location)
4. Add Maven to PATH:
   - Right-click **This PC** → Properties → **Advanced system settings**
   - Click **Environment Variables**
   - Under System variables, click **New**
   - Variable name: `MAVEN_HOME`, Value: `C:\Program Files\Maven`
   - Click **OK**
   - Find `Path` in System variables, click **Edit**
   - Click **New** and add: `%MAVEN_HOME%\bin`
   - Click **OK** to save

5. Verify installation:

```powershell
mvn -version
# Should output: Apache Maven 3.9.x
```

#### Windows (Using Chocolatey)

```bash
choco install maven
mvn -version
```

#### macOS

```bash
brew install maven
mvn -version
```

#### Linux (Ubuntu/Debian)

```bash
sudo apt-get install maven
mvn -version
```

---

## Project Structure

```
EPMC-java/
├── src/
│   ├── main/
│   │   ├── java/com/example/epmc/
│   │   │   ├── EpmcApplication.java
│   │   │   ├── config/
│   │   │   ├── controllers/
│   │   │   ├── exceptions/
│   │   │   ├── models/
│   │   │   └── services/
│   │   │       └── interfaces/
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test/java/com/example/epmc/
│       ├── GrantValidatorTest.java
│       └── PortfolioServiceIntegrationTest.java
├── pom.xml                              # Maven configuration
├── Dockerfile                           # Container build
├── docker-compose.yml                   # Local development
├── docs/SETUP.md
└── README.md                            # Project-specific notes
```

---

## Quick Start

### Option 1: Build and Run with Maven

```bash
cd EPMC-java

# Build the project
mvn clean package

# Run the JAR file
java -jar target/epmc-java-1.0.0.jar

# Service is now running at http://localhost:8080
```

**Output:**
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.4.x)

2024-11-10 22:12:15.342  INFO 12345 --- [main] c.e.epmc.EpmcApplication : Started EpmcApplication in 2.345 seconds
```

### Option 2: Run with Maven Plugin

```bash
cd EPMC-java

# Run directly with Maven
mvn spring-boot:run

# Service is now running at http://localhost:8080
```

### Option 3: Run with Docker

```bash
cd EPMC-java

# Option 3a: Using docker-compose (recommended)
docker-compose up

# Option 3b: Manual Docker build
docker build -t epmc-java:latest .
docker run -p 8080:8080 epmc-java:latest

# Service is now running at http://localhost:8080
```

**View logs:**
```bash
docker-compose logs -f          # Continuous logs
docker-compose down             # Stop and clean up
```

### Option 4: Run in IDE (IntelliJ IDEA)

1. Open the project folder
2. Wait for Maven to download dependencies (look for "Downloading..." in status bar)
3. Find `EpmcApplication.java` in `src/main/java/com/example/epmc/`
4. Right-click → **Run 'EpmcApplication.main()'**
5. Application starts with debugger attached

---

## Running Tests

### All Tests

```bash
cd EPMC-java
mvn test
```

**Output:**
```
Tests run: 32, Failures: 0, Skipped: 0

BUILD SUCCESS
```

### Specific Test Class

```bash
# Run only GrantValidator tests (25 tests)
mvn test -Dtest=GrantValidatorTest

# Run only PortfolioService tests (7 tests)
mvn test -Dtest=PortfolioServiceIntegrationTest
```

### Verbose Test Output

```bash
mvn test -X
```

### Test Coverage Report (JaCoCo)

```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
# Open: target/site/jacoco/index.html in browser
```

### Skip Tests During Build

```bash
# Build without running tests
mvn clean package -DskipTests
```

---

## Configuration

### application.yml

Default configuration file:

```yaml
spring:
  application:
    name: epmc-java

server:
  port: 8080
  servlet:
    context-path: /

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

### Optional development profile file

The repository does not include `application-dev.yml`. To add development-specific overrides, create this file; Spring loads it when `spring.profiles.active=dev`.

```yaml
logging:
  level:
    root: INFO
    com.epmc: DEBUG

server:
  port: 8080
```

### Optional production profile file

The repository does not include `application-prod.yml`. To add production-specific overrides, create this file; Spring loads it when `spring.profiles.active=prod`.

```yaml
logging:
  level:
    root: WARN
    com.epmc: INFO

portfolio:
  max-concurrent-lookups: 50

server:
  port: 8080
  compression:
    enabled: true
    min-response-size: 1024
```

### Override with Environment Variables

```bash
# Unix/Linux/macOS
export SPRING_PROFILES_ACTIVE=production
export PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20
export EXTERNAL_APIS_GRIST_API_BASE_URL=https://prod-grist.example.com

java -jar target/epmc-java-1.0.0.jar
```

```powershell
# Windows PowerShell
$env:SPRING_PROFILES_ACTIVE = "production"
$env:PORTFOLIO_MAX_CONCURRENT_LOOKUPS = "20"
$env:EXTERNAL_APIS_GRIST_API_BASE_URL = "https://prod-grist.example.com"

java -jar target/epmc-java-1.0.0.jar
```

### Environment Names

| Name | Use Case | Logging |
|------|----------|---------|
| **dev** | Local development | DEBUG (verbose) |
| **staging** | Pre-production testing | INFO |
| **production** | Live environment | WARN (minimal) |

---

## Testing the API

### Using curl

```bash
# Basic request (uses defaults: limit=10, publicationsPerGrant=5)
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"

# Custom limits
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Manchester&maxGrants=5&maxPublications=3"

# Validation error test (exceeds max limit)
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge&maxGrants=11"
```

### Using Swagger UI

Open browser to: http://localhost:8080/swagger-ui.html

**Features:**
- Interactive API explorer
- Try out endpoints from the browser
- View request/response schemas
- See error response examples
- Download OpenAPI specification

### Using Postman

1. Install [Postman](https://www.postman.com/downloads/)
2. Import OpenAPI spec:
   - Open Postman
   - Click **Import** → **Link** tab
   - Enter: `http://localhost:8080/v3/api-docs`
3. Or manually create request:
   - Method: `GET`
   - URL: `http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge`

---

## 🐛 Debugging

### Enable Debug Logging

Edit `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.epmc: DEBUG
    org.springframework: DEBUG
```

Then run:
```bash
mvn spring-boot:run
```

### Using IntelliJ IDEA Debugger

1. Open `PortfolioController.java`
2. Click left margin to set breakpoint on line with `@GetMapping`
3. Click **Debug** icon (or Shift+F9)
4. Use Debug panel to inspect variables and step through code

### Using VS Code Debugger

1. Install **Debugger for Java** extension
2. Press **Ctrl+Shift+D** to open Debug view
3. Click **create a launch.json file** and select Java
4. Press **F5** to start debugging

### Common Breakpoint Locations

- `PortfolioController.getPortfolio()` — Entry point
- `PortfolioServiceImpl.getPortfolio()` — Main orchestration
- `GrantValidator.validateRequest()` — Input validation
- `ArticlesApiClient.getPublications()` — Publication fetch

---

## 🔨 Build & Package

### Clean Build Artifacts

```bash
cd EPMC-java

# Remove compiled classes and build artifacts
mvn clean

# Or manually delete
rm -r target/
```

### Full Build and Package

```bash
# Compile, test, and package
mvn clean package

# Output: target/epmc-java-1.0.0.jar
```

### Build Without Tests

```bash
mvn clean package -DskipTests
```

### Install Locally (for other projects to depend on)

```bash
mvn install
```

---

## 🐳 Docker Deployment

### Build Docker Image

```bash
cd EPMC-java

# Build image
docker build -t epmc-java:latest .

# List images
docker image ls | grep epmc-java
```

### Run Docker Container

```bash
# Basic run
docker run -p 8080:8080 epmc-java:latest

# With environment variable overrides
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e PORTFOLIO_MAX_CONCURRENT_LOOKUPS=20 \
  epmc-java:latest

# In background
docker run -d -p 8080:8080 --name epmc epmc-java:latest
```

### Using Docker Compose

```bash
# Start services
docker-compose up

# Start in background
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Clean up (remove images, volumes)
docker-compose down -v
```

### Multi-Stage Build

The `Dockerfile` uses multi-stage build for efficiency:

```dockerfile
# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /src
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src src
RUN mvn clean package -DskipTests

# Stage 2: Runtime (smaller image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /src/target/epmc-java-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Result**: Smaller final image (JRE only, no Maven/compiler).

---

## 📦 Dependency Management

### View Dependencies

```bash
# List all dependencies
mvn dependency:tree

# Check for conflicts
mvn dependency:analyze
```

### Update Dependencies

```bash
# Check for newer versions
mvn versions:display-dependency-updates

# Update to newer versions
mvn versions:use-latest-versions
```

### Add New Dependency

Edit `pom.xml` and add to `<dependencies>` section:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>my-library</artifactId>
    <version>1.0.0</version>
</dependency>
```

Then:
```bash
mvn clean install
```

---

## Production Checklist

Before deploying to production:

- [ ] Set `SPRING_PROFILES_ACTIVE=production`
- [ ] Update `EXTERNAL_APIS_GRIST_API_BASE_URL` and `ARTICLES_API_BASE_URL` to prod endpoints
- [ ] Increase `PORTFOLIO_MAX_CONCURRENT_LOOKUPS` for high-volume (e.g., 20-50)
- [ ] Set appropriate timeouts: `PORTFOLIO_PER_CALL_TIMEOUT_SECONDS`, `PORTFOLIO_OVERALL_TIMEOUT_SECONDS`
- [ ] Configure logging to WARN level (reduce verbose output)
- [ ] Run full test suite: `mvn test`
- [ ] Run with coverage: `mvn clean test jacoco:report`
- [ ] Build Docker image without tests: `mvn clean package -DskipTests`
- [ ] Test image locally: `docker run -p 8080:8080 epmc-java:latest`
- [ ] Set up monitoring/alerting (Spring Boot Actuator, Prometheus, Grafana)
- [ ] Configure health check endpoint for load balancers: `http://localhost:8080/actuator/health`

---

## Common Commands Cheat Sheet

```bash
# Build and test
mvn clean package                   # Full build with tests
mvn clean package -DskipTests       # Build without tests
mvn clean install                   # Build and install locally

# Run
mvn spring-boot:run                 # Run directly
java -jar target/epmc-java-1.0.0.jar  # Run JAR file

# Test
mvn test                             # Run all tests
mvn test -Dtest=GrantValidatorTest  # Run specific test class
mvn test jacoco:report              # Run with coverage

# Dependencies
mvn dependency:tree                 # Show dependency tree
mvn dependency:analyze              # Find unused/conflicting deps
mvn versions:display-dependency-updates  # Check for updates

# Clean
mvn clean                           # Remove target/ directory

# Docker
docker build -t epmc-java:latest .  # Build image
docker run -p 8080:8080 epmc-java:latest  # Run container
docker-compose up                   # Run with docker-compose
docker-compose down                 # Stop docker-compose services
```

---

## Troubleshooting

### Maven Command Not Found

**Symptom**: `mvn: command not found`

**Solution**:
```bash
# Verify Maven is installed
mvn -version

# If not found, reinstall from https://maven.apache.org/download.cgi
# Or use system package manager:
# Ubuntu: sudo apt-get install maven
# macOS: brew install maven
```

### Java Version Mismatch

**Symptom**: `error: source release 21 requires target release 21` or similar

**Solution**:
```bash
# Check Java version
java -version

# Should be Java 21.x.x LTS

# If wrong version:
# Windows: download from https://www.oracle.com/java/technologies/downloads/#java21
# macOS: brew install openjdk@21
# Linux: sudo apt-get install openjdk-21-jdk
```

### Port 8080 Already in Use

**Symptom**: `Web server failed to start. Port 8080 was already in use`

**Solution**:
```bash
# Find process using port 8080
netstat -ano | findstr :8080         # Windows
lsof -i :8080                         # macOS/Linux

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F              # Windows
kill -9 <PID>                        # macOS/Linux

# Or use a different port
export SERVER_PORT=8081
mvn spring-boot:run
```

### Tests Failing

**Symptom**: `mvn test` fails

**Solution**:
```bash
# Rebuild everything
mvn clean install

# Run with verbose output
mvn test -X

# Run specific test
mvn test -Dtest=GrantValidatorTest
```

### Docker Build Fails

**Symptom**: `docker build` fails

**Solution**:
```bash
# Ensure local build works first
mvn clean package

# Clear Docker cache
docker build --no-cache -t epmc-java:latest .

# Check Docker disk space
docker system df
docker system prune          # Remove unused images/containers
```

### Application Fails to Start

**Symptom**: Application exits immediately with error

**Solution**:
```bash
# Run with verbose logging
export LOG_LEVEL=DEBUG
java -jar target/epmc-java-1.0.0.jar

# Or with Spring Boot debug mode
mvn spring-boot:run -Dspring-boot.run.arguments="--debug"
```

---

## Additional Resources

- [Java 21 Documentation](https://docs.oracle.com/en/java/javase/21/)
- [Maven Documentation](https://maven.apache.org/guides/)
- [Spring Boot 3 Documentation](https://spring.io/projects/spring-boot)
- [Spring Framework Reference](https://docs.spring.io/spring-framework/reference/)
- [Docker Documentation](https://docs.docker.com/)
- [Project README](../README.md)
- [GLOSSARY](../GLOSSARY.md)

---

**Last Updated**: November 10, 2026  
**Version**: 1.0
