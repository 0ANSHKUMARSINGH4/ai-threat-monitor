# AI Threat Monitor 🛡️

A production-grade API rate-limiting SaaS that intercepts traffic, performs real-time behavioral analysis using Groq AI, and enforces dynamic rate limits with a resilient rule-based fallback.

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
