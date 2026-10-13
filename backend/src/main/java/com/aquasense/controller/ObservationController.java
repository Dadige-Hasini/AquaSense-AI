package com.aquasense.controller;

import com.aquasense.model.ReservoirRecord;
import com.aquasense.service.ObservationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/observations")
@CrossOrigin(origins = "*")
public class ObservationController {

    private final ObservationService service;

    public ObservationController(ObservationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Page<ReservoirRecord>> getObservations(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "recordDate") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<ReservoirRecord> records = service.getObservations(reservoir, startDate, endDate, pageRequest);
        return ResponseEntity.ok(records);
    }

    @GetMapping("/latest")
    public ResponseEntity<ReservoirRecord> getLatest(
            @RequestParam(defaultValue = "B.A. Steinhagen") String reservoir) {
        return service.getLatestObservation(reservoir)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservoirRecord> getById(@PathVariable Long id) {
        return service.getObservationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ReservoirRecord> create(@Valid @RequestBody ReservoirRecord record) {
        ReservoirRecord created = service.createObservation(record);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservoirRecord> update(
            @PathVariable Long id, @Valid @RequestBody ReservoirRecord updated) {
        return ResponseEntity.ok(service.updateObservation(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteObservation(id);
        return ResponseEntity.noContent().build();
    }
}
