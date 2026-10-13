import os
import logging
from pathlib import Path
from typing import Optional, Tuple
import numpy as np
import pandas as pd

from .config import (
    DEFAULT_RAW_CSV,
    FALLBACK_RAW_CSV,
    CLEANED_CSV_PATH,
    DATA_DIR,
    RESERVOIR_NAME,
    TOP_OF_CONSERVATION_ELEVATION
)
from .models import CleaningReport

logger = logging.getLogger("AquaSense.DataProcessor")
logging.basicConfig(level=logging.INFO)

class ReservoirDataCleaner:
    """
    Cleans, validates, and feature-engineers historical hydrological series
    for B.A. Steinhagen Lake.
    """

    def __init__(self, raw_path: Optional[Path] = None, output_path: Optional[Path] = None):
        self.raw_path = raw_path or self._resolve_raw_dataset_path()
        self.output_path = output_path or CLEANED_CSV_PATH

    def _resolve_raw_dataset_path(self) -> Path:
        """Finds the dataset file across potential local and container paths."""
        candidates = [
            DEFAULT_RAW_CSV,
            FALLBACK_RAW_CSV,
            Path("/data/b-a-steinhagen-under-1mb (1).csv"),
            Path("/data/b-a-steinhagen.csv"),
            Path("data/b-a-steinhagen-under-1mb (1).csv"),
            Path("data/b-a-steinhagen.csv"),
            Path("../data/b-a-steinhagen-under-1mb (1).csv"),
            Path("../data/b-a-steinhagen.csv"),
        ]
        for p in candidates:
            if p.exists() and p.is_file():
                return p
        # Default fallback
        return DEFAULT_RAW_CSV

    def clean_and_prepare(self) -> Tuple[pd.DataFrame, CleaningReport]:
        """
        Executes complete cleaning pipeline:
        - Ingestion & deduplication
        - Sensor glitch / outlier correction
        - Chronological continuity & interpolation
        - Derived physical elevation & stage features
        - Time-series lags & rolling window metrics
        """
        if not self.raw_path.exists():
            raise FileNotFoundError(f"Source raw dataset not found at: {self.raw_path}")

        logger.info(f"Loading raw dataset from {self.raw_path}...")
        df = pd.read_csv(self.raw_path)
        raw_count = len(df)

        # 1. Column normalization & type enforcement
        df.columns = [c.strip().lower() for c in df.columns]
        if 'date' not in df.columns:
            raise ValueError("Dataset missing mandatory 'date' column.")

        df['date'] = pd.to_datetime(df['date'], errors='coerce')
        df = df.dropna(subset=['date'])

        for numeric_col in ['reservoir_storage', 'conservation_capacity', 'percent_full']:
            if numeric_col in df.columns:
                df[numeric_col] = pd.to_numeric(df[numeric_col], errors='coerce')

        # 2. Sort chronologically and drop duplicate dates
        df = df.sort_values('date').drop_duplicates(subset=['date'], keep='last')

        # 3. Detect and fix sensor glitches & unphysical outliers
        # Conservation capacity is historically ~69,186 - 94,250 acre-feet.
        # Several historical entries recorded glitches exceeding 5M acre-feet with 100% full.
        capacity_series = df['conservation_capacity'].fillna(69186.0)
        unphysical_high = (df['reservoir_storage'] > (capacity_series * 1.5))
        unphysical_low = (df['reservoir_storage'] < 0)
        glitch_mask = unphysical_high | unphysical_low

        outliers_count = int(glitch_mask.sum())
        if outliers_count > 0:
            logger.info(f"Repairing {outliers_count} sensor glitch outliers...")
            # For records where percent_full is valid, reconstruct storage using capacity
            reconstructable = glitch_mask & (df['percent_full'] >= 0) & (df['percent_full'] <= 100)
            df.loc[reconstructable, 'reservoir_storage'] = (
                (df.loc[reconstructable, 'percent_full'] / 100.0) * df.loc[reconstructable, 'conservation_capacity']
            )
            # For any remaining glitches, set to NaN and interpolate
            remaining_glitches = (df['reservoir_storage'] > (capacity_series * 1.5)) | (df['reservoir_storage'] < 0)
            df.loc[remaining_glitches, 'reservoir_storage'] = np.nan
            df['reservoir_storage'] = df['reservoir_storage'].interpolate(method='linear')

        # 4. Enforce domain boundaries for percent_full [0.0 - 100.0]
        # Recompute percent_full if missing or inconsistent
        df['percent_full'] = df['percent_full'].clip(lower=0.0, upper=100.0)
        inconsistent_pct = df['percent_full'].isna() | (df['percent_full'] <= 0) & (df['reservoir_storage'] > 1000)
        df.loc[inconsistent_pct, 'percent_full'] = (
            (df.loc[inconsistent_pct, 'reservoir_storage'] / df.loc[inconsistent_pct, 'conservation_capacity']) * 100.0
        ).clip(0.0, 100.0)

        # 5. Continuous chronological time-series reindexing
        # Fills missing dates to guarantee uninterrupted lag computation
        min_date = df['date'].min()
        max_date = df['date'].max()
        full_date_idx = pd.date_range(start=min_date, end=max_date, freq='D')
        missing_days_count = len(full_date_idx) - len(df)

        df = df.set_index('date').reindex(full_date_idx)
        df.index.name = 'date'

        # Interpolate missing days smoothly across time
        df['reservoir_storage'] = df['reservoir_storage'].interpolate(method='time').bfill().ffill()
        df['conservation_capacity'] = df['conservation_capacity'].ffill().bfill()
        df['percent_full'] = df['percent_full'].interpolate(method='time').bfill().ffill()

        df = df.reset_index()

        # 6. Physical estimation of water level elevation (feet above msl)
        # Based on USGS Town Bluff Dam elevation stage rating:
        # Conservation pool: ~82.5 ft msl at 69,186 acre-ft; minimum pool ~70.0 ft msl
        norm_ratio = (df['reservoir_storage'] / df['conservation_capacity']).clip(lower=0.0, upper=1.2)
        df['estimated_water_level'] = (70.0 + 12.5 * (norm_ratio ** 0.65)).round(2)

        # 7. Calendar and cyclical seasonal features
        df['day_of_year'] = df['date'].dt.dayofyear
        df['month'] = df['date'].dt.month
        df['year'] = df['date'].dt.year
        df['day_of_week'] = df['date'].dt.dayofweek
        df['sin_day_of_year'] = np.sin(2 * np.pi * df['day_of_year'] / 365.25)
        df['cos_day_of_year'] = np.cos(2 * np.pi * df['day_of_year'] / 365.25)

        # 8. Time-series lag features (1, 2, 3, 7, 14 days)
        for lag in [1, 2, 3, 7, 14]:
            df[f'storage_lag_{lag}'] = df['reservoir_storage'].shift(lag)
            df[f'pct_lag_{lag}'] = df['percent_full'].shift(lag)

        # 9. Rolling moving averages & volatility
        df['storage_roll_mean_7'] = df['reservoir_storage'].shift(1).rolling(7).mean()
        df['storage_roll_mean_30'] = df['reservoir_storage'].shift(1).rolling(30).mean()
        df['storage_roll_std_7'] = df['reservoir_storage'].shift(1).rolling(7).std().fillna(0.0)
        df['storage_roll_std_30'] = df['reservoir_storage'].shift(1).rolling(30).std().fillna(0.0)

        # 10. Daily & weekly rate of change (depletion/inflow speed in acre-ft)
        df['storage_diff_1d'] = df['reservoir_storage'].diff(1)
        df['storage_diff_7d'] = df['reservoir_storage'].diff(7)

        # Backfill initial lag warmup days
        df = df.bfill()

        # 11. Save cleaned dataset
        self.output_path.parent.mkdir(parents=True, exist_ok=True)
        df.to_csv(self.output_path, index=False)
        logger.info(f"Cleaned dataset saved successfully to {self.output_path} ({len(df)} rows).")

        report = CleaningReport(
            raw_records_count=raw_count,
            cleaned_records_count=len(df),
            outliers_detected_and_repaired=outliers_count,
            missing_dates_interpolated=missing_days_count,
            date_range_start=min_date.strftime("%Y-%m-%d"),
            date_range_end=max_date.strftime("%Y-%m-%d"),
            reservoir_name=RESERVOIR_NAME,
            cleaned_file_path=str(self.output_path),
            message="Dataset successfully cleaned, normalized, and augmented with hydrological lag features."
        )

        return df, report

def get_cleaned_dataframe() -> pd.DataFrame:
    """Helper that returns the cleaned dataframe, running the pipeline if not cached."""
    cleaner = ReservoirDataCleaner()
    if CLEANED_CSV_PATH.exists():
        try:
            df = pd.read_csv(CLEANED_CSV_PATH)
            df['date'] = pd.to_datetime(df['date'])
            if len(df) > 1000:
                return df
        except Exception:
            pass
    df, _ = cleaner.clean_and_prepare()
    return df

if __name__ == "__main__":
    cleaner = ReservoirDataCleaner()
    df, report = cleaner.clean_and_prepare()
    print("=== DATA CLEANING REPORT ===")
    print(report.model_dump_json(indent=2))
