FROM mcr.microsoft.com/dotnet/sdk:8.0 AS build
WORKDIR /src
COPY ["EPMC.csproj", "."]
RUN dotnet restore "./EPMC.csproj"
COPY . .
RUN dotnet build "./EPMC.csproj" -c Release -o /app/build

FROM build AS publish
RUN dotnet publish "./EPMC.csproj" -c Release -o /app/publish /p:UseAppHost=false

FROM mcr.microsoft.com/dotnet/aspnet:8.0 AS runtime
WORKDIR /app
EXPOSE 8080
COPY --from=publish /app/publish .
ENTRYPOINT ["dotnet", "EPMC.dll"]
