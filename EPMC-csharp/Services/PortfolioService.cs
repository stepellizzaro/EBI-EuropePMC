namespace EPMC.Services;
using EPMC.Config;
using EPMC.Exceptions;
using EPMC.Models;
using EPMC.Services.Interfaces;

public class PortfolioService : IPortfolioService
{
    private readonly IGristApiClient _gristClient;
    private readonly IArticlesApiClient _articlesClient;
    private readonly GrantValidator _validator;
    private readonly PortfolioBuilder _builder;
    private readonly PortfolioConfiguration _config;
    private readonly ILogger<PortfolioService> _logger;

    public PortfolioService(IGristApiClient gristClient, IArticlesApiClient articlesClient, GrantValidator validator, PortfolioBuilder builder, PortfolioConfiguration config, ILogger<PortfolioService> logger)
    {
        _gristClient = gristClient;
        _articlesClient = articlesClient;
        _validator = validator;
        _builder = builder;
        _config = config;
        _logger = logger;
    }

    public async Task<GrantPortfolio> GetPortfolioAsync(PortfolioRequest request, CancellationToken cancellationToken = default)
    {
        _validator.ValidateRequest(request);

        var limit = request.Limit ?? _config.MaxGrants;
        var pubsPerGrant = request.PublicationsPerGrant ?? _config.MaxPublicationsPerGrant;

        try
        {
            _logger.LogInformation("Fetching portfolio for institution: {Institution}", request.Institution);
            var grants = await _gristClient.GetGrantsAsync(request.Institution, limit, cancellationToken);

            using var semaphore = new SemaphoreSlim(_config.MaxConcurrentLookups);
            var publicationTasks = grants.Select(async grant =>
            {
                await semaphore.WaitAsync(cancellationToken);
                try
                {
                    var publications = await _articlesClient.GetPublicationsAsync(grant.Id, pubsPerGrant, cancellationToken);
                    return new GrantWithPublications(grant, publications, publications.Any() ? "found" : "not_found");
                }
                catch (Exception ex)
                {
                    _logger.LogWarning(ex, "Failed to fetch publications for grant {GrantId}", grant.Id);
                    return new GrantWithPublications(grant, new List<Publication>(), "error", ex.Message);
                }
                finally { semaphore.Release(); }
            });

            var grantsWithPubs = await Task.WhenAll(publicationTasks);
            var institution = new Institution(request.Institution);
            return _builder.Build(institution, grantsWithPubs.ToList());
        }
        catch (ValidationException) { throw; }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Unexpected error fetching portfolio");
            throw new ExternalApiException("Failed to fetch portfolio", ex);
        }
    }
}
