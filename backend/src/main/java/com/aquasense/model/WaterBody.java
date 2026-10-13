package com.aquasense.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.OffsetDateTime;

@Entity
@Table(name = "water_bodies")
public class WaterBody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Water body name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 50)
    private String state = "Texas";

    @Column(length = 100)
    private String county = "Tyler / Jasper";

    @Column(name = "river_basin", length = 100)
    private String riverBasin = "Neches River Basin";

    @Column(name = "conservation_capacity")
    private Double conservationCapacity = 69186.0;

    @Column(name = "conservation_pool_elevation")
    private Double conservationPoolElevation = 82.5;

    @Column(name = "surface_area")
    private Double surfaceArea = 13700.0;

    @Column(name = "dam_name", length = 100)
    private String damName = "Town Bluff Dam / Dam B";

    @Column(name = "managing_agency", length = 100)
    private String managingAgency = "US Army Corps of Engineers / TWDB";

    private Double latitude = 30.7989;

    private Double longitude = -94.1706;

    @Column(length = 50)
    private String status = "ACTIVE";

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public WaterBody() {
    }

    public WaterBody(String name, String riverBasin, Double conservationCapacity) {
        this.name = name;
        this.riverBasin = riverBasin;
        this.conservationCapacity = conservationCapacity;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getRiverBasin() {
        return riverBasin;
    }

    public void setRiverBasin(String riverBasin) {
        this.riverBasin = riverBasin;
    }

    public Double getConservationCapacity() {
        return conservationCapacity;
    }

    public void setConservationCapacity(Double conservationCapacity) {
        this.conservationCapacity = conservationCapacity;
    }

    public Double getConservationPoolElevation() {
        return conservationPoolElevation;
    }

    public void setConservationPoolElevation(Double conservationPoolElevation) {
        this.conservationPoolElevation = conservationPoolElevation;
    }

    public Double getSurfaceArea() {
        return surfaceArea;
    }

    public void setSurfaceArea(Double surfaceArea) {
        this.surfaceArea = surfaceArea;
    }

    public String getDamName() {
        return damName;
    }

    public void setDamName(String damName) {
        this.damName = damName;
    }

    public String getManagingAgency() {
        return managingAgency;
    }

    public void setManagingAgency(String managingAgency) {
        this.managingAgency = managingAgency;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
