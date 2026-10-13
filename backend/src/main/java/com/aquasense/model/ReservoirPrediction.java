package com.aquasense.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservoir_predictions", uniqueConstraints = {
    @UniqueConstraint(name = "uq_prediction_target", columnNames = {"reservoir_name", "forecast_date", "model_name", "model_version"})
})
public class ReservoirPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservoir_name", nullable = false, length = 100)
    private String reservoirName = "B.A. Steinhagen";

    @Column(name = "forecast_date", nullable = false)
    private LocalDate forecastDate;

    @Column(name = "predicted_storage", nullable = false)
    private Double predictedStorage;

    @Column(name = "predicted_percent_full", precision = 5, scale = 2)
    private BigDecimal predictedPercentFull;

    @Column(name = "predicted_water_level")
    private Double predictedWaterLevel;

    @Column(name = "confidence_interval_lower")
    private Double confidenceIntervalLower;

    @Column(name = "confidence_interval_upper")
    private Double confidenceIntervalUpper;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion = "1.0.0";

    @Column(name = "generated_at")
    private OffsetDateTime generatedAt;

    public ReservoirPrediction() {
    }

    public ReservoirPrediction(String reservoirName, LocalDate forecastDate, Double predictedStorage,
                               BigDecimal predictedPercentFull, String modelName) {
        this.reservoirName = reservoirName;
        this.forecastDate = forecastDate;
        this.predictedStorage = predictedStorage;
        this.predictedPercentFull = predictedPercentFull;
        this.modelName = modelName;
    }

    @PrePersist
    protected void onCreate() {
        if (this.generatedAt == null) {
            this.generatedAt = OffsetDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservoirName() {
        return reservoirName;
    }

    public void setReservoirName(String reservoirName) {
        this.reservoirName = reservoirName;
    }

    public LocalDate getForecastDate() {
        return forecastDate;
    }

    public void setForecastDate(LocalDate forecastDate) {
        this.forecastDate = forecastDate;
    }

    public Double getPredictedStorage() {
        return predictedStorage;
    }

    public void setPredictedStorage(Double predictedStorage) {
        this.predictedStorage = predictedStorage;
    }

    public BigDecimal getPredictedPercentFull() {
        return predictedPercentFull;
    }

    public void setPredictedPercentFull(BigDecimal predictedPercentFull) {
        this.predictedPercentFull = predictedPercentFull;
    }

    public Double getPredictedWaterLevel() {
        return predictedWaterLevel;
    }

    public void setPredictedWaterLevel(Double predictedWaterLevel) {
        this.predictedWaterLevel = predictedWaterLevel;
    }

    public Double getConfidenceIntervalLower() {
        return confidenceIntervalLower;
    }

    public void setConfidenceIntervalLower(Double confidenceIntervalLower) {
        this.confidenceIntervalLower = confidenceIntervalLower;
    }

    public Double getConfidenceIntervalUpper() {
        return confidenceIntervalUpper;
    }

    public void setConfidenceIntervalUpper(Double confidenceIntervalUpper) {
        this.confidenceIntervalUpper = confidenceIntervalUpper;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public OffsetDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(OffsetDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
