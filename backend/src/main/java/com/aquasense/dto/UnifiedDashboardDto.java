package com.aquasense.dto;

import com.aquasense.model.ReservoirRecord;
import com.aquasense.model.WaterBody;
import java.util.Map;

public class UnifiedDashboardDto {

    private WaterBody waterBody;
    private ReservoirRecord latestObservation;
    private SustainabilityAssessmentDto sustainability;
    private PredictionResponseDto prediction;
    private Map<String, Object> benchmark;
    private Map<String, Object> historicalSummary;

    public UnifiedDashboardDto() {
    }

    public WaterBody getWaterBody() {
        return waterBody;
    }

    public void setWaterBody(WaterBody waterBody) {
        this.waterBody = waterBody;
    }

    public ReservoirRecord getLatestObservation() {
        return latestObservation;
    }

    public void setLatestObservation(ReservoirRecord latestObservation) {
        this.latestObservation = latestObservation;
    }

    public SustainabilityAssessmentDto getSustainability() {
        return sustainability;
    }

    public void setSustainability(SustainabilityAssessmentDto sustainability) {
        this.sustainability = sustainability;
    }

    public PredictionResponseDto getPrediction() {
        return prediction;
    }

    public void setPrediction(PredictionResponseDto prediction) {
        this.prediction = prediction;
    }

    public Map<String, Object> getBenchmark() {
        return benchmark;
    }

    public void setBenchmark(Map<String, Object> benchmark) {
        this.benchmark = benchmark;
    }

    public Map<String, Object> getHistoricalSummary() {
        return historicalSummary;
    }

    public void setHistoricalSummary(Map<String, Object> historicalSummary) {
        this.historicalSummary = historicalSummary;
    }
}
