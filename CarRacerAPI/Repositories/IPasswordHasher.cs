namespace CarRacerAPI.Repositories;

using Microsoft.AspNetCore.Identity;

public interface IPasswordHasher
{
    string HashPassword(string password);
    bool VerifyPassword(string hashedPassword, string providedPassword);
}
public class PasswordHasherWrapper : IPasswordHasher
{
    private readonly PasswordHasher<object> _hasher = new PasswordHasher<object>();

    public string HashPassword(string password)
    {
        return _hasher.HashPassword(null, password);
    }

    public bool VerifyPassword(string hashedPassword, string providedPassword)
    {
        var result = _hasher.VerifyHashedPassword(null, hashedPassword, providedPassword);
        return result == PasswordVerificationResult.Success || result == PasswordVerificationResult.SuccessRehashNeeded;
    }
}



