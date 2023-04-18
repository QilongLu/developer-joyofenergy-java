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
import uk.tw.energy.adapter.SmartMeter.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.adapter.SmartMeter.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class MeterReadingCostControllerTest {
    private static final String SMART_METER_ID = "smart-meter-0";
    private static final String UNKNOWN_ID = "unknown-meter";
    private static final String DURATION = "last-week";
    @MockBean
    private MeterReadingCostService meterReadingCostService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenMeterIdWithoutDateEnteredShouldReturnDefaultLastWeekUsageCost() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(SMART_METER_ID), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/costs")
                        .param("duration", DURATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.costs").value(100.0));
    }

    @Test
    void shouldThrowReadingsNotFoundStatusWhenGivenUnknownId() throws Exception {

        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(UNKNOWN_ID), any(Instant.class)))
                .thenThrow(new ReadingsNotFoundException());

        mockMvc.perform(MockMvcRequestBuilders.get("/smart-meters/"+ UNKNOWN_ID + "/costs")
                        .param("duration", DURATION))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No Readings Found."));
    }

    @Test
    void shouldThrowPricePlanNotMatchedException() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(SMART_METER_ID), any(Instant.class)))
                .thenThrow(new PricePlanNotMatchedException(SMART_METER_ID));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/costs")
                        .param("duration", DURATION)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No price plan matched with " + SMART_METER_ID));
    }

    @Test
    void shouldReturnLastWeekCostOfTheGivenDate() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(SMART_METER_ID), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", DURATION)
                        .param("entered", "2023-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.costs").value(100.0));
    }

    @Test
    void shouldGiveErrorMessageWhenGivenWrongDate() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", DURATION)
                        .param("enteredDate", "2088-04-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Date not present"));
    }

    @Test
    void shouldGiveErrorMessageWhenGivenWrongDuration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", "lastMonth")
                        .param("enteredDate", "2023-04-10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("No Readings Found."));
    }

    @Test
    void shouldGiveErrorMessageWhenNotGivenDuration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", (String) null)
                        .param("enteredDate", "2023-04-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Unknown date range."));
    }

    @Test
    void shouldGiveErrorMessageWhenGivenWrongDateFormatAndBadDuration() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", (String) null)
                        .param("enteredDate", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Invalid date format entered."));
    }

    @Test
    void shouldReturnLastWeekCostOfTheGivenDateWhetherMatchedAnyTypeOfLastWeekDuration() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(SMART_METER_ID), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/costs")
                        .param("duration", "lAst@#$%^&*()weeK")
                        .param("entered", "2023-04-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.costs").value(100.0));
    }
}