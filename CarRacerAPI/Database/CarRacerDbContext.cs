using CarRacerAPI.Entities;

using Microsoft.EntityFrameworkCore;

namespace CarRacerAPI.Database
{
    public class CarRacerDbContext : DbContext
    {
        private readonly IConfiguration _configuration;
        public CarRacerDbContext(DbContextOptions<CarRacerDbContext> options, IConfiguration configuration) : base(options)
        {
            _configuration = configuration;
        }
        public DbSet<Users> Users { get; set; }
        public DbSet<Measurements> Measurements { get; set; }
        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            // USERS
            modelBuilder.Entity<Users>(entity =>
            {
                entity.ToTable("Users");

                entity.HasKey(u => u.Id);

                entity.Property(u => u.Id)
                      .ValueGeneratedOnAdd();

                entity.Property(u => u.Login)
                      .IsRequired();

                entity.Property(u => u.Password)
                      .IsRequired();

                entity.Property(u => u.CreatedAt)
                      .HasDefaultValueSql("GETUTCDATE()");
            });

            // MEASUREMENTS
            modelBuilder.Entity<Measurements>(entity =>
            {
                entity.ToTable("Measurements");

                entity.HasKey(m => m.Id);

                entity.Property(m => m.Id)
                      .ValueGeneratedOnAdd();

                entity.Property(m => m.Type)
                      .HasConversion<string>()
                      .IsRequired();

                entity.Property(m => m.MeasuredAt)
                      .HasDefaultValueSql("GETUTCDATE()");

                entity.HasOne(m => m.User)
                      .WithMany(u => u.Measurements)
                      .HasForeignKey(m => m.UserId)
                      .OnDelete(DeleteBehavior.Cascade);
            });
        }

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            base.OnConfiguring(optionsBuilder);
            if (!optionsBuilder.IsConfigured)
            {
                optionsBuilder.UseSqlServer(_configuration.GetConnectionString("CarRacerConnectionString"));
            }
        }
    }

}
