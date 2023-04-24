package uk.tw.energy.service;

import org.springframework.stereotype.Service;
import uk.tw.energy.adapter.SmartMeter.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.domain.PricePlan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PricePlanService {

    private final List<PricePlan> pricePlans;
    private final MeterReadingService meterReadingService;

    public PricePlanService(List<PricePlan> pricePlans, MeterReadingService meterReadingService) {
        this.pricePlans = pricePlans;
        this.meterReadingService = meterReadingService;
    }

    public Optional<Map<String, BigDecimal>> getCostOfElectricityReadingsForEachPricePlan(String smartMeterId) {
        Optional<List<ElectricityReading>> electricityReadings = meterReadingService.getReadings(smartMeterId);

        return electricityReadings.map(readings -> pricePlans.stream().collect(
                Collectors.toMap(PricePlan::getPlanName, t -> calculateCost(readings, t.getPlanName()))));
    }


    public Integer getRankForCurrentPricePlan(List<ElectricityReading> dailyReadings, String pricePlanId) {
        Map<String, BigDecimal> dailyCostForEachPricePlan = pricePlans.stream().collect(
                Collectors.toMap(PricePlan::getPlanName, t -> calculateCostByDateAndAddUp(dailyReadings, t.getPlanName())));

        List<String> sortedCostOfPricePlans = dailyCostForEachPricePlan.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return sortedCostOfPricePlans.indexOf(pricePlanId);
    }

    public BigDecimal calculateCostByDateAndAddUp(List<ElectricityReading> dailyReadings, String pricePlanId) {
        Map<LocalDate, List<ElectricityReading>> readingsByDate = dailyReadings.stream()
                .collect(Collectors.groupingBy(reading -> LocalDateTime.ofInstant(reading.getTime(), ZoneOffset.UTC).toLocalDate()));

        return readingsByDate.values().stream()
                .map(readings -> calculateCost(readings, pricePlanId))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal calculateConsumed(List<ElectricityReading> electricityReadings) {
        BigDecimal average = calculateAverageReading(electricityReadings);
        BigDecimal timeElapsed = calculateTimeElapsed(electricityReadings);
        return average.multiply(timeElapsed);
    }

    public BigDecimal calculateCost(List<ElectricityReading> electricityReadings, String pricePlanId) {
        if (electricityReadings.isEmpty()) {throw new ReadingsNotFoundException();}
        if (electricityReadings.size() == 1) {throw new IllegalArgumentException("Invalid reading");}
        PricePlan pricePlan = pricePlans.stream()
                .filter(p -> p.getPlanName().equals(pricePlanId))
                .findFirst()
                .get();
        BigDecimal energyConsumed = calculateConsumed(electricityReadings);

        return energyConsumed.multiply(pricePlan.getUnitRate()).setScale(1, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateAverageReading(List<ElectricityReading> electricityReadings) {
        BigDecimal summedReadings = electricityReadings.stream()
                .map(ElectricityReading::getReading)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return summedReadings.divide(BigDecimal.valueOf(electricityReadings.size()), RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTimeElapsed(List<ElectricityReading> electricityReadings) {
        ElectricityReading first = electricityReadings.stream()
                .min(Comparator.comparing(ElectricityReading::getTime))
                .get();
        ElectricityReading last = electricityReadings.stream()
                .max(Comparator.comparing(ElectricityReading::getTime))
                .get();

        return BigDecimal.valueOf(Duration.between(first.getTime(), last.getTime()).getSeconds() / 3600.0);
    }

}
