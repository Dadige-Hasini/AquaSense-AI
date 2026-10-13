package com.aquasense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class PredictionResponseDto {

    @JsonProperty("reservoir_name")
    private String reservoirName;

    @JsonProperty("model_type")
    private String modelType;

    @JsonProperty("horizon_days")
    private Integer horizonDays;

    @JsonProperty("last_observed_date")
    private String lastObservedDate;

    @JsonProperty("last_observed_storage")
    private Double lastObservedStorage;

    @JsonProperty("last_observed_percent_full")
    private Double lastObservedPercentFull;

    @JsonProperty("generated_at")
    private String generatedAt;

    private List<DailyPredictionDto> predictions;

    @JsonProperty("model_metrics")
    private Map<String, Object> modelMetrics;

    public PredictionResponseDto() {
    }

    public String getReservoirName() {
        return reservoirName;
    }

    public void setReservoirName(String reservoirName) {
        this.reservoirName = reservoirName;
    }

    public String getModelType() {
        return modelType;
    }

    public void setModelType(String modelType) {
        this.modelType = modelType;
    }

    public Integer getHorizonDays() {
        return horizonDays;
    }

    public void setHorizonDays(Integer horizonDays) {
        this.horizonDays = horizonDays;
    }

    public String getLastObservedDate() {
        return lastObservedDate;
    }

    public void setLastObservedDate(String lastObservedDate) {
        this.lastObservedDate = lastObservedDate;
    }

    public Double getLastObservedStorage() {
        return lastObservedStorage;
    }

    public void setLastObservedStorage(Double lastObservedStorage) {
        this.lastObservedStorage = lastObservedStorage;
    }

    public Double getLastObservedPercentFull() {
        return lastObservedPercentFull;
    }

    public void setLastObservedPercentFull(Double lastObservedPercentFull) {
        this.lastObservedPercentFull = lastObservedPercentFull;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public List<DailyPredictionDto> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<DailyPredictionDto> predictions) {
        this.predictions = predictions;
    }

    public Map<String, Object> getModelMetrics() {
        return modelMetrics;
    }

    public void setModelMetrics(Map<String, Object> modelMetrics) {
        this.modelMetrics = modelMetrics;
    }
}
