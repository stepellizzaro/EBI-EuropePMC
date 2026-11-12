namespace EPMC.Models;
public record GrantPortfolio(Institution Institution, List<GrantWithPublications> Grants, PortfolioSummary Summary);
public record PortfolioSummary(int TotalGrants, int TotalPublications, Dictionary<string, int> FunderCounts, int SuccessfulGrants, int PartialFailureGrants, int FailedGrants);
