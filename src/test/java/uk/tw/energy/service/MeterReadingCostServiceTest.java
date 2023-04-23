package uk.tw.energy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import uk.tw.energy.adapter.SmartMeter.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.adapter.SmartMeter.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.DayOfWeekCost;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MeterReadingCostServiceTest {

    private static final String SMART_METER_ID = "smart-meter-0";
    private static final String UNKNOWN_METER_ID = "unknown-meter";
    private static final String PRICE_PLAN_ID = "price-plan-1";
    private static final Instant TEST_DATE = Instant.now();
    private static final Instant TEST_ONE_WEEK_AGO = TEST_DATE.minus(Duration.ofDays(7));
    private static final Instant TEST_THIS_WEEK_SUNDAY = LocalDateTime.ofInstant(TEST_DATE, ZoneId.systemDefault())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toInstant(ZoneOffset.UTC);

    private static final Instant SUNDAY = getDayOfWeek(DayOfWeek.SUNDAY);
    private static final Instant MONDAY = getDayOfWeek(DayOfWeek.MONDAY);
    private static final Instant TUESDAY = getDayOfWeek(DayOfWeek.TUESDAY);
    private static final Instant WEDNESDAY = getDayOfWeek(DayOfWeek.WEDNESDAY);
    private static final Instant THURSDAY = getDayOfWeek(DayOfWeek.THURSDAY);
    private static final Instant FRIDAY = getDayOfWeek(DayOfWeek.FRIDAY);
    private static final Instant SATURDAY = getDayOfWeek(DayOfWeek.SATURDAY);

    List<ElectricityReading> customizeReadings = Arrays.asList(
            new ElectricityReading(TEST_DATE, BigDecimal.valueOf(0.2)),
            new ElectricityReading(TEST_ONE_WEEK_AGO, BigDecimal.valueOf(0.3)),
            new ElectricityReading(TEST_ONE_WEEK_AGO.plus(1, ChronoUnit.DAYS), BigDecimal.valueOf(15.0))
    );
    List<ElectricityReading> sundayReadings = getDailyElectricityReadings(SUNDAY);
    List<ElectricityReading> mondayReadings = getDailyElectricityReadings(MONDAY);
    List<ElectricityReading> tuesdayReadings = getDailyElectricityReadings(TUESDAY);
    List<ElectricityReading> wednesdayReadings = getDailyElectricityReadings(WEDNESDAY);
    List<ElectricityReading> thursdayReadings = getDailyElectricityReadings(THURSDAY);
    List<ElectricityReading> fridayReadings = getDailyElectricityReadings(FRIDAY);
    List<ElectricityReading> saturdayReadings = getDailyElectricityReadings(SATURDAY);

    @Mock
    private AccountService accountService;
    private MeterReadingCostService meterReadingCostService;
    @Mock
    private PricePlanService pricePlanService;

    @BeforeEach
    public void setUp() {
        List<ElectricityReading> testReadings = Stream.of(
                    customizeReadings,
                    sundayReadings,
                    mondayReadings,
                    tuesdayReadings,
                    wednesdayReadings,
                    thursdayReadings,
                    fridayReadings,
                    saturdayReadings)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();
        meterAssociatedReadings.put(SMART_METER_ID, testReadings);

        meterReadingCostService = new MeterReadingCostService(meterAssociatedReadings, accountService, pricePlanService);
    }


    @Test
    void shouldThrowPricePlanNotMatchedException() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID))
                .thenReturn(null);
        Assertions.assertThrows(
                PricePlanNotMatchedException.class,
                () -> meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE));
    }

    @Test
    void shouldThrowReadingsNotFoundExceptionWhenGivenAnUnknownMeterId() {
        assertThrows(
                ReadingsNotFoundException.class,
                () -> meterReadingCostService.getLastWeekCostOfTheDate(UNKNOWN_METER_ID, TEST_DATE));
    }

    @Test
    void shouldReturnCorrectCosts() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), eq(PRICE_PLAN_ID))).thenReturn(BigDecimal.valueOf(1848.0));
        BigDecimal lastWeekCosts = meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE);
        assertEquals(BigDecimal.valueOf(1848.0), lastWeekCosts);
    }

    @Test
    void shouldReturnCorrectCostsFromLastWeekOfTheDay() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), eq(PRICE_PLAN_ID))).thenReturn(BigDecimal.valueOf(1248.0));
        BigDecimal lastWeekOfTheDayCosts = meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_THIS_WEEK_SUNDAY);
        assertEquals(BigDecimal.valueOf(1248.0), lastWeekOfTheDayCosts);
    }

    @Test
    void shouldReturnDailyCostForWeek() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), eq(PRICE_PLAN_ID))).thenReturn(
                BigDecimal.valueOf(120.0), BigDecimal.valueOf(140.0),
                BigDecimal.valueOf(160.0), BigDecimal.valueOf(170.0),
                BigDecimal.valueOf(110.0), BigDecimal.valueOf(130.0), BigDecimal.valueOf(100.0));

        List<DayOfWeekCost> testDailyCostOfWeek = new ArrayList<>();
        DayOfWeekCost sundayCost = buildDayOfWeekCost(sundayReadings, 100.0, DayOfWeek.SUNDAY);
        DayOfWeekCost mondayCost = buildDayOfWeekCost(mondayReadings, 120.0, DayOfWeek.MONDAY);
        DayOfWeekCost tuesdayCost = buildDayOfWeekCost(tuesdayReadings, 140.0, DayOfWeek.TUESDAY);
        DayOfWeekCost wednesdayCost = buildDayOfWeekCost(wednesdayReadings, 160.0, DayOfWeek.WEDNESDAY);
        DayOfWeekCost thursdayCost = buildDayOfWeekCost(thursdayReadings, 170.0, DayOfWeek.THURSDAY);
        DayOfWeekCost fridayCost = buildDayOfWeekCost(fridayReadings, 110.0, DayOfWeek.FRIDAY);
        DayOfWeekCost saturdayCost = buildDayOfWeekCost(saturdayReadings, 130.0, DayOfWeek.SATURDAY);

        testDailyCostOfWeek.add(sundayCost);
        testDailyCostOfWeek.add(mondayCost);
        testDailyCostOfWeek.add(tuesdayCost);
        testDailyCostOfWeek.add(wednesdayCost);
        testDailyCostOfWeek.add(thursdayCost);
        testDailyCostOfWeek.add(fridayCost);
        testDailyCostOfWeek.add(saturdayCost);

        List<DayOfWeekCost> dailyCostOfWeek = meterReadingCostService.getDayOfWeekCost(SMART_METER_ID);

        assertEquals(dailyCostOfWeek.get(0).getDayOfWeek(), testDailyCostOfWeek.get(1).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(0).getCost(), testDailyCostOfWeek.get(1).getCost());
        assertEquals(dailyCostOfWeek.get(1).getDayOfWeek(), testDailyCostOfWeek.get(2).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(1).getCost(), testDailyCostOfWeek.get(2).getCost());
        assertEquals(dailyCostOfWeek.get(2).getDayOfWeek(), testDailyCostOfWeek.get(3).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(2).getCost(), testDailyCostOfWeek.get(3).getCost());
        assertEquals(dailyCostOfWeek.get(3).getDayOfWeek(), testDailyCostOfWeek.get(4).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(3).getCost(), testDailyCostOfWeek.get(4).getCost());
        assertEquals(dailyCostOfWeek.get(4).getDayOfWeek(), testDailyCostOfWeek.get(5).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(4).getCost(), testDailyCostOfWeek.get(5).getCost());
        assertEquals(dailyCostOfWeek.get(5).getDayOfWeek(), testDailyCostOfWeek.get(6).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(5).getCost(), testDailyCostOfWeek.get(6).getCost());
        assertEquals(dailyCostOfWeek.get(6).getDayOfWeek(), testDailyCostOfWeek.get(0).getDayOfWeek());
        assertEquals(dailyCostOfWeek.get(6).getCost(), testDailyCostOfWeek.get(0).getCost());
    }
    private static Instant getDayOfWeek(DayOfWeek dayOfWeek) {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(dayOfWeek)).toInstant(ZoneOffset.UTC);
    }
    private static DayOfWeekCost buildDayOfWeekCost(List<ElectricityReading> dailyReadings, Double cost, DayOfWeek dayOfWeek) {
        return DayOfWeekCost.builder()
                .dayOfWeek(dayOfWeek)
                .cost(BigDecimal.valueOf(cost))
                .dailyElectricityReadings(dailyReadings)
                .build();
    }

    private static List<ElectricityReading> getDailyElectricityReadings(Instant weekDay) {
        return new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, weekDay)
                .build()
                .getElectricityReadings();
    }
}
