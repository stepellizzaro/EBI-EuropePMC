namespace EPMC.Models;
public record GrantWithPublications(Grant Grant, List<Publication> Publications, string PublicationStatus, string? ErrorMessage = null);
