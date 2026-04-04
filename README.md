# 🚂 RailYatra — Advanced Train Booking Platform

> Production-ready IRCTC alternative. 100% free stack. Startup-grade.

## 🚀 Quick Start (5 minutes)

```bash
# 1. Clone / unzip this project
# 2. Run setup script
chmod +x setup.sh && ./setup.sh

# 3. Start backend (Terminal 1)
cd backend && mvn spring-boot:run

# 4. Start frontend (Terminal 2)
cd frontend && npm run dev

# 5. Open http://localhost:5173
```

**Demo accounts:**
| Role  | Email                    | Password   |
|-------|--------------------------|------------|
| Admin | admin@railyatra.com      | Admin@123  |
| User  | demo@railyatra.com       | Demo@1234  |

---

## 🐳 Docker (even easier)

```bash
# Starts Postgres + Redis automatically
docker-compose up -d postgres redis

# Then run backend + frontend as above
cd backend && mvn spring-boot:run
cd frontend && npm run dev
```

---

## 📁 Project Structure

```
railyatra/
├── backend/                          # Spring Boot (Java 17)
│   ├── src/main/java/com/railyatra/
│   │   ├── config/                   # Security, CORS, Redis, WebSocket
│   │   ├── controller/               # 6 REST controllers
│   │   ├── dto/request/              # Input DTOs (validated)
│   │   ├── dto/response/             # Output DTOs
│   │   ├── entity/                   # 6 JPA entities
│   │   ├── exception/                # Global error handler
│   │   ├── repository/               # 5 JPA repositories
│   │   ├── security/                 # JWT filter + provider
│   │   ├── service/                  # 7 business logic services
│   │   └── websocket/                # Real-time seat handler
│   └── src/main/resources/
│       ├── application.yml           # All config (env-var driven)
│       └── data.sql                  # 10 sample trains + demo users
├── frontend/                         # React 18 + Vite + Tailwind
│   └── src/
│       ├── api/                      # 4 Axios API modules
│       ├── components/common/        # Navbar, Footer
│       ├── hooks/                    # useDebounce
│       ├── pages/                    # 9 pages + Admin
│       ├── store/                    # 3 Zustand stores
│       └── utils/                    # formatters
├── docker-compose.yml                # Postgres + Redis
├── setup.sh                          # One-shot setup
└── README.md
```

---

## 🛠️ Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Backend     | Java 17, Spring Boot 3.2            |
| Security    | Spring Security, JWT (jjwt 0.12.3)  |
| Database    | PostgreSQL + Spring Data JPA        |
| Cache       | Redis + Spring Cache                |
| Real-time   | WebSocket + STOMP                   |
| Rate Limit  | Bucket4j                            |
| PDF         | OpenPDF                             |
| Email       | Spring Mail (Gmail SMTP)            |
| Payments    | Razorpay (test mode)                |
| Frontend    | React 18, Vite, Tailwind CSS        |
| State       | Zustand                             |
| HTTP        | Axios (with JWT interceptor)        |

---

## 🔑 Environment Variables (backend)

Edit `backend/src/main/resources/application.yml` or set as env vars:

```
DATABASE_URL         jdbc:postgresql://localhost:5432/railyatra
DATABASE_USERNAME    postgres
DATABASE_PASSWORD    postgres
REDIS_URL            redis://localhost:6379
JWT_SECRET           (any string, min 32 chars)
RAZORPAY_KEY_ID      rzp_test_... (from razorpay.com)
RAZORPAY_KEY_SECRET  (from razorpay.com)
MAIL_USERNAME        your@gmail.com
MAIL_PASSWORD        your-gmail-app-password
FRONTEND_URL         http://localhost:5173
```

---

## 📡 API Endpoints

```
POST /api/auth/register
POST /api/auth/login
GET  /api/auth/me

GET  /api/trains/search?from=&to=&date=
GET  /api/trains/{id}
GET  /api/trains/stations
GET  /api/trains/popular

POST   /api/bookings
GET    /api/bookings/my
GET    /api/bookings/{id}
GET    /api/bookings/pnr/{pnr}      (public)
DELETE /api/bookings/{id}/cancel
GET    /api/bookings/{id}/ticket    (PDF download)
GET    /api/bookings/wl-predict     (public)

POST /api/payments/create-order
POST /api/payments/verify

GET    /api/admin/analytics         (ADMIN)
POST   /api/admin/trains            (ADMIN)
PUT    /api/admin/trains/{id}       (ADMIN)
DELETE /api/admin/trains/{id}       (ADMIN)
GET    /api/admin/bookings          (ADMIN)
```

---

## 🚀 Deploy to Production (Free)

1. **Database** → [Supabase](https://supabase.com) (free PostgreSQL)
2. **Redis**    → [Upstash](https://upstash.com) (free Redis)
3. **Backend**  → [Render](https://render.com) (free Web Service)
4. **Frontend** → [Vercel](https://vercel.com) (free)

See full step-by-step in the previous DEPLOYMENT.md guide.

---

## 🔒 Security

- BCrypt password hashing (cost=12)
- JWT access tokens (24h) + refresh tokens (7d)
- Rate limiting: 5 login/min, 3 bookings/min per user
- HMAC-SHA256 Razorpay payment verification
- Role-based access: USER / ADMIN
- Input validation on all DTOs
- SQL injection prevention via JPA

---

Built by Harish Kumar 🚀 — Portfolio project demonstrating production-grade Java + React development.
