package uk.tw.energy.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
public class DayOfWeekCost {
    private DayOfWeek dayOfWeek;
    private final BigDecimal cost;
    private List<ElectricityReading> dailyElectricityReadings;
}
