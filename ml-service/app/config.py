import os
from pathlib import Path

# Base directories
BASE_DIR = Path(__file__).resolve().parent.parent
WORKSPACE_DIR = BASE_DIR.parent
DATA_DIR = BASE_DIR / "data"
GLOBAL_DATA_DIR = WORKSPACE_DIR / "data"

# Dataset paths
DEFAULT_RAW_CSV = GLOBAL_DATA_DIR / "b-a-steinhagen-under-1mb (1).csv"
FALLBACK_RAW_CSV = GLOBAL_DATA_DIR / "b-a-steinhagen.csv"
CLEANED_CSV_PATH = DATA_DIR / "cleaned_reservoir_data.csv"

# Reservoir Metadata for B.A. Steinhagen Lake (Dam B / Town Bluff Dam, Neches River Basin, TX)
RESERVOIR_NAME = "B.A. Steinhagen"
CONSERVATION_CAPACITY_DEFAULT = 69186.0  # Normal conservation pool capacity in acre-feet (TWDB 2020-2026 datum)
TOP_OF_CONSERVATION_ELEVATION = 82.5     # Feet above mean sea level
DEAD_POOL_STORAGE = 2000.0              # Acre-feet

# Modeling constants
CHRONOLOGICAL_SPLIT_RATIO = 0.80        # 80% train (chronological), 20% test
FEATURE_LAGS = [1, 2, 3, 7, 14]         # Daily lag offsets
ROLLING_WINDOWS = [7, 30]               # Rolling moving average windows

# Rule-Based Sustainability Thresholds (% Full)
THRESHOLD_CRITICAL = 30.0               # Below 30%: Emergency Drought / Critical Depletion
THRESHOLD_SEVERE = 50.0                 # 30% - 50%: Severe Water Stress (Stage 2 Mandatory Restrictions)
THRESHOLD_WATCH = 70.0                  # 50% - 70%: Moderate Stress / Watch (Stage 1 Voluntary Restrictions)
THRESHOLD_HEALTHY = 90.0                # 70% - 90%: Normal Operational Sustainability
THRESHOLD_HIGH = 95.0                   # 90% - 95%: Abundant Conservation Storage
# Above 95%: Flood Pool Warning / Controlled Spillway Management

# Environmental Database connection string
DATABASE_URL = os.getenv(
    "DATABASE_URL", 
    "postgresql://aquasense:aquasense_secret@localhost:5432/aquasense_db"
)
