# Deployment Guide

## 🌐 Deployment Options

### 1. Traditional VPS (DigitalOcean, AWS EC2, etc.)

#### Prerequisites
- Ubuntu 20.04+ or similar Linux distribution
- PostgreSQL installed
- Go 1.21+ installed
- Domain name (optional but recommended)

#### Steps

1. **Setup PostgreSQL**
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo -u postgres createdb myhabit_db
sudo -u postgres createuser myhabit_user -P
```

2. **Clone Repository**
```bash
git clone https://github.com/yourusername/myhabit-backend.git
cd myhabit-backend/backend
```

3. **Configure Environment**
```bash
cp .env.example .env
nano .env
```

Update with production values:
```env
PORT=8080
GIN_MODE=release

DB_HOST=localhost
DB_PORT=5432
DB_USER=myhabit_user
DB_PASSWORD=strong_password_here
DB_NAME=myhabit_db
DB_SSLMODE=require

JWT_SECRET=very-long-random-secret-key
JWT_EXPIRATION_HOURS=168

ALLOWED_ORIGINS=https://yourdomain.com
```

4. **Build and Run**
```bash
go mod download
go build -o server cmd/api/main.go
./server
```

5. **Setup as Service (systemd)**

Create `/etc/systemd/system/myhabit.service`:
```ini
[Unit]
Description=MyHabit Backend API
After=network.target postgresql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu/myhabit-backend/backend
ExecStart=/home/ubuntu/myhabit-backend/backend/server
Restart=on-failure
RestartSec=5s

Environment="GIN_MODE=release"

[Install]
WantedBy=multi-user.target
```

Enable and start:
```bash
sudo systemctl enable myhabit
sudo systemctl start myhabit
sudo systemctl status myhabit
```

6. **Setup Nginx Reverse Proxy**

Install Nginx:
```bash
sudo apt install nginx
```

Create `/etc/nginx/sites-available/myhabit`:
```nginx
server {
    listen 80;
    server_name api.yourdomain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable site:
```bash
sudo ln -s /etc/nginx/sites-available/myhabit /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

7. **Setup SSL with Let's Encrypt**
```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d api.yourdomain.com
```

### 2. Docker Deployment

#### Create docker-compose.yml

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    environment:
      POSTGRES_USER: myhabit_user
      POSTGRES_PASSWORD: your_password
      POSTGRES_DB: myhabit_db
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myhabit_user"]
      interval: 10s
      timeout: 5s
      retries: 5

  api:
    build: .
    ports:
      - "8080:8080"
    environment:
      PORT: 8080
      GIN_MODE: release
      DB_HOST: postgres
      DB_PORT: 5432
      DB_USER: myhabit_user
      DB_PASSWORD: your_password
      DB_NAME: myhabit_db
      DB_SSLMODE: disable
      JWT_SECRET: your-secret-key
      JWT_EXPIRATION_HOURS: 168
      ALLOWED_ORIGINS: "*"
    depends_on:
      postgres:
        condition: service_healthy
    restart: unless-stopped

volumes:
  postgres_data:
```

#### Deploy
```bash
docker-compose up -d
```

### 3. Heroku Deployment

#### Install Heroku CLI
```bash
curl https://cli-assets.heroku.com/install.sh | sh
```

#### Deploy
```bash
heroku login
heroku create myhabit-api
heroku addons:create heroku-postgresql:hobby-dev

# Set environment variables
heroku config:set GIN_MODE=release
heroku config:set JWT_SECRET=your-secret-key
heroku config:set JWT_EXPIRATION_HOURS=168

# Deploy
git push heroku main
```

### 4. Railway.app Deployment

1. Go to https://railway.app
2. Connect your GitHub repository
3. Add PostgreSQL database
4. Set environment variables in Railway dashboard
5. Deploy automatically

### 5. Render.com Deployment

1. Go to https://render.com
2. Create new Web Service
3. Connect repository
4. Add PostgreSQL database
5. Set environment variables:
   - `GIN_MODE=release`
   - `JWT_SECRET=your-secret`
   - Database variables (auto-filled)
6. Deploy

## 🔒 Security Best Practices

1. **Use strong JWT secret**
   ```bash
   openssl rand -base64 32
   ```

2. **Enable SSL/TLS** - Always use HTTPS in production

3. **Database Security**
   - Use strong passwords
   - Enable SSL mode
   - Restrict access by IP

4. **Environment Variables** - Never commit `.env` to git

5. **Rate Limiting** - Add rate limiting middleware

6. **CORS** - Configure specific allowed origins

7. **Database Backups**
   ```bash
   pg_dump myhabit_db > backup_$(date +%Y%m%d).sql
   ```

## 📊 Monitoring

### Setup Logging
Use structured logging in production:
```go
log.SetFormatter(&log.JSONFormatter{})
```

### Health Checks
Monitor `/health` endpoint:
```bash
curl http://localhost:8080/health
```

### Database Monitoring
```sql
-- Active connections
SELECT count(*) FROM pg_stat_activity;

-- Long running queries
SELECT pid, now() - query_start as duration, query
FROM pg_stat_activity
WHERE state = 'active'
ORDER BY duration DESC;
```

## 🚀 Performance Optimization

1. **Database Indexing**
```sql
CREATE INDEX idx_habits_user_id ON habits(user_id);
CREATE INDEX idx_checkins_habit_id ON check_ins(habit_id);
CREATE INDEX idx_checkins_date ON check_ins(date);
```

2. **Connection Pooling** - Configure in database.go

3. **Caching** - Add Redis for frequently accessed data

4. **CDN** - Use CDN for static assets

## 🔄 CI/CD with GitHub Actions

Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy to Production

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2

      - name: Set up Go
        uses: actions/setup-go@v2
        with:
          go-version: 1.21

      - name: Run tests
        run: |
          cd backend
          go test ./...

      - name: Build
        run: |
          cd backend
          go build -o server cmd/api/main.go

      - name: Deploy to server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.SSH_KEY }}
          script: |
            cd /home/ubuntu/myhabit-backend
            git pull
            cd backend
            go build -o server cmd/api/main.go
            sudo systemctl restart myhabit
```

## 📈 Scaling

### Horizontal Scaling
- Use load balancer (Nginx, HAProxy)
- Deploy multiple API instances
- Use managed PostgreSQL (AWS RDS, DigitalOcean Managed Databases)

### Vertical Scaling
- Upgrade server resources
- Optimize queries
- Add caching layer

## 🆘 Troubleshooting

### Check logs
```bash
sudo journalctl -u myhabit -f
```

### Database connection issues
```bash
psql -h localhost -U myhabit_user -d myhabit_db
```

### Port already in use
```bash
lsof -i :8080
kill -9 <PID>
```
