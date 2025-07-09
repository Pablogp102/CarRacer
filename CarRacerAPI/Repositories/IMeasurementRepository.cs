using CarRacerAPI.Database;
using CarRacerAPI.Entities;
using CarRacerAPI.Enums;

using Microsoft.EntityFrameworkCore;

namespace CarRacerAPI.Repositories;

public interface IMeasurementRepository
{
    Task<List<Measurements>> GetMeasurementsAsync(MeasurementType? type);
    Task AddMeasurementsAsync(List<Measurements> measurements);
    Task<List<Guid>> GetExistingClientIdsAsync(Guid userId, List<Guid> clientIds);

    Task<bool> DeleteByClientIdAsync(Guid userId, Guid clientMeasurementId);
}

public class MeasurementRepository : IMeasurementRepository
{
    private readonly CarRacerDbContext _context;
    public MeasurementRepository(CarRacerDbContext context)
    {
        _context = context;
    }
    public async Task AddMeasurementsAsync(List<Measurements> measurements)
    {
        if (measurements == null || !measurements.Any())
        {
            return;
        }
        await _context.Measurements.AddRangeAsync(measurements);
        await _context.SaveChangesAsync();
    }

    public async Task<List<Measurements>> GetMeasurementsAsync(MeasurementType? type)
    {
        return await _context.Measurements
           .Include(m => m.User).AsQueryable()
           .Where(m => !type.HasValue || m.Type == type)
           .ToListAsync();
    }

    public async Task<List<Guid>> GetExistingClientIdsAsync(Guid userId, List<Guid> clientIds)
    {
        return await _context.Measurements
            .Where(m => m.UserId == userId && m.ClientGeneratedId.HasValue && clientIds.Contains(m.ClientGeneratedId.Value))
            .Select(m => m.ClientGeneratedId!.Value)
            .ToListAsync();
    }

    public async Task<bool> DeleteByClientIdAsync(Guid userId, Guid clientMeasurementId)
    {
        var measurement = await _context.Measurements
            .FirstOrDefaultAsync(m => m.UserId == userId && m.ClientGeneratedId == clientMeasurementId);

        if (measurement == null)
        {
            return false;
        }

        _context.Measurements.Remove(measurement);
        await _context.SaveChangesAsync();
        return true;
    }
}
