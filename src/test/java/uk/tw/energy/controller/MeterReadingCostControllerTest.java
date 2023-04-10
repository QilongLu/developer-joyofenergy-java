package uk.tw.energy.controller;

import javassist.NotFoundException;
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
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MeterReadingCostControllerTest {
    private static final String SMART_METER_ID = "smart-meter-0";
    private static final String UNKNOWN_ID = "unknown-meter";
    @MockBean
    private MeterReadingCostService meterReadingCostService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenMeterIdShouldReturnLastWeekUsageCost() throws Exception {
        when(meterReadingCostService.getLastWeekCost(SMART_METER_ID)).thenReturn(BigDecimal.valueOf(100.0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/readings/last-week/costs/" + SMART_METER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }

    @Test
    void shouldThrowNotFoundStatus() throws Exception {
        when(meterReadingCostService.getLastWeekCost(UNKNOWN_ID)).thenThrow(NotFoundException.class);

        mockMvc.perform(MockMvcRequestBuilders.get("/readings/last-week/costs/" + UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(content().string("0.0"));
    }

    @Test
    void shouldThrowPricePlanNotMatchedException() throws Exception {
        Mockito.doThrow(new PricePlanNotMatchedException(SMART_METER_ID))
                .when(meterReadingCostService).getLastWeekCost(SMART_METER_ID);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/readings/last-week/costs/" + SMART_METER_ID)
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No price plan matched with " + SMART_METER_ID)));
    }
}