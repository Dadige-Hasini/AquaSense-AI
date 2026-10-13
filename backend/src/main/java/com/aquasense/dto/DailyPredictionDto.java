package com.aquasense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DailyPredictionDto {

    private String date;

    @JsonProperty("day_step")
    private Integer dayStep;

    @JsonProperty("predicted_storage")
    private Double predictedStorage;

    @JsonProperty("predicted_percent_full")
    private Double predictedPercentFull;

    @JsonProperty("confidence_lower_storage")
    private Double confidenceLowerStorage;

    @JsonProperty("confidence_upper_storage")
    private Double confidenceUpperStorage;

    @JsonProperty("confidence_lower_percent_full")
    private Double confidenceLowerPercentFull;

    @JsonProperty("confidence_upper_percent_full")
    private Double confidenceUpperPercentFull;

    @JsonProperty("estimated_water_level")
    private Double estimatedWaterLevel;

    public DailyPredictionDto() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Integer getDayStep() {
        return dayStep;
    }

    public void setDayStep(Integer dayStep) {
        this.dayStep = dayStep;
    }

    public Double getPredictedStorage() {
        return predictedStorage;
    }

    public void setPredictedStorage(Double predictedStorage) {
        this.predictedStorage = predictedStorage;
    }

    public Double getPredictedPercentFull() {
        return predictedPercentFull;
    }

    public void setPredictedPercentFull(Double predictedPercentFull) {
        this.predictedPercentFull = predictedPercentFull;
    }

    public Double getConfidenceLowerStorage() {
        return confidenceLowerStorage;
    }

    public void setConfidenceLowerStorage(Double confidenceLowerStorage) {
        this.confidenceLowerStorage = confidenceLowerStorage;
    }

    public Double getConfidenceUpperStorage() {
        return confidenceUpperStorage;
    }

    public void setConfidenceUpperStorage(Double confidenceUpperStorage) {
        this.confidenceUpperStorage = confidenceUpperStorage;
    }

    public Double getConfidenceLowerPercentFull() {
        return confidenceLowerPercentFull;
    }

    public void setConfidenceLowerPercentFull(Double confidenceLowerPercentFull) {
        this.confidenceLowerPercentFull = confidenceLowerPercentFull;
    }

    public Double getConfidenceUpperPercentFull() {
        return confidenceUpperPercentFull;
    }

    public void setConfidenceUpperPercentFull(Double confidenceUpperPercentFull) {
        this.confidenceUpperPercentFull = confidenceUpperPercentFull;
    }

    public Double getEstimatedWaterLevel() {
        return estimatedWaterLevel;
    }

    public void setEstimatedWaterLevel(Double estimatedWaterLevel) {
        this.estimatedWaterLevel = estimatedWaterLevel;
    }
}
