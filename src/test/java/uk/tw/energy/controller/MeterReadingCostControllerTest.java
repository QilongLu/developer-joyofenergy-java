package uk.tw.energy.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.tw.energy.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        when(meterReadingCostService.getLastWeekCostOfTheDate(any(String.class), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/costs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(100.0));
    }

    @Test
    void shouldThrowReadingsNotFoundStatus() throws Exception {

        when(meterReadingCostService.getLastWeekCostOfTheDate(any(String.class), any(Instant.class)))
                .thenThrow(new ReadingsNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.get("/smart-meters/"+ UNKNOWN_ID + "/costs"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No Readings Found."));
    }

    @Test
    void shouldThrowPricePlanNotMatchedException() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(any(String.class), any(Instant.class)))
                .thenThrow(new PricePlanNotMatchedException(SMART_METER_ID));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/costs")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No price plan matched with " + SMART_METER_ID));
    }

    @Test
    void shouldReturnLastWeekCostOfTheGivenDate() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(any(String.class), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/smart-meters/" + SMART_METER_ID + "/costs"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }
}