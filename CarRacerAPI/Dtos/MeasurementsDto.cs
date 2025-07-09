namespace CarRacerAPI.Dtos;

public class MeasurementDto
{
    public Guid? Id { get; set; }
    public string Type { get; set; } = string.Empty;
    public double DurationS { get; set; }
    public double PeakSpeedKmh { get; set; }
    public double DistanceMeters { get; set; }
    public long MeasuredAt { get; set; }
}