namespace EPMC.Config;
public class PortfolioConfiguration
{
    public int MaxGrants { get; set; } = 10;
    public int MaxPublicationsPerGrant { get; set; } = 5;
    public int PerCallTimeoutSeconds { get; set; } = 5;
    public int OverallTimeoutSeconds { get; set; } = 30;
    public int MaxConcurrentLookups { get; set; } = 10;
    public int RetryMaxAttempts { get; set; } = 3;
    public int RetryBackoffMs { get; set; } = 100;
}
