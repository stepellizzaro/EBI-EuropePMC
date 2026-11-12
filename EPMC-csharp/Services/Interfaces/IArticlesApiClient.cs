namespace EPMC.Services.Interfaces;
using EPMC.Models;
public interface IArticlesApiClient
{
    Task<List<Publication>> GetPublicationsAsync(string grantId, int limit, CancellationToken cancellationToken = default);
}
