using Xunit;
using EPMC.Exceptions;
using EPMC.Services;
using EPMC.Config;
using EPMC.Models;
using Moq;
using Microsoft.Extensions.Logging;

namespace EPMC.Tests.Unit;

public class GrantValidatorTests
{
    private readonly GrantValidator _validator;
    private readonly Mock<ILogger<GrantValidator>> _loggerMock;

    public GrantValidatorTests()
    {
        _loggerMock = new Mock<ILogger<GrantValidator>>();
        var config = new PortfolioConfiguration
        {
            MaxGrants = 10,
            MaxPublicationsPerGrant = 5,
            PerCallTimeoutSeconds = 5,
            OverallTimeoutSeconds = 30,
            MaxConcurrentLookups = 10,
            RetryMaxAttempts = 3,
            RetryBackoffMs = 100
        };
        _validator = new GrantValidator(config, _loggerMock.Object);
    }

    #region Happy Path Tests
    
    [Fact]
    public void ValidateRequest_WithValidRequest_ShouldNotThrow()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", 5, 3);

        // Act & Assert - no exception should be thrown
        _validator.ValidateRequest(request);
    }

    [Theory]
    [InlineData("Cambridge")]
    [InlineData("Manchester")]
    [InlineData("Oxford")]
    [InlineData("A")]
    public void ValidateRequest_WithVariousValidInstitutions_ShouldNotThrow(string institution)
    {
        // Arrange
        var request = new PortfolioRequest(institution, 10, 5);

        // Act & Assert
        _validator.ValidateRequest(request);
    }

    [Fact]
    public void ValidateRequest_WithNullLimitAndPublications_ShouldUseDefaults()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", null, null);

        // Act & Assert - should not throw since defaults are within limits
        _validator.ValidateRequest(request);
    }

    #endregion

    #region Institution Validation Tests

    [Fact]
    public void ValidateRequest_WithNullInstitution_ShouldThrowValidationException()
    {
        // Arrange
        var request = new PortfolioRequest(null!, 10, 5);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("institution", ex.Message.ToLower());
    }

    [Fact]
    public void ValidateRequest_WithEmptyInstitution_ShouldThrowValidationException()
    {
        // Arrange
        var request = new PortfolioRequest(string.Empty, 10, 5);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("institution", ex.Message.ToLower());
    }

    [Fact]
    public void ValidateRequest_WithWhitespaceOnlyInstitution_ShouldThrowValidationException()
    {
        // Arrange
        var request = new PortfolioRequest("   ", 10, 5);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("institution", ex.Message.ToLower());
    }

    [Fact]
    public void ValidateRequest_WithInstitutionExceedingMaxLength_ShouldThrowValidationException()
    {
        // Arrange - institution with 256+ characters (exceeds typical max)
        var longInstitution = new string('A', 300);
        var request = new PortfolioRequest(longInstitution, 10, 5);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("institution", ex.Message.ToLower());
    }

    #endregion

    #region Limit Validation Tests

    [Theory]
    [InlineData(11)]
    [InlineData(20)]
    [InlineData(100)]
    public void ValidateRequest_WithLimitExceedingMaximum_ShouldThrowValidationException(int limit)
    {
        // Arrange - MaxGrants is 10
        var request = new PortfolioRequest("Cambridge", limit, 5);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("limit", ex.Message.ToLower());
    }

    [Fact]
    public void ValidateRequest_WithLimitAtMaximum_ShouldNotThrow()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", 10, 5);

        // Act & Assert
        _validator.ValidateRequest(request);
    }

    [Fact]
    public void ValidateRequest_WithLimitBelowMaximum_ShouldNotThrow()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", 1, 5);

        // Act & Assert
        _validator.ValidateRequest(request);
    }

    #endregion

    #region PublicationsPerGrant Validation Tests

    [Theory]
    [InlineData(6)]
    [InlineData(10)]
    [InlineData(100)]
    public void ValidateRequest_WithPublicationsExceedingMaximum_ShouldThrowValidationException(int publications)
    {
        // Arrange - MaxPublicationsPerGrant is 5
        var request = new PortfolioRequest("Cambridge", 10, publications);

        // Act & Assert
        var ex = Assert.Throws<ValidationException>(() => _validator.ValidateRequest(request));
        Assert.Contains("publications", ex.Message.ToLower());
    }

    [Fact]
    public void ValidateRequest_WithPublicationsAtMaximum_ShouldNotThrow()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", 10, 5);

        // Act & Assert
        _validator.ValidateRequest(request);
    }

    [Fact]
    public void ValidateRequest_WithPublicationsBelowMaximum_ShouldNotThrow()
    {
        // Arrange
        var request = new PortfolioRequest("Cambridge", 10, 1);

        // Act & Assert
        _validator.ValidateRequest(request);
    }

    #endregion

    #region IsValidGrant Tests

    [Fact]
    public void IsValidGrant_WithCompleteGrant_ShouldReturnTrue()
    {
        // Arrange
        var grant = new Grant("G001", "Research Grant", "UKRI", "Cambridge", "https://example.com");

        // Act
        var result = _validator.IsValidGrant(grant);

        // Assert
        Assert.True(result);
    }

    [Fact]
    public void IsValidGrant_WithMissingId_ShouldReturnFalse()
    {
        // Arrange
        var grant = new Grant(null, "Research Grant", "UKRI", "Cambridge", null);

        // Act
        var result = _validator.IsValidGrant(grant);

        // Assert
        Assert.False(result);
    }

    [Fact]
    public void IsValidGrant_WithMissingTitle_ShouldReturnFalse()
    {
        // Arrange
        var grant = new Grant("G001", null, "UKRI", "Cambridge", null);

        // Act
        var result = _validator.IsValidGrant(grant);

        // Assert
        Assert.False(result);
    }

    [Fact]
    public void IsValidGrant_WithMissingFunder_ShouldReturnFalse()
    {
        // Arrange
        var grant = new Grant("G001", "Research Grant", null, "Cambridge", null);

        // Act
        var result = _validator.IsValidGrant(grant);

        // Assert
        Assert.False(result);
    }

    [Fact]
    public void IsValidGrant_WithEmptyId_ShouldReturnFalse()
    {
        // Arrange
        var grant = new Grant(string.Empty, "Research Grant", "UKRI", "Cambridge", null);

        // Act
        var result = _validator.IsValidGrant(grant);

        // Assert
        Assert.False(result);
    }

    #endregion
}
