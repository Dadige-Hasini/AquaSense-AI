package com.aquasense.repository;

import com.aquasense.model.ReservoirPrediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservoirPredictionRepository extends JpaRepository<ReservoirPrediction, Long> {

    List<ReservoirPrediction> findByReservoirNameOrderByForecastDateAsc(String reservoirName);

    List<ReservoirPrediction> findByReservoirNameAndModelNameOrderByForecastDateAsc(
            String reservoirName, String modelName);

    void deleteByReservoirName(String reservoirName);
}
