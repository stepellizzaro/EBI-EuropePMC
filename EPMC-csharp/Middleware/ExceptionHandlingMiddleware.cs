namespace EPMC.Middleware;
using EPMC.Exceptions;

public class ExceptionHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionHandlingMiddleware> _logger;

    public ExceptionHandlingMiddleware(RequestDelegate next, ILogger<ExceptionHandlingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try { await _next(context); }
        catch (Exception ex) { await HandleExceptionAsync(context, ex); }
    }

    private static Task HandleExceptionAsync(HttpContext context, Exception exception)
    {
        context.Response.ContentType = "application/json";
        var response = new { error = exception.Message };
        int statusCode = exception switch
        {
            ValidationException => StatusCodes.Status400BadRequest,
            RequestTimeoutException => StatusCodes.Status504GatewayTimeout,
            ExternalApiException => StatusCodes.Status502BadGateway,
            _ => StatusCodes.Status500InternalServerError,
        };
        context.Response.StatusCode = statusCode;
        return context.Response.WriteAsJsonAsync(response);
    }
}
