package uk.tw.energy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import uk.tw.energy.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MeterReadingCostServiceTest {

    private static final String SMART_METER_ID = "smart-meter-0";
    private static final String UNKNOWN_METER_ID = "unknown-meter";
    private static final String PRICE_PLAN_ID = "price-plan-1";
    private static final Instant NOW = Instant.now();
    private static final Instant ONE_WEEK_AGO = NOW.minus(Duration.ofDays(7));
    private static final String TEST_DATE_STR = LocalDate.now().toString();
    private static final String TEST_LAST_WEEK_DATE_STR = LocalDate.now().minusWeeks(1).toString();

    @Mock
    private AccountService accountService;
    private MeterReadingCostService meterReadingCostService;
    @Mock
    private PricePlanService pricePlanService;

    @BeforeEach
    public void setUp() {
        Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();
        meterAssociatedReadings.put(SMART_METER_ID, Arrays.asList(
                new ElectricityReading(NOW, BigDecimal.valueOf(0.2)),
                new ElectricityReading(ONE_WEEK_AGO, BigDecimal.valueOf(0.3)),
                new ElectricityReading(ONE_WEEK_AGO.plus(1, ChronoUnit.DAYS), BigDecimal.valueOf(15.0))
        ));
        meterReadingCostService = new MeterReadingCostService(meterAssociatedReadings, accountService, pricePlanService);
    }


    @Test
    void shouldThrowPricePlanNotMatchedException() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID))
                .thenReturn(null);
        Assertions.assertThrows(PricePlanNotMatchedException.class, () -> meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE_STR));
    }

    @Test
    void shouldThrowReadingsNotFoundExceptionWhenGivenAnUnknownMeterId() {
        assertThrows(ReadingsNotFoundException.class, () -> meterReadingCostService.getLastWeekCostOfTheDate(UNKNOWN_METER_ID, TEST_DATE_STR));
    }

    @Test
    void shouldReturnCorrectCosts() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), any(String.class))).thenReturn(BigDecimal.valueOf(1848.0));
        BigDecimal lastWeekCosts = meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE_STR);
        assertEquals(BigDecimal.valueOf(1848.0), lastWeekCosts);
    }

    @Test
    void shouldReturnCorrectCostsFromLastWeekOfTheDay() {
        when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID)).thenReturn(PRICE_PLAN_ID);
        when(pricePlanService.calculateCost(anyList(), any(String.class))).thenReturn(BigDecimal.valueOf(1248.0));
        BigDecimal lastWeekOfTheDayCosts = meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_LAST_WEEK_DATE_STR);
        assertEquals(BigDecimal.valueOf(1248.0), lastWeekOfTheDayCosts);
    }
}
