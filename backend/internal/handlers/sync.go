package handlers

import (
	"time"

	"myhabit-backend/internal/database"
	"myhabit-backend/internal/middleware"
	"myhabit-backend/internal/models"
	"myhabit-backend/internal/utils"

	"github.com/gin-gonic/gin"
)

type SyncHandler struct{}

func NewSyncHandler() *SyncHandler {
	return &SyncHandler{}
}

// SyncData godoc
// @Summary Sync data from mobile
// @Description Upload habits and check-ins from mobile app
// @Tags sync
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body models.SyncRequest true "Data to sync"
// @Success 200 {object} utils.Response
// @Router /api/sync [post]
func (h *SyncHandler) SyncData(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req models.SyncRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	habitsCreated := 0
	habitsUpdated := 0
	checkInsCreated := 0

	// Sync habits
	for _, habit := range req.Habits {
		habit.UserID = userID

		var existing models.Habit
		result := database.DB.Where("id = ? AND user_id = ?", habit.ID, userID).First(&existing)

		if result.Error != nil {
			// Create new habit
			if err := database.DB.Create(&habit).Error; err == nil {
				habitsCreated++
			}
		} else {
			// Update existing habit
			if err := database.DB.Model(&existing).Updates(habit).Error; err == nil {
				habitsUpdated++
			}
		}
	}

	// Sync check-ins
	for _, checkIn := range req.CheckIns {
		// Verify habit exists and belongs to user
		var habit models.Habit
		if err := database.DB.Where("id = ? AND user_id = ?", checkIn.HabitID, userID).First(&habit).Error; err != nil {
			continue // Skip if habit not found
		}

		// Check if check-in already exists
		var existing models.CheckIn
		result := database.DB.Where("habit_id = ? AND date = ?", checkIn.HabitID, checkIn.Date).First(&existing)

		if result.Error != nil {
			// Create new check-in
			if checkIn.Timestamp.IsZero() {
				checkIn.Timestamp = time.Now()
			}
			if err := database.DB.Create(&checkIn).Error; err == nil {
				checkInsCreated++
			}
		}
	}

	// Get all user data to send back
	var habits []models.Habit
	database.DB.Where("user_id = ?", userID).Preload("CheckIns").Find(&habits)

	var allCheckIns []models.CheckIn
	database.DB.Joins("JOIN habits ON habits.id = check_ins.habit_id").
		Where("habits.user_id = ?", userID).
		Find(&allCheckIns)

	response := models.SyncResponse{
		Success:         true,
		HabitsCreated:   habitsCreated,
		HabitsUpdated:   habitsUpdated,
		CheckInsCreated: checkInsCreated,
		Habits:          habits,
		CheckIns:        allCheckIns,
	}

	utils.SuccessResponse(c, 200, "Data synced successfully", response)
}

// GetAllData godoc
// @Summary Get all user data
// @Description Download all habits and check-ins
// @Tags sync
// @Produce json
// @Security BearerAuth
// @Success 200 {object} utils.Response
// @Router /api/sync [get]
func (h *SyncHandler) GetAllData(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var habits []models.Habit
	if err := database.DB.Where("user_id = ?", userID).Preload("CheckIns").Find(&habits).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to fetch habits")
		return
	}

	var checkIns []models.CheckIn
	database.DB.Joins("JOIN habits ON habits.id = check_ins.habit_id").
		Where("habits.user_id = ?", userID).
		Find(&checkIns)

	data := models.ExportData{
		ExportDate: time.Now().Unix(),
		Version:    "1.0",
		Habits:     habits,
		CheckIns:   checkIns,
	}

	utils.SuccessResponse(c, 200, "Data retrieved successfully", data)
}
