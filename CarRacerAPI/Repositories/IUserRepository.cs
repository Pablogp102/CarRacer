using CarRacerAPI.Database;
using CarRacerAPI.Entities;

using Microsoft.EntityFrameworkCore;

namespace CarRacerAPI.Repositories;


public interface IUserRepository
{
    Task<Users?> GetByLoginAsync(string login);
    Task<Users?> GetByIdAsync(Guid id);
    Task AddAsync(Users user);
    Task<bool> ExistsByLoginAsync(string login);
    Task<bool> DeleteAsync(Guid id);
}

public class UserRepository : IUserRepository
{
    private readonly CarRacerDbContext _context;

    public UserRepository(CarRacerDbContext context)
    {
        _context = context;
    }

    public async Task<Users?> GetByLoginAsync(string login)
    {
        return await _context.Users
            .FirstOrDefaultAsync(u => u.Login == login);
    }

    public async Task<Users?> GetByIdAsync(Guid id)
    {
        return await _context.Users.FindAsync(id);
    }

    public async Task AddAsync(Users user)
    {
        await _context.Users.AddAsync(user);
        await _context.SaveChangesAsync();
    }

    public async Task<bool> ExistsByLoginAsync(string login)
    {
        return await _context.Users.AnyAsync(u => u.Login == login);
    }

    public async Task<bool> DeleteAsync(Guid id)
    {
        var user = await _context.Users.FindAsync(id);
        if (user == null)
        {
            return false;
        }

        _context.Users.Remove(user);
        await _context.SaveChangesAsync();
        return true;
    }

}