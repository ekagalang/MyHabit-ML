# MyHabit Backend API

Backend API untuk aplikasi MyHabit - Habit Tracker dengan Machine Learning.

## 🚀 Features

- ✅ **Authentication** - JWT-based authentication
- ✅ **User Management** - Register, login, profile
- ✅ **Habits CRUD** - Create, read, update, delete habits
- ✅ **Check-ins** - Track daily habit completions
- ✅ **Data Sync** - Sync data from mobile app
- ✅ **Habit Templates** - Pre-built and custom templates
- ✅ **Advanced Tracking** - Mood, energy, stress, location, weather
- ✅ **PostgreSQL** - Robust database with GORM
- ✅ **CORS Support** - Cross-origin resource sharing
- ✅ **Graceful Shutdown** - Clean server shutdown

## 📋 Prerequisites

- Go 1.21 or higher
- PostgreSQL 14 or higher
- Git

## 🛠️ Installation

### 1. Clone Repository

```bash
cd backend
```

### 2. Install Dependencies

```bash
go mod download
```

### 3. Setup Database

Create PostgreSQL database:

```sql
CREATE DATABASE myhabit_db;
```

### 4. Configure Environment

Copy `.env.example` to `.env` and update:

```bash
cp .env.example .env
```

Edit `.env`:

```env
PORT=8080
GIN_MODE=debug

DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=your_password
DB_NAME=myhabit_db
DB_SSLMODE=disable

JWT_SECRET=your-super-secret-jwt-key-change-this
JWT_EXPIRATION_HOURS=168

ALLOWED_ORIGINS=http://localhost:3000,http://localhost:8080
```

### 5. Run Migrations

Migrations run automatically on startup. Database tables will be created:

- `users`
- `habits`
- `check_ins`
- `habit_templates`

### 6. Run Server

```bash
go run cmd/api/main.go
```

Server will start on `http://localhost:8080`

## 📚 API Endpoints

### Authentication

#### Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "name": "John Doe"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe"
    }
  }
}
```

#### Get Profile
```http
GET /api/auth/profile
Authorization: Bearer <token>
```

### Habits

#### Get All Habits
```http
GET /api/habits?include_checkins=true
Authorization: Bearer <token>
```

#### Get Single Habit
```http
GET /api/habits/:id?include_checkins=true
Authorization: Bearer <token>
```

#### Create Habit
```http
POST /api/habits
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Morning Workout",
  "description": "30 minutes cardio",
  "category": "health",
  "target_frequency": 5,
  "reminder_time": "06:30",
  "reminder_enabled": true,
  "repeat_days": "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY",
  "color": "#EF4444",
  "icon": "💪"
}
```

#### Update Habit
```http
PUT /api/habits/:id
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Updated Name",
  "is_active": false
}
```

#### Delete Habit
```http
DELETE /api/habits/:id
Authorization: Bearer <token>
```

### Check-ins

#### Get Check-ins
```http
GET /api/checkins?habit_id=1&date=2025-01-15
Authorization: Bearer <token>
```

#### Create Check-in
```http
POST /api/checkins
Authorization: Bearer <token>
Content-Type: application/json

{
  "habit_id": 1,
  "date": "2025-01-15",
  "completed_at": "06:35",
  "note": "Great workout!",
  "mood": "very_happy",
  "energy_level": 5,
  "stress_level": 1,
  "location": "gym",
  "weather": "sunny"
}
```

#### Bulk Create Check-ins
```http
POST /api/checkins/bulk
Authorization: Bearer <token>
Content-Type: application/json

{
  "check_ins": [
    {
      "habit_id": 1,
      "date": "2025-01-15",
      "completed_at": "06:35",
      "mood": "happy"
    },
    {
      "habit_id": 2,
      "date": "2025-01-15",
      "completed_at": "07:00"
    }
  ]
}
```

### Data Sync

#### Download All Data
```http
GET /api/sync
Authorization: Bearer <token>
```

Response:
```json
{
  "success": true,
  "data": {
    "exportDate": 1738195200,
    "version": "1.0",
    "habits": [...],
    "checkIns": [...]
  }
}
```

#### Upload Data from Mobile
```http
POST /api/sync
Authorization: Bearer <token>
Content-Type: application/json

{
  "habits": [...],
  "check_ins": [...]
}
```

### Templates

#### Get Templates
```http
GET /api/templates
Authorization: Bearer <token>
```

#### Create Custom Template
```http
POST /api/templates
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Custom Habit",
  "description": "My custom habit",
  "category": "health",
  "icon": "🎯",
  "default_frequency": "daily",
  "tags": "custom,health"
}
```

## 🗄️ Database Schema

### Users Table
```sql
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL,
  profile_photo VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);
```

### Habits Table
```sql
CREATE TABLE habits (
  id SERIAL PRIMARY KEY,
  user_id INTEGER REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  category VARCHAR(50) NOT NULL,
  target_frequency INTEGER NOT NULL,
  reminder_time VARCHAR(5),
  reminder_enabled BOOLEAN DEFAULT FALSE,
  repeat_days TEXT,
  color VARCHAR(7) DEFAULT '#6366F1',
  icon VARCHAR(10) DEFAULT '🎯',
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);
```

### Check-ins Table
```sql
CREATE TABLE check_ins (
  id SERIAL PRIMARY KEY,
  habit_id INTEGER REFERENCES habits(id) ON DELETE CASCADE,
  timestamp TIMESTAMP NOT NULL,
  date VARCHAR(10) NOT NULL,
  completed_at VARCHAR(5) NOT NULL,
  note TEXT,
  mood VARCHAR(20),
  is_late BOOLEAN DEFAULT FALSE,
  minutes_late INTEGER DEFAULT 0,
  energy_level INTEGER,
  stress_level INTEGER,
  location VARCHAR(50),
  weather VARCHAR(20),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);
```

## 🔐 Security

- Passwords hashed with bcrypt
- JWT tokens for authentication
- CORS configured
- SQL injection protection via GORM
- Environment variables for secrets

## 🏗️ Project Structure

```
backend/
├── cmd/
│   └── api/
│       ├── main.go           # Application entry point
│       └── routes.go         # API routes configuration
├── internal/
│   ├── config/
│   │   └── config.go         # Configuration loader
│   ├── database/
│   │   └── database.go       # Database connection & migrations
│   ├── handlers/
│   │   ├── auth.go           # Authentication handlers
│   │   ├── habit.go          # Habit handlers
│   │   ├── checkin.go        # Check-in handlers
│   │   ├── sync.go           # Sync handlers
│   │   └── template.go       # Template handlers
│   ├── middleware/
│   │   ├── auth.go           # JWT authentication middleware
│   │   └── cors.go           # CORS middleware
│   ├── models/
│   │   ├── user.go           # User model
│   │   ├── habit.go          # Habit model
│   │   ├── checkin.go        # Check-in model
│   │   ├── habit_template.go # Template model
│   │   └── sync.go           # Sync models
│   └── utils/
│       ├── jwt.go            # JWT utilities
│       └── response.go       # Response helpers
├── .env.example              # Environment variables example
├── .gitignore
├── go.mod
├── go.sum
└── README.md
```

## 🧪 Testing

Run tests:
```bash
go test ./...
```

Run with coverage:
```bash
go test -cover ./...
```

## 📦 Building for Production

Build binary:
```bash
go build -o myhabit-server cmd/api/main.go
```

Run production server:
```bash
GIN_MODE=release ./myhabit-server
```

## 🐳 Docker (Optional)

Create `Dockerfile`:
```dockerfile
FROM golang:1.21-alpine AS builder
WORKDIR /app
COPY go.* ./
RUN go mod download
COPY . .
RUN go build -o server cmd/api/main.go

FROM alpine:latest
WORKDIR /app
COPY --from=builder /app/server .
COPY .env .
EXPOSE 8080
CMD ["./server"]
```

Build and run:
```bash
docker build -t myhabit-backend .
docker run -p 8080:8080 --env-file .env myhabit-backend
```

## 📝 License

MIT License

## 👨‍💻 Author

MyHabit Backend API - 2025
