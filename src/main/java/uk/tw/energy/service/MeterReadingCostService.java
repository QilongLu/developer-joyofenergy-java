package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.adapter.SmartMeter.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.adapter.SmartMeter.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.domain.DayOfWeekCost;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
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
import java.util.TreeMap;
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

    public BigDecimal getLastWeekCostOfTheDate(String smartMeterId, Instant enteredDate) {
        List<ElectricityReading> thisReadings = meterAssociatedReadings.get(smartMeterId);
        if (thisReadings == null) {throw new ReadingsNotFoundException();}
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        if (pricePlanId==null) {throw new PricePlanNotMatchedException(smartMeterId);}
        List<ElectricityReading> lastWeekReadings = thisReadings.stream()
                .filter(reading -> isWithinLastWeek(reading.getTime(), enteredDate))
                .collect(Collectors.toList());
        return pricePlanService.calculateCost(lastWeekReadings, pricePlanId);
    }

    private boolean isWithinLastWeek(Instant readingTime, Instant enteredDate) {
        LocalDateTime thisWeekSunday = LocalDateTime.ofInstant(enteredDate, ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDateTime lastWeekSunday = thisWeekSunday
                .minusWeeks(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        Instant lastWeekStart = lastWeekSunday.toInstant(ZoneOffset.UTC);
        Instant lastWeekEnd = lastWeekStart.plus(7, ChronoUnit.DAYS);
        return readingTime.isAfter(lastWeekStart) && readingTime.isBefore(lastWeekEnd);
    }

    public List<DayOfWeekCost> getDayOfWeekCost(String smartMeterId) {
        List<ElectricityReading> readings = meterAssociatedReadings.get(smartMeterId);
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);

        Map<DayOfWeek, List<ElectricityReading>> dailyOfWeekReadings = readings.stream()
                .collect(Collectors.groupingBy(
                        reading -> reading.getTime().atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek(),
                        TreeMap::new, Collectors.toList()));

        List<DayOfWeekCost> dayOfWeekCosts = new ArrayList<>();
        for (DayOfWeek dayOfWeek : dailyOfWeekReadings.keySet()) {
            DayOfWeekCost dayOfWeekCost = DayOfWeekCost.builder()
                    .dayOfWeek(dayOfWeek)
                    .cost(pricePlanService.calculateCost(dailyOfWeekReadings.get(dayOfWeek), pricePlanId))
                    .dailyElectricityReadings(dailyOfWeekReadings.get(dayOfWeek))
                    .build();
            dayOfWeekCosts.add(dayOfWeekCost);
        }
        return dayOfWeekCosts;
    }
}
