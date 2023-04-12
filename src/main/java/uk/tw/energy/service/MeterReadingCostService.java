package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MeterReadingCostService {
    private final Map<String, List<ElectricityReading>> meterAssociatedReadings;
    private final AccountService accountService;
    private final PricePlanService pricePlanService;

    public MeterReadingCostService(Map<String, List<ElectricityReading>> meterAssociatedReadings,
                                   AccountService accountService,
                                   PricePlanService pricePlanService) {
        this.meterAssociatedReadings = meterAssociatedReadings;
        this.accountService = accountService;
        this.pricePlanService = pricePlanService;
    }
    public BigDecimal getLastWeekCost(String smartMeterId) throws ReadingsNotFoundException {
        List<ElectricityReading> thisReadings = meterAssociatedReadings.get(smartMeterId);
        if (thisReadings == null) {throw new ReadingsNotFoundException();}
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        if (pricePlanId==null) {throw new PricePlanNotMatchedException(smartMeterId);}
        List<ElectricityReading> lastWeekReadings = thisReadings.stream()
                .filter(reading -> isWithinLastWeek(reading.getTime()))
                .collect(Collectors.toList());
        return pricePlanService.calculateCost(lastWeekReadings, pricePlanId);
    }

    private boolean isWithinLastWeek(Instant time) {
        Instant now = Instant.now();
        LocalDateTime thisWeekSunday = LocalDateTime.ofInstant(now, ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDateTime lastWeekSunday = thisWeekSunday
                .minusWeeks(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        Instant lastWeekStart = lastWeekSunday.toInstant(ZoneOffset.UTC);
        Instant lastWeekEnd = lastWeekStart.plus(7, ChronoUnit.DAYS);
        return time.isAfter(lastWeekStart) && time.isBefore(lastWeekEnd);
    }
}
