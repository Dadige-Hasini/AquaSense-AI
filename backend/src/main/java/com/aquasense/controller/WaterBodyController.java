package com.aquasense.controller;

import com.aquasense.model.WaterBody;
import com.aquasense.service.WaterBodyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/water-bodies")
@CrossOrigin(origins = "*")
public class WaterBodyController {

    private final WaterBodyService service;

    public WaterBodyController(WaterBodyService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WaterBody>> getAll() {
        return ResponseEntity.ok(service.getAllWaterBodies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WaterBody> getById(@PathVariable Long id) {
        return service.getWaterBodyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-name/{name}")
    public ResponseEntity<WaterBody> getByName(@PathVariable String name) {
        return service.getWaterBodyByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WaterBody> create(@Valid @RequestBody WaterBody waterBody) {
        WaterBody created = service.createWaterBody(waterBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WaterBody> update(@PathVariable Long id, @Valid @RequestBody WaterBody updated) {
        return ResponseEntity.ok(service.updateWaterBody(id, updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteWaterBody(id);
        return ResponseEntity.noContent().build();
    }
}
