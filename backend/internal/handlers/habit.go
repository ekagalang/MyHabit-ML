package handlers

import (
	"strconv"

	"myhabit-backend/internal/database"
	"myhabit-backend/internal/middleware"
	"myhabit-backend/internal/models"
	"myhabit-backend/internal/utils"

	"github.com/gin-gonic/gin"
)

type HabitHandler struct{}

func NewHabitHandler() *HabitHandler {
	return &HabitHandler{}
}

// GetHabits godoc
// @Summary Get all habits for user
// @Description Get all habits for authenticated user
// @Tags habits
// @Produce json
// @Security BearerAuth
// @Param include_checkins query bool false "Include check-ins"
// @Success 200 {object} utils.Response
// @Router /api/habits [get]
func (h *HabitHandler) GetHabits(c *gin.Context) {
	userID := middleware.GetUserID(c)
	includeCheckIns := c.Query("include_checkins") == "true"

	var habits []models.Habit
	query := database.DB.Where("user_id = ?", userID)

	if includeCheckIns {
		query = query.Preload("CheckIns")
	}

	if err := query.Find(&habits).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to fetch habits")
		return
	}

	utils.SuccessResponse(c, 200, "Habits retrieved successfully", habits)
}

// GetHabit godoc
// @Summary Get habit by ID
// @Description Get a specific habit with optional check-ins
// @Tags habits
// @Produce json
// @Security BearerAuth
// @Param id path int true "Habit ID"
// @Param include_checkins query bool false "Include check-ins"
// @Success 200 {object} utils.Response
// @Router /api/habits/{id} [get]
func (h *HabitHandler) GetHabit(c *gin.Context) {
	userID := middleware.GetUserID(c)
	habitID := c.Param("id")
	includeCheckIns := c.Query("include_checkins") == "true"

	var habit models.Habit
	query := database.DB.Where("id = ? AND user_id = ?", habitID, userID)

	if includeCheckIns {
		query = query.Preload("CheckIns")
	}

	if err := query.First(&habit).Error; err != nil {
		utils.ErrorResponse(c, 404, "Habit not found")
		return
	}

	utils.SuccessResponse(c, 200, "Habit retrieved successfully", habit)
}

// CreateHabit godoc
// @Summary Create a new habit
// @Description Create a new habit for authenticated user
// @Tags habits
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body models.HabitCreateRequest true "Habit data"
// @Success 201 {object} utils.Response
// @Router /api/habits [post]
func (h *HabitHandler) CreateHabit(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req models.HabitCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	habit := models.Habit{
		UserID:          userID,
		Name:            req.Name,
		Description:     req.Description,
		Category:        req.Category,
		TargetFrequency: req.TargetFrequency,
		ReminderTime:    req.ReminderTime,
		ReminderEnabled: req.ReminderEnabled,
		RepeatDays:      req.RepeatDays,
		Color:           req.Color,
		Icon:            req.Icon,
		IsActive:        true,
	}

	if err := database.DB.Create(&habit).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to create habit")
		return
	}

	utils.SuccessResponse(c, 201, "Habit created successfully", habit)
}

// UpdateHabit godoc
// @Summary Update a habit
// @Description Update habit details
// @Tags habits
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param id path int true "Habit ID"
// @Param request body models.HabitUpdateRequest true "Updated habit data"
// @Success 200 {object} utils.Response
// @Router /api/habits/{id} [put]
func (h *HabitHandler) UpdateHabit(c *gin.Context) {
	userID := middleware.GetUserID(c)
	habitID := c.Param("id")

	var habit models.Habit
	if err := database.DB.Where("id = ? AND user_id = ?", habitID, userID).First(&habit).Error; err != nil {
		utils.ErrorResponse(c, 404, "Habit not found")
		return
	}

	var req models.HabitUpdateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	// Update fields if provided
	updates := make(map[string]interface{})
	if req.Name != nil {
		updates["name"] = *req.Name
	}
	if req.Description != nil {
		updates["description"] = *req.Description
	}
	if req.Category != nil {
		updates["category"] = *req.Category
	}
	if req.TargetFrequency != nil {
		updates["target_frequency"] = *req.TargetFrequency
	}
	if req.ReminderTime != nil {
		updates["reminder_time"] = *req.ReminderTime
	}
	if req.ReminderEnabled != nil {
		updates["reminder_enabled"] = *req.ReminderEnabled
	}
	if req.RepeatDays != nil {
		updates["repeat_days"] = *req.RepeatDays
	}
	if req.Color != nil {
		updates["color"] = *req.Color
	}
	if req.Icon != nil {
		updates["icon"] = *req.Icon
	}
	if req.IsActive != nil {
		updates["is_active"] = *req.IsActive
	}

	if err := database.DB.Model(&habit).Updates(updates).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to update habit")
		return
	}

	utils.SuccessResponse(c, 200, "Habit updated successfully", habit)
}

// DeleteHabit godoc
// @Summary Delete a habit
// @Description Soft delete a habit
// @Tags habits
// @Security BearerAuth
// @Param id path int true "Habit ID"
// @Success 200 {object} utils.Response
// @Router /api/habits/{id} [delete]
func (h *HabitHandler) DeleteHabit(c *gin.Context) {
	userID := middleware.GetUserID(c)
	habitID := c.Param("id")

	var habit models.Habit
	if err := database.DB.Where("id = ? AND user_id = ?", habitID, userID).First(&habit).Error; err != nil {
		utils.ErrorResponse(c, 404, "Habit not found")
		return
	}

	if err := database.DB.Delete(&habit).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to delete habit")
		return
	}

	utils.SuccessResponse(c, 200, "Habit deleted successfully", nil)
}

// GetHabitStats godoc
// @Summary Get habit statistics
// @Description Get statistics for a specific habit
// @Tags habits
// @Produce json
// @Security BearerAuth
// @Param id path int true "Habit ID"
// @Success 200 {object} utils.Response
// @Router /api/habits/{id}/stats [get]
func (h *HabitHandler) GetHabitStats(c *gin.Context) {
	userID := middleware.GetUserID(c)
	habitIDStr := c.Param("id")
	habitID, _ := strconv.ParseUint(habitIDStr, 10, 64)

	var habit models.Habit
	if err := database.DB.Where("id = ? AND user_id = ?", habitID, userID).First(&habit).Error; err != nil {
		utils.ErrorResponse(c, 404, "Habit not found")
		return
	}

	// Get check-ins
	var checkIns []models.CheckIn
	database.DB.Where("habit_id = ?", habitID).Find(&checkIns)

	// Calculate stats
	stats := map[string]interface{}{
		"total_checkins":   len(checkIns),
		"habit_id":         habitID,
		"habit_name":       habit.Name,
		"target_frequency": habit.TargetFrequency,
	}

	utils.SuccessResponse(c, 200, "Stats retrieved successfully", stats)
}
