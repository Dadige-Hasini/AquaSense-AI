package com.aquasense.repository;

import com.aquasense.model.ReservoirRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservoirRecordRepository extends JpaRepository<ReservoirRecord, Long> {

    Optional<ReservoirRecord> findFirstByReservoirNameOrderByRecordDateDesc(String reservoirName);

    Optional<ReservoirRecord> findByReservoirNameAndRecordDate(String reservoirName, LocalDate recordDate);

    boolean existsByReservoirNameAndRecordDate(String reservoirName, LocalDate recordDate);

    Page<ReservoirRecord> findByReservoirNameOrderByRecordDateDesc(String reservoirName, Pageable pageable);

    Page<ReservoirRecord> findByReservoirNameAndRecordDateBetweenOrderByRecordDateDesc(
            String reservoirName, LocalDate startDate, LocalDate endDate, Pageable pageable);

    List<ReservoirRecord> findByReservoirNameAndRecordDateBetweenOrderByRecordDateAsc(
            String reservoirName, LocalDate startDate, LocalDate endDate);

    long countByReservoirName(String reservoirName);

    @Query("SELECT AVG(r.reservoirStorage) FROM ReservoirRecord r WHERE r.reservoirName = :reservoirName")
    Double findAverageStorage(String reservoirName);

    @Query("SELECT MAX(r.recordDate) FROM ReservoirRecord r WHERE r.reservoirName = :reservoirName")
    LocalDate findLatestRecordDate(String reservoirName);
}
