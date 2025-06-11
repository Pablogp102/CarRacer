namespace CarRacerAPI.Repositories;

using CarRacerAPI.Entities;

public static class UserFactory
{
    public static Users Create(string login, string hashedPassword)
    {

        return new Users
        {
            Id = Guid.NewGuid(),
            Login = login,
            Password = hashedPassword,
            CreatedAt = DateTime.UtcNow,
            Measurements = new List<Measurements>()
        };
    }
}