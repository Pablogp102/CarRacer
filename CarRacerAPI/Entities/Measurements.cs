namespace CarRacerAPI.Entities;

using CarRacerAPI.Enums;
public class Measurements
{
    public long Id { get; set; }

    public MeasurementType Type { get; set; }

    public double DurationS { get; set; }

    public double PeakSpeedKmh { get; set; }

    public double DistanceMeters { get; set; }

    public DateTime MeasuredAt { get; set; }

    public Guid UserId { get; set; }
    public Users User { get; set; }
}




