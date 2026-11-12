# SOLID Principles in EPMC

Both EPMC implementations (C# and Java) demonstrate all 5 SOLID principles through their architecture.

---

## S - Single Responsibility

Each class has ONE reason to change:

| Class | Responsibility |
|-------|---|
| `GrantValidator` | Input validation only |
| `PortfolioBuilder` | Aggregating statistics only |
| `PortfolioService` | Orchestrating fetch/link/error handling |
| `GristApiClient` | Fetching grants only |
| `ArticlesApiClient` | Fetching publications only |

**Why it matters**: When validation rules change, you modify only `GrantValidator`. When aggregation logic changes, you modify only `PortfolioBuilder`. Changes are localized and predictable.

---

## O - Open/Closed

**Open for extension, closed for modification**

- Interfaces (`IGristApiClient`, `IPortfolioService`) allow new implementations without changing existing code
- Mock implementations in tests work without changing production code
- New exception types can be mapped without modifying the exception handler
- New business logic can be layered on top without touching the core

**Why it matters**: You can add features (new API clients, new validators) without touching existing code. Reduces regression risk.

---

## L - Liskov Substitution

**Implementations honor their contracts**

- Mock API clients behave identically to real ones
- Tests pass with mocks; would pass identically with real APIs (if they existed)
- All implementations are drop-in replacements for their interfaces

Example: `IGristApiClient` contract is honored by:
- Production `GristApiClient` (calls real APIs)
- Test `MockGristApiClient` (returns test data)

Both satisfy the contract: "Given an institution and limit, return a list of grants."

**Why it matters**: You can swap implementations with confidence. If tests pass with mocks, they'll pass with real APIs (assuming the real API contract matches the mock contract).

---

## I - Interface Segregation

**Clients depend only on methods they use**

- `PortfolioService` doesn't inherit bloated base classes
- Controllers don't see database internals
- Services have focused, minimal interfaces

Example: `PortfolioService` depends on:
- `IGristApiClient` (only needs `GetGrantsAsync`)
- `IArticlesApiClient` (only needs `GetPublicationsAsync`)

NOT on a bloated `IExternalApiClient` that has methods for searching, filtering, caching, etc. that it doesn't use.

**Why it matters**: Interfaces are small and focused. Mocking is easier. Dependencies are clear. Changes to one API don't force changes to unrelated code.

---

## D - Dependency Inversion

**High-level modules depend on abstractions, not concrete implementations**

```csharp
// GOOD: Depends on interfaces (abstractions)
public class PortfolioService
{
    public PortfolioService(
        IGristApiClient gristClient,        // Abstract
        IArticlesApiClient articlesClient,  // Abstract
        GrantValidator validator,           // Injected, not new'd
        PortfolioConfiguration config       // Injected, not new'd
    ) { ... }
}

// BAD: Would depend on concrete implementations
public class PortfolioService
{
    private GristApiClient gristClient = new GristApiClient();  // Hard dependency
    private ArticlesApiClient articlesClient = new ArticlesApiClient();  // Hard dependency
}
```

**Why it matters**: 
- `PortfolioService` doesn't create its dependencies; it receives them
- In tests, you inject mocks
- In production, you inject real implementations
- `PortfolioService` doesn't care which is which—it just uses the interface

---

## How It All Works Together

```
1. PortfolioController receives HTTP request
        ↓
2. Calls PortfolioService.GetPortfolioAsync(request)
        ↓
3. PortfolioService (orchestration layer)
   - Validates request via GrantValidator
   - Fetches grants via IGristApiClient (could be mock or real)
   - Fetches publications via IArticlesApiClient (could be mock or real)
   - Aggregates results via PortfolioBuilder
   - Returns GrantPortfolio
        ↓
4. Controller returns HTTP 200 with JSON response
```

Each layer has one responsibility. Each depends on abstractions. Each can be tested in isolation with mocks. Each honors the contract its interface defines.

---

## Testing Benefit

Because of SOLID principles, testing is straightforward:

```csharp
// Unit test
[Fact]
public async Task GetPortfolioAsync_ValidRequest_ReturnsPortfolio()
{
    // Arrange: Inject mocks
    var mockGristClient = new MockGristApiClient();
    var mockArticlesClient = new MockArticlesApiClient();
    var validator = new GrantValidator();
    var builder = new PortfolioBuilder();
    var service = new PortfolioService(mockGristClient, mockArticlesClient, validator, builder, config);

    // Act
    var result = await service.GetPortfolioAsync(new PortfolioRequest("Cambridge"));

    // Assert
    Assert.NotNull(result);
    Assert.True(result.Grants.Count > 0);
}
```

The mocks satisfy the same interfaces as production implementations. The test is completely isolated from external APIs, databases, or network calls.

---

## See Also

- [README.md](./README.md) — Overall architecture
- [DECISION_LOG.md](./DECISION_LOG.md) — Why these patterns were chosen
- [EPMC-csharp/](./EPMC-csharp/) — C# implementation
- [EPMC-java/](./EPMC-java/) — Java implementation
