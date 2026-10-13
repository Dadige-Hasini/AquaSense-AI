import logging
from typing import Dict, Any, List, Optional
import numpy as np
import pandas as pd

from .config import (
    RESERVOIR_NAME,
    THRESHOLD_CRITICAL,
    THRESHOLD_SEVERE,
    THRESHOLD_WATCH,
    THRESHOLD_HEALTHY,
    THRESHOLD_HIGH,
    CONSERVATION_CAPACITY_DEFAULT
)
from .data_processor import get_cleaned_dataframe
from .models import SustainabilityAssessment

logger = logging.getLogger("AquaSense.Sustainability")

class SustainabilityEngine:
    """
    Rule-Based Hydrological Sustainability and Drought Assessment Engine
    for B.A. Steinhagen Lake.
    """

    def __init__(self):
        pass

    def evaluate(self, forecast_pipeline_results: Optional[Dict[str, Any]] = None) -> SustainabilityAssessment:
        df = get_cleaned_dataframe()
        recent = df.tail(35).copy().reset_index(drop=True)
        latest = recent.iloc[-1]

        assessment_date = latest['date'].strftime("%Y-%m-%d")
        curr_storage = float(latest['reservoir_storage'])
        curr_pct = float(latest['percent_full'])
        capacity = float(latest['conservation_capacity']) or CONSERVATION_CAPACITY_DEFAULT

        # 1. Calculate historical depletion rates (acre-feet/day)
        # 7-day rate of change
        storage_7d_ago = float(recent.iloc[-8]['reservoir_storage']) if len(recent) >= 8 else curr_storage
        rate_7d = (curr_storage - storage_7d_ago) / 7.0

        # 30-day rate of change
        storage_30d_ago = float(recent.iloc[-31]['reservoir_storage']) if len(recent) >= 31 else curr_storage
        rate_30d = (curr_storage - storage_30d_ago) / 30.0

        # 2. Determine Trend Direction
        if rate_7d < -75.0:
            trend_direction = "Rapid Depletion"
        elif rate_7d < -10.0:
            trend_direction = "Moderate Depletion"
        elif rate_7d > 75.0:
            trend_direction = "Rapid Inflow / Refilling"
        elif rate_7d > 10.0:
            trend_direction = "Moderate Inflow / Recharge"
        else:
            trend_direction = "Steady State / Stable"

        # 3. Rule-Based Drought & Stress Classification
        if curr_pct < THRESHOLD_CRITICAL:
            drought_stage = "Stage 4 - Emergency / Extreme Drought"
            water_stress_level = "Critical Water Deficit"
            risk_color_code = "#ef4444" # Red
            advisory_summary = (
                f"Reservoir storage is at {curr_pct:.1f}% capacity, below the {THRESHOLD_CRITICAL}% critical reserve threshold. "
                "Immediate emergency rationing and mandatory curtailment required."
            )
            actions = [
                "Enforce mandatory Stage 4 emergency drought restrictions",
                "Curtail all non-essential and ornamental water use",
                "Activate emergency inter-basin transfers or backup groundwater wells",
                "Maintain strictly required environmental pulse flows down the Neches River"
            ]
        elif curr_pct < THRESHOLD_SEVERE:
            drought_stage = "Stage 3 - Severe Drought"
            water_stress_level = "High Water Stress"
            risk_color_code = "#f97316" # Orange
            advisory_summary = (
                f"Reservoir storage is at {curr_pct:.1f}% capacity. Water reserves are severely constrained. "
                "Mandatory municipal and industrial water reduction targets must be enforced."
            )
            actions = [
                "Implement mandatory Stage 3 water usage cutbacks (20% reduction target)",
                "Prohibit daytime automated lawn irrigation and vehicle washing",
                "Audit industrial and thermoelectric cooling withdrawal permits",
                "Coordinate release schedules with Sam Rayburn Reservoir upstream"
            ]
        elif curr_pct < THRESHOLD_WATCH:
            drought_stage = "Stage 2 - Moderate Drought / Watch"
            water_stress_level = "Moderate Stress"
            risk_color_code = "#eab308" # Yellow
            advisory_summary = (
                f"Reservoir storage is at {curr_pct:.1f}% capacity. Sub-normal conservation storage. "
                "Voluntary conservation and heightened monitoring active."
            )
            actions = [
                "Implement Stage 2 voluntary conservation (10% target)",
                "Inspect municipal utility distribution systems for pipeline leaks",
                "Monitor 14-day weather forecasts and soil moisture deficits across Neches Basin",
                "Prepare drought contingency triggers for municipal water systems"
            ]
        elif curr_pct <= THRESHOLD_HIGH:
            drought_stage = "Normal Operations / Sustainable"
            water_stress_level = "Optimal / Low Stress"
            risk_color_code = "#10b981" # Green
            advisory_summary = (
                f"Reservoir storage is healthy at {curr_pct:.1f}% capacity ({curr_storage:,.0f} acre-ft). "
                "Operating within standard conservation pool rules."
            )
            actions = [
                "Maintain standard baseline municipal and irrigation supply deliveries",
                "Continue standard continuous automated telemetry monitoring",
                "Operate normal run-of-the-river hydropower generation if available"
            ]
        else:
            drought_stage = "High Storage / Flood Surcharge Watch"
            water_stress_level = "Spillway Discharge Monitoring"
            risk_color_code = "#06b6d4" # Cyan
            advisory_summary = (
                f"Reservoir storage is at {curr_pct:.1f}% capacity, exceeding normal conservation pool. "
                "Monitor dam gates for controlled spillway releases."
            )
            actions = [
                "Coordinate with USACE Town Bluff Dam operators for controlled gate releases",
                "Monitor downstream stage gages on lower Neches River near Evadale and Beaumont",
                "Verify spillway gate readiness and debris clearances"
            ]

        # 4. Estimated Days to Critical Reserve
        days_to_critical: Optional[float] = None
        critical_storage = (THRESHOLD_CRITICAL / 100.0) * capacity
        if curr_storage > critical_storage and rate_7d < -5.0:
            deficit_to_critical = curr_storage - critical_storage
            days_to_critical = round(deficit_to_critical / abs(rate_7d), 1)

        # 5. Sustainability Score Calculation (0 to 100 index)
        # Factor A: Current capacity percentage (0 - 55 pts)
        pct_score = min(55.0, (curr_pct / 85.0) * 55.0)
        
        # Factor B: Depletion rate velocity (0 - 25 pts)
        if rate_7d >= 0:
            rate_score = min(25.0, 15.0 + min(10.0, rate_7d / 10.0))
        else:
            rate_score = max(0.0, 15.0 - min(15.0, abs(rate_7d) / 10.0))

        # Factor C: Forecast 30-day Outlook (0 - 20 pts)
        forecast_storage_30d = curr_storage + (rate_30d * 30.0)
        forecast_pct_30d = min(100.0, max(0.0, (forecast_storage_30d / capacity) * 100.0))

        if forecast_pipeline_results and "forecast_pct_30d" in forecast_pipeline_results:
            forecast_pct_30d = forecast_pipeline_results["forecast_pct_30d"]
            forecast_storage_30d = forecast_pipeline_results.get("forecast_storage_30d", forecast_storage_30d)

        outlook_score = min(20.0, (forecast_pct_30d / 85.0) * 20.0)

        total_score = round(min(100.0, max(0.0, pct_score + rate_score + outlook_score)), 1)

        # Assign letter grade
        if total_score >= 85.0:
            grade = "A (Highly Sustainable)"
        elif total_score >= 70.0:
            grade = "B (Sustainable / Stable)"
        elif total_score >= 55.0:
            grade = "C (Guarded / Moderate Risk)"
        elif total_score >= 40.0:
            grade = "D (Vulnerable / High Risk)"
        else:
            grade = "F (Critical Water Stress)"

        outlook_dict = {
            "forecast_date_30d": (pd.to_datetime(assessment_date) + pd.Timedelta(days=30)).strftime("%Y-%m-%d"),
            "projected_storage_acre_feet": round(forecast_storage_30d, 2),
            "projected_percent_full": round(forecast_pct_30d, 2),
            "net_30d_projected_change_acre_feet": round(forecast_storage_30d - curr_storage, 2)
        }

        return SustainabilityAssessment(
            reservoir_name=RESERVOIR_NAME,
            assessment_date=assessment_date,
            current_storage=round(curr_storage, 2),
            current_percent_full=round(curr_pct, 2),
            current_conservation_capacity=round(capacity, 2),
            drought_stage=drought_stage,
            water_stress_level=water_stress_level,
            risk_color_code=risk_color_code,
            depletion_rate_7d_acre_feet_day=round(rate_7d, 2),
            depletion_rate_30d_acre_feet_day=round(rate_30d, 2),
            trend_direction=trend_direction,
            days_to_critical_reserve=days_to_critical,
            sustainability_index_score=total_score,
            sustainability_grade=grade,
            advisory_summary=advisory_summary,
            recommended_actions=actions,
            forecast_30d_outlook=outlook_dict
        )

# Global singleton instance
sustainability_engine = SustainabilityEngine()
