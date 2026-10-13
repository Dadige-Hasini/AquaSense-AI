package com.aquasense.service;

import com.aquasense.model.ReservoirRecord;
import com.aquasense.repository.ReservoirRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@Transactional
public class ObservationService {

    private final ReservoirRecordRepository repository;

    public ObservationService(ReservoirRecordRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ReservoirRecord> getObservations(
            String reservoirName, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            return repository.findByReservoirNameAndRecordDateBetweenOrderByRecordDateDesc(
                    reservoirName, startDate, endDate, pageable);
        }
        return repository.findByReservoirNameOrderByRecordDateDesc(reservoirName, pageable);
    }

    @Transactional(readOnly = true)
    public Optional<ReservoirRecord> getObservationById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<ReservoirRecord> getLatestObservation(String reservoirName) {
        return repository.findFirstByReservoirNameOrderByRecordDateDesc(reservoirName);
    }

    public ReservoirRecord createObservation(ReservoirRecord record) {
        if (record.getReservoirName() == null || record.getReservoirName().trim().isEmpty()) {
            record.setReservoirName("B.A. Steinhagen");
        }
        if (record.getRecordDate() == null) {
            throw new IllegalArgumentException("Observation recordDate is mandatory.");
        }
        if (repository.existsByReservoirNameAndRecordDate(record.getReservoirName(), record.getRecordDate())) {
            throw new IllegalArgumentException("Observation for reservoir '" + record.getReservoirName() 
                    + "' on date " + record.getRecordDate() + " already exists.");
        }
        return repository.save(record);
    }

    public ReservoirRecord updateObservation(Long id, ReservoirRecord updated) {
        return repository.findById(id)
                .map(existing -> {
                    if (updated.getWaterLevel() != null) existing.setWaterLevel(updated.getWaterLevel());
                    if (updated.getSurfaceArea() != null) existing.setSurfaceArea(updated.getSurfaceArea());
                    if (updated.getReservoirStorage() != null) existing.setReservoirStorage(updated.getReservoirStorage());
                    if (updated.getConservationStorage() != null) existing.setConservationStorage(updated.getConservationStorage());
                    if (updated.getPercentFull() != null) existing.setPercentFull(updated.getPercentFull());
                    if (updated.getConservationCapacity() != null) existing.setConservationCapacity(updated.getConservationCapacity());
                    if (updated.getSource() != null) existing.setSource(updated.getSource());
                    return repository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Observation not found with id: " + id));
    }

    public void deleteObservation(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Observation not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getTotalObservationsCount(String reservoirName) {
        return repository.countByReservoirName(reservoirName);
    }
}
