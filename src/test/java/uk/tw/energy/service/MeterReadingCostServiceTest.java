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
import uk.tw.energy.builders.DailyInfoBuilder;
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

    private static final Instant TEST_ONE_WEEK_AGO = DailyInfoBuilder.TEST_DATE.minus(Duration.ofDays(7));
    private static final Instant TEST_THIS_WEEK_SUNDAY = LocalDateTime.ofInstant(DailyInfoBuilder.TEST_DATE, ZoneId.systemDefault())
            .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)).toInstant(ZoneOffset.UTC);

    List<ElectricityReading> customizeReadings = Arrays.asList(
            new ElectricityReading(DailyInfoBuilder.TEST_DATE, BigDecimal.valueOf(0.2)),
            new ElectricityReading(DailyInfoBuilder.TEST_DATE, BigDecimal.valueOf(0.5)),
            new ElectricityReading(TEST_ONE_WEEK_AGO, BigDecimal.valueOf(0.3)),
            new ElectricityReading(TEST_ONE_WEEK_AGO, BigDecimal.valueOf(0.6)),
            new ElectricityReading(TEST_ONE_WEEK_AGO.plus(1, ChronoUnit.DAYS), BigDecimal.valueOf(15.0))
            ,new ElectricityReading(TEST_ONE_WEEK_AGO.plus(1, ChronoUnit.DAYS), BigDecimal.valueOf(18.2))
    );

    @Mock
    private AccountService accountService;
    private MeterReadingCostService meterReadingCostService;
    @Mock
    private PricePlanService pricePlanService;

    @BeforeEach
    public void setUp() {
        List<ElectricityReading> testReadings = Stream.of(
                    customizeReadings,
                    DailyInfoBuilder.sundayReadings,
                    DailyInfoBuilder.mondayReadings,
                    DailyInfoBuilder.tuesdayReadings,
                    DailyInfoBuilder.wednesdayReadings,
                    DailyInfoBuilder.thursdayReadings,
                    DailyInfoBuilder.fridayReadings,
                    DailyInfoBuilder.saturdayReadings)
                .flatMap(List::stream)
                .collect(Collectors.toList());
        Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();
        meterAssociatedReadings.put(DailyInfoBuilder.SMART_METER_ID, testReadings);

        meterReadingCostService = new MeterReadingCostService(meterAssociatedReadings, accountService, pricePlanService);
    }


    @Test
    void shouldThrowPricePlanNotMatchedException() {
        when(accountService.getPricePlanIdForSmartMeterId(DailyInfoBuilder.SMART_METER_ID))
                .thenReturn(null);
        Assertions.assertThrows(
                PricePlanNotMatchedException.class,
                () -> meterReadingCostService.getLastWeekCostOfTheDate(DailyInfoBuilder.SMART_METER_ID, DailyInfoBuilder.TEST_DATE));
    }

    @Test
    void shouldThrowReadingsNotFoundExceptionWhenGivenAnUnknownMeterId() {
        assertThrows(
                ReadingsNotFoundException.class,
                () -> meterReadingCostService.getLastWeekCostOfTheDate(DailyInfoBuilder.UNKNOWN_METER_ID, DailyInfoBuilder.TEST_DATE));
    }

    @Test
    void shouldReturnCorrectCosts() {
        when(accountService.getPricePlanIdForSmartMeterId(DailyInfoBuilder.SMART_METER_ID)).thenReturn(DailyInfoBuilder.PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), eq(DailyInfoBuilder.PRICE_PLAN_ID))).thenReturn(BigDecimal.valueOf(1848.0));
        BigDecimal lastWeekCosts = meterReadingCostService.getLastWeekCostOfTheDate(DailyInfoBuilder.SMART_METER_ID, DailyInfoBuilder.TEST_DATE);
        assertEquals(BigDecimal.valueOf(1848.0), lastWeekCosts);
    }

    @Test
    void shouldReturnCorrectCostsFromLastWeekOfTheDay() {
        when(accountService.getPricePlanIdForSmartMeterId(DailyInfoBuilder.SMART_METER_ID)).thenReturn(DailyInfoBuilder.PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), eq(DailyInfoBuilder.PRICE_PLAN_ID))).thenReturn(BigDecimal.valueOf(1248.0));
        BigDecimal lastWeekOfTheDayCosts = meterReadingCostService.getLastWeekCostOfTheDate(DailyInfoBuilder.SMART_METER_ID, TEST_THIS_WEEK_SUNDAY);
        assertEquals(BigDecimal.valueOf(1248.0), lastWeekOfTheDayCosts);
    }

    @Test
    void shouldReturnDailyCostForWeek() {
        when(accountService.getPricePlanIdForSmartMeterId(DailyInfoBuilder.SMART_METER_ID)).thenReturn(DailyInfoBuilder.PRICE_PLAN_ID);
        when(pricePlanService.calculateCostByDateAndAddUp(anyList(), eq(DailyInfoBuilder.PRICE_PLAN_ID))).thenReturn(
                BigDecimal.valueOf(120.0), BigDecimal.valueOf(140.0),
                BigDecimal.valueOf(160.0), BigDecimal.valueOf(180.0),
                BigDecimal.valueOf(110.0), BigDecimal.valueOf(130.0), BigDecimal.valueOf(100.0));

        List<DayOfWeekCost> dailyCostOfWeek = meterReadingCostService.getDayOfWeekCost(DailyInfoBuilder.SMART_METER_ID);


        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(1).getDayOfWeek(), dailyCostOfWeek.get(0).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(1).getCost(), dailyCostOfWeek.get(0).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(2).getDayOfWeek(), dailyCostOfWeek.get(1).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(2).getCost(), dailyCostOfWeek.get(1).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(3).getDayOfWeek(), dailyCostOfWeek.get(2).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(3).getCost(), dailyCostOfWeek.get(2).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(4).getDayOfWeek(), dailyCostOfWeek.get(3).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(4).getCost(), dailyCostOfWeek.get(3).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(5).getDayOfWeek(), dailyCostOfWeek.get(4).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(5).getCost(), dailyCostOfWeek.get(4).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(6).getDayOfWeek(), dailyCostOfWeek.get(5).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(6).getCost(), dailyCostOfWeek.get(5).getCost());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(0).getDayOfWeek(), dailyCostOfWeek.get(6).getDayOfWeek());
        assertEquals(DailyInfoBuilder.buildDaysOfWeekCostsList().get(0).getCost(), dailyCostOfWeek.get(6).getCost());
    }

    @Test
    void shouldReturnRankForAllPricePlansWhenGivenCostsOfBondedPricePlan() {
        when(accountService.getPricePlanIdForSmartMeterId(DailyInfoBuilder.SMART_METER_ID)).thenReturn(DailyInfoBuilder.PRICE_PLAN_ID);
        when(pricePlanService.getRankForCurrentPricePlan(anyList(), eq(DailyInfoBuilder.PRICE_PLAN_ID))).thenReturn(2);

        DayOfWeekCost sundayCost = DayOfWeekCost.builder()
                .dayOfWeek(DayOfWeek.SUNDAY)
                .cost(BigDecimal.valueOf(130.0))
                .currentPricePlanRank(2)
                .dailyElectricityReadings(DailyInfoBuilder.sundayReadings)
                .build();
        List<DayOfWeekCost> buildDaysOfWeekCostsList = List.of(sundayCost);
        List<DayOfWeekCost> dailyCostOfWeek = meterReadingCostService.getDayOfWeekCost(DailyInfoBuilder.SMART_METER_ID);

        assertEquals(buildDaysOfWeekCostsList.get(0).getCurrentPricePlanRank(), dailyCostOfWeek.get(0).getCurrentPricePlanRank());
    }
}
