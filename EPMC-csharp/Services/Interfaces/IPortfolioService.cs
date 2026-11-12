namespace EPMC.Services.Interfaces;
using EPMC.Models;
public interface IPortfolioService
{
    Task<GrantPortfolio> GetPortfolioAsync(PortfolioRequest request, CancellationToken cancellationToken = default);
}
