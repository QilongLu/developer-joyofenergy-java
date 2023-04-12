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
import uk.tw.energy.controller.exception.PricePlanNotMatchedException;
import uk.tw.energy.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.LocalDate;

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
    private static final String TEST_DATE_STR = LocalDate.now().toString();
    @MockBean
    private MeterReadingCostService meterReadingCostService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void givenMeterIdShouldReturnLastWeekUsageCost() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE_STR)).thenReturn(BigDecimal.valueOf(100.0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/last-week/costs/"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }

    @Test
    void shouldThrowReadingsNotFoundStatus() throws Exception {
        Mockito.doThrow(new ReadingsNotFoundException()).when(meterReadingCostService).getLastWeekCostOfTheDate(UNKNOWN_ID, TEST_DATE_STR);

        mockMvc.perform(MockMvcRequestBuilders.get("/smart-meters/"+ UNKNOWN_ID + "/last-week/costs/"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No Readings Found.")));
    }

    @Test
    void shouldThrowPricePlanNotMatchedException() throws Exception {
        Mockito.doThrow(new PricePlanNotMatchedException(SMART_METER_ID))
                .when(meterReadingCostService).getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE_STR);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/last-week/costs/")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("No price plan matched with " + SMART_METER_ID)));
    }

    @Test
    void shouldReturnLastWeekCostOfTheGivenDate() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(SMART_METER_ID, TEST_DATE_STR)).thenReturn(BigDecimal.valueOf(100.0));
        mockMvc.perform(MockMvcRequestBuilders
                .get("/smart-meters/" + SMART_METER_ID +"/"+ TEST_DATE_STR+ "/last-week/costs"))
                .andExpect(status().isOk())
                .andExpect(content().string("100.0"));
    }
}