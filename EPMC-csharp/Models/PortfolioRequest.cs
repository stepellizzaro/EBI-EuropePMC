namespace EPMC.Models;
public record PortfolioRequest(string Institution, int? Limit = null, int? PublicationsPerGrant = null);
