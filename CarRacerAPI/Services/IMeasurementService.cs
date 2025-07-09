using CarRacerAPI.Dtos;
using CarRacerAPI.Entities;
using CarRacerAPI.Enums;
using CarRacerAPI.Repositories;
using CarRacerAPI.Requests;
using CarRacerAPI.Responeses;

namespace CarRacerAPI.Services;

public interface IMeasurementService
{
    Task<SyncResponse> SyncMeasurementsAsync(Guid userId, List<MeasurementDto> measurements);
    Task<LeaderboardResponse> GetAllMeasurementsAsync(string? typeString);
    Task<DeleteResponse> DeleteMeasurementAsync(Guid userId, string clientMeasurementId);
}

public class MeasurementService : IMeasurementService
{
    private readonly IMeasurementRepository _measurementRepository;
    private readonly IUserRepository _userRepository;

    public MeasurementService(IMeasurementRepository measurementRepository, IUserRepository userRepository)
    {
        _measurementRepository = measurementRepository;
        _userRepository = userRepository;
    }

    public async Task<SyncResponse> SyncMeasurementsAsync(Guid userId, List<MeasurementDto> measurements)
    {

        if (measurements == null || !measurements.Any())
        {
            return new SyncResponse { IsSuccess = true, Message = "No measurements to sync." };
        }

        var clientIdsFromRequest = measurements
            .Where(d => d.Id.HasValue)
            .Select(d => d.Id.Value)
            .ToList();

        var existingIdsInDb = await _measurementRepository.GetExistingClientIdsAsync(userId, clientIdsFromRequest);

        var userEntity = await _userRepository.GetByIdAsync(userId);
        if (userEntity == null)
        {
            return new SyncResponse { IsSuccess = false, Message = "User not found." };
        }

        var measurementsToCreate = new List<Measurements>();
        foreach (var dto in measurements)
        {
            if (!dto.Id.HasValue || existingIdsInDb.Contains(dto.Id.Value))
            {
                continue;
            }

            if (Enum.TryParse<MeasurementType>(dto.Type, true, out var measurementType))
            {
                var newEntity = new Measurements
                {
                    ClientGeneratedId = dto.Id.Value,
                    Type = measurementType,
                    DurationS = dto.DurationS,
                    PeakSpeedKmh = dto.PeakSpeedKmh,
                    DistanceMeters = dto.DistanceMeters,
                    MeasuredAt = DateTimeOffset.FromUnixTimeMilliseconds(dto.MeasuredAt).UtcDateTime,
                    UserId = userId,
                    User = userEntity 
                };
                measurementsToCreate.Add(newEntity);
            }
        }

        if (measurementsToCreate.Any())
        {
            await _measurementRepository.AddMeasurementsAsync(measurementsToCreate);
        }

        return new SyncResponse
        {
            IsSuccess = true,
            Message = $"Synchronized {measurementsToCreate.Count} new measurements.",
            SyncedMeasurementIds = measurementsToCreate.Select(m => m.ClientGeneratedId.Value).ToList()
        };
    }

    public async Task<LeaderboardResponse> GetAllMeasurementsAsync(string? typeString)
    {
        try
        {
            MeasurementType? measurementType = null;
            if (!string.IsNullOrEmpty(typeString))
            {
                if (Enum.TryParse<MeasurementType>(typeString, true, out var parsedType))
                {
                    measurementType = parsedType;
                }
                else
                {
                    return new LeaderboardResponse { IsSuccess = false, Message = $"Invalid measurement type: {typeString}" };
                }
            }

            var filteredMeasurements = await _measurementRepository.GetMeasurementsAsync(measurementType);

            var leaderboardDtos = filteredMeasurements.Select(m => new LeaderboardDto
            {
                Id = m.ClientGeneratedId.GetValueOrDefault(),
                UserLogin = m.User?.Login ?? "Unknown",
                Type = m.Type.ToString(),
                DurationS = m.DurationS,
                PeakSpeedKmh = m.PeakSpeedKmh,
                DistanceMeters = m.DistanceMeters,
                MeasuredAt = new DateTimeOffset(m.MeasuredAt).ToUnixTimeMilliseconds()
            }).ToList();

            if (measurementType.HasValue && measurementType.Value == MeasurementType.TOP_SPEED)
            {
                leaderboardDtos = leaderboardDtos.OrderByDescending(d => d.PeakSpeedKmh).ToList();
            }
            else
            {
                leaderboardDtos = leaderboardDtos.OrderBy(d => d.DurationS).ToList();
            }

            return new LeaderboardResponse
            {
                IsSuccess = true,
                Leaderboard = leaderboardDtos
            };
        }
        catch (Exception ex)
        {
            return new LeaderboardResponse { IsSuccess = false, Message = "An unexpected error occurred." };
        }
    }

    public async Task<DeleteResponse> DeleteMeasurementAsync(Guid userId, string clientMeasurementId)
    {
        if(string.IsNullOrEmpty(clientMeasurementId))
        {
            return new DeleteResponse { IsSuccess = false, Message = "Measurment ID is required" };
        }
        if (!Guid.TryParse(clientMeasurementId, out Guid measurementId))
        {
            return new DeleteResponse { IsSuccess = false, Message = "Invalid measurement ID format." };
        }

        var result = await _measurementRepository.DeleteByClientIdAsync(userId, measurementId);
        
        if (!result)
        {
            return new DeleteResponse { IsSuccess = false, Message = "Measurement not found or you do not have permission to delete it." };
        }
        return new DeleteResponse { IsSuccess = true, Message = "Measurement deleted successfully." };
    }
}
