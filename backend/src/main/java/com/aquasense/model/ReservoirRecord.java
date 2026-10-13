package com.aquasense.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "reservoir_records", uniqueConstraints = {
    @UniqueConstraint(name = "uq_reservoir_record", columnNames = {"reservoir_name", "record_date"})
})
public class ReservoirRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reservoir_name", nullable = false, length = 100)
    private String reservoirName = "B.A. Steinhagen";

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "water_level")
    private Double waterLevel;

    @Column(name = "surface_area")
    private Double surfaceArea;

    @Column(name = "reservoir_storage")
    private Double reservoirStorage;

    @Column(name = "conservation_storage")
    private Double conservationStorage;

    @Column(name = "percent_full", precision = 5, scale = 2)
    private BigDecimal percentFull;

    @Column(name = "conservation_capacity")
    private Double conservationCapacity;

    @Column(name = "source", length = 50)
    private String source = "HISTORICAL";

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public ReservoirRecord() {
    }

    public ReservoirRecord(String reservoirName, LocalDate recordDate, Double reservoirStorage, 
                           Double conservationCapacity, BigDecimal percentFull) {
        this.reservoirName = reservoirName;
        this.recordDate = recordDate;
        this.reservoirStorage = reservoirStorage;
        this.conservationCapacity = conservationCapacity;
        this.percentFull = percentFull;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getReservoirName() {
        return reservoirName;
    }

    public void setReservoirName(String reservoirName) {
        this.reservoirName = reservoirName;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public Double getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(Double waterLevel) {
        this.waterLevel = waterLevel;
    }

    public Double getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(Double surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public Double getReservoirStorage() {
        return reservoirStorage;
    }

    public void setReservoirStorage(Double reservoirStorage) {
        this.reservoirStorage = reservoirStorage;
    }

    public Double getConservationStorage() {
        return conservationStorage;
    }

    public void setConservationStorage(Double conservationStorage) {
        this.conservationStorage = conservationStorage;
    }

    public BigDecimal getPercentFull() {
        return percentFull;
    }

    public void setPercentFull(BigDecimal percentFull) {
        this.percentFull = percentFull;
    }

    public Double getConservationCapacity() {
        return conservationCapacity;
    }

    public void setConservationCapacity(Double conservationCapacity) {
        this.conservationCapacity = conservationCapacity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
