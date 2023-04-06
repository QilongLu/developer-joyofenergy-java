package uk.tw.energy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.tw.energy.controller.PricePlanNotMatchedException;
import uk.tw.energy.domain.ElectricityReading;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MeterReadingCostServiceTest {

    private static final String SMART_METER_ID = "smart-meter-0";
    private static final String UNKNOWN_METER_ID = "unknown-meter";
    private static final String PRICE_PLAN_ID = "price-plan-1";
    private static final Instant NOW = Instant.now();
    private static final Instant ONE_WEEK_AGO = NOW.minus(Duration.ofDays(7));

    @Mock
    private AccountService accountService;

    private MeterReadingCostService meterReadingCostService;

    @BeforeEach
    public void setUp() {
        Map<String, List<ElectricityReading>> meterAssociatedReadings = new HashMap<>();
        meterAssociatedReadings.put(SMART_METER_ID, Arrays.asList(
                new ElectricityReading(NOW, BigDecimal.valueOf(0.2)),
                new ElectricityReading(ONE_WEEK_AGO, BigDecimal.valueOf(0.3))
        ));
        meterReadingCostService = new MeterReadingCostService(meterAssociatedReadings, accountService);
    }


    @Test
    void shouldThrowPricePlanNotMatchedException() {
        Mockito.when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID))
                .thenReturn(null);
        Assertions.assertThrows(PricePlanNotMatchedException.class, () -> meterReadingCostService.getLastWeekReadings(SMART_METER_ID));
    }

    @Test
    void shouldReturnEmptyOptional() {
        Optional<List<ElectricityReading>> result = meterReadingCostService.getLastWeekReadings(UNKNOWN_METER_ID);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnLastWeekReadings() {
        Mockito.when(accountService.getPricePlanIdForSmartMeterId(SMART_METER_ID))
                .thenReturn(PRICE_PLAN_ID);
        List<ElectricityReading> expected = Collections.singletonList(
                new ElectricityReading(ONE_WEEK_AGO, BigDecimal.valueOf(0.3))
        );
        Optional<List<ElectricityReading>> result = meterReadingCostService.getLastWeekReadings(SMART_METER_ID);
        Assertions.assertEquals(expected.get(0).getReading(), result.get().get(0).getReading());
        Assertions.assertEquals(expected.get(0).getTime(), result.get().get(0).getTime());
    }
}
