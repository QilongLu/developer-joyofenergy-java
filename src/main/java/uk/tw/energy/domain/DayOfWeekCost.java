package uk.tw.energy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class DayOfWeekCost {
    private LocalDate date;
    private final BigDecimal dailyCost;
    private List<ElectricityReading> dailyElectricityReadings;
}
