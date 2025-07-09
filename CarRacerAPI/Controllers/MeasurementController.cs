using CarRacerAPI.Requests;
using CarRacerAPI.Responeses;
using CarRacerAPI.Services;

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

using System.Security.Claims;

namespace CarRacerAPI.Controllers;

[Authorize]
[ApiController]
[Route("api/[controller]")]
public class MeasurementController : ControllerBase
{
    private readonly IMeasurementService _measurementService;
    public MeasurementController(IMeasurementService measurementService)
    {
        _measurementService = measurementService;
    }

    [HttpPost("sync")]
    public async Task<IActionResult> SyncMeasurements([FromBody] SyncRequest request)
    {

        var userIdString = User.FindFirstValue("id");
        if (!Guid.TryParse(userIdString, out Guid userId))
        {
            return Unauthorized("Nieprawidłowy token. Brak ID użytkownika.");
        }

        var response = await _measurementService.SyncMeasurementsAsync(userId, request.Measurements);

        if (!response.IsSuccess)
        {
            return BadRequest(response);
        }

        return Ok(response);
    }

    [HttpPost("leaderboard")]
    public async Task<IActionResult> GetAllMeasurements([FromBody] LeaderboardRequest request)
    {
        var response = await _measurementService.GetAllMeasurementsAsync(request.Type);

        if (!response.IsSuccess)
        {
            return BadRequest(response);
        }

        return Ok(response);
    }

    [HttpPost("delete")]
    public async Task<IActionResult> DeleteMeasurement([FromBody] DeleteRequest request)
    {
        var userIdString = User.FindFirstValue("id");
        if (!Guid.TryParse(userIdString, out Guid userId))
        {
            return Unauthorized();
        }

        var response = await _measurementService.DeleteMeasurementAsync(userId, request.Id);

        if (!response.IsSuccess)
        {
            return NotFound(response);
        }

        return Ok(response);
    }
}
