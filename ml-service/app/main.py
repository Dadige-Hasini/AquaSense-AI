import os
import logging
from typing import Optional, List, Dict, Any
from datetime import datetime
from fastapi import FastAPI, HTTPException, Query, status
from fastapi.middleware.cors import CORSMiddleware
import numpy as np
import pandas as pd

from .config import RESERVOIR_NAME, CONSERVATION_CAPACITY_DEFAULT
from .data_processor import ReservoirDataCleaner, get_cleaned_dataframe
from .pipeline import pipeline
from .sustainability import sustainability_engine
from .models import (
    CleaningReport,
    HistoricalAnalyticsSummary,
    HistoricalRecordItem,
    BenchmarkResponse,
    PredictionRequest,
    PredictionResponse,
    SustainabilityAssessment
)

# Logging configuration
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("AquaSense.FastAPI")

app = FastAPI(
    title="AquaSense AI - Hydrological ML & Sustainability Microservice",
    description=(
        "Production-grade hydrological intelligence service for B.A. Steinhagen Lake. "
        "Provides automated data cleaning, comparative scikit-learn time-series forecasting "
        "(Linear Regression, Decision Tree, KNN), historical analytics, and rule-based sustainability assessment."
    ),
    version="1.1.0"
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.on_event("startup")
def startup_event():
    logger.info("Initializing AquaSense AI ML Service...")
    # Preload and train models on startup
    try:
        pipeline.train_models()
        logger.info("Initial pipeline training complete on startup.")
    except Exception as e:
        logger.warning(f"Startup training deferred: {e}")

# -----------------------------------------------------------------------------
# System & Health Endpoints
# -----------------------------------------------------------------------------
@app.get("/", tags=["System"])
def root():
    return {
        "service": "AquaSense AI - ML & Hydro-Informatics Service",
        "reservoir": RESERVOIR_NAME,
        "status": "online",
        "version": "1.1.0",
        "endpoints": {
            "historical_analytics": "/api/v1/analytics/historical",
            "historical_records": "/api/v1/analytics/records",
            "model_benchmark": "/api/v1/models/benchmark",
            "future_predictions": "/api/v1/predict",
            "sustainability_assessment": "/api/v1/sustainability/assessment",
            "data_cleaning": "/api/v1/process-data",
            "swagger_documentation": "/docs"
        },
        "timestamp": datetime.now().isoformat()
    }

@app.get("/health", tags=["System"])
def health_check():
    df = get_cleaned_dataframe()
    return {
        "status": "healthy",
        "models_trained": pipeline.is_trained,
        "dataset_rows": len(df) if df is not None else 0,
        "latest_observation_date": df['date'].max().strftime("%Y-%m-%d") if df is not None else None,
        "active_models": ["linear_regression", "decision_tree", "knn", "ensemble"]
    }

# -----------------------------------------------------------------------------
# 1. Data Processing Endpoint
# -----------------------------------------------------------------------------
@app.post("/api/v1/process-data", response_model=CleaningReport, tags=["Data Processing"])
def trigger_data_cleaning():
    """
    Cleans raw b-a-steinhagen-under-1mb (1).csv dataset:
    - Repairs sensor glitches (storage > 1.5x capacity)
    - Reconstructs missing dates
    - Generates temporal, lag, and rolling features
    - Retrains scikit-learn models
    """
    try:
        cleaner = ReservoirDataCleaner()
        df, report = cleaner.clean_and_prepare()
        pipeline.load_data_and_split()
        pipeline.train_models()
        return report
    except Exception as e:
        logger.error(f"Error during data cleaning: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Data processing failed: {str(e)}"
        )

# -----------------------------------------------------------------------------
# 2. Historical Analytics Endpoints
# -----------------------------------------------------------------------------
@app.get("/api/v1/analytics/historical", response_model=HistoricalAnalyticsSummary, tags=["Analytics"])
def get_historical_analytics():
    """
    Returns aggregated historical statistical insights spanning 1951 to present:
    - Overall min/max/mean/median storage and percent full
    - All-time record highs and lows
    - Monthly seasonal cycle profile
    - Drought event occurrences (days below 30%, 50%, 70%)
    - Annual average trajectories
    """
    try:
        df = get_cleaned_dataframe()
        storage = df['reservoir_storage']
        pct = df['percent_full']

        max_row = df.loc[storage.idxmax()]
        min_row = df.loc[storage.idxmin()]
        latest_row = df.iloc[-1]

        # Monthly seasonality aggregation (1-12)
        month_names = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
        monthly_stats = []
        for m in range(1, 13):
            m_sub = df[df['month'] == m]
            monthly_stats.append({
                "month_number": m,
                "month_name": month_names[m - 1],
                "avg_storage": round(float(m_sub['reservoir_storage'].mean()), 2),
                "avg_percent_full": round(float(m_sub['percent_full'].mean()), 2),
                "min_storage": round(float(m_sub['reservoir_storage'].min()), 2),
                "max_storage": round(float(m_sub['reservoir_storage'].max()), 2),
                "record_count": len(m_sub)
            })

        # Annual breakdown (sample last 15 years for concise response)
        annual_stats = []
        recent_years = sorted(df['year'].unique())[-15:]
        for y in recent_years:
            y_sub = df[df['year'] == y]
            annual_stats.append({
                "year": int(y),
                "avg_storage": round(float(y_sub['reservoir_storage'].mean()), 2),
                "avg_percent_full": round(float(y_sub['percent_full'].mean()), 2),
                "min_storage": round(float(y_sub['reservoir_storage'].min()), 2),
                "max_storage": round(float(y_sub['reservoir_storage'].max()), 2)
            })

        return HistoricalAnalyticsSummary(
            reservoir_name=RESERVOIR_NAME,
            total_records=len(df),
            start_date=df['date'].min().strftime("%Y-%m-%d"),
            end_date=df['date'].max().strftime("%Y-%m-%d"),
            latest_record={
                "date": latest_row['date'].strftime("%Y-%m-%d"),
                "storage_acre_feet": round(float(latest_row['reservoir_storage']), 2),
                "percent_full": round(float(latest_row['percent_full']), 2),
                "conservation_capacity": round(float(latest_row['conservation_capacity']), 2),
                "estimated_water_level_ft": round(float(latest_row['estimated_water_level']), 2)
            },
            storage_stats={
                "mean": round(float(storage.mean()), 2),
                "median": round(float(storage.median()), 2),
                "std": round(float(storage.std()), 2),
                "min": round(float(storage.min()), 2),
                "max": round(float(storage.max()), 2),
                "q25": round(float(storage.quantile(0.25)), 2),
                "q75": round(float(storage.quantile(0.75)), 2)
            },
            percent_full_stats={
                "mean": round(float(pct.mean()), 2),
                "median": round(float(pct.median()), 2),
                "std": round(float(pct.std()), 2),
                "min": round(float(pct.min()), 2),
                "max": round(float(pct.max()), 2)
            },
            all_time_high_storage={
                "date": max_row['date'].strftime("%Y-%m-%d"),
                "storage_acre_feet": round(float(max_row['reservoir_storage']), 2),
                "percent_full": round(float(max_row['percent_full']), 2)
            },
            all_time_low_storage={
                "date": min_row['date'].strftime("%Y-%m-%d"),
                "storage_acre_feet": round(float(min_row['reservoir_storage']), 2),
                "percent_full": round(float(min_row['percent_full']), 2)
            },
            drought_events_count={
                "days_below_30pct_critical": int((pct < 30.0).sum()),
                "days_below_50pct_severe": int((pct < 50.0).sum()),
                "days_below_70pct_moderate": int((pct < 70.0).sum()),
                "days_above_90pct_healthy": int((pct >= 90.0).sum())
            },
            monthly_seasonality=monthly_stats,
            annual_averages=annual_stats
        )
    except Exception as e:
        logger.error(f"Error computing analytics: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Analytics calculation failed: {str(e)}"
        )

@app.get("/api/v1/analytics/records", response_model=Dict[str, Any], tags=["Analytics"])
def get_historical_records(
    start_date: Optional[str] = Query(None, description="Start date YYYY-MM-DD"),
    end_date: Optional[str] = Query(None, description="End date YYYY-MM-DD"),
    page: int = Query(0, ge=0),
    page_size: int = Query(50, ge=1, le=1000)
):
    """
    Returns paginated historical records, filterable by date range.
    """
    df = get_cleaned_dataframe()
    if start_date:
        df = df[df['date'] >= pd.to_datetime(start_date)]
    if end_date:
        df = df[df['date'] <= pd.to_datetime(end_date)]

    total_records = len(df)
    start_idx = page * page_size
    end_idx = start_idx + page_size
    page_df = df.iloc[start_idx:end_idx]

    records = [
        HistoricalRecordItem(
            date=row['date'].strftime("%Y-%m-%d"),
            reservoir_storage=round(float(row['reservoir_storage']), 2),
            conservation_capacity=round(float(row['conservation_capacity']), 2),
            percent_full=round(float(row['percent_full']), 2),
            estimated_water_level=round(float(row['estimated_water_level']), 2)
        )
        for _, row in page_df.iterrows()
    ]

    return {
        "page": page,
        "page_size": page_size,
        "total_records": total_records,
        "total_pages": int(np.ceil(total_records / page_size)),
        "records": records
    }

# -----------------------------------------------------------------------------
# 3. Model Benchmark & Prediction Endpoints
# -----------------------------------------------------------------------------
@app.get("/api/v1/models/benchmark", response_model=BenchmarkResponse, tags=["Machine Learning"])
def get_model_benchmark():
    """
    Evaluates Linear Regression, Decision Tree, and KNN models side-by-side:
    - Trained strictly on historical chronological 80% train split
    - Evaluated on chronological 20% out-of-time test split
    - Reports RMSE, MAE, MAPE, R2 for storage and percent_full
    """
    try:
        return pipeline.get_benchmark()
    except Exception as e:
        logger.error(f"Benchmark error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Benchmark generation failed: {str(e)}"
        )

@app.post("/api/v1/predict", response_model=PredictionResponse, tags=["Machine Learning"])
def predict_future(request: PredictionRequest):
    """
    Generates future predictions of storage and percent full for a requested horizon:
    - Model choices: 'linear_regression', 'decision_tree', 'knn', or 'ensemble'
    - Horizons: 1 to 90 days
    - Returns daily predictions with 95% confidence intervals and water elevation
    """
    try:
        return pipeline.forecast(
            model_type=request.model_type,
            horizon_days=request.horizon_days,
            confidence_level=request.confidence_level
        )
    except ValueError as ve:
        raise HTTPException(status_code=status.HTTP_400_BAD_REQUEST, detail=str(ve))
    except Exception as e:
        logger.error(f"Prediction error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Prediction failed: {str(e)}"
        )

# -----------------------------------------------------------------------------
# 4. Rule-Based Sustainability Assessment Endpoint
# -----------------------------------------------------------------------------
@app.get("/api/v1/sustainability/assessment", response_model=SustainabilityAssessment, tags=["Sustainability"])
def get_sustainability_assessment():
    """
    Computes rule-based hydrological sustainability and water stress evaluation:
    - Drought classification (Stage 1 to 4)
    - 7-day and 30-day depletion rate (acre-feet/day)
    - Days to reach critical 30% reserve
    - Sustainability Index score (0 to 100) and letter grade
    - Actionable conservation and flood mitigation advisories
    """
    try:
        # Run 30-day forecast to enrich the sustainability outlook
        fc = pipeline.forecast(model_type="linear_regression", horizon_days=30)
        day_30 = fc.predictions[-1]
        forecast_context = {
            "forecast_pct_30d": day_30.predicted_percent_full,
            "forecast_storage_30d": day_30.predicted_storage
        }
        return sustainability_engine.evaluate(forecast_pipeline_results=forecast_context)
    except Exception as e:
        logger.error(f"Sustainability assessment error: {e}", exc_info=True)
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Sustainability assessment failed: {str(e)}"
        )
