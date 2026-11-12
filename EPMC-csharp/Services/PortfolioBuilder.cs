namespace EPMC.Services;
using EPMC.Models;

public class PortfolioBuilder
{
    public GrantPortfolio Build(Institution institution, List<GrantWithPublications> grants)
    {
        var summary = new PortfolioSummary(
            TotalGrants: grants.Count,
            TotalPublications: grants.Sum(g => g.Publications.Count),
            FunderCounts: grants.GroupBy(g => g.Grant.Funder).ToDictionary(g => g.Key, g => g.Count()),
            SuccessfulGrants: grants.Count(g => g.PublicationStatus == "found"),
            PartialFailureGrants: grants.Count(g => g.PublicationStatus == "partial"),
            FailedGrants: grants.Count(g => g.PublicationStatus == "error")
        );
        return new GrantPortfolio(institution, grants, summary);
    }
}
