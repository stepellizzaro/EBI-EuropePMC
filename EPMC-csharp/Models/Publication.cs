namespace EPMC.Models;
public record Publication(string Id, string Title, string? Authors = null, string? Journal = null, string? Url = null, DateTime? PublicationDate = null);
