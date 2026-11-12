# EPMC C# Setup Guide

Complete setup and development guide for the EPMC C# ASP.NET Core implementation.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Development Setup](#development-setup)
- [Configuration](#configuration)
- [Running Tests](#running-tests)
- [Docker Deployment](#docker-deployment)
- [Debugging](#debugging)
- [Troubleshooting](#troubleshooting)
- [Production Checklist](#production-checklist)

---

## Prerequisites

Before starting, ensure you have the following installed:

### Required Software

| Component | Version | Notes |
|-----------|---------|-------|
| **.NET SDK** | 8.0+ | [Download](https://dotnet.microsoft.com/download/dotnet/8.0) |
| **Git** | Latest | Version control |
| **VS Code** | Latest | Recommended IDE |
| **C# DevKit** | Latest | VS Code extension |

### Optional but Recommended

| Component | Purpose |
|-----------|---------|
| **Docker Desktop** | Container deployment |
| **Docker Compose** | Multi-container orchestration |
| **Azure CLI** | Cloud deployment (if using Azure) |
| **SQL Server Management Studio** | Database management |

### System Requirements

- **OS**: Windows 10+, macOS, or Linux
- **RAM**: Minimum 4 GB (8 GB recommended)
- **Disk Space**: 2 GB free (for .NET SDK + dependencies)
- **Network**: Internet connection for NuGet package restore

---

## Quick Start

### Option 1: Command Line (Fastest)

```bash
# From the repository root
cd EPMC-csharp

# Restore dependencies
dotnet restore

# Build the project
dotnet build

# Run the application
dotnet run

# The API will be available at: http://localhost:8080
```

### Option 2: VS Code with Debugger

1. **Open the project in VS Code:**
   ```bash
   code .\EPMC-csharp
   ```

2. **Install C# DevKit extension** (if not already installed)
   - Press `Ctrl+Shift+X` → Search for "C# DevKit"
   - Click Install

3. **Build and Run:**
   - Press `Ctrl+Shift+B` to build
   - Press `F5` to start debugging
   - VS Code will open the API in your default browser

4. **Test the API:**
   - Navigate to `http://localhost:8080/swagger` for interactive API documentation

### Option 3: Docker Container

```bash
# Build the Docker image
docker build -t epmc:latest .

# Run the container
docker run -p 5000:8080 epmc:latest

# The API will be available at: http://localhost:5000
```

---

## Development Setup

### Step 1: Install .NET SDK

#### Windows
```powershell
# Download and run installer from https://dotnet.microsoft.com/download/dotnet/8.0
# Or use Chocolatey:
choco install dotnet-sdk-8.0

# Verify installation
dotnet --version
```

#### macOS
```bash
brew install dotnet-sdk
dotnet --version
```

#### Linux (Ubuntu/Debian)
```bash
wget https://dot.net/v1/dotnet-install.sh
chmod +x dotnet-install.sh
./dotnet-install.sh -c 8.0

# Add to PATH
export PATH=$PATH:$HOME/.dotnet
```

### Step 2: Open the C# Project

```bash
# From the repository root
cd EPMC-csharp

# Restore NuGet packages
dotnet restore

# List all available commands
dotnet --help
```

### Step 3: Build the Project

```bash
# Full clean build
dotnet clean
dotnet build --configuration Release

# Build output location:
# bin/Release/net8.0/EPMC.dll
```

### Step 4: Set Up Development Environment Variables

Create a `.env` file in the `EPMC` folder:

```env
ASPNETCORE_ENVIRONMENT=Development
ASPNETCORE_URLS=http://localhost:5000;https://localhost:5001
ConnectionStrings__DefaultConnection=Server=localhost;Database=EPMC_Dev;Integrated Security=true;
Logging__LogLevel__Default=Debug
Logging__LogLevel__Microsoft.EntityFrameworkCore=Information
```

Or use `appsettings.Development.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=(localdb)\\mssqllocaldb;Database=EPMC_Dev;Trusted_Connection=true;"
  },
  "Logging": {
    "LogLevel": {
      "Default": "Debug",
      "Microsoft.EntityFrameworkCore": "Information"
    }
  }
}
```

---

## Configuration

### appsettings.json

The main configuration file located at `EPMC-csharp/appsettings.json`:

```json
{
  "ConnectionStrings": {
    "DefaultConnection": "Server=localhost;Database=EPMC_Prod;User Id=sa;Password=YourPassword;"
  },
  "Logging": {
    "LogLevel": {
      "Default": "Information",
      "Microsoft": "Warning",
      "Microsoft.EntityFrameworkCore": "Warning"
    }
  },
  "Jwt": {
    "SecretKey": "your-secret-key-min-32-characters-long",
    "ExpirationMinutes": 60
  },
  "API": {
    "BaseUrl": "https://api.example.com",
    "Timeout": 30
  }
}
```

### Key Configuration Properties

| Property | Purpose | Default |
|----------|---------|---------|
| `ConnectionStrings.DefaultConnection` | Database connection string | localdb |
| `Logging.LogLevel.Default` | Log level (Trace/Debug/Info/Warn/Error/Fatal) | Information |
| `Jwt.SecretKey` | JWT secret for token signing | Required, min 32 chars |
| `Jwt.ExpirationMinutes` | Token expiration time | 60 |
| `API.BaseUrl` | API base URL for CORS | https://api.example.com |

### Environment-Specific Configuration

- **Development**: `appsettings.Development.json` - Relaxed security, verbose logging
- **Staging**: `appsettings.Staging.json` - Production-like, integration testing
- **Production**: `appsettings.json` - Strict security, minimal logging

---

## Running Tests

### Run All Tests

```bash
# Run all tests with output
dotnet test

# Run with detailed output
dotnet test --verbosity detailed

# Run specific test file
dotnet test Tests/Services/GrantServiceTests.cs

# Run specific test class
dotnet test --filter ClassName=GrantServiceTests
```

### Coverage Report

```bash
# Install coverage tool (one time)
dotnet tool install -g dotnet-reportgenerator-globaltool

# Run tests with coverage
dotnet test /p:CollectCoverage=true /p:CoverageFormat=cobertura

# Generate HTML report
reportgenerator -reports:"**/coverage.cobertura.xml" -targetdir:"coveragereport" -reporttypes:"Html"

# View report
start coveragereport/index.html  # Windows
open coveragereport/index.html   # macOS
```

### Test Categories

| Category | Count | Command |
|----------|-------|---------|
| Unit Tests | 25 | `dotnet test --filter Category=Unit` |
| Integration Tests | 7 | `dotnet test --filter Category=Integration` |
| All Tests | 32 | `dotnet test` |

**Coverage Target**: ~75% overall

---

## Docker Deployment

### Build Docker Image

```bash
# Build image with tag
docker build -t epmc:latest -f Dockerfile .

# Build with specific .NET version
docker build --build-arg DOTNET_VERSION=8.0 -t epmc:8.0 .

# View image details
docker images | grep epmc
```

### Run Container Locally

```bash
# Run standalone
docker run -d \
  --name epmc-app \
  -p 5000:8080 \
  -e ASPNETCORE_ENVIRONMENT=Development \
  epmc:latest

# Run with environment file
docker run -d \
  --name epmc-app \
  -p 5000:8080 \
  --env-file .env \
  epmc:latest

# View logs
docker logs -f epmc-app

# Stop container
docker stop epmc-app

# Remove container
docker rm epmc-app
```

### Docker Compose

Use `docker-compose.yml` to run the full stack:

```bash
# Start all services
docker-compose up -d

# View status
docker-compose ps

# View logs
docker-compose logs -f epmc

# Stop services
docker-compose down

# Rebuild images
docker-compose up -d --build
```

### Dockerfile Structure

```dockerfile
# Multi-stage build
FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY . .
RUN dotnet build -c Release

# Production image
FROM mcr.microsoft.com/dotnet/aspnet:8.0
WORKDIR /app
COPY --from=build /src/bin/Release/net8.0 .
EXPOSE 8080
ENTRYPOINT ["dotnet", "EPMC.dll"]
```

---

## Debugging

### VS Code Debugging

#### Launch Configuration

Create `.vscode/launch.json`:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "name": ".NET Core Launch (web)",
      "type": "coreclr",
      "request": "launch",
      "preLaunchTask": "build",
      "program": "${workspaceFolder}/EPMC-csharp/bin/Debug/net8.0/EPMC.dll",
      "args": [],
      "cwd": "${workspaceFolder}/EPMC",
      "stopAtEntry": false,
      "serverReadyAction": {
        "pattern": "\\bNow listening on:\\s+(https?://\\S+)",
        "uriFormat": "{0}",
        "action": "openExternally"
      }
    }
  ]
}
```

#### Debug Commands

- **F5** - Start debugging
- **F10** - Step over
- **F11** - Step into
- **Shift+F11** - Step out
- **Ctrl+Shift+D** - Open Debug view
- **Ctrl+K Ctrl+I** - Peek definition

### Console Debugging

```csharp
// In your code
Console.WriteLine($"Value: {variable}");
System.Diagnostics.Debug.WriteLine("Debug message");
```

### Logging

```csharp
// Inject ILogger
public class GrantService {
    private readonly ILogger<GrantService> _logger;
    
    public GrantService(ILogger<GrantService> logger) {
        _logger = logger;
    }
    
    public void ProcessGrant(Grant grant) {
        _logger.LogInformation("Processing grant: {GrantId}", grant.Id);
        _logger.LogDebug("Grant details: {@Grant}", grant);
    }
}
```

---

## Troubleshooting

### Common Issues and Solutions

#### 1. ".NET SDK not found"

```bash
# Check .NET installation
dotnet --version

# If not found, reinstall from https://dotnet.microsoft.com/download/dotnet/8.0
# Then verify:
dotnet --list-sdks
```

#### 2. "NuGet restore fails"

```bash
# Clear NuGet cache
dotnet nuget locals all --clear

# Restore packages
dotnet restore --force
```

#### 3. "Port 8080 already in use"

```bash
# Find process using port 8080
netstat -ano | findstr :8080

# Kill process (replace PID with actual process ID)
taskkill /PID <PID> /F

# Or run on different port
dotnet run --urls "http://localhost:5001"
```

#### 4. "Database connection failed"

```bash
# Test connection string
dotnet ef dbcontext info -c DefaultConnection

# Check SQL Server is running
# Windows: Services.msc → look for "SQL Server"
# Or use LocalDB:
dotnet ef database update --context DefaultConnection
```

#### 5. "Tests not running"

```bash
# Clean test cache
dotnet test --no-build --no-restore --collect:"XPlat Code Coverage"

# Run with verbosity
dotnet test -v d
```

#### 6. "Docker image won't build"

```bash
# Check Dockerfile syntax
docker build --no-cache -t epmc:latest .

# View build logs with timestamps
docker build --progress=plain -t epmc:latest .

# Verify base image is available
docker pull mcr.microsoft.com/dotnet/sdk:8.0
```

---

## Production Checklist

Before deploying to production:

### Pre-Deployment

- [ ] All tests passing (`dotnet test`)
- [ ] Build succeeds in Release mode (`dotnet build -c Release`)
- [ ] No warnings in build output
- [ ] Code review completed
- [ ] Security scan passed
- [ ] Performance tests successful

### Configuration

- [ ] `appsettings.json` configured for production
- [ ] Database connection string is secure
- [ ] JWT secret key is strong (min 32 characters)
- [ ] CORS origins are restricted to production domains
- [ ] Logging level set to Information or higher
- [ ] All secrets are in secure vault (not in source code)

### Database

- [ ] Latest migrations applied
- [ ] Database backups configured
- [ ] Connection pooling optimized
- [ ] Query performance verified

### Security

- [ ] HTTPS enforced
- [ ] CORS properly configured
- [ ] SQL injection prevention verified (parameterized queries)
- [ ] XSS protection enabled (input validation)
- [ ] Rate limiting implemented
- [ ] Authentication/Authorization working

### Deployment

- [ ] Docker image tested
- [ ] Health check endpoint configured
- [ ] Load balancer configured (if needed)
- [ ] Monitoring/alerting configured
- [ ] Rollback plan documented
- [ ] Deployment tested in staging first

### Post-Deployment

- [ ] Application health check passes
- [ ] API endpoints responding
- [ ] Database connectivity confirmed
- [ ] Logs monitored for errors
- [ ] Performance baseline established
- [ ] Stakeholders notified

---

## Useful Commands Reference

```bash
# Project Management
dotnet new                          # Create new project
dotnet add package <PackageName>    # Add NuGet package
dotnet remove package <PackageName> # Remove package
dotnet restore                      # Restore packages
dotnet clean                        # Clean build artifacts

# Build & Run
dotnet build                        # Build project
dotnet run                          # Run application
dotnet publish -c Release           # Publish for production

# Testing
dotnet test                         # Run tests
dotnet test --filter "TestName"     # Run specific test
dotnet test -c Release              # Test Release build

# Database (EF Core)
dotnet ef database update           # Apply migrations
dotnet ef database drop             # Drop database
dotnet ef migrations add            # Create migration
dotnet ef migrations remove         # Remove migration

# NuGet
dotnet nuget push                   # Publish package
dotnet nuget delete                 # Delete package
dotnet nuget locals                 # Manage local cache

# Code Analysis
dotnet format                       # Format code
dotnet publish -c Release -o ./pub  # Publish to folder
```

---

## Getting Help

- **Official Docs**: https://docs.microsoft.com/dotnet/
- **VS Code C#**: https://github.com/OmniSharp/omnisharp-vscode
- **ASP.NET Core**: https://docs.microsoft.com/aspnet/core/
- **Entity Framework Core**: https://docs.microsoft.com/ef/core/
- **NuGet**: https://www.nuget.org/
- **GitHub Issues**: Submit issues to the project repository

---

## See Also

- [Root README](../../README.md) - Complete EPMC documentation
- [GLOSSARY](../../GLOSSARY.md) - Domain terminology
- [API Specification](../../README.md#api-documentation) - API documentation
- [Architecture](../../README.md#architecture) - System design

---

**Last Updated**: November 10, 2026  
**Maintained By**: Development Team
