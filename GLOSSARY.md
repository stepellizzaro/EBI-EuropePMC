# EPMC Domain Glossary

This document defines the core domain terminology used across the EPMC project. 
All team members should use these terms consistently.

---

## Domain Entities

### Institution

**Definition**: A university, research center, or other organization that receives research funding.

**Examples**: University of Cambridge, MIT, Oxford University, Max Planck Institute

**Key Properties**:
- Name (unique identifier within the system)
- Country (optional)

**Usage**: "The _institution_ name is used to match grants from the Grist API."

---

### Grant

**Definition**: A funding award from a _funder_ to an _institution_, typically for a specific research project or time period.

**Key Properties**:
- **ID** (required): Unique identifier from the Grist API
- **Title** (required): Name/description of the grant
- **Funder** (required): Organization providing the funding (e.g., UKRI, NSF, EU)
- **Institution**: Name of the receiving institution
- **Link** (optional): URL to grant details on funder's website

**Examples**:
- ID: "G001", Title: "AI Research Initiative", Funder: "UKRI", Institution: "Cambridge"
- ID: "G042", Title: "Climate Change Mitigation Study", Funder: "NSF", Institution: "MIT"

**Relationship**: A _grant_ is associated with one or more _publications_.

**Usage**: "The API returns a list of _grants_ matching the queried _institution_."

---

### Funder

**Definition**: An organization that provides research funding to institutions.

**Examples**: 
- **UKRI** (UK Research and Innovation)
- **NSF** (US National Science Foundation)
- **EU** (European Union Horizon programs)
- **DARPA** (US Defense Advanced Research Projects Agency)
- **NIH** (US National Institutes of Health)
- **Leverhulme Trust** (UK charitable trust)

**Key Properties**:
- Name (unique)
- Country/Region (optional)

**Usage**: "Summary counts are aggregated by _funder_ to show funding distribution."

---

### Publication

**Definition**: A peer-reviewed scientific paper, article, or other research output that is associated with one or more _grants_.

**Key Properties**:
- **ID** (required): Unique identifier from the Articles API
- **Title** (required): Article title
- **Authors** (optional): List of author names
- **Journal** (optional): Name of the journal/venue
- **URL** (optional): Link to the publication
- **Publication Date** (optional): Date of publication

**Examples**:
- Title: "Advances in Machine Learning", Journal: "Nature", Authors: "Smith, J., et al."
- Title: "Climate Models for 2050", Journal: "Science", Authors: "Johnson, M., et al."

**Relationship**: A _publication_ is linked to one or more _grants_ (many-to-many).

**Usage**: "The API returns _publications_ linked to each _grant_ for the queried _institution_."

---

### Publication Status

**Definition**: Indicates whether the system successfully retrieved publications for a given _grant_.

**Possible Values**:

| Status | Meaning | When It Occurs |
|--------|---------|---|
| **FOUND** | Publications were successfully retrieved | One or more publications linked to the grant |
| **NOT_FOUND** | No publications found for the grant | Grant exists but has no linked publications |
| **ERROR** | Lookup failed (exception, timeout, API error) | External API failed or timed out |
| **TIMEOUT** | Request exceeded time limit | Per-call timeout (5s) or overall timeout (30s) |

**Usage**: "Each _grant_ in the response includes a _publication status_ indicating lookup success."

---

### Portfolio

**Definition**: A complete, aggregated view of all _grants_ and linked _publications_ for a single _institution_.

**Key Components**:
- **Institution**: The institution being queried
- **Grants**: List of _GrantWithPublications_ (one per grant found)
- **Summary**: Aggregated statistics

**Usage**: "The API returns a _portfolio_ containing grants and publications for the queried institution."

---

### GrantWithPublications

**Definition**: A composite object linking a single _grant_ with its associated _publications_ and metadata.

**Key Properties**:
- **Grant**: The grant details (ID, title, funder, institution, link)
- **Publications**: List of associated _publication_ objects
- **PublicationStatus**: Indicates success/failure of publication lookup
- **ErrorMessage**: Human-readable error description (if status = ERROR)

**Usage**: "The response contains an array of _GrantWithPublications_ objects, one per grant."

---

### PortfolioSummary

**Definition**: Aggregated metrics and statistics for a _portfolio_.

**Key Properties**:
- **TotalGrants**: Count of grants in the portfolio
- **TotalPublications**: Count of all publications across all grants
- **FunderCounts**: Map of funder name → grant count
- **SuccessfulGrants**: Count of grants with publications found
- **PartialFailureGrants**: Count of grants where publication lookup failed
- **FailedGrants**: Count of grants that could not be fetched

**Example**:
```json
{
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
```

**Usage**: "The _summary_ provides quick insights into the portfolio composition."

---

## Related Documentation

- [README.md](./README.md) — Main project documentation
- [EPMC-csharp/docs/SETUP.md](./EPMC-csharp/docs/SETUP.md) — C# setup guide
- [EPMC-java/docs/SETUP.md](./EPMC-java/docs/SETUP.md) — Java setup guide

---

**Last Updated**: November 10, 2026  
**Version**: 1.0
