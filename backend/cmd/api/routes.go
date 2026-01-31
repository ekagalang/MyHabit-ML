package main

import (
	"myhabit-backend/internal/config"
	"myhabit-backend/internal/handlers"
	"myhabit-backend/internal/middleware"

	"github.com/gin-gonic/gin"
)

func setupRoutes(r *gin.Engine, cfg *config.Config) {
	// Initialize handlers
	authHandler := handlers.NewAuthHandler(cfg)
	habitHandler := handlers.NewHabitHandler()
	checkInHandler := handlers.NewCheckInHandler()
	syncHandler := handlers.NewSyncHandler()
	templateHandler := handlers.NewTemplateHandler()

	// Health check
	r.GET("/health", func(c *gin.Context) {
		c.JSON(200, gin.H{
			"status":  "ok",
			"service": "myhabit-backend",
			"version": "1.0.0",
		})
	})

	// API v1 routes
	api := r.Group("/api")
	{
		// Public routes
		auth := api.Group("/auth")
		{
			auth.POST("/register", authHandler.Register)
			auth.POST("/login", authHandler.Login)
		}

		// Protected routes
		protected := api.Group("")
		protected.Use(middleware.AuthRequired())
		{
			// Auth
			protected.GET("/auth/profile", authHandler.GetProfile)

			// Habits
			habits := protected.Group("/habits")
			{
				habits.GET("", habitHandler.GetHabits)
				habits.GET("/:id", habitHandler.GetHabit)
				habits.POST("", habitHandler.CreateHabit)
				habits.PUT("/:id", habitHandler.UpdateHabit)
				habits.DELETE("/:id", habitHandler.DeleteHabit)
				habits.GET("/:id/stats", habitHandler.GetHabitStats)
			}

			// Check-ins
			checkIns := protected.Group("/checkins")
			{
				checkIns.GET("", checkInHandler.GetCheckIns)
				checkIns.POST("", checkInHandler.CreateCheckIn)
				checkIns.POST("/bulk", checkInHandler.BulkCreateCheckIns)
				checkIns.DELETE("/:id", checkInHandler.DeleteCheckIn)
			}

			// Sync
			sync := protected.Group("/sync")
			{
				sync.GET("", syncHandler.GetAllData)
				sync.POST("", syncHandler.SyncData)
			}

			// Templates
			templates := protected.Group("/templates")
			{
				templates.GET("", templateHandler.GetTemplates)
				templates.POST("", templateHandler.CreateTemplate)
				templates.DELETE("/:id", templateHandler.DeleteTemplate)
			}
		}
	}
}
