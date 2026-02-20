package database

import (
	"fmt"
	"log"

	"myhabit-backend/internal/config"
	"myhabit-backend/internal/models"

	"gorm.io/driver/mysql"
	"gorm.io/gorm"
	"gorm.io/gorm/logger"
)

var DB *gorm.DB

// Connect establishes database connection
func Connect(cfg *config.Config) error {
	var err error

	// Configure logger
	logLevel := logger.Info
	if cfg.GinMode == "release" {
		logLevel = logger.Warn
	}

	DB, err = gorm.Open(mysql.Open(cfg.GetDSN()), &gorm.Config{
		Logger: logger.Default.LogMode(logLevel),
	})
	if err != nil {
		return fmt.Errorf("failed to connect to database: %w", err)
	}

	log.Println("✅ Database connected successfully")
	return nil
}

// Migrate runs database migrations
func Migrate() error {
	log.Println("🔄 Running database migrations...")

	err := DB.AutoMigrate(
		&models.User{},
		&models.Habit{},
		&models.CheckIn{},
		&models.HabitTemplate{},
	)
	if err != nil {
		return fmt.Errorf("migration failed: %w", err)
	}

	log.Println("✅ Database migrations completed")
	return nil
}

// SeedTemplates creates default system templates
func SeedTemplates() error {
	var count int64
	DB.Model(&models.HabitTemplate{}).Where("is_system_template = ?", true).Count(&count)

	if count > 0 {
		log.Println("ℹ️  System templates already exist, skipping seed")
		return nil
	}

	log.Println("🌱 Seeding default habit templates...")

	templates := []models.HabitTemplate{
		{
			Name:               "Morning Workout",
			Description:        "30 minutes cardio and strength training",
			Category:           "health",
			Icon:               "💪",
			DefaultFrequency:   "daily",
			DefaultReminderTime: stringPtr("06:30"),
			Tags:               "fitness,health,morning",
			IsSystemTemplate:   true,
		},
		{
			Name:               "Meditation",
			Description:        "10 minutes mindfulness meditation",
			Category:           "mindfulness",
			Icon:               "🧘",
			DefaultFrequency:   "daily",
			DefaultReminderTime: stringPtr("07:00"),
			Tags:               "meditation,mindfulness,morning",
			IsSystemTemplate:   true,
		},
		{
			Name:               "Read for 30 Minutes",
			Description:        "Read books or articles to expand knowledge",
			Category:           "learning",
			Icon:               "📚",
			DefaultFrequency:   "daily",
			DefaultReminderTime: stringPtr("20:00"),
			Tags:               "reading,learning,evening",
			IsSystemTemplate:   true,
		},
		{
			Name:               "Drink Water",
			Description:        "Stay hydrated throughout the day",
			Category:           "health",
			Icon:               "💧",
			DefaultFrequency:   "daily",
			DefaultReminderTime: stringPtr("09:00"),
			Tags:               "health,hydration",
			IsSystemTemplate:   true,
		},
		{
			Name:               "Journaling",
			Description:        "Write down thoughts and reflections",
			Category:           "mindfulness",
			Icon:               "✍️",
			DefaultFrequency:   "daily",
			DefaultReminderTime: stringPtr("21:00"),
			Tags:               "journaling,reflection,evening",
			IsSystemTemplate:   true,
		},
	}

	result := DB.Create(&templates)
	if result.Error != nil {
		return fmt.Errorf("failed to seed templates: %w", result.Error)
	}

	log.Printf("✅ Seeded %d system templates\n", len(templates))
	return nil
}

func stringPtr(s string) *string {
	return &s
}

// Close closes database connection
func Close() error {
	sqlDB, err := DB.DB()
	if err != nil {
		return err
	}
	return sqlDB.Close()
}
