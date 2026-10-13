package com.aquasense.controller;

import com.aquasense.model.ReservoirRecord;
import com.aquasense.repository.ReservoirRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reservoirs")
@CrossOrigin(origins = "*")
public class ReservoirController {

    private final ReservoirRecordRepository repository;

    public ReservoirController(ReservoirRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "aquasense-backend");
        status.put("status", "UP");
        status.put("timestamp", LocalDate.now());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/status/latest")
    public ResponseEntity<ReservoirRecord> getLatestStatus(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir) {
        return repository.findFirstByReservoirNameOrderByRecordDateDesc(reservoir)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/records")
    public ResponseEntity<Page<ReservoirRecord>> getRecords(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("recordDate").descending());
        return ResponseEntity.ok(repository.findByReservoirNameOrderByRecordDateDesc(reservoir, pageRequest));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ReservoirRecord>> getHistory(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(repository.findByReservoirNameAndRecordDateBetweenOrderByRecordDateAsc(reservoir, start, end));
    }

    @PostMapping("/records")
    public ResponseEntity<ReservoirRecord> addRecord(@RequestBody ReservoirRecord record) {
        ReservoirRecord saved = repository.save(record);
        return ResponseEntity.ok(saved);
    }
}
