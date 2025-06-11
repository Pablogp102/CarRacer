using System.ComponentModel.DataAnnotations;

namespace CarRacerAPI.Requests
{
    public class DeleteRequest
    {
        [Required]
        public string Id { get; set; } = string.Empty;
    }
}
