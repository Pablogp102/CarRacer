using System.ComponentModel.DataAnnotations;

namespace CarRacerAPI.Requests;
public class RegisterRequest
{
    [Required]
    public string Login { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}
