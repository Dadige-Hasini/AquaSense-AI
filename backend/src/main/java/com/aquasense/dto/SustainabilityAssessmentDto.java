package com.aquasense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class SustainabilityAssessmentDto {

    @JsonProperty("reservoir_name")
    private String reservoirName;

    @JsonProperty("assessment_date")
    private String assessmentDate;

    @JsonProperty("current_storage")
    private Double currentStorage;

    @JsonProperty("current_percent_full")
    private Double currentPercentFull;

    @JsonProperty("current_conservation_capacity")
    private Double currentConservationCapacity;

    @JsonProperty("drought_stage")
    private String droughtStage;

    @JsonProperty("water_stress_level")
    private String waterStressLevel;

    @JsonProperty("risk_color_code")
    private String riskColorCode;

    @JsonProperty("depletion_rate_7d_acre_feet_day")
    private Double depletionRate7dAcreFeetDay;

    @JsonProperty("depletion_rate_30d_acre_feet_day")
    private Double depletionRate30dAcreFeetDay;

    @JsonProperty("trend_direction")
    private String trendDirection;

    @JsonProperty("days_to_critical_reserve")
    private Double daysToCriticalReserve;

    @JsonProperty("sustainability_index_score")
    private Double sustainabilityIndexScore;

    @JsonProperty("sustainability_grade")
    private String sustainabilityGrade;

    @JsonProperty("advisory_summary")
    private String advisorySummary;

    @JsonProperty("recommended_actions")
    private List<String> recommendedActions;

    @JsonProperty("forecast_30d_outlook")
    private Map<String, Object> forecast30dOutlook;

    public SustainabilityAssessmentDto() {
    }

    public String getReservoirName() {
        return reservoirName;
    }

    public void setReservoirName(String reservoirName) {
        this.reservoirName = reservoirName;
    }

    public String getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(String assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    public Double getCurrentStorage() {
        return currentStorage;
    }

    public void setCurrentStorage(Double currentStorage) {
        this.currentStorage = currentStorage;
    }

    public Double getCurrentPercentFull() {
        return currentPercentFull;
    }

    public void setCurrentPercentFull(Double currentPercentFull) {
        this.currentPercentFull = currentPercentFull;
    }

    public Double getCurrentConservationCapacity() {
        return currentConservationCapacity;
    }

    public void setCurrentConservationCapacity(Double currentConservationCapacity) {
        this.currentConservationCapacity = currentConservationCapacity;
    }

    public String getDroughtStage() {
        return droughtStage;
    }

    public void setDroughtStage(String droughtStage) {
        this.droughtStage = droughtStage;
    }

    public String getWaterStressLevel() {
        return waterStressLevel;
    }

    public void setWaterStressLevel(String waterStressLevel) {
        this.waterStressLevel = waterStressLevel;
    }

    public String getRiskColorCode() {
        return riskColorCode;
    }

    public void setRiskColorCode(String riskColorCode) {
        this.riskColorCode = riskColorCode;
    }

    public Double getDepletionRate7dAcreFeetDay() {
        return depletionRate7dAcreFeetDay;
    }

    public void setDepletionRate7dAcreFeetDay(Double depletionRate7dAcreFeetDay) {
        this.depletionRate7dAcreFeetDay = depletionRate7dAcreFeetDay;
    }

    public Double getDepletionRate30dAcreFeetDay() {
        return depletionRate30dAcreFeetDay;
    }

    public void setDepletionRate30dAcreFeetDay(Double depletionRate30dAcreFeetDay) {
        this.depletionRate30dAcreFeetDay = depletionRate30dAcreFeetDay;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public void setTrendDirection(String trendDirection) {
        this.trendDirection = trendDirection;
    }

    public Double getDaysToCriticalReserve() {
        return daysToCriticalReserve;
    }

    public void setDaysToCriticalReserve(Double daysToCriticalReserve) {
        this.daysToCriticalReserve = daysToCriticalReserve;
    }

    public Double getSustainabilityIndexScore() {
        return sustainabilityIndexScore;
    }

    public void setSustainabilityIndexScore(Double sustainabilityIndexScore) {
        this.sustainabilityIndexScore = sustainabilityIndexScore;
    }

    public String getSustainabilityGrade() {
        return sustainabilityGrade;
    }

    public void setSustainabilityGrade(String sustainabilityGrade) {
        this.sustainabilityGrade = sustainabilityGrade;
    }

    public String getAdvisorySummary() {
        return advisorySummary;
    }

    public void setAdvisorySummary(String advisorySummary) {
        this.advisorySummary = advisorySummary;
    }

    public List<String> getRecommendedActions() {
        return recommendedActions;
    }

    public void setRecommendedActions(List<String> recommendedActions) {
        this.recommendedActions = recommendedActions;
    }

    public Map<String, Object> getForecast30dOutlook() {
        return forecast30dOutlook;
    }

    public void setForecast30dOutlook(Map<String, Object> forecast30dOutlook) {
        this.forecast30dOutlook = forecast30dOutlook;
    }
}
