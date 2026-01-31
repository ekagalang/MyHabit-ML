package main

import (
	"fmt"
	"log"
	"os"
	"os/signal"
	"syscall"

	"myhabit-backend/internal/config"
	"myhabit-backend/internal/database"
	"myhabit-backend/internal/middleware"
	"myhabit-backend/internal/utils"

	"github.com/gin-gonic/gin"
)

func main() {
	// Load configuration
	cfg, err := config.Load()
	if err != nil {
		log.Fatalf("❌ Failed to load configuration: %v", err)
	}

	// Set Gin mode
	gin.SetMode(cfg.GinMode)

	// Initialize JWT
	utils.InitJWT(cfg)

	// Connect to database
	if err := database.Connect(cfg); err != nil {
		log.Fatalf("❌ Failed to connect to database: %v", err)
	}

	// Run migrations
	if err := database.Migrate(); err != nil {
		log.Fatalf("❌ Failed to run migrations: %v", err)
	}

	// Seed templates
	if err := database.SeedTemplates(); err != nil {
		log.Printf("⚠️  Warning: Failed to seed templates: %v", err)
	}

	// Initialize Gin router
	r := gin.Default()

	// Apply middlewares
	r.Use(middleware.CORS(cfg.AllowedOrigins))
	r.Use(gin.Recovery())

	// Setup routes
	setupRoutes(r, cfg)

	// Start server
	port := cfg.ServerPort
	addr := fmt.Sprintf(":%s", port)

	log.Printf("🚀 Server starting on http://localhost%s", addr)
	log.Printf("📚 API documentation available at http://localhost%s/api", addr)
	log.Printf("🏥 Health check available at http://localhost%s/health", addr)

	// Graceful shutdown
	go func() {
		if err := r.Run(addr); err != nil {
			log.Fatalf("❌ Failed to start server: %v", err)
		}
	}()

	// Wait for interrupt signal
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	log.Println("🛑 Shutting down server...")

	// Close database connection
	if err := database.Close(); err != nil {
		log.Printf("⚠️  Error closing database: %v", err)
	}

	log.Println("✅ Server stopped gracefully")
}
