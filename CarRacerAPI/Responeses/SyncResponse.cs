namespace CarRacerAPI.Responeses
{
    public class SyncResponse
    {
        public bool IsSuccess { get; set; }
        public string Message { get; set; } = string.Empty;

        public List<Guid> SyncedMeasurementIds { get; set; } = new();
    }
}
