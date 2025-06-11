using CarRacerAPI.Entities;

namespace CarRacerAPI.Dtos;
public class UserDto
{
    public Guid Id { get; set; }
    public string Login { get; set; } = string.Empty;
    public long CreatedAt { get; set; }
    public List<MeasurementDto>? Measurements { get; set; } = new();

    public static UserDto FromEntity(Users userEntity)
    {
        return new UserDto
        {
            Id = userEntity.Id,
            Login = userEntity.Login,
            CreatedAt = (long)(userEntity.CreatedAt.ToUniversalTime() - new DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)).TotalMilliseconds,
            Measurements = userEntity.Measurements?
                               .Select(m => new MeasurementDto
                               {
                                   Id = m.Id,
                                   Type = m.Type.ToString(), 
                                   DurationS = m.DurationS,
                                   PeakSpeedKmh = m.PeakSpeedKmh,
                                   DistanceMeters = m.DistanceMeters,
                                   MeasuredAt = m.MeasuredAt 
                               })
                               .ToList() ?? new List<MeasurementDto>()
        };
    }
}
