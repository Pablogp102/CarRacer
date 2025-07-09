using CarRacerAPI.Repositories;
using CarRacerAPI.Services;

namespace CarRacerAPI.Extensions;
public static class DependencyInjection
{
    public static IServiceCollection AddServices(this IServiceCollection services)
    {
        services.AddScoped<IUserService, UserService>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IPasswordHasher, PasswordHasherWrapper>();
        services.AddScoped<IJwtService, JwtService>();
        services.AddScoped<IMeasurementService, MeasurementService>();
        services.AddScoped<IMeasurementRepository, MeasurementRepository>();

        return services;
    }
}