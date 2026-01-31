package handlers

import (
	"myhabit-backend/internal/database"
	"myhabit-backend/internal/middleware"
	"myhabit-backend/internal/models"
	"myhabit-backend/internal/utils"

	"github.com/gin-gonic/gin"
)

type TemplateHandler struct{}

func NewTemplateHandler() *TemplateHandler {
	return &TemplateHandler{}
}

// GetTemplates godoc
// @Summary Get habit templates
// @Description Get all templates (system + user's custom templates)
// @Tags templates
// @Produce json
// @Security BearerAuth
// @Success 200 {object} utils.Response
// @Router /api/templates [get]
func (h *TemplateHandler) GetTemplates(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var templates []models.HabitTemplate
	if err := database.DB.Where("is_system_template = ? OR user_id = ?", true, userID).
		Order("is_system_template DESC, created_at DESC").
		Find(&templates).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to fetch templates")
		return
	}

	utils.SuccessResponse(c, 200, "Templates retrieved successfully", templates)
}

// CreateTemplate godoc
// @Summary Create custom template
// @Description Create a custom habit template
// @Tags templates
// @Accept json
// @Produce json
// @Security BearerAuth
// @Param request body models.TemplateCreateRequest true "Template data"
// @Success 201 {object} utils.Response
// @Router /api/templates [post]
func (h *TemplateHandler) CreateTemplate(c *gin.Context) {
	userID := middleware.GetUserID(c)

	var req models.TemplateCreateRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		utils.ValidationErrorResponse(c, err)
		return
	}

	template := models.HabitTemplate{
		UserID:             &userID,
		Name:               req.Name,
		Description:        req.Description,
		Category:           req.Category,
		Icon:               req.Icon,
		DefaultFrequency:   req.DefaultFrequency,
		DefaultReminderTime: req.DefaultReminderTime,
		Tags:               req.Tags,
		IsSystemTemplate:   false,
	}

	if err := database.DB.Create(&template).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to create template")
		return
	}

	utils.SuccessResponse(c, 201, "Template created successfully", template)
}

// DeleteTemplate godoc
// @Summary Delete custom template
// @Description Delete user's custom template
// @Tags templates
// @Security BearerAuth
// @Param id path int true "Template ID"
// @Success 200 {object} utils.Response
// @Router /api/templates/{id} [delete]
func (h *TemplateHandler) DeleteTemplate(c *gin.Context) {
	userID := middleware.GetUserID(c)
	templateID := c.Param("id")

	var template models.HabitTemplate
	if err := database.DB.Where("id = ? AND user_id = ? AND is_system_template = ?", templateID, userID, false).
		First(&template).Error; err != nil {
		utils.ErrorResponse(c, 404, "Template not found or cannot be deleted")
		return
	}

	if err := database.DB.Delete(&template).Error; err != nil {
		utils.ErrorResponse(c, 500, "Failed to delete template")
		return
	}

	utils.SuccessResponse(c, 200, "Template deleted successfully", nil)
}
