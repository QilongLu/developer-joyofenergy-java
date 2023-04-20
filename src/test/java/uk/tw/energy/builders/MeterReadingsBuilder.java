package uk.tw.energy.builders;

import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.MeterReadings;
import uk.tw.energy.generator.ElectricityReadingsGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MeterReadingsBuilder {

    private static final String DEFAULT_METER_ID = "id";

    private String smartMeterId = DEFAULT_METER_ID;
    private List<ElectricityReading> electricityReadings = new ArrayList<>();

    public MeterReadingsBuilder setSmartMeterId(String smartMeterId) {
        this.smartMeterId = smartMeterId;
        return this;
    }

    public MeterReadingsBuilder generateElectricityReadings() {
        return generateElectricityReadings(5, Instant.now());
    }

    public MeterReadingsBuilder generateElectricityReadings(int number, Instant date) {
        ElectricityReadingsGenerator readingsBuilder = new ElectricityReadingsGenerator();
        this.electricityReadings = readingsBuilder.generate(number, date);
        return this;
    }

    public MeterReadings build() {
        return new MeterReadings(smartMeterId, electricityReadings);
    }
}
