package config

import (
	"fmt"
	"os"
	"strconv"

	"github.com/joho/godotenv"
)

type Config struct {
	ServerPort         string
	GinMode            string
	DBHost             string
	DBPort             string
	DBUser             string
	DBPassword         string
	DBName             string
	DBSSLMode          string
	JWTSecret          string
	JWTExpirationHours int
	AllowedOrigins     string
}

func Load() (*Config, error) {
	// Load .env file if exists
	_ = godotenv.Load()

	jwtExpHours, err := strconv.Atoi(getEnv("JWT_EXPIRATION_HOURS", "168"))
	if err != nil {
		jwtExpHours = 168 // Default 7 days
	}

	config := &Config{
		ServerPort:         getEnv("PORT", "8080"),
		GinMode:            getEnv("GIN_MODE", "debug"),
		DBHost:             getEnv("DB_HOST", "localhost"),
		DBPort:             getEnv("DB_PORT", "5432"),
		DBUser:             getEnv("DB_USER", "postgres"),
		DBPassword:         getEnv("DB_PASSWORD", ""),
		DBName:             getEnv("DB_NAME", "myhabit_db"),
		DBSSLMode:          getEnv("DB_SSLMODE", "disable"),
		JWTSecret:          getEnv("JWT_SECRET", ""),
		JWTExpirationHours: jwtExpHours,
		AllowedOrigins:     getEnv("ALLOWED_ORIGINS", "*"),
	}

	if config.JWTSecret == "" {
		return nil, fmt.Errorf("JWT_SECRET must be set")
	}

	return config, nil
}

func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func (c *Config) GetDSN() string {
	return fmt.Sprintf(
		"host=%s port=%s user=%s password=%s dbname=%s sslmode=%s",
		c.DBHost, c.DBPort, c.DBUser, c.DBPassword, c.DBName, c.DBSSLMode,
	)
}
