package com.aquasense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PredictionRequestDto {

    @JsonProperty("model_type")
    private String modelType = "linear_regression";

    @JsonProperty("horizon_days")
    private Integer horizonDays = 14;

    @JsonProperty("confidence_level")
    private Double confidenceLevel = 0.95;

    public PredictionRequestDto() {
    }

    public PredictionRequestDto(String modelType, Integer horizonDays) {
        this.modelType = modelType;
        this.horizonDays = horizonDays;
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

    public Double getConfidenceLevel() {
        return confidenceLevel;
    }

    public void setConfidenceLevel(Double confidenceLevel) {
        this.confidenceLevel = confidenceLevel;
    }
}
