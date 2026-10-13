package com.aquasense.controller;

import com.aquasense.dto.PredictionRequestDto;
import com.aquasense.dto.PredictionResponseDto;
import com.aquasense.dto.SustainabilityAssessmentDto;
import com.aquasense.dto.UnifiedDashboardDto;
import com.aquasense.service.UnifiedIntelligenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/intelligence")
@CrossOrigin(origins = "*")
public class UnifiedIntelligenceController {

    private final UnifiedIntelligenceService service;

    public UnifiedIntelligenceController(UnifiedIntelligenceService service) {
        this.service = service;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<UnifiedDashboardDto> getDashboard(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir,
            @RequestParam(defaultValue = "linear_regression") String modelType,
            @RequestParam(defaultValue = "14") int horizonDays) {
        UnifiedDashboardDto dashboard = service.getUnifiedDashboard(reservoir, modelType, horizonDays);
        return ResponseEntity.ok(dashboard);
    }

    @PostMapping("/forecast")
    public ResponseEntity<PredictionResponseDto> getForecast(
            @RequestBody PredictionRequestDto request) {
        return ResponseEntity.ok(service.getForecast(request));
    }

    @GetMapping("/sustainability")
    public ResponseEntity<SustainabilityAssessmentDto> getSustainability() {
        return ResponseEntity.ok(service.getSustainabilityAssessment());
    }

    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> getBenchmark() {
        return ResponseEntity.ok(service.getModelBenchmark());
    }
}
