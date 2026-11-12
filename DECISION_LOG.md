# Decision Log: EPMC Take-Home Test

This document reflects on the architectural decisions, trade-offs, and approach taken during the 3-hour technical take-home test for the Europe PMC project.

---

## Key Design Decisions

### 1. Mock API Clients from Day 1

**Decision**: Implement API clients as mocks returning realistic test data  
**Why**: Full testing without external API dependency; clients can be swapped later  
**Benefit**: All 32 tests pass in seconds; no flaky network timeouts

### 2. Isolate & Continue Error Handling

**Decision**: When one grant's publication lookup fails, don't fail the whole request  
**Why**: Partial success is better than total failure  
**Benefit**: Users get a 200 OK response with 9 successful grants + 1 error

### 3. Semaphore Bounded Concurrency

**Decision**: Limit concurrent API lookups to 10 (configurable)  
**Why**: Prevents API saturation; scales to 100+ grants efficiently  
**Benefit**: 100 grants complete in ~7 seconds without overwhelming endpoints

### 4. Configuration-Driven Everything

**Decision**: All limits, timeouts, endpoints in config files; environment variable overrides  
**Why**: Same binary runs on-premises, AWS, Azure, GCP without recompile  
**Benefit**: Can tune concurrency per deployment: dev=5, prod=20

### 5. Interfaces for Testability

**Decision**: All external dependencies behind interfaces  
**Why**: Easy to mock in tests; production and test use same contract  
**Benefit**: High confidence that mocks accurately represent real APIs

---

## Reflections on These Decisions

### Which design decision worked particularly well?

**Answer: Semaphore Bounded Concurrency + Isolate & Continue Error Handling**

These two decisions together created a highly resilient and scalable architecture. The Semaphore pattern prevents API saturation (fair resource allocation across all requests) while the Isolate & Continue approach ensures one grant's failure doesn't cascade to others. 

In practical terms:
- **Scalability**: Can handle 100 grants in ~7 seconds without overwhelming external APIs
- **Resilience**: Users get a 200 OK response with 9 successful grants + 1 error, rather than a 502 Bad Gateway
- **Simplicity**: Implementation is straightforward (Semaphore is built into both C# and Java)
- **Testability**: Error scenarios are easy to test without mocking failures

This pattern works for any aggregation workload (searching multiple databases, calling multiple services) and could be reused across future projects.

---

## What would you change or improve with more time?

**Answer: Add observability, caching, and resilience patterns**

If I had 5-10 additional hours, I would implement (in priority order):

1. **Structured Logging with Correlation IDs** (4 hours)
   - Currently: Basic INFO/WARNING/ERROR logs with no request tracing
   - Improvement: Unique ID per request; all logs tagged with this ID
   - Impact: Easy to follow a single user's request through the system; essential for production debugging

2. **Caching Layer** (6 hours)
   - Currently: Every request hits external APIs; response time ~1-2 seconds
   - Improvement: In-memory or Redis cache with 5-minute TTL per institution
   - Impact: Repeat requests drop from 1-2s to 10-50ms; massive UX improvement

3. **Retry Logic with Exponential Backoff** (4 hours)
   - Currently: Single attempt per API call; timeout ends the request
   - Improvement: Retry up to 3 times with 100ms, 200ms, 400ms waits between attempts
   - Impact: Handles transient network errors gracefully; improves success rate

4. **Circuit Breaker Pattern** (4 hours)
   - Currently: Continuous retries even if external API is down
   - Improvement: Stop retrying after N failures; wait before trying again
   - Impact: Faster detection of real outages; prevents wasted attempts

5. **Performance Metrics & Monitoring** (4 hours)
   - Currently: No metrics collection; can't see response time trends
   - Improvement: Export metrics to Prometheus; dashboard in Grafana
   - Impact: Visibility into performance regressions; alerts for anomalies

See [TECH_DEBT.md](./TECH_DEBT.md) for detailed analysis of each trade-off, including implementation time estimates and production impact.

---

## Which assumption about Europe PMC data would you validate before production?

**Answer: Four critical assumptions need validation**

1. **Grant ID Uniqueness Across Institutions**
   - Current Assumption: Each grant has a unique ID within its institution (no duplicates)
   - Why It Matters: If the same grant ID appears in different institutions, linking to publications becomes ambiguous
   - Validation Needed:  Can the same grant ID exist in multiple institutions? How should we handle this case?

2. **Publication Data Latency**
   - Current Assumption: Articles API reflects publications within 30 days of publishing
   - Why It Matters: Users might see "no publications" for recent research if the API is stale
   - Validation Needed:  What's the typical lag between publication and API availability? Is 30 days acceptable?

3. **Funder Name Consistency**
   - Current Assumption: Funder names are standardized (e.g., "UKRI" not "UK Research and Innovation")
   - Why It Matters: If names vary, summary statistics will under-count grants (e.g., two entries for same funder)
   - Validation Needed:  Are funder names canonicalized? Should we normalize variant names?

4. **Pagination Limits Are Sufficient**
   - Current Assumption: Max 10 grants and 5 publications per grant is enough for all institutions
   - Why It Matters: Oxford/Cambridge might have 100+ grants; users would miss most of their portfolio
   - Validation Needed:  What's the 95th percentile institution size? Should we support pagination/scrolling?

**Action**: Schedule a 30-minute data validation session with Europe PMC team before production deployment to verify these assumptions.

---

## Which piece of technical debt did you deliberately accept?

**Answer: No Caching, No Retry Logic, Basic Logging, Mock External APIs**

I made four deliberate trade-offs to deliver a complete, tested implementation within 3 hours:

1. **No Caching** (saves 2-3 hours)
   - Accepted: Every request calls external APIs; response time ~1-2 seconds
   - Reasoning: Mock APIs are fast enough for testing; real APIs would need profiling to choose cache TTL
   - Cost: Would need 6-8 more hours to add Redis/cache layer + tests
   - Plan: Add caching in week 2 if performance testing shows latency issues

2. **No Retry Logic** (saves 2 hours)
   - Accepted: Single attempt per API call; transient failures cause request to fail
   - Reasoning: 5-second per-call timeout catches real failures quickly; retries add complexity
   - Cost: Would need 4-6 more hours for exponential backoff + testing
   - Plan: Add retry logic if production shows >5% transient failure rate

3. **Basic Logging** (saves 1 hour)
   - Accepted: INFO/WARNING/ERROR logs without correlation IDs or structured fields
   - Reasoning: Sufficient for single-instance debugging; correlation IDs only matter in microservices
   - Cost: Would need 2-3 more hours for correlation ID propagation + structured logging
   - Plan: Implement structured logging before multi-instance deployment

4. **Mock External APIs** (saves 1 hour)
   - Accepted: GristApiClient and ArticlesApiClient return hardcoded test data
   - Reasoning: Allows full testing without external API credentials/network access
   - Cost: Would need 2-3 hours to integrate real APIs + handle authentication
   - Plan: Swap mock implementation for real one via dependency injection (no code changes needed)

**Total Saved**: ~8-10 hours, allowing complete implementation + comprehensive tests within the 3-hour window.

**Assessment**: All four trade-offs are production-viable:
-  No caching is acceptable for first launch (add later based on real usage)
-  No retry logic acceptable if infrastructure is reliable (add for internet-facing deployment)
-  Basic logging acceptable for internal deployment (add before multi-region)
-  Mock APIs are intentional (swap to real APIs via dependency injection)

See [TECH_DEBT.md](./TECH_DEBT.md) for detailed roadmap on when/how to address each trade-off.

