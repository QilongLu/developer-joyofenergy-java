package uk.tw.energy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.MeterReadingService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class MeterReadingCostControllerTest {
    private static final String SMART_METER_ID = "10101010";
    private final MeterReadingService meterReadingService;
    private final MeterReadingCostController meterReadingCostController;

    MeterReadingCostControllerTest(MeterReadingService meterReadingService, MeterReadingCostController meterReadingCostController) {
        this.meterReadingService = meterReadingService;
        this.meterReadingCostController = meterReadingCostController;
    }


    @Test
    public void givenMeterIdShouldReturnLastWeekCostUsage() {
        Instant now = Instant.now();
        ElectricityReading lastWeekReading = new ElectricityReading(now.minus(8,
                ChronoUnit.DAYS),
                BigDecimal.valueOf(0.5));
        ElectricityReading yesterdayReading = new ElectricityReading(now.minus(1,
                ChronoUnit.DAYS),
                BigDecimal.valueOf(0.8));
        ElectricityReading todayReading = new ElectricityReading(now, BigDecimal.valueOf(1.2));
        List<ElectricityReading> readings = Arrays.asList(lastWeekReading, yesterdayReading, todayReading);
        meterReadingService.storeReadings(SMART_METER_ID, readings);

        ResponseEntity<List<ElectricityReading>> result = meterReadingCostController.getLastWeekReadings(SMART_METER_ID);

        List<ElectricityReading> lastWeekReadings = result.getBody();
        assert lastWeekReadings != null;
        assertEquals(1, lastWeekReadings.size());
        assertEquals(lastWeekReading, lastWeekReadings.get(0));
    }
}