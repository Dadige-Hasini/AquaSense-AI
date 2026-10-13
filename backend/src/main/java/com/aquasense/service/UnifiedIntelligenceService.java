package com.aquasense.service;

import com.aquasense.dto.*;
import com.aquasense.model.ReservoirPrediction;
import com.aquasense.model.ReservoirRecord;
import com.aquasense.model.WaterBody;
import com.aquasense.repository.ReservoirPredictionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class UnifiedIntelligenceService {

    private static final Logger log = LoggerFactory.getLogger(UnifiedIntelligenceService.class);

    private final WaterBodyService waterBodyService;
    private final ObservationService observationService;
    private final MlServiceClient mlServiceClient;
    private final ReservoirPredictionRepository predictionRepository;

    public UnifiedIntelligenceService(
            WaterBodyService waterBodyService,
            ObservationService observationService,
            MlServiceClient mlServiceClient,
            ReservoirPredictionRepository predictionRepository) {
        this.waterBodyService = waterBodyService;
        this.observationService = observationService;
        this.mlServiceClient = mlServiceClient;
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public UnifiedDashboardDto getUnifiedDashboard(String reservoirName, String modelType, int horizonDays) {
        String targetName = (reservoirName != null && !reservoirName.trim().isEmpty()) 
                ? reservoirName : "B.A. Steinhagen";
        String targetModel = (modelType != null && !modelType.trim().isEmpty()) 
                ? modelType : "linear_regression";

        UnifiedDashboardDto dashboard = new UnifiedDashboardDto();

        // 1. Fetch Water Body Metadata
        WaterBody waterBody = waterBodyService.getWaterBodyByName(targetName)
                .orElseGet(() -> {
                    WaterBody defaultBody = new WaterBody();
                    defaultBody.setName(targetName);
                    defaultBody.setRiverBasin("Neches River Basin");
                    defaultBody.setConservationCapacity(69186.0);
                    return defaultBody;
                });
        dashboard.setWaterBody(waterBody);

        // 2. Fetch Latest Observation from DB
        ReservoirRecord latestObs = observationService.getLatestObservation(targetName)
                .orElseGet(() -> {
                    ReservoirRecord seed = new ReservoirRecord();
                    seed.setReservoirName(targetName);
                    seed.setRecordDate(LocalDate.now());
                    seed.setReservoirStorage(66931.0);
                    seed.setPercentFull(BigDecimal.valueOf(96.6));
                    seed.setConservationCapacity(69186.0);
                    seed.setWaterLevel(82.4);
                    seed.setSurfaceArea(13700.0);
                    return seed;
                });
        dashboard.setLatestObservation(latestObs);

        // 3. Fetch Predictions from Python ML Service
        PredictionRequestDto predReq = new PredictionRequestDto(targetModel, horizonDays);
        PredictionResponseDto predictions = mlServiceClient.fetchPredictions(predReq);
        dashboard.setPrediction(predictions);

        // Cache predictions into PostgreSQL table if received
        if (predictions != null && predictions.getPredictions() != null && !predictions.getPredictions().isEmpty()) {
            persistPredictions(targetName, targetModel, predictions);
        }

        // 4. Fetch Sustainability Assessment from Python ML Service
        SustainabilityAssessmentDto sustainability = mlServiceClient.fetchSustainabilityAssessment();
        dashboard.setSustainability(sustainability);

        // 5. Fetch Benchmark Comparison
        Map<String, Object> benchmark = mlServiceClient.fetchModelBenchmark();
        dashboard.setBenchmark(benchmark);

        // 6. Historical Summary Stats
        Map<String, Object> summary = mlServiceClient.fetchHistoricalAnalytics();
        if (summary == null || summary.containsKey("error")) {
            summary = new HashMap<>();
            summary.put("total_observations", observationService.getTotalObservationsCount(targetName));
            summary.put("reservoir_name", targetName);
        }
        dashboard.setHistoricalSummary(summary);

        return dashboard;
    }

    public PredictionResponseDto getForecast(PredictionRequestDto request) {
        PredictionResponseDto response = mlServiceClient.fetchPredictions(request);
        if (response != null && response.getPredictions() != null) {
            persistPredictions(request.getModelType(), request.getModelType(), response);
        }
        return response;
    }

    public SustainabilityAssessmentDto getSustainabilityAssessment() {
        return mlServiceClient.fetchSustainabilityAssessment();
    }

    public Map<String, Object> getModelBenchmark() {
        return mlServiceClient.fetchModelBenchmark();
    }

    private void persistPredictions(String reservoirName, String modelName, PredictionResponseDto response) {
        try {
            for (DailyPredictionDto p : response.getPredictions()) {
                LocalDate fDate = LocalDate.parse(p.getDate());
                ReservoirPrediction pred = new ReservoirPrediction();
                pred.setReservoirName(reservoirName);
                pred.setForecastDate(fDate);
                pred.setPredictedStorage(p.getPredictedStorage());
                if (p.getPredictedPercentFull() != null) {
                    pred.setPredictedPercentFull(BigDecimal.valueOf(p.getPredictedPercentFull()));
                }
                pred.setPredictedWaterLevel(p.getEstimatedWaterLevel());
                pred.setConfidenceIntervalLower(p.getConfidenceLowerStorage());
                pred.setConfidenceIntervalUpper(p.getConfidenceUpperStorage());
                pred.setModelName(modelName);
                pred.setModelVersion("1.0.0");
                predictionRepository.save(pred);
            }
        } catch (Exception ex) {
            log.debug("Prediction persistence notice (duplicate or db sync): {}", ex.getMessage());
        }
    }
}
