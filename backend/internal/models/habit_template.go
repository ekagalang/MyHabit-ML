package models

import (
	"time"

	"gorm.io/gorm"
)

type HabitTemplate struct {
	ID        uint           `gorm:"primarykey" json:"id"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`

	UserID             *uint   `gorm:"index" json:"user_id,omitempty"` // null for system templates
	Name               string  `gorm:"not null" json:"name"`
	Description        string  `json:"description"`
	Category           string  `gorm:"not null" json:"category"`
	Icon               string  `gorm:"default:🎯" json:"icon"`
	DefaultFrequency   string  `gorm:"default:daily" json:"default_frequency"`
	DefaultReminderTime *string `json:"default_reminder_time,omitempty"`
	Tags               string  `json:"tags"` // CSV of tags
	IsSystemTemplate   bool    `gorm:"default:false" json:"is_system_template"`

	// Relations
	User *User `gorm:"foreignKey:UserID" json:"-"`
}

type TemplateCreateRequest struct {
	Name               string  `json:"name" binding:"required"`
	Description        string  `json:"description"`
	Category           string  `json:"category" binding:"required"`
	Icon               string  `json:"icon"`
	DefaultFrequency   string  `json:"default_frequency"`
	DefaultReminderTime *string `json:"default_reminder_time,omitempty"`
	Tags               string  `json:"tags"`
}
