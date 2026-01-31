package models

import (
	"time"

	"gorm.io/gorm"
)

type Habit struct {
	ID        uint           `gorm:"primarykey" json:"id"`
	CreatedAt time.Time      `json:"created_at"`
	UpdatedAt time.Time      `json:"updated_at"`
	DeletedAt gorm.DeletedAt `gorm:"index" json:"-"`

	UserID          uint   `gorm:"not null;index" json:"user_id"`
	Name            string `gorm:"not null" json:"name"`
	Description     string `json:"description"`
	Category        string `gorm:"not null" json:"category"` // health, productivity, learning, mindfulness, social
	TargetFrequency int    `gorm:"not null" json:"target_frequency"`

	ReminderTime    *string `json:"reminder_time,omitempty"`    // HH:mm format
	ReminderEnabled bool    `gorm:"default:false" json:"reminder_enabled"`
	RepeatDays      *string `json:"repeat_days,omitempty"`      // CSV of day names

	Color  string `gorm:"default:#6366F1" json:"color"` // hex color
	Icon   string `gorm:"default:🎯" json:"icon"`       // emoji

	IsActive  bool  `gorm:"default:true" json:"is_active"`

	// Relations
	CheckIns []CheckIn `gorm:"foreignKey:HabitID;constraint:OnDelete:CASCADE" json:"check_ins,omitempty"`
	User     User      `gorm:"foreignKey:UserID" json:"-"`
}

type HabitCreateRequest struct {
	Name            string  `json:"name" binding:"required"`
	Description     string  `json:"description"`
	Category        string  `json:"category" binding:"required"`
	TargetFrequency int     `json:"target_frequency" binding:"required,min=1,max=7"`
	ReminderTime    *string `json:"reminder_time,omitempty"`
	ReminderEnabled bool    `json:"reminder_enabled"`
	RepeatDays      *string `json:"repeat_days,omitempty"`
	Color           string  `json:"color"`
	Icon            string  `json:"icon"`
}

type HabitUpdateRequest struct {
	Name            *string `json:"name,omitempty"`
	Description     *string `json:"description,omitempty"`
	Category        *string `json:"category,omitempty"`
	TargetFrequency *int    `json:"target_frequency,omitempty"`
	ReminderTime    *string `json:"reminder_time,omitempty"`
	ReminderEnabled *bool   `json:"reminder_enabled,omitempty"`
	RepeatDays      *string `json:"repeat_days,omitempty"`
	Color           *string `json:"color,omitempty"`
	Icon            *string `json:"icon,omitempty"`
	IsActive        *bool   `json:"is_active,omitempty"`
}

type HabitWithCheckIns struct {
	Habit
	CheckIns []CheckIn `json:"check_ins"`
}
