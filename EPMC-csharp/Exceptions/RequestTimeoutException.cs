namespace EPMC.Exceptions;
public class RequestTimeoutException : ExternalApiException
{
    public RequestTimeoutException(string message) : base(message) { }
}
