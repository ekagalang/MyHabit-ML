package handlers

import (
	"time"

	"myhabit-backend/internal/database"
	"myhabit-backend/internal/middleware"
	"myhabit-backend/internal/models"
	"myhabit-backend/internal/utils"

	"github.com/gin-gonic/gin"
)

type CheckInHandler struct{}

func NewCheckInHandler() *CheckInHandler {
	return &CheckInHandler{}
}

// GetCheckIns godoc
// @Summary Get check-ins
// @Description Get all check-ins for user's habits
// @Tags checkins
// @Produce json
// @Security BearerAuth
// @Param habit_id query int false "Filter by habit ID"
// @Param date query string false "Filter by date (yyyy-MM-dd)"
// @Success 200 {object} utils.Response
// @Router /api/checkins [get]
func (h *CheckInHandler) GetCheckIns(c *gin.Context) {
	userID := middleware.GetUserID(c)
	habitIDStr := c.Query("habit_id")
	date := c.Query("date")

	query := database.DB.Joins("JOIN habits ON habits.id = check_ins.habit_id").
		Where("habits.user_id = ?", userID)

	if habitIDStr != "" {
		query = query.Where("check_ins.habit_id = ?", habitIDStr)
	}

	if date != "" {
		query = query.Where("check_ins.date = ?", date)
	}

	var checkIns []models.CheckIn
	if err := query.Order("check_ins.timestamp DESC").Find(&checkIns).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to fetch check-ins")
		return
	}

	utils.SuccessResponse(c, 200, "Check-ins retrieved successfully", checkIns)
}

// CreateCheckIn godoc
// @Summary Create a check-in
// @Description Create a new check-in for a habit
// @Tags checkins
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body models.CheckInCreateRequest true "Check-in data"
// @Success 201 {object} utils.Response
// @Router /api/checkins [post]
func (h *CheckInHandler) CreateCheckIn(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req models.CheckInCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	// Verify habit belongs to user
	var habit models.Habit
	if err := database.DB.Where("id = ? AND user_id = ?", req.HabitID, userID).First(&habit).Error; err != nil {
		utils.ErrorResponse(c, 404, "Habit not found")
		return
	}

	// Check if check-in already exists for this date
	var existing models.CheckIn
	if err := database.DB.Where("habit_id = ? AND date = ?", req.HabitID, req.Date).First(&existing).Error; err == nil {
		utils.ErrorResponse(c, 400, "Check-in already exists for this date")
		return
	}

	checkIn := models.CheckIn{
		HabitID:     req.HabitID,
		Timestamp:   time.Now(),
		Date:        req.Date,
		CompletedAt: req.CompletedAt,
		Note:        req.Note,
		Mood:        req.Mood,
		IsLate:      req.IsLate,
		MinutesLate: req.MinutesLate,
		EnergyLevel: req.EnergyLevel,
		StressLevel: req.StressLevel,
		Location:    req.Location,
		Weather:     req.Weather,
	}

	if err := database.DB.Create(&checkIn).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to create check-in")
		return
	}

	utils.SuccessResponse(c, 201, "Check-in created successfully", checkIn)
}

// BulkCreateCheckIns godoc
// @Summary Bulk create check-ins
// @Description Create multiple check-ins at once
// @Tags checkins
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body models.CheckInBulkCreateRequest true "Bulk check-ins data"
// @Success 201 {object} utils.Response
// @Router /api/checkins/bulk [post]
func (h *CheckInHandler) BulkCreateCheckIns(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req models.CheckInBulkCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	// Verify all habits belong to user
	habitIDs := make([]uint, 0)
	for _, checkIn := range req.CheckIns {
		habitIDs = append(habitIDs, checkIn.HabitID)
	}

	var habits []models.Habit
	if err := database.DB.Where("user_id = ? AND id IN ?", userID, habitIDs).Find(&habits).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to verify habits")
		return
	}

	if len(habits) != len(uniqueUints(habitIDs)) {
		utils.ErrorResponse(c, 400, "One or more habits not found")
		return
	}

	// Create check-ins
	checkIns := make([]models.CheckIn, 0)
	for _, item := range req.CheckIns {
		checkIn := models.CheckIn{
			HabitID:     item.HabitID,
			Timestamp:   time.Now(),
			Date:        item.Date,
			CompletedAt: item.CompletedAt,
			Note:        item.Note,
			Mood:        item.Mood,
			IsLate:      item.IsLate,
			MinutesLate: item.MinutesLate,
			EnergyLevel: item.EnergyLevel,
			StressLevel: item.StressLevel,
			Location:    item.Location,
			Weather:     item.Weather,
		}
		checkIns = append(checkIns, checkIn)
	}

	if err := database.DB.Create(&checkIns).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to create check-ins")
		return
	}

	utils.SuccessResponse(c, 201, "Check-ins created successfully", map[string]interface{}{
		"count":     len(checkIns),
		"check_ins": checkIns,
	})
}

// DeleteCheckIn godoc
// @Summary Delete a check-in
// @Description Delete a check-in
// @Tags checkins
// @Security BearerAuth
// @Param id path int true "Check-in ID"
// @Success 200 {object} utils.Response
// @Router /api/checkins/{id} [delete]
func (h *CheckInHandler) DeleteCheckIn(c *gin.Context) {
	userID := middleware.GetUserID(c)
	checkInID := c.Param("id")

	var checkIn models.CheckIn
	if err := database.DB.Joins("JOIN habits ON habits.id = check_ins.habit_id").
		Where("check_ins.id = ? AND habits.user_id = ?", checkInID, userID).
		First(&checkIn).Error; err != nil {
		utils.ErrorResponse(c, 404, "Check-in not found")
		return
	}

	if err := database.DB.Delete(&checkIn).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to delete check-in")
		return
	}

	utils.SuccessResponse(c, 200, "Check-in deleted successfully", nil)
}

func uniqueUints(arr []uint) []uint {
	keys := make(map[uint]bool)
	list := []uint{}
	for _, entry := range arr {
		if _, value := keys[entry]; !value {
			keys[entry] = true
			list = append(list, entry)
		}
	}
	return list
}
