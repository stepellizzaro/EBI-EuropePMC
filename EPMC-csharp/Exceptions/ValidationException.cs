namespace EPMC.Exceptions;
public class ValidationException : ExternalApiException
{
    public ValidationException(string message) : base(message) { }
}
