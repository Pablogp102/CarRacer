using CarRacerAPI.Dtos;
using CarRacerAPI.Repositories;
using CarRacerAPI.Requests;
using CarRacerAPI.Responeses;

using System.Text.RegularExpressions;

namespace CarRacerAPI.Services;
public interface IUserService
{
    Task<RegisterResponse> RegisterUserAsync(string login, string plainPassword);
    Task<LoginResponse> LoginAsync(string login, string plainPassword);
    Task<DeleteResponse> DeleteUserAsync(string userIdString);
}


public class UserService : IUserService
{
    private readonly IUserRepository _userRepository;
    private readonly IPasswordHasher _passwordHasher;
    private readonly IJwtService _jwtService;
    public UserService(IUserRepository userRepository, IPasswordHasher passwordHasher, IJwtService jwtService)
    {
        _userRepository = userRepository;
        _passwordHasher = passwordHasher;
        _jwtService = jwtService;
    }

    public async Task<LoginResponse> LoginAsync(string login, string plainPassword)
    {
        var user = await _userRepository.GetByLoginAsync(login);
        if (user == null)
        {
            return new LoginResponse { IsSuccess = false, Message = "Invalid login or password."};
        }

        if (!_passwordHasher.VerifyPassword(user.Password, plainPassword))
        {
            return new LoginResponse { IsSuccess = false, Message = "Invalid login or password." };
        }

        var token = _jwtService.GenerateToken(user.Id.ToString(), user.Login);

        var userDto = UserDto.FromEntity(user);

        return new LoginResponse
        {
            IsSuccess = true,
            Token = token,
            User = userDto
        };

    }

    public async Task<RegisterResponse> RegisterUserAsync(string login, string plainPassword)
    {
        if (await _userRepository.ExistsByLoginAsync(login))
        {
            return new RegisterResponse
            {
                IsSuccess = false,
                Message = "Login already exists."
            };
        }

        var hashedPassword = _passwordHasher.HashPassword(plainPassword);
        var user = UserFactory.Create(login, hashedPassword);
        await _userRepository.AddAsync(user);

        var token = _jwtService.GenerateToken(user.Id.ToString() ,user.Login);

        var userDto = UserDto.FromEntity(user);

        return new RegisterResponse
        {
            IsSuccess = true,
            User = userDto,
            Token = token
        };
    }

    public async Task<DeleteResponse> DeleteUserAsync(string userIdString)
    {
        if (string.IsNullOrEmpty(userIdString))
        {
            return new DeleteResponse { IsSuccess = false, Message = "User ID is required." };
        }

        if (!Guid.TryParse(userIdString, out Guid userId))
        {
            return new DeleteResponse { IsSuccess = false, Message = "Invalid user ID format." };
        }

        var success = await _userRepository.DeleteAsync(userId);

        if (success)
        {
            return new DeleteResponse { IsSuccess = true, Message = "User deleted successfully." };
        }
        else
        {
            return new DeleteResponse { IsSuccess = false, Message = "User not found or could not be deleted." };
        }
    }
}
