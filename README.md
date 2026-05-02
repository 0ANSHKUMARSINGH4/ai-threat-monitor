# AI Threat Monitor 🛡️

A production-grade API rate-limiting SaaS that intercepts traffic, performs real-time behavioral analysis using Groq AI, and enforces dynamic rate limits with a resilient rule-based fallback.

## Live Demo

| Service | URL |
|---|---|
| Frontend Dashboard | https://ai-threat-monitor.vercel.app |
| Backend API | https://ai-threat-monitor.onrender.com |
| Health Check | https://ai-threat-monitor.onrender.com/health |

**Demo credentials:** `admin` / `admin123`

> Note: Backend is on Render free tier — first load after inactivity 
> may take up to 60 seconds.

---

## 🏗️ Tech Stack

**Backend (Spring Boot)**
- Java 17
- Spring Data JPA (MySQL)
- Spring Data Redis
- Groq AI (Llama 3 Integration)
- Standard SLF4J/Logback logging

**Frontend (React)**
- Vite & Tailwind CSS v4
- Lucide Icons & Recharts (Analytics Dashboard)
- Axios for API interaction

---

## 🚀 Getting Started

### 1. Prerequisites
- **Java 17** and **Maven**
- **Node.js** (v18+)
- **MySQL** and **Redis** installed locally or reachable via cloud.

### 2. Backend Setup
1. Navigate to `/backend`.
2. Copy `.env.example` to `.env` (or set environment variables).
3. Configure your `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `AI_API_KEY` (Get one from [Groq](https://console.groq.com/)).
4. Run the application:
   ```bash
   mvn clean spring-boot:run
   ```

### 3. Frontend Setup
1. Navigate to `/frontend`.
2. Copy `.env.example` to `.env`.
3. Install dependencies and run:
   ```bash
   npm install
   npm run dev
   ```

## Environment Variables

| Variable | Description | Example |
|---|---|---|
| CORS_ALLOWED_ORIGIN | Frontend URL for CORS | https://your-app.vercel.app |
| DATABASE_URL | PostgreSQL URL (Render provides this) | postgres://user:pass@host/db |
| GROQ_API_KEY | Groq API key for Llama-3 | gsk_... |

---

## Running Locally with Docker

### Prerequisites
- Docker Desktop installed and running
- A Groq API key from https://console.groq.com

### Steps

1. Clone the repository
   ```bash
   git clone https://github.com/YOUR_USERNAME/ai-threat-monitor.git
   cd ai-threat-monitor
   ```

2. Set environment variables
   Create a `.env` file in the `backend/` directory:
   ```env
   GROQ_API_KEY=your_groq_key_here
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=admin123
   ```

3. Start the full stack
   ```bash
   docker-compose up --build
   ```

4. Access the dashboard
   - **Frontend:** http://localhost:5173
   - **Backend:**  http://localhost:8080
   - **Login:**    `admin` / `admin123`

### Services started by docker-compose
- Spring Boot backend (port 8080)
- MySQL database (port 3306)
- Redis (port 6379)
- React frontend via Vite dev server (port 5173)

---

## 📡 Deployment

### Backend (Render)
- **Build Command**: `mvn clean package -DskipTests`
- **Start Command**: `java -jar target/*.jar --spring.profiles.active=prod`
- **Environment Variables**: Map all variables from `backend/.env.example`.

### Frontend (Vercel)
- **Framework Preset**: Vite
- **Root Directory**: `frontend`
- **Environment Variables**: Map `VITE_API_URL` to your backend endpoint.

---

## 🛠️ Testing the Rate Limiter
Use the provided simulation script to generate anomalous traffic profiles:
```powershell
.\simulate_traffic.ps1
```
The dashboard will dynamically update to show AI-classified behavioral states.

---

## ⚠️ Known Limitations
The backend is hosted on Render's free tier and may experience a cold start delay of up to 60 seconds after periods of inactivity. This is a hosting constraint, not an application bug.
