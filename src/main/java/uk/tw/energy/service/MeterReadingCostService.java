package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.controller.PricePlanNotMatchedException;
import uk.tw.energy.domain.ElectricityReading;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MeterReadingCostService {
    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;
    private final AccountService accountService;

    public MeterReadingCostService(Map<String, List<ElectricityReading>> meterAssociatedReadings, AccountService accountService) {
        this.meterAssociatedReadings = meterAssociatedReadings;
        this.accountService = accountService;
    }
    public Optional<List<ElectricityReading>> getLastWeekReadings(String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        if (pricePlanId==null) {
            throw new PricePlanNotMatchedException(smartMeterId);
        }
        List<ElectricityReading> thisReadings = meterAssociatedReadings.get(smartMeterId);
        List<ElectricityReading> lastWeekReadings = thisReadings.stream()
                .filter(reading -> isWithinLastWeek(reading.getTime()))
                .collect(Collectors.toList());
        return Optional.of(lastWeekReadings);
    }

    private boolean isWithinLastWeek(Instant time) {
        Instant now = Instant.now();
        LocalDateTime thisWeekSaturday = LocalDateTime.ofInstant(now, ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY));
        LocalDateTime lastWeekSaturday = thisWeekSaturday.minusWeeks(1);
        Instant lastWeekStart = lastWeekSaturday.toInstant(ZoneOffset.UTC);
        Instant lastWeekEnd = lastWeekStart.plus(7, ChronoUnit.DAYS);

        return time.isAfter(lastWeekStart) && time.isBefore(lastWeekEnd);
    }
}
