import asyncio
import httpx
import json
from app.main import app

async def run_tests():
    transport = httpx.ASGITransport(app=app)
    async with httpx.AsyncClient(transport=transport, base_url="http://test") as client:
        print("="*60)
        print("1. TESTING HEALTH & SYSTEM")
        print("="*60)
        r = await client.get("/health")
        print(f"Status: {r.status_code}")
        print(json.dumps(r.json(), indent=2))

        print("\n" + "="*60)
        print("2. TESTING BENCHMARK (LR, DT, KNN)")
        print("="*60)
        r = await client.get("/api/v1/models/benchmark")
        data = r.json()
        print(f"Train Period: {data['train_period']}")
        print(f"Test Period:  {data['test_period']}")
        print(f"Best Storage: {data['best_model_storage']} | Best % Full: {data['best_model_percent_full']}")
        for m in data['models_comparison']:
            print(f"- {m['model_name']:<30} R2={m['r2_score']} RMSE={m['rmse']} MAE={m['mae']}")

        print("\n" + "="*60)
        print("3. TESTING FUTURE PREDICTIONS (7 Days Ahead)")
        print("="*60)
        for m_name in ["linear_regression", "decision_tree", "knn", "ensemble"]:
            r = await client.post("/api/v1/predict", json={"model_type": m_name, "horizon_days": 7})
            pred_data = r.json()
            print(f"\nModel: {m_name.upper()} (Last observed: {pred_data['last_observed_date']}, {pred_data['last_observed_storage']} acre-ft)")
            for p in pred_data['predictions'][:3]:
                print(f"  Day {p['day_step']} ({p['date']}): {p['predicted_storage']} ac-ft ({p['predicted_percent_full']}%) [CI: {p['confidence_lower_storage']} - {p['confidence_upper_storage']}] Elev: {p['estimated_water_level']} ft")

        print("\n" + "="*60)
        print("4. TESTING SUSTAINABILITY ASSESSMENT")
        print("="*60)
        r = await client.get("/api/v1/sustainability/assessment")
        sust = r.json()
        print(f"Assessment Date:    {sust['assessment_date']}")
        print(f"Current Status:     {sust['current_storage']} acre-ft ({sust['current_percent_full']}%)")
        print(f"Drought Stage:      {sust['drought_stage']}")
        print(f"Water Stress Level: {sust['water_stress_level']}")
        print(f"7-Day Depletion:    {sust['depletion_rate_7d_acre_feet_day']} acre-ft/day ({sust['trend_direction']})")
        print(f"Sustainability:     Score {sust['sustainability_index_score']}/100 [{sust['sustainability_grade']}]")
        print(f"Advisory:           {sust['advisory_summary']}")
        print(f"Recommended Actions:")
        for action in sust['recommended_actions']:
            print(f"  • {action}")
        print(f"30-Day Outlook:     {json.dumps(sust['forecast_30d_outlook'])}")

        print("\n" + "="*60)
        print("5. TESTING HISTORICAL ANALYTICS")
        print("="*60)
        r = await client.get("/api/v1/analytics/historical")
        hist = r.json()
        print(f"Total Records:      {hist['total_records']} (Span: {hist['start_date']} to {hist['end_date']})")
        print(f"Storage Mean:       {hist['storage_stats']['mean']} acre-ft (Std: {hist['storage_stats']['std']})")
        print(f"All-Time High:      {hist['all_time_high_storage']}")
        print(f"All-Time Low:       {hist['all_time_low_storage']}")
        print(f"Drought Days Count: {json.dumps(hist['drought_events_count'])}")

if __name__ == "__main__":
    asyncio.run(run_tests())
