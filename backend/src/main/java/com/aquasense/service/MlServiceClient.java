package com.aquasense.service;

import com.aquasense.dto.PredictionRequestDto;
import com.aquasense.dto.PredictionResponseDto;
import com.aquasense.dto.SustainabilityAssessmentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class MlServiceClient {

    private static final Logger log = LoggerFactory.getLogger(MlServiceClient.class);

    private final RestTemplate restTemplate;
    private final String mlServiceUrl;

    public MlServiceClient(
            RestTemplateBuilder builder,
            @Value("${integrations.ml-service.url:http://localhost:8000}") String mlServiceUrl) {
        this.mlServiceUrl = mlServiceUrl;
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    public PredictionResponseDto fetchPredictions(PredictionRequestDto request) {
        String url = mlServiceUrl + "/api/v1/predict";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PredictionRequestDto> entity = new HttpEntity<>(request, headers);

            ResponseEntity<PredictionResponseDto> response = restTemplate.postForEntity(
                    url, entity, PredictionResponseDto.class);
            return response.getBody();
        } catch (Exception ex) {
            log.error("Failed to fetch predictions from ML service at {}: {}", url, ex.getMessage());
            return fallbackPredictions(request);
        }
    }

    public SustainabilityAssessmentDto fetchSustainabilityAssessment() {
        String url = mlServiceUrl + "/api/v1/sustainability/assessment";
        try {
            ResponseEntity<SustainabilityAssessmentDto> response = restTemplate.getForEntity(
                    url, SustainabilityAssessmentDto.class);
            return response.getBody();
        } catch (Exception ex) {
            log.error("Failed to fetch sustainability assessment from ML service at {}: {}", url, ex.getMessage());
            return fallbackSustainability();
        }
    }

    public Map<String, Object> fetchModelBenchmark() {
        String url = mlServiceUrl + "/api/v1/models/benchmark";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getBody();
        } catch (Exception ex) {
            log.error("Failed to fetch model benchmark from ML service at {}: {}", url, ex.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("status", "UNAVAILABLE");
            fallback.put("error", ex.getMessage());
            return fallback;
        }
    }

    public Map<String, Object> fetchHistoricalAnalytics() {
        String url = mlServiceUrl + "/api/v1/analytics/historical";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getBody();
        } catch (Exception ex) {
            log.error("Failed to fetch historical analytics from ML service at {}: {}", url, ex.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("status", "UNAVAILABLE");
            fallback.put("error", ex.getMessage());
            return fallback;
        }
    }

    public boolean isHealthy() {
        String url = mlServiceUrl + "/health";
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null, new ParameterizedTypeReference<Map<String, Object>>() {});
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception ex) {
            return false;
        }
    }

    private PredictionResponseDto fallbackPredictions(PredictionRequestDto request) {
        PredictionResponseDto dto = new PredictionResponseDto();
        dto.setReservoirName("B.A. Steinhagen");
        dto.setModelType(request.getModelType() + " (fallback)");
        dto.setHorizonDays(request.getHorizonDays());
        dto.setLastObservedDate("2026-10-13");
        dto.setLastObservedStorage(66931.0);
        dto.setLastObservedPercentFull(96.6);
        dto.setPredictions(Collections.emptyList());
        return dto;
    }

    private SustainabilityAssessmentDto fallbackSustainability() {
        SustainabilityAssessmentDto dto = new SustainabilityAssessmentDto();
        dto.setReservoirName("B.A. Steinhagen");
        dto.setAssessmentDate("2026-10-13");
        dto.setCurrentStorage(66931.0);
        dto.setCurrentPercentFull(96.6);
        dto.setCurrentConservationCapacity(69186.0);
        dto.setDroughtStage("Normal Operations / Sustainable");
        dto.setWaterStressLevel("Optimal");
        dto.setRiskColorCode("#10b981");
        dto.setSustainabilityIndexScore(95.0);
        dto.setSustainabilityGrade("A");
        dto.setAdvisorySummary("Baseline operational capacity maintained. Connect to ML microservice for real-time dynamic forecasts.");
        dto.setRecommendedActions(Collections.singletonList("Verify Python ML service connectivity on port :8000"));
        return dto;
    }
}
