package com.aquasense.repository;

import com.aquasense.model.WaterBody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaterBodyRepository extends JpaRepository<WaterBody, Long> {

    Optional<WaterBody> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<WaterBody> findByRiverBasinIgnoreCase(String riverBasin);

    List<WaterBody> findByStatusIgnoreCase(String status);
}
