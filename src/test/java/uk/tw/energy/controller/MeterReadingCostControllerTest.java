package uk.tw.energy.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MeterReadingCostControllerTest {
    private static final String SMART_METER_ID = "smart-meter-0";
    @MockBean
    private MeterReadingCostService meterReadingCostService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenMeterIdShouldReturnLastWeekCostUsage() throws Exception {
        Instant now = Instant.now();
        ElectricityReading lastWeekReading = new ElectricityReading(now.minus(7,
                ChronoUnit.DAYS),
                BigDecimal.valueOf(0.5));
        List<ElectricityReading> readings = List.of(lastWeekReading);

        when(meterReadingCostService.getLastWeekReadings(SMART_METER_ID)).thenReturn(Optional.of(readings));

        mockMvc.perform(MockMvcRequestBuilders.get("/readings/last-week/" + SMART_METER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].time", equalTo(now.minus(7, ChronoUnit.DAYS).toString())))
                .andExpect(jsonPath("$.[0].reading").value(0.5));
    }

    @Test
    void shouldThrowPricePlanNotMatchedException() throws Exception {
        Mockito.doThrow(new PricePlanNotMatchedException(SMART_METER_ID))
                .when(meterReadingCostService).getLastWeekReadings(SMART_METER_ID);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/readings/last-week/" + SMART_METER_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No price plan matched with " + SMART_METER_ID)));
    }
}