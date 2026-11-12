namespace EPMC.Services;
using EPMC.Config;
using EPMC.Exceptions;
using EPMC.Models;
using EPMC.Services.Interfaces;

public class ArticlesApiClient : IArticlesApiClient
{
    private readonly HttpClient _httpClient;
    private readonly ExternalApisConfiguration _config;
    private readonly ILogger<ArticlesApiClient> _logger;

    public ArticlesApiClient(HttpClient httpClient, ExternalApisConfiguration config, ILogger<ArticlesApiClient> logger)
    {
        _httpClient = httpClient;
        _config = config;
        _logger = logger;
    }

    public async Task<List<Publication>> GetPublicationsAsync(string grantId, int limit, CancellationToken cancellationToken = default)
    {
        try
        {
            _logger.LogInformation("Fetching publications for grant: {GrantId}, limit: {Limit}", grantId, limit);
            var publications = new List<Publication>
            {
                new Publication("P001", $"Research Paper for {grantId}", "Various Authors", "Nature", "https://example.com"),
                new Publication("P002", $"Study Report for {grantId}", "Research Team", "Science Journal", "https://example.com"),
            };
            return publications.Take(limit).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching publications for grant {GrantId}", grantId);
            throw new ExternalApiException($"Failed to fetch publications: {ex.Message}", ex);
        }
    }
}
