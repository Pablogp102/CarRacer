using CarRacerAPI.Dtos;

namespace CarRacerAPI.Responeses;

public class LeaderboardResponse
{
    public bool IsSuccess { get; set; }
    public string? Message { get; set; }
    public List<LeaderboardDto> Leaderboard { get; set; } = new List<LeaderboardDto>();
}