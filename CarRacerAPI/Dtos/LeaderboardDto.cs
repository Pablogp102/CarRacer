namespace CarRacerAPI.Dtos;

public class LeaderboardDto
{
    public Guid Id { get; set; }
    public string UserLogin { get; set; } = string.Empty; 
    public string Type { get; set; } = string.Empty;
    public double DurationS { get; set; }
    public double PeakSpeedKmh { get; set; }
    public double DistanceMeters { get; set; }
    public long MeasuredAt { get; set; }
}