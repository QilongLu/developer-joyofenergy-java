package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.domain.ElectricityReading;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MeterReadingService {

    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;

    public MeterReadingService(Map<String, List<ElectricityReading>> meterAssociatedReadings) {
        this.meterAssociatedReadings = meterAssociatedReadings;
    }

    public Optional<List<ElectricityReading>> getReadings(String smartMeterId) {
        return Optional.ofNullable(meterAssociatedReadings.get(smartMeterId));
    }

    public Optional<List<ElectricityReading>> getLastWeekReadings(String smartMeterId) {
        List<ElectricityReading> thisReadings = meterAssociatedReadings.get(smartMeterId);
        List<ElectricityReading> lastWeekReadings = new ArrayList<>();
        for (ElectricityReading thisReading : thisReadings) {
            if (isWithinLastWeek(thisReading.getTime())) {
                lastWeekReadings.add(thisReading);
            }
        }
        return Optional.of(lastWeekReadings);
    }

    public boolean isWithinLastWeek(Instant time) {
        Instant now = Instant.now();
        LocalDateTime thisWeekSaturday = LocalDateTime.ofInstant(now, ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        LocalDateTime lastWeekSaturday = thisWeekSaturday.minusWeeks(1);
        Instant lastWeekStart = lastWeekSaturday.toInstant(ZoneOffset.UTC);
        Instant lastWeekEnd = lastWeekStart.plus(7, ChronoUnit.DAYS);

        return time.isAfter(lastWeekStart) && time.isBefore(lastWeekEnd);
    }

    public void storeReadings(String smartMeterId, List<ElectricityReading> electricityReadings) {
        if (!meterAssociatedReadings.containsKey(smartMeterId)) {
            meterAssociatedReadings.put(smartMeterId, new ArrayList<>());
        }
        meterAssociatedReadings.get(smartMeterId).addAll(electricityReadings);
    }
}
