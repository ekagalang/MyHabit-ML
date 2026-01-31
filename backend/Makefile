.PHONY: run build test clean migrate seed dev

# Run development server
dev:
	@echo "🚀 Starting development server..."
	go run cmd/api/main.go

# Build binary
build:
	@echo "📦 Building binary..."
	go build -o bin/myhabit-server cmd/api/main.go

# Run server
run: build
	@echo "🏃 Running server..."
	./bin/myhabit-server

# Run tests
test:
	@echo "🧪 Running tests..."
	go test -v ./...

# Run tests with coverage
test-coverage:
	@echo "📊 Running tests with coverage..."
	go test -cover ./...

# Clean build artifacts
clean:
	@echo "🧹 Cleaning..."
	rm -rf bin/

# Install dependencies
deps:
	@echo "📥 Installing dependencies..."
	go mod download
	go mod tidy

# Format code
fmt:
	@echo "✨ Formatting code..."
	go fmt ./...

# Lint code
lint:
	@echo "🔍 Linting code..."
	golangci-lint run

# Database setup
db-create:
	@echo "🗄️ Creating database..."
	createdb myhabit_db

# Database drop
db-drop:
	@echo "⚠️ Dropping database..."
	dropdb myhabit_db

# Help
help:
	@echo "Available commands:"
	@echo "  make dev           - Run development server"
	@echo "  make build         - Build binary"
	@echo "  make run           - Build and run server"
	@echo "  make test          - Run tests"
	@echo "  make test-coverage - Run tests with coverage"
	@echo "  make clean         - Clean build artifacts"
	@echo "  make deps          - Install dependencies"
	@echo "  make fmt           - Format code"
	@echo "  make lint          - Lint code"
	@echo "  make db-create     - Create database"
	@echo "  make db-drop       - Drop database"
