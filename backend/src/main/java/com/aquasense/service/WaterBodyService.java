package com.aquasense.service;

import com.aquasense.model.WaterBody;
import com.aquasense.repository.WaterBodyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class WaterBodyService {

    private final WaterBodyRepository repository;

    public WaterBodyService(WaterBodyRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<WaterBody> getAllWaterBodies() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<WaterBody> getWaterBodyById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<WaterBody> getWaterBodyByName(String name) {
        return repository.findByNameIgnoreCase(name);
    }

    public WaterBody createWaterBody(WaterBody waterBody) {
        if (repository.existsByNameIgnoreCase(waterBody.getName())) {
            throw new IllegalArgumentException("Water body with name '" + waterBody.getName() + "' already exists.");
        }
        return repository.save(waterBody);
    }

    public WaterBody updateWaterBody(Long id, WaterBody updated) {
        return repository.findById(id)
                .map(existing -> {
                    if (updated.getName() != null) existing.setName(updated.getName());
                    if (updated.getState() != null) existing.setState(updated.getState());
                    if (updated.getCounty() != null) existing.setCounty(updated.getCounty());
                    if (updated.getRiverBasin() != null) existing.setRiverBasin(updated.getRiverBasin());
                    if (updated.getConservationCapacity() != null) existing.setConservationCapacity(updated.getConservationCapacity());
                    if (updated.getConservationPoolElevation() != null) existing.setConservationPoolElevation(updated.getConservationPoolElevation());
                    if (updated.getSurfaceArea() != null) existing.setSurfaceArea(updated.getSurfaceArea());
                    if (updated.getDamName() != null) existing.setDamName(updated.getDamName());
                    if (updated.getManagingAgency() != null) existing.setManagingAgency(updated.getManagingAgency());
                    if (updated.getLatitude() != null) existing.setLatitude(updated.getLatitude());
                    if (updated.getLongitude() != null) existing.setLongitude(updated.getLongitude());
                    if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Water body not found with id: " + id));
    }

    public void deleteWaterBody(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Water body not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
