namespace CarRacerAPI.Middleware;

public class ApiKeyMiddleware
{
    private readonly RequestDelegate _next;
    private const string API_KEY_HEADER_NAME = "X-Api-Key";

    public ApiKeyMiddleware(RequestDelegate next)
    {
        _next = next;
    }

    public async Task InvokeAsync(HttpContext context, IConfiguration configuration)
    {
        if (!context.Request.Headers.TryGetValue(API_KEY_HEADER_NAME, out var extractedApiKey))
        {
            context.Response.StatusCode = 401; 
            await context.Response.WriteAsync("API Key was not provided.");
            return;
        }

        var actualApiKey = configuration["ApiKey"];

        if (string.IsNullOrEmpty(actualApiKey) || !actualApiKey.Equals(extractedApiKey))
        {
            context.Response.StatusCode = 401; 
            await context.Response.WriteAsync("Invalid API Key.");
            return;
        }

        await _next(context);
    }
}