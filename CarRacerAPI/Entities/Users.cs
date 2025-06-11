namespace CarRacerAPI.Entities;

public class Users
{
    public Guid Id { get; set; }
    public string Login { get; set; } = string.Empty;
    public string Password { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; }
    public ICollection<Measurements>? Measurements { get; set; } = new List<Measurements>();
}
