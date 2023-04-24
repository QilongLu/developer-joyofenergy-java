package uk.tw.energy.builders;

import uk.tw.energy.domain.DayOfWeekCost;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

public class DailyInfoBuilder {
    public static final String SMART_METER_ID = "smart-meter-0";
    public static final String UNKNOWN_METER_ID = "unknown-meter";
    public static final String PRICE_PLAN_ID = "price-plan-1";
    public static final Instant TEST_DATE = Instant.now();
    public static final Instant SUNDAY = getDayOfWeek(DayOfWeek.SUNDAY);
    public static final Instant MONDAY = getDayOfWeek(DayOfWeek.MONDAY);
    public static final Instant TUESDAY = getDayOfWeek(DayOfWeek.TUESDAY);
    public static final Instant WEDNESDAY = getDayOfWeek(DayOfWeek.WEDNESDAY);
    public static final Instant THURSDAY = getDayOfWeek(DayOfWeek.THURSDAY);
    public static final Instant FRIDAY = getDayOfWeek(DayOfWeek.FRIDAY);
    public static final Instant SATURDAY = getDayOfWeek(DayOfWeek.SATURDAY);

    public static List<ElectricityReading> sundayReadings = getDailyElectricityReadings(SUNDAY);
    public static List<ElectricityReading> mondayReadings = getDailyElectricityReadings(MONDAY);
    public static List<ElectricityReading> tuesdayReadings = getDailyElectricityReadings(TUESDAY);
    public static List<ElectricityReading> wednesdayReadings = getDailyElectricityReadings(WEDNESDAY);
    public static List<ElectricityReading> thursdayReadings = getDailyElectricityReadings(THURSDAY);
    public static List<ElectricityReading> fridayReadings = getDailyElectricityReadings(FRIDAY);
    public static List<ElectricityReading> saturdayReadings = getDailyElectricityReadings(SATURDAY);

    static DayOfWeekCost sundayCost = buildDayOfWeekCost(sundayReadings, 100.0, DayOfWeek.SUNDAY);
    static DayOfWeekCost mondayCost = buildDayOfWeekCost(mondayReadings, 120.0, DayOfWeek.MONDAY);
    static DayOfWeekCost tuesdayCost = buildDayOfWeekCost(tuesdayReadings, 140.0, DayOfWeek.TUESDAY);
    static DayOfWeekCost wednesdayCost = buildDayOfWeekCost(wednesdayReadings, 160.0, DayOfWeek.WEDNESDAY);
    static DayOfWeekCost thursdayCost = buildDayOfWeekCost(thursdayReadings, 180.0, DayOfWeek.THURSDAY);
    static DayOfWeekCost fridayCost = buildDayOfWeekCost(fridayReadings, 110.0, DayOfWeek.FRIDAY);
    static DayOfWeekCost saturdayCost = buildDayOfWeekCost(saturdayReadings, 130.0, DayOfWeek.SATURDAY);

    private static Instant getDayOfWeek(DayOfWeek dayOfWeek) {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(dayOfWeek)).toInstant(ZoneOffset.UTC);
    }

    public static DayOfWeekCost buildDayOfWeekCost(List<ElectricityReading> dailyReadings, Double cost, DayOfWeek dayOfWeek) {
        return DayOfWeekCost.builder()
                .dayOfWeek(dayOfWeek)
                .cost(BigDecimal.valueOf(cost))
                .dailyElectricityReadings(dailyReadings)
                .build();
    }

    public static List<ElectricityReading> getDailyElectricityReadings(Instant weekDay) {
        return new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, weekDay)
                .build()
                .getElectricityReadings();
    }

    public static List<DayOfWeekCost> buildDaysOfWeekCostsList() {
        return List.of(
                sundayCost,
                mondayCost,
                tuesdayCost,
                wednesdayCost,
                thursdayCost,
                fridayCost,
                saturdayCost
        );
    }
}
