package models

import (
	"time"

	"gorm.io/gorm"
)

type CheckIn struct {
	ID        uint           `gorm:"primarykey" json:"id"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`

	HabitID     uint      `gorm:"not null;index" json:"habit_id"`
	Timestamp   time.Time `gorm:"not null" json:"timestamp"`
	Date        string    `gorm:"not null;index" json:"date"`        // yyyy-MM-dd
	CompletedAt string    `gorm:"not null" json:"completed_at"`      // HH:mm

	Note *string `json:"note,omitempty"`
	Mood *string `json:"mood,omitempty"` // very_happy, happy, neutral, sad, very_sad

	// Advanced tracking
	IsLate       bool    `gorm:"default:false" json:"is_late"`
	MinutesLate  int     `gorm:"default:0" json:"minutes_late"`
	EnergyLevel  *int    `json:"energy_level,omitempty"`  // 1-5 scale
	StressLevel  *int    `json:"stress_level,omitempty"`  // 1-5 scale
	Location     *string `json:"location,omitempty"`      // home, work, gym, outdoor
	Weather      *string `json:"weather,omitempty"`       // sunny, rainy, cloudy, cold

	// Relations
	Habit Habit `gorm:"foreignKey:HabitID" json:"-"`
}

type CheckInCreateRequest struct {
	HabitID     uint    `json:"habit_id" binding:"required"`
	Date        string  `json:"date" binding:"required"`
	CompletedAt string  `json:"completed_at" binding:"required"`
	Note        *string `json:"note,omitempty"`
	Mood        *string `json:"mood,omitempty"`
	IsLate      bool    `json:"is_late"`
	MinutesLate int     `json:"minutes_late"`
	EnergyLevel *int    `json:"energy_level,omitempty"`
	StressLevel *int    `json:"stress_level,omitempty"`
	Location    *string `json:"location,omitempty"`
	Weather     *string `json:"weather,omitempty"`
}

type CheckInBulkCreateRequest struct {
	CheckIns []CheckInCreateRequest `json:"check_ins" binding:"required,dive"`
}
