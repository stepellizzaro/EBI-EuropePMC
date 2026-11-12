using EPMC.Config;
using EPMC.Exceptions;
using EPMC.Middleware;
using EPMC.Services;
using EPMC.Services.Interfaces;

var builder = WebApplication.CreateBuilder(args);

var portfolioConfig = builder.Configuration.GetSection("Portfolio").Get<PortfolioConfiguration>() ?? new();
var externalApisConfig = builder.Configuration.GetSection("ExternalApis").Get<ExternalApisConfiguration>() ?? new();

builder.Services.AddSingleton(portfolioConfig);
builder.Services.AddSingleton(externalApisConfig);
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

builder.Services.AddHttpClient<IGristApiClient, GristApiClient>();
builder.Services.AddHttpClient<IArticlesApiClient, ArticlesApiClient>();
builder.Services.AddScoped<GrantValidator>();
builder.Services.AddScoped<PortfolioBuilder>();
builder.Services.AddScoped<IPortfolioService, PortfolioService>();

var app = builder.Build();

app.UseMiddleware<ExceptionHandlingMiddleware>();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
    // Don't redirect HTTP to HTTPS in development
}
else
{
    app.UseHttpsRedirection();
}

app.UseAuthorization();
app.MapControllers();
app.Run();