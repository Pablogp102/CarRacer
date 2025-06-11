using CarRacerAPI.Dtos;

namespace CarRacerAPI.Responeses;
public class LoginResponse
{
    public bool IsSuccess { get; set; }
    public string? Message { get; set; }
    public string? Token { get; set; }
    public UserDto? User { get; set; }
}
