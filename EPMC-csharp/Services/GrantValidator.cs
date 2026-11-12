namespace EPMC.Services;
using EPMC.Config;
using EPMC.Exceptions;
using EPMC.Models;

public class GrantValidator
{
    private readonly PortfolioConfiguration _config;
    private readonly ILogger<GrantValidator> _logger;

    public GrantValidator(PortfolioConfiguration config, ILogger<GrantValidator> logger)
    {
        _config = config;
        _logger = logger;
    }

    public void ValidateRequest(PortfolioRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Institution))
            throw new ValidationException("Institution name cannot be empty");

        if (request.Institution.Length > 255)
            throw new ValidationException("Institution name exceeds maximum length of 255 characters");

        var limit = request.Limit ?? _config.MaxGrants;
        if (limit > _config.MaxGrants)
            throw new ValidationException($"Limit cannot exceed maximum of {_config.MaxGrants}");

        var pubsPerGrant = request.PublicationsPerGrant ?? _config.MaxPublicationsPerGrant;
        if (pubsPerGrant > _config.MaxPublicationsPerGrant)
            throw new ValidationException($"PublicationsPerGrant cannot exceed maximum of {_config.MaxPublicationsPerGrant}");
    }

    public bool IsValidGrant(Grant grant)
    {
        return !string.IsNullOrWhiteSpace(grant.Id) &&
               !string.IsNullOrWhiteSpace(grant.Title) &&
               !string.IsNullOrWhiteSpace(grant.Funder);
    }
}
