package com.aquasense.config;

import com.aquasense.model.ReservoirRecord;
import com.aquasense.model.WaterBody;
import com.aquasense.repository.ReservoirRecordRepository;
import com.aquasense.repository.WaterBodyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final WaterBodyRepository waterBodyRepository;
    private final ReservoirRecordRepository recordRepository;

    public DataInitializer(WaterBodyRepository waterBodyRepository, ReservoirRecordRepository recordRepository) {
        this.waterBodyRepository = waterBodyRepository;
        this.recordRepository = recordRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Seed B.A. Steinhagen Lake in Catalog if missing
        if (!waterBodyRepository.existsByNameIgnoreCase("B.A. Steinhagen")) {
            WaterBody steinhagen = new WaterBody();
            steinhagen.setName("B.A. Steinhagen");
            steinhagen.setState("Texas");
            steinhagen.setCounty("Tyler / Jasper");
            steinhagen.setRiverBasin("Neches River Basin");
            steinhagen.setConservationCapacity(69186.0);
            steinhagen.setConservationPoolElevation(82.5);
            steinhagen.setSurfaceArea(13700.0);
            steinhagen.setDamName("Town Bluff Dam / Dam B");
            steinhagen.setManagingAgency("US Army Corps of Engineers / TWDB");
            steinhagen.setLatitude(30.7989);
            steinhagen.setLongitude(-94.1706);
            steinhagen.setStatus("ACTIVE");

            waterBodyRepository.save(steinhagen);
            log.info("Initialized default water body catalog entry for 'B.A. Steinhagen'.");
        }

        // 2. Seed baseline record if no observations exist
        if (recordRepository.count() == 0) {
            ReservoirRecord baseline = new ReservoirRecord();
            baseline.setReservoirName("B.A. Steinhagen");
            baseline.setRecordDate(LocalDate.now());
            baseline.setReservoirStorage(66931.0);
            baseline.setConservationCapacity(69186.0);
            baseline.setPercentFull(BigDecimal.valueOf(96.6));
            baseline.setWaterLevel(82.4);
            baseline.setSurfaceArea(13700.0);
            baseline.setSource("SEED_INITIALIZER");

            recordRepository.save(baseline);
            log.info("Initialized baseline observation record for 'B.A. Steinhagen'.");
        }
    }
}
