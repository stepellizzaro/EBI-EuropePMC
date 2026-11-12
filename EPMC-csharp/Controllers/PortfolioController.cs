namespace EPMC.Controllers;
using EPMC.Models;
using EPMC.Services.Interfaces;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class PortfolioController : ControllerBase
{
    private readonly IPortfolioService _portfolioService;
    private readonly ILogger<PortfolioController> _logger;

    public PortfolioController(IPortfolioService portfolioService, ILogger<PortfolioController> logger)
    {
        _portfolioService = portfolioService;
        _logger = logger;
    }

    [HttpGet("institution-portfolio")]
    public async Task<ActionResult<GrantPortfolio>> GetInstitutionPortfolio([FromQuery] string institution, [FromQuery] int? limit = null, [FromQuery] int? publicationsPerGrant = null, CancellationToken cancellationToken = default)
    {
        _logger.LogInformation("Portfolio request for: {Institution}", institution);
        var request = new PortfolioRequest(institution, limit, publicationsPerGrant);
        var portfolio = await _portfolioService.GetPortfolioAsync(request, cancellationToken);
        return Ok(portfolio);
    }
}
