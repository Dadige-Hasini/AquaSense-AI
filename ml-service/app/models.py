from pydantic import BaseModel, Field
from typing import List, Dict, Optional, Any
from datetime import date, datetime

# -----------------------------------------------------------------------------
# Data Processing Models
# -----------------------------------------------------------------------------
class CleaningReport(BaseModel):
    raw_records_count: int
    cleaned_records_count: int
    outliers_detected_and_repaired: int
    missing_dates_interpolated: int
    date_range_start: str
    date_range_end: str
    reservoir_name: str
    cleaned_file_path: str
    message: str

# -----------------------------------------------------------------------------
# Historical Analytics Models
# -----------------------------------------------------------------------------
class HistoricalRecordItem(BaseModel):
    date: str
    reservoir_storage: float
    conservation_capacity: float
    percent_full: float
    estimated_water_level: Optional[float] = None

class HistoricalAnalyticsSummary(BaseModel):
    reservoir_name: str
    total_records: int
    start_date: str
    end_date: str
    latest_record: Dict[str, Any]
    storage_stats: Dict[str, float]
    percent_full_stats: Dict[str, float]
    all_time_high_storage: Dict[str, Any]
    all_time_low_storage: Dict[str, Any]
    drought_events_count: Dict[str, int]
    monthly_seasonality: List[Dict[str, Any]]
    annual_averages: List[Dict[str, Any]]

# -----------------------------------------------------------------------------
# Machine Learning Benchmark & Prediction Models
# -----------------------------------------------------------------------------
class ModelEvaluationMetric(BaseModel):
    model_name: str
    target_variable: str
    r2_score: float
    rmse: float
    mae: float
    mape: float
    train_samples: int
    test_samples: int
    chronological_split_date: str

class BenchmarkResponse(BaseModel):
    reservoir_name: str
    split_strategy: str = "Chronological 80/20 (Zero Lookahead Leakage)"
    train_period: str
    test_period: str
    models_comparison: List[ModelEvaluationMetric]
    best_model_storage: str
    best_model_percent_full: str

class PredictionRequest(BaseModel):
    model_type: str = Field(
        default="linear_regression", 
        description="Model algorithm: 'linear_regression', 'decision_tree', 'knn', or 'ensemble'"
    )
    horizon_days: int = Field(
        default=14, 
        ge=1, 
        le=90, 
        description="Forecast horizon in days (1 to 90)"
    )
    confidence_level: float = Field(
        default=0.95, 
        ge=0.80, 
        le=0.99, 
        description="Confidence interval coverage (default: 0.95)"
    )

class DailyPredictionPoint(BaseModel):
    date: str
    day_step: int
    predicted_storage: float
    predicted_percent_full: float
    confidence_lower_storage: float
    confidence_upper_storage: float
    confidence_lower_percent_full: float
    confidence_upper_percent_full: float
    estimated_water_level: float

class PredictionResponse(BaseModel):
    reservoir_name: str
    model_type: str
    horizon_days: int
    last_observed_date: str
    last_observed_storage: float
    last_observed_percent_full: float
    generated_at: str
    predictions: List[DailyPredictionPoint]
    model_metrics: Dict[str, float]

# -----------------------------------------------------------------------------
# Rule-Based Sustainability Assessment Models
# -----------------------------------------------------------------------------
class SustainabilityAssessment(BaseModel):
    reservoir_name: str
    assessment_date: str
    current_storage: float
    current_percent_full: float
    current_conservation_capacity: float
    drought_stage: str
    water_stress_level: str
    risk_color_code: str
    depletion_rate_7d_acre_feet_day: float
    depletion_rate_30d_acre_feet_day: float
    trend_direction: str
    days_to_critical_reserve: Optional[float] = None
    sustainability_index_score: float
    sustainability_grade: str
    advisory_summary: str
    recommended_actions: List[str]
    forecast_30d_outlook: Dict[str, Any]
