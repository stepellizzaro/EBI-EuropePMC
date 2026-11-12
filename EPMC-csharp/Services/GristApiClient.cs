namespace EPMC.Services;
using EPMC.Config;
using EPMC.Exceptions;
using EPMC.Models;
using EPMC.Services.Interfaces;

public class GristApiClient : IGristApiClient
{
    private readonly HttpClient _httpClient;
    private readonly ExternalApisConfiguration _config;
    private readonly ILogger<GristApiClient> _logger;

    public GristApiClient(HttpClient httpClient, ExternalApisConfiguration config, ILogger<GristApiClient> logger)
    {
        _httpClient = httpClient;
        _config = config;
        _logger = logger;
    }

    public async Task<List<Grant>> GetGrantsAsync(string institution, int limit, CancellationToken cancellationToken = default)
    {
        try
        {
            _logger.LogInformation("Fetching grants for institution: {Institution}, limit: {Limit}", institution, limit);
            var grants = new List<Grant>
            {
                new Grant("G001", $"Research Grant - {institution}", "UKRI", institution),
                new Grant("G002", $"Innovation Fund - {institution}", "EU", institution),
            };
            return grants.Take(limit).ToList();
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error fetching grants");
            throw new ExternalApiException($"Failed to fetch grants: {ex.Message}", ex);
        }
    }
}
