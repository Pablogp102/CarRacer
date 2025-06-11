namespace CarRacerAPI.Controllers;

using CarRacerAPI.Dtos;
using CarRacerAPI.Requests;
using CarRacerAPI.Responeses;
using CarRacerAPI.Services;

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly IUserService _userService;
    public AuthController(IUserService userService)
    {
        _userService = userService;
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register([FromBody] RegisterRequest request)
     {
        if (!ModelState.IsValid)
            return BadRequest(new RegisterResponse
            {
                IsSuccess = false,
                Message = "Invalid request data"
            });

        var response = await _userService.RegisterUserAsync(request.Login, request.Password);

        if (!response.IsSuccess)
            return BadRequest(response);

        return Ok(response);
    }

    [HttpPost("login")]
    public async Task<IActionResult> Login([FromBody] LoginRequest request)
    {
        var result = await _userService.LoginAsync(request.Login, request.Password);
        if (!result.IsSuccess)
        {
            return BadRequest(result);
        }

        return Ok(result);
    }

    [HttpPost("delete")]
    [Authorize]
    public async Task<IActionResult> DeleteUser([FromBody] DeleteRequest request)
    {
        if (!ModelState.IsValid)
            return BadRequest(new DeleteResponse
            {
                IsSuccess = false,
                Message = "Invalid request data"
            });

        var result = await _userService.DeleteUserAsync(request.Id);

        if (!result.IsSuccess)
        {
            return BadRequest(result);
        }
        return Ok(result);
    }

    [HttpGet("test")]
    [Authorize] // 🔒 Tylko z ważnym JWT
    public IActionResult GetSecretMeasurement()
    {
        return Ok("Dostęp do chronionego endpointu uzyskany!");
    }
}