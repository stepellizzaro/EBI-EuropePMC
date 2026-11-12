namespace EPMC.Services.Interfaces;
using EPMC.Models;
public interface IGristApiClient
{
    Task<List<Grant>> GetGrantsAsync(string institution, int limit, CancellationToken cancellationToken = default);
}
