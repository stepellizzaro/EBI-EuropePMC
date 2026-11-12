using Xunit;
using EPMC.Models;
using EPMC.Services;
using EPMC.Services.Interfaces;
using EPMC.Config;
using EPMC.Exceptions;
using Moq;
using Microsoft.Extensions.Logging;

namespace EPMC.Tests.Integration;

public class PortfolioServiceIntegrationTests
{
    private readonly PortfolioService _service;
    private readonly Mock<IGristApiClient> _gristClientMock;
    private readonly Mock<IArticlesApiClient> _articlesClientMock;
    private readonly GrantValidator _validator;
    private readonly PortfolioBuilder _builder;
    private readonly PortfolioConfiguration _config;

    public PortfolioServiceIntegrationTests()
    {
        _gristClientMock = new Mock<IGristApiClient>();
        _articlesClientMock = new Mock<IArticlesApiClient>();

        var loggerValidator = new Mock<ILogger<GrantValidator>>();
        _config = new PortfolioConfiguration
        {
            MaxGrants = 10,
            MaxPublicationsPerGrant = 5,
            PerCallTimeoutSeconds = 5,
            OverallTimeoutSeconds = 30,
            MaxConcurrentLookups = 10,
            RetryMaxAttempts = 3,
            RetryBackoffMs = 100
        };
        _validator = new GrantValidator(_config, loggerValidator.Object);
        _builder = new PortfolioBuilder();

        var loggerService = new Mock<ILogger<PortfolioService>>();
        _service = new PortfolioService(_gristClientMock.Object, _articlesClientMock.Object, _validator, _builder, _config, loggerService.Object);
    }

    [Fact]
    public async Task GetPortfolioAsync_WithValidInstitution_ShouldReturnPortfolio()
    {
        // Arrange
        var grants = new List<Grant>
        {
            new("G001", "Grant 1", "UKRI", "Cambridge", null),
            new("G002", "Grant 2", "EU", "Cambridge", null)
        };

        _gristClientMock
            .Setup(x => x.GetGrantsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(grants);

        _articlesClientMock
            .Setup(x => x.GetPublicationsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Publication>
            {
                new("P001", "Paper", "Authors", "Journal", "https://example.com", null)
            });

        // Act
        var result = await _service.GetPortfolioAsync(new PortfolioRequest("Cambridge", 10, 5));

        // Assert
        Assert.NotNull(result);
        Assert.Equal(2, result.Grants.Count);
        Assert.Equal("Cambridge", result.Institution.Name);
        Assert.Equal(2, result.Summary.TotalGrants);
    }

    [Fact]
    public async Task GetPortfolioAsync_WithNoResults_ShouldReturnEmptyPortfolio()
    {
        // Arrange
        _gristClientMock
            .Setup(x => x.GetGrantsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Grant>());

        // Act
        var result = await _service.GetPortfolioAsync(new PortfolioRequest("UnknownInstitution", 10, 5));

        // Assert
        Assert.NotNull(result);
        Assert.Empty(result.Grants);
        Assert.Equal(0, result.Summary.TotalGrants);
    }

    [Fact]
    public async Task GetPortfolioAsync_WithPartialPublicationFailure_ShouldReturnPortfolioWithErrors()
    {
        // Arrange
        var grants = new List<Grant>
        {
            new("G001", "Grant 1", "UKRI", "Cambridge", null),
            new("G002", "Grant 2", "EU", "Cambridge", null),
            new("G003", "Grant 3", "Wellcome", "Cambridge", null)
        };

        _gristClientMock
            .Setup(x => x.GetGrantsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(grants);

        // First and third succeed, second throws exception
        _articlesClientMock
            .Setup(x => x.GetPublicationsAsync("G001", It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Publication>
            {
                new("P001", "Paper 1", "Authors", "Journal", "https://example.com", null)
            });

        _articlesClientMock
            .Setup(x => x.GetPublicationsAsync("G002", It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new Exception("API Error"));

        _articlesClientMock
            .Setup(x => x.GetPublicationsAsync("G003", It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Publication>
            {
                new("P003", "Paper 3", "Authors", "Journal", "https://example.com", null)
            });

        // Act
        var result = await _service.GetPortfolioAsync(new PortfolioRequest("Cambridge", 10, 5));

        // Assert
        Assert.Equal(3, result.Grants.Count);
        Assert.Equal(2, result.Summary.SuccessfulGrants);
        var failedGrant = result.Grants.FirstOrDefault(g => g.Grant.Id == "G002");
        Assert.NotNull(failedGrant);
        Assert.Equal("error", failedGrant.PublicationStatus);
    }

    [Fact]
    public async Task GetPortfolioAsync_WithValidationError_ShouldThrow()
    {
        // Act & Assert - empty institution should throw
        await Assert.ThrowsAsync<Exceptions.ValidationException>(() =>
            _service.GetPortfolioAsync(new PortfolioRequest(string.Empty, 10, 5)));
    }

    [Fact]
    public async Task GetPortfolioAsync_WithLimitExceeded_ShouldThrow()
    {
        // Act & Assert - limit > 10 should throw
        await Assert.ThrowsAsync<Exceptions.ValidationException>(() =>
            _service.GetPortfolioAsync(new PortfolioRequest("Cambridge", 15, 5)));
    }

    [Fact]
    public async Task GetPortfolioAsync_WithPublicationsPerGrantExceeded_ShouldThrow()
    {
        // Act & Assert - publicationsPerGrant > 5 should throw
        await Assert.ThrowsAsync<Exceptions.ValidationException>(() =>
            _service.GetPortfolioAsync(new PortfolioRequest("Cambridge", 10, 10)));
    }

    [Fact]
    public async Task GetPortfolioAsync_CalculatesSummaryCorrectly()
    {
        // Arrange
        var grants = new List<Grant>
        {
            new("G001", "Grant 1", "UKRI", "Cambridge", null),
            new("G002", "Grant 2", "UKRI", "Cambridge", null),
            new("G003", "Grant 3", "EU", "Cambridge", null)
        };

        _gristClientMock
            .Setup(x => x.GetGrantsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(grants);

        _articlesClientMock
            .Setup(x => x.GetPublicationsAsync(It.IsAny<string>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<Publication>
            {
                new("P1", "Paper 1", "Authors", "Journal", "https://example.com", null),
                new("P2", "Paper 2", "Authors", "Journal", "https://example.com", null)
            });

        // Act
        var result = await _service.GetPortfolioAsync(new PortfolioRequest("Cambridge", 10, 5));

        // Assert
        Assert.Equal(3, result.Summary.TotalGrants);
        Assert.Equal(6, result.Summary.TotalPublications); // 3 grants * 2 publications
        Assert.Equal(2, result.Summary.FunderCounts["UKRI"]);
        Assert.Equal(1, result.Summary.FunderCounts["EU"]);
        Assert.Equal(3, result.Summary.SuccessfulGrants);
    }
}
