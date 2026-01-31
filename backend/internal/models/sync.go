package models

type SyncRequest struct {
	Habits   []Habit   `json:"habits" binding:"required,dive"`
	CheckIns []CheckIn `json:"check_ins" binding:"required,dive"`
}

type SyncResponse struct {
	Success       bool      `json:"success"`
	HabitsCreated int       `json:"habits_created"`
	HabitsUpdated int       `json:"habits_updated"`
	CheckInsCreated int     `json:"check_ins_created"`
	Habits        []Habit   `json:"habits"`
	CheckIns      []CheckIn `json:"check_ins"`
}

type ExportData struct {
	ExportDate int64     `json:"exportDate"`
	Version    string    `json:"version"`
	Habits     []Habit   `json:"habits"`
	CheckIns   []CheckIn `json:"checkIns"`
}
