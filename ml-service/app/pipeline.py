import logging
from typing import Dict, List, Tuple, Any, Optional
from datetime import timedelta
import numpy as np
import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.tree import DecisionTreeRegressor
from sklearn.neighbors import KNeighborsRegressor
from sklearn.metrics import mean_squared_error, mean_absolute_error, r2_score

from .config import (
    CHRONOLOGICAL_SPLIT_RATIO,
    RESERVOIR_NAME,
    CONSERVATION_CAPACITY_DEFAULT
)
from .data_processor import get_cleaned_dataframe
from .models import (
    ModelEvaluationMetric,
    BenchmarkResponse,
    DailyPredictionPoint,
    PredictionResponse
)

logger = logging.getLogger("AquaSense.Pipeline")

class PredictionPipeline:
    """
    Scikit-learn Prediction Pipeline for Reservoir Storage and Percent Full.
    Supports Linear Regression, Decision Tree, KNN, and Ensemble.
    Uses strict chronological train/test splitting to prevent lookahead data leakage.
    """

    FEATURE_COLS = [
        'storage_lag_1', 'storage_lag_2', 'storage_lag_3', 'storage_lag_7', 'storage_lag_14',
        'pct_lag_1', 'pct_lag_2', 'pct_lag_7',
        'storage_roll_mean_7', 'storage_roll_mean_30',
        'sin_day_of_year', 'cos_day_of_year',
        'month', 'day_of_week'
    ]

    def __init__(self):
        self.df: Optional[pd.DataFrame] = None
        self.train_df: Optional[pd.DataFrame] = None
        self.test_df: Optional[pd.DataFrame] = None
        
        # Models dictionary: {model_name: {target: model_instance}}
        self.models: Dict[str, Dict[str, Any]] = {}
        self.evaluation_metrics: Dict[str, Dict[str, ModelEvaluationMetric]] = {}
        self.residual_std: Dict[str, Dict[str, float]] = {}
        self.is_trained: bool = False

    def load_data_and_split(self) -> Tuple[pd.DataFrame, pd.DataFrame]:
        """Loads cleaned dataset and applies chronological train/test splitting."""
        self.df = get_cleaned_dataframe()
        
        # Verify required features exist
        for col in self.FEATURE_COLS:
            if col not in self.df.columns:
                raise ValueError(f"Required feature '{col}' missing from cleaned dataset.")

        split_idx = int(len(self.df) * CHRONOLOGICAL_SPLIT_RATIO)
        self.train_df = self.df.iloc[:split_idx].copy()
        self.test_df = self.df.iloc[split_idx:].copy()

        logger.info(
            f"Chronological split completed: Train={len(self.train_df)} rows "
            f"({self.train_df['date'].min().strftime('%Y-%m-%d')} to {self.train_df['date'].max().strftime('%Y-%m-%d')}), "
            f"Test={len(self.test_df)} rows "
            f"({self.test_df['date'].min().strftime('%Y-%m-%d')} to {self.test_df['date'].max().strftime('%Y-%m-%d')})"
        )
        return self.train_df, self.test_df

    def train_models(self) -> None:
        """Trains Linear Regression, Decision Tree, and KNN on chronological training slice."""
        if self.train_df is None:
            self.load_data_and_split()

        X_train = self.train_df[self.FEATURE_COLS]
        y_train_storage = self.train_df['reservoir_storage']
        y_train_pct = self.train_df['percent_full']

        X_test = self.test_df[self.FEATURE_COLS]
        y_test_storage = self.test_df['reservoir_storage']
        y_test_pct = self.test_df['percent_full']

        from sklearn.pipeline import Pipeline
        from sklearn.preprocessing import StandardScaler

        model_factories = {
            "linear_regression": {
                "storage": Pipeline([("scaler", StandardScaler()), ("model", LinearRegression())]),
                "percent_full": Pipeline([("scaler", StandardScaler()), ("model", LinearRegression())])
            },
            "decision_tree": {
                "storage": DecisionTreeRegressor(max_depth=8, min_samples_leaf=5, random_state=42),
                "percent_full": DecisionTreeRegressor(max_depth=8, min_samples_leaf=5, random_state=42)
            },
            "knn": {
                "storage": Pipeline([("scaler", StandardScaler()), ("model", KNeighborsRegressor(n_neighbors=7, weights="distance"))]),
                "percent_full": Pipeline([("scaler", StandardScaler()), ("model", KNeighborsRegressor(n_neighbors=7, weights="distance"))])
            }
        }

        split_date_str = self.test_df['date'].min().strftime("%Y-%m-%d")

        for name, target_models in model_factories.items():
            self.models[name] = {}
            self.evaluation_metrics[name] = {}
            self.residual_std[name] = {}

            # 1. Train Storage Model
            m_storage = target_models["storage"]
            m_storage.fit(X_train, y_train_storage)
            preds_storage = m_storage.predict(X_test)
            r2_s = float(r2_score(y_test_storage, preds_storage))
            rmse_s = float(np.sqrt(mean_squared_error(y_test_storage, preds_storage)))
            mae_s = float(mean_absolute_error(y_test_storage, preds_storage))
            mape_s = float(np.mean(np.abs((y_test_storage - preds_storage) / np.maximum(y_test_storage, 1.0))) * 100)

            self.models[name]["storage"] = m_storage
            self.residual_std[name]["storage"] = float(np.std(y_test_storage - preds_storage))
            self.evaluation_metrics[name]["storage"] = ModelEvaluationMetric(
                model_name=f"{name}_storage",
                target_variable="reservoir_storage",
                r2_score=round(r2_s, 4),
                rmse=round(rmse_s, 2),
                mae=round(mae_s, 2),
                mape=round(mape_s, 2),
                train_samples=len(X_train),
                test_samples=len(X_test),
                chronological_split_date=split_date_str
            )

            # 2. Train Percent Full Model
            m_pct = target_models["percent_full"]
            m_pct.fit(X_train, y_train_pct)
            preds_pct = m_pct.predict(X_test)
            r2_p = float(r2_score(y_test_pct, preds_pct))
            rmse_p = float(np.sqrt(mean_squared_error(y_test_pct, preds_pct)))
            mae_p = float(mean_absolute_error(y_test_pct, preds_pct))
            mape_p = float(np.mean(np.abs((y_test_pct - preds_pct) / np.maximum(y_test_pct, 1.0))) * 100)

            self.models[name]["percent_full"] = m_pct
            self.residual_std[name]["percent_full"] = float(np.std(y_test_pct - preds_pct))
            self.evaluation_metrics[name]["percent_full"] = ModelEvaluationMetric(
                model_name=f"{name}_percent_full",
                target_variable="percent_full",
                r2_score=round(r2_p, 4),
                rmse=round(rmse_p, 2),
                mae=round(mae_p, 2),
                mape=round(mape_p, 2),
                train_samples=len(X_train),
                test_samples=len(X_test),
                chronological_split_date=split_date_str
            )

        self.is_trained = True
        logger.info("All scikit-learn models successfully trained and evaluated.")

    def get_benchmark(self) -> BenchmarkResponse:
        """Returns comparative benchmark evaluation for all 3 models."""
        if not self.is_trained:
            self.train_models()

        metrics_list: List[ModelEvaluationMetric] = []
        for name in ["linear_regression", "decision_tree", "knn"]:
            metrics_list.append(self.evaluation_metrics[name]["storage"])
            metrics_list.append(self.evaluation_metrics[name]["percent_full"])

        train_period = f"{self.train_df['date'].min().strftime('%Y-%m-%d')} to {self.train_df['date'].max().strftime('%Y-%m-%d')}"
        test_period = f"{self.test_df['date'].min().strftime('%Y-%m-%d')} to {self.test_df['date'].max().strftime('%Y-%m-%d')}"

        # Determine best performing model by lowest RMSE
        best_storage = min(
            ["linear_regression", "decision_tree", "knn"],
            key=lambda m: self.evaluation_metrics[m]["storage"].rmse
        )
        best_pct = min(
            ["linear_regression", "decision_tree", "knn"],
            key=lambda m: self.evaluation_metrics[m]["percent_full"].rmse
        )

        return BenchmarkResponse(
            reservoir_name=RESERVOIR_NAME,
            train_period=train_period,
            test_period=test_period,
            models_comparison=metrics_list,
            best_model_storage=best_storage,
            best_model_percent_full=best_pct
        )

    def forecast(
        self, 
        model_type: str = "linear_regression", 
        horizon_days: int = 14, 
        confidence_level: float = 0.95
    ) -> PredictionResponse:
        """
        Executes multi-step recursive forecasting starting from the latest observation.
        """
        if not self.is_trained:
            self.train_models()

        valid_models = ["linear_regression", "decision_tree", "knn", "ensemble"]
        m_type = model_type.lower().strip()
        if m_type not in valid_models:
            raise ValueError(f"Invalid model_type '{model_type}'. Choose from {valid_models}")

        # Normal critical value for confidence interval
        z_score = 1.96 if confidence_level >= 0.95 else 1.645

        # Get latest records to seed autoregressive lags
        latest_history = self.df.tail(60).copy().reset_index(drop=True)
        last_row = latest_history.iloc[-1]
        last_date = last_row['date']
        last_storage = float(last_row['reservoir_storage'])
        last_pct = float(last_row['percent_full'])
        capacity = float(last_row['conservation_capacity']) or CONSERVATION_CAPACITY_DEFAULT

        # Prepare memory buffer for recursive stepping
        storage_history = list(latest_history['reservoir_storage'].values)
        pct_history = list(latest_history['percent_full'].values)

        forecast_points: List[DailyPredictionPoint] = []
        
        # Calculate std for bounds
        if m_type == "ensemble":
            sigma_s = float(np.mean([self.residual_std[m]["storage"] for m in ["linear_regression", "decision_tree", "knn"]]))
            sigma_p = float(np.mean([self.residual_std[m]["percent_full"] for m in ["linear_regression", "decision_tree", "knn"]]))
        else:
            sigma_s = self.residual_std[m_type]["storage"]
            sigma_p = self.residual_std[m_type]["percent_full"]

        for step in range(1, horizon_days + 1):
            future_date = last_date + timedelta(days=step)
            doy = future_date.timetuple().tm_yday

            # Build feature dictionary
            feat_dict = {
                'storage_lag_1': storage_history[-1],
                'storage_lag_2': storage_history[-2],
                'storage_lag_3': storage_history[-3],
                'storage_lag_7': storage_history[-7],
                'storage_lag_14': storage_history[-14],
                'pct_lag_1': pct_history[-1],
                'pct_lag_2': pct_history[-2],
                'pct_lag_7': pct_history[-7],
                'storage_roll_mean_7': float(np.mean(storage_history[-7:])),
                'storage_roll_mean_30': float(np.mean(storage_history[-30:])),
                'sin_day_of_year': float(np.sin(2 * np.pi * doy / 365.25)),
                'cos_day_of_year': float(np.cos(2 * np.pi * doy / 365.25)),
                'month': future_date.month,
                'day_of_week': future_date.weekday()
            }
            X_curr = pd.DataFrame([feat_dict])[self.FEATURE_COLS]

            # Generate step prediction
            if m_type == "ensemble":
                p_storages = [self.models[m]["storage"].predict(X_curr)[0] for m in ["linear_regression", "decision_tree", "knn"]]
                p_pcts = [self.models[m]["percent_full"].predict(X_curr)[0] for m in ["linear_regression", "decision_tree", "knn"]]
                pred_storage = float(np.mean(p_storages))
                pred_pct = float(np.mean(p_pcts))
            else:
                pred_storage = float(self.models[m_type]["storage"].predict(X_curr)[0])
                pred_pct = float(self.models[m_type]["percent_full"].predict(X_curr)[0])

            # Apply physical consistency limits
            pred_storage = max(0.0, pred_storage)
            pred_pct = min(100.0, max(0.0, pred_pct))

            # Uncertainty expands with time step
            ci_s = sigma_s * np.sqrt(step) * z_score * 0.4
            ci_p = sigma_p * np.sqrt(step) * z_score * 0.4

            # Estimate water elevation
            norm_ratio = min(1.2, max(0.0, pred_storage / capacity))
            est_elev = round(70.0 + 12.5 * (norm_ratio ** 0.65), 2)

            point = DailyPredictionPoint(
                date=future_date.strftime("%Y-%m-%d"),
                day_step=step,
                predicted_storage=round(pred_storage, 2),
                predicted_percent_full=round(pred_pct, 2),
                confidence_lower_storage=round(max(0.0, pred_storage - ci_s), 2),
                confidence_upper_storage=round(pred_storage + ci_s, 2),
                confidence_lower_percent_full=round(max(0.0, pred_pct - ci_p), 2),
                confidence_upper_percent_full=round(min(100.0, pred_pct + ci_p), 2),
                estimated_water_level=est_elev
            )
            forecast_points.append(point)

            # Advance buffer for next autoregressive step
            storage_history.append(pred_storage)
            pct_history.append(pred_pct)

        # Retrieve model evaluation metrics for report
        if m_type == "ensemble":
            metrics_summary = {
                "r2_storage": round(float(np.mean([self.evaluation_metrics[m]["storage"].r2_score for m in ["linear_regression", "decision_tree", "knn"]])), 4),
                "rmse_storage": round(float(np.mean([self.evaluation_metrics[m]["storage"].rmse for m in ["linear_regression", "decision_tree", "knn"]])), 2),
                "r2_percent_full": round(float(np.mean([self.evaluation_metrics[m]["percent_full"].r2_score for m in ["linear_regression", "decision_tree", "knn"]])), 4),
                "rmse_percent_full": round(float(np.mean([self.evaluation_metrics[m]["percent_full"].rmse for m in ["linear_regression", "decision_tree", "knn"]])), 2)
            }
        else:
            metrics_summary = {
                "r2_storage": self.evaluation_metrics[m_type]["storage"].r2_score,
                "rmse_storage": self.evaluation_metrics[m_type]["storage"].rmse,
                "r2_percent_full": self.evaluation_metrics[m_type]["percent_full"].r2_score,
                "rmse_percent_full": self.evaluation_metrics[m_type]["percent_full"].rmse
            }

        return PredictionResponse(
            reservoir_name=RESERVOIR_NAME,
            model_type=m_type,
            horizon_days=horizon_days,
            last_observed_date=last_date.strftime("%Y-%m-%d"),
            last_observed_storage=round(last_storage, 2),
            last_observed_percent_full=round(last_pct, 2),
            generated_at=pd.Timestamp.now().isoformat(),
            predictions=forecast_points,
            model_metrics=metrics_summary
        )

# Global singleton pipeline instance
pipeline = PredictionPipeline()
