# Quick Start

Get EPMC running locally in minutes.

---

## Prerequisites

### C# EPMC-csharp
- **.NET 8 SDK** ([download](https://dotnet.microsoft.com/download))
- **VS Code** (optional, for debugging)

### Java EPMC-Java
- **Java 21 LTS** ([download](https://www.oracle.com/java/technologies/downloads/#java21))
- **Maven 3.8+** ([download](https://maven.apache.org/download.cgi))

---

## C# Implementation (ASP.NET Core 8)

### Run Locally

```bash
cd EPMC-csharp

# Restore dependencies
dotnet restore

# Build the project
dotnet build

# Run the service
dotnet run
# Service at http://localhost:8080
```

### Run with Debugger (VS Code)

```bash
cd EPMC-csharp
# Press F5 in VS Code
# Service at http://localhost:8080
```

### Run Tests

```bash
cd EPMC-csharp

# All tests
dotnet test

# Specific test class
dotnet test --filter "GrantValidator"

# Verbose output
dotnet test --verbosity detailed
```

### Run with Docker

```bash
cd EPMC-csharp
docker-compose up
# Service at http://localhost:8080
```

**Full setup guide**: See [EPMC-csharp/docs/SETUP.md](./EPMC-csharp/docs/SETUP.md)

---

## Java Implementation (Spring Boot 3)

### Build and Run

```bash
cd EPMC-java

# Build and package
mvn clean package

# Run the service
java -jar target/epmc-java-1.0.0.jar
# Service at http://localhost:8080
```

### Run Tests

```bash
cd EPMC-java

# All tests
mvn test

# Specific test class
mvn test -Dtest=GrantValidatorTest

# With coverage report
mvn test jacoco:report
```

### Run with Maven Plugin

```bash
cd EPMC-java
mvn spring-boot:run
# Service at http://localhost:8080
```

### Run with Docker

```bash
cd EPMC-java
docker-compose up
# Service at http://localhost:8080
```

**Full setup guide**: See [EPMC-java/docs/SETUP.md](./EPMC-java/docs/SETUP.md)

---

## Verify the Service is Running

### Using curl

```bash
# Basic request (uses defaults: limit=10, publicationsPerGrant=5)
curl "http://localhost:8080/api/portfolio/institution-portfolio?institution=Cambridge"
```

### Using Swagger UI

- **C# EPMC-csharp**: http://localhost:8080/swagger
- **Java EPMC-Java**: http://localhost:8080/swagger-ui.html

---

## Next Steps

- **Test the API**: See [SUPPORT.md](./SUPPORT.md) for API testing examples
- **Understand the Architecture**: See [README.md](./README.md) for design overview
- **Review Decisions**: See [DECISION_LOG.md](./DECISION_LOG.md) for architectural rationales
- **Check Test Coverage**: See [TESTING.md](./TESTING.md) for test guide
