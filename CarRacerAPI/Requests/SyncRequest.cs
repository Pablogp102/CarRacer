using CarRacerAPI.Dtos;
using System.ComponentModel.DataAnnotations;

namespace CarRacerAPI.Requests
{
    public class SyncRequest
    {
        [Required]
        public List<MeasurementDto> Measurements { get; set; } = new();
    }
}
