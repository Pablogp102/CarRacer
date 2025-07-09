using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace CarRacerAPI.Migrations
{
    /// <inheritdoc />
    public partial class ClientGeneratedId : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_Measurements_UserId",
                table: "Measurements");

            migrationBuilder.AddColumn<Guid>(
                name: "ClientGeneratedId",
                table: "Measurements",
                type: "uniqueidentifier",
                nullable: true);

            migrationBuilder.CreateIndex(
                name: "IX_Measurements_UserId_ClientGeneratedId",
                table: "Measurements",
                columns: new[] { "UserId", "ClientGeneratedId" },
                unique: true,
                filter: "[ClientGeneratedId] IS NOT NULL");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "IX_Measurements_UserId_ClientGeneratedId",
                table: "Measurements");

            migrationBuilder.DropColumn(
                name: "ClientGeneratedId",
                table: "Measurements");

            migrationBuilder.CreateIndex(
                name: "IX_Measurements_UserId",
                table: "Measurements",
                column: "UserId");
        }
    }
}
