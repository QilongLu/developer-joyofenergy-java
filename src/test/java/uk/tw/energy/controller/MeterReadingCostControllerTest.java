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
import uk.tw.energy.builders.MeterReadingsBuilder;
import uk.tw.energy.domain.DayOfWeekCost;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

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

    private static Instant getDayOfWeek(DayOfWeek dayOfWeek) {
        return LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
                .with(TemporalAdjusters.previousOrSame(dayOfWeek)).toInstant(ZoneOffset.UTC);
    }

    private static final Instant SUNDAY = getDayOfWeek(DayOfWeek.SUNDAY);
    private static final Instant MONDAY = getDayOfWeek(DayOfWeek.MONDAY);
    private static final Instant TUESDAY = getDayOfWeek(DayOfWeek.TUESDAY);
    private static final Instant WEDNESDAY = getDayOfWeek(DayOfWeek.WEDNESDAY);
    private static final Instant THURSDAY = getDayOfWeek(DayOfWeek.THURSDAY);
    private static final Instant FRIDAY = getDayOfWeek(DayOfWeek.FRIDAY);
    private static final Instant SATURDAY = getDayOfWeek(DayOfWeek.SATURDAY);
    @MockBean
    private MeterReadingCostService meterReadingCostService;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void ShouldReturnDefaultLastWeekUsageCostWhenGivenMeterIdWithoutDateEntered() throws Exception {
        when(meterReadingCostService.getLastWeekCostOfTheDate(eq(SMART_METER_ID), any(Instant.class))).thenReturn(BigDecimal.valueOf(100.0));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/"+ SMART_METER_ID + "/costs")
                        .param("duration", DURATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.bill").value(100.0));
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
                .andExpect(jsonPath("$.bill").value(100.0));
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
                .andExpect(jsonPath("$.bill").value(100.0));
    }

    @Test
    void shouldReturnDayOfWeekCostsWhenGivenSmartMeterId() throws Exception {
        List<ElectricityReading> sundayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, SUNDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> mondayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, MONDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> tuesdayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, TUESDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> wednesdayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, WEDNESDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> thursdayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, THURSDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> fridayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, FRIDAY)
                .build()
                .getElectricityReadings();
        List<ElectricityReading> saturdayReadings = new MeterReadingsBuilder()
                .setSmartMeterId(SMART_METER_ID)
                .generateElectricityReadings(5, SATURDAY)
                .build()
                .getElectricityReadings();

        DayOfWeekCost sundayCost = DayOfWeekCost.builder()
                .date(sundayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(100.0))
                .dailyElectricityReadings(sundayReadings)
                .build();
        DayOfWeekCost mondayCost = DayOfWeekCost.builder()
                .date(mondayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(120.0))
                .dailyElectricityReadings(mondayReadings)
                .build();
        DayOfWeekCost tuesdayCost = DayOfWeekCost.builder()
                .date(tuesdayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(140.0))
                .dailyElectricityReadings(tuesdayReadings)
                .build();
        DayOfWeekCost wednesdayCost = DayOfWeekCost.builder()
                .date(wednesdayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(160.0))
                .dailyElectricityReadings(wednesdayReadings)
                .build();
        DayOfWeekCost thursdayCost = DayOfWeekCost.builder()
                .date(thursdayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(180.0))
                .dailyElectricityReadings(thursdayReadings)
                .build();
        DayOfWeekCost fridayCost = DayOfWeekCost.builder()
                .date(fridayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(110.0))
                .dailyElectricityReadings(fridayReadings)
                .build();
        DayOfWeekCost saturdayCost = DayOfWeekCost.builder()
                .date(saturdayReadings.get(0).getTime().atZone(ZoneId.systemDefault()).toLocalDate())
                .dailyCost(BigDecimal.valueOf(130.0))
                .dailyElectricityReadings(saturdayReadings)
                .build();
        List<DayOfWeekCost> daysOfWeekCosts = List.of(
                sundayCost,
                mondayCost,
                tuesdayCost,
                wednesdayCost,
                thursdayCost,
                fridayCost,
                saturdayCost
        );
        when(meterReadingCostService.getDayOfWeekCost(SMART_METER_ID)).thenReturn(daysOfWeekCosts);
        mockMvc.perform(MockMvcRequestBuilders
                .get("/smart-meters/" + SMART_METER_ID + "/daily-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.bill[0].date").value(SUNDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[0].dailyCost").value(100.0))
                .andExpect(jsonPath("$.bill[1].date").value(MONDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[1].dailyCost").value(120.0))
                .andExpect(jsonPath("$.bill[2].date").value(TUESDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[2].dailyCost").value(140.0))
                .andExpect(jsonPath("$.bill[3].date").value(WEDNESDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[3].dailyCost").value(160.0))
                .andExpect(jsonPath("$.bill[4].date").value(THURSDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[4].dailyCost").value(180.0))
                .andExpect(jsonPath("$.bill[5].date").value(FRIDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[5].dailyCost").value(110.0))
                .andExpect(jsonPath("$.bill[6].date").value(SATURDAY.atZone(ZoneId.systemDefault()).toLocalDate().toString()))
                .andExpect(jsonPath("$.bill[6].dailyCost").value(130.0));
    }
}