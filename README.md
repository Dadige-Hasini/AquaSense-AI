# AquaSense AI 🌊

> **Intelligent Reservoir Analytics & Hydro-Forecasting Platform**  
> Scalable microservices ecosystem designed for real-time hydrological monitoring, storage predictions, and water resource management.

---

## 🏛️ Architecture Overview

The system consists of 4 core services orchestrated via Docker Compose:

| Service | Technology | Port | Description |
| :--- | :--- | :--- | :--- |
| **Database** | PostgreSQL 16 | `5432` | Stores historical sensor records, CSV staging data, AI forecasts, and analytical views |
| **Backend** | Java 17 / Spring Boot 3 | `8080` | REST API layer, JPA entity persistence, data access and system health metrics |
| **ML Engine** | Python 3.11 / FastAPI | `8000` | Scikit-learn time-series forecasting, lag regression, and automated model training |
| **Frontend** | React 18 / Vite | `3000` (`5173` in dev) | Modern reactive dashboard, forecast controls, and system telemetry monitor |

---

## 📁 Project Directory Structure

```text
AquaSense AI/
├── docker-compose.yml              # Multi-container orchestration
├── .env.example                    # Environment variable configuration template
├── .env                            # Default environment configuration
├── .gitignore                      # Git exclusion rules
├── README.md                       # Architecture & setup documentation
├── data/                           # Dataset storage
│   ├── b-a-steinhagen.csv          # Hydrological observations (1951 - 2026, 27,418 records)
│   └── b-a-steinhagen-under-1mb (1).csv
├── database/                       # Database schema & initialization
│   ├── init.sql                    # PostgreSQL schema definition, indices, views & triggers
│   ├── import_csv.sql              # Helper script to ingest dataset
│   └── data/
│       └── b-a-steinhagen.csv      # Container-mounted dataset copy
├── backend/                        # Java Spring Boot backend
│   ├── Dockerfile                  # Multi-stage Maven + Eclipse Temurin 17 build
│   ├── pom.xml                     # Maven project configuration & dependencies
│   └── src/
│       └── main/
│           ├── java/com/aquasense/
│           │   ├── AquaSenseApplication.java
│           │   ├── config/
│           │   │   ├── CorsConfig.java
│           │   │   ├── DataInitializer.java
│           │   │   └── GlobalExceptionHandler.java
│           │   ├── controller/
│           │   │   ├── WaterBodyController.java
│           │   │   ├── ObservationController.java
│           │   │   ├── UnifiedIntelligenceController.java
│           │   │   └── ReservoirController.java
│           │   ├── model/
│           │   │   ├── WaterBody.java
│           │   │   ├── ReservoirRecord.java
│           │   │   └── ReservoirPrediction.java
│           │   ├── repository/
│           │   │   ├── WaterBodyRepository.java
│           │   │   ├── ReservoirRecordRepository.java
│           │   │   └── ReservoirPredictionRepository.java
│           │   ├── service/
│           │   │   ├── WaterBodyService.java
│           │   │   ├── ObservationService.java
│           │   │   ├── MlServiceClient.java
│           │   │   └── UnifiedIntelligenceService.java
│           │   └── dto/
│           │       ├── PredictionRequestDto.java
│           │       ├── PredictionResponseDto.java
│           │       ├── DailyPredictionDto.java
│           │       ├── SustainabilityAssessmentDto.java
│           │       └── UnifiedDashboardDto.java
│           └── resources/
│               └── application.yml
├── ml-service/                     # Python FastAPI ML forecasting service
│   ├── Dockerfile                  # Python 3.11-slim container
│   ├── requirements.txt            # FastAPI, Uvicorn, Pandas, Scikit-learn, SQLAlchemy
│   ├── test_api.py                 # Integration test suite for endpoints
│   └── app/
│       ├── __init__.py
│       ├── config.py               # Database and hydrological thresholds
│       ├── data_processor.py       # Data cleaning, glitch repair, and feature engineering
│       ├── pipeline.py             # Scikit-learn models (LR, DT, KNN) & chronological split
│       ├── sustainability.py       # Rule-based sustainability, drought & water stress engine
│       ├── models.py               # Pydantic schemas for requests and responses
│       └── main.py                 # FastAPI application routes
└── frontend/                       # React Vite web dashboard
    ├── Dockerfile                  # Multi-stage build with Nginx reverse proxy
    ├── package.json                # Dependencies (React, Lucide icons, Vite)
    ├── vite.config.js              # Dev proxy configuration
    ├── index.html                  # HTML entry point with Outfit typography
    └── src/
        ├── App.jsx                 # Dashboard UI component
        ├── main.jsx                # React DOM root
        └── index.css               # Modern dark-mode water aesthetic styling
```

---

## 🗄️ Database Schema (`database/init.sql`)

The database schema is modeled to capture all columns from `b-a-steinhagen-under-1mb (1).csv` along with extended hydrological parameters:

### Table: `reservoir_records`
| Column | Type | Description |
| :--- | :--- | :--- |
| `id` | `BIGSERIAL PRIMARY KEY` | Unique record identifier |
| `reservoir_name` | `VARCHAR(100)` | Name of reservoir (default: `'B.A. Steinhagen'`) |
| `record_date` | `DATE NOT NULL` | Observation date (YYYY-MM-DD) |
| `water_level` | `DOUBLE PRECISION` | Water elevation (feet above mean sea level) |
| `surface_area` | `DOUBLE PRECISION` | Lake surface area (acres) |
| `reservoir_storage` | `DOUBLE PRECISION` | Total reservoir storage (acre-feet) |
| `conservation_storage`| `DOUBLE PRECISION` | Conservation storage (acre-feet) |
| `percent_full` | `NUMERIC(5, 2)` | Percentage full relative to conservation capacity |
| `conservation_capacity`| `DOUBLE PRECISION` | Conservation pool capacity (acre-feet) |
| `source` | `VARCHAR(50)` | Data source (`TWDB_HISTORICAL`, `CSV_IMPORT`, etc.) |
| `created_at` / `updated_at` | `TIMESTAMPTZ` | Record audit timestamps |

### Key Constraints & Indices
- **Unique Constraint**: `uq_reservoir_record UNIQUE (reservoir_name, record_date)`
- **Indices**: Fast query lookups on `record_date DESC`, `(reservoir_name, record_date DESC)`, and `percent_full`.
- **Staging Table & Sync Procedure**: `reservoir_csv_staging` and `sync_staging_to_reservoir_records()` allows instant bulk ingestion without schema mismatch errors.

---

## 🚀 Getting Started

### 1. Run Everything with Docker Compose

Ensure Docker is running, then execute:

```bash
docker compose up --build
```

Access the services:
- **Frontend Dashboard**: [http://localhost:3000](http://localhost:3000)
- **Spring Boot Backend**: [http://localhost:8080/api/v1/reservoirs/status/latest](http://localhost:8080/api/v1/reservoirs/status/latest)
- **FastAPI Interactive Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)
- **PostgreSQL Database**: `localhost:5432` (`aquasense_db` / `aquasense` / `aquasense_secret`)

---

### 2. Running Services Individually (Local Development)

#### Frontend (React + Vite)
```bash
cd frontend
npm install
npm run dev
# Running on http://localhost:5173
```

#### ML Service (Python FastAPI)
```bash
cd ml-service
python -m venv venv
# On Windows:
.\venv\Scripts\activate
# On Linux/macOS:
source venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload --port 8000
```

#### Spring Boot Backend (Java)
```bash
cd backend
mvn spring-boot:run
```

---

## 📊 Dataset Ingestion

To ingest the complete 27,418 records into PostgreSQL:

```bash
# Using Docker:
docker exec -i aquasense-postgres psql -U aquasense -d aquasense_db < database/import_csv.sql

# Or using the FastAPI ML service sync:
curl -X POST "http://localhost:8000/api/v1/train?reservoir_name=B.A.%20Steinhagen"
```
