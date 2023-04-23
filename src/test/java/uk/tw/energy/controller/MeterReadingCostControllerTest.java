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

    @Test
    void shouldReturnDayOfWeekCostsWhenGivenSmartMeterId() throws Exception {
        List<ElectricityReading> sundayReadings = getDailyElectricityReadings(SUNDAY);
        List<ElectricityReading> mondayReadings = getDailyElectricityReadings(MONDAY);
        List<ElectricityReading> tuesdayReadings = getDailyElectricityReadings(TUESDAY);
        List<ElectricityReading> wednesdayReadings = getDailyElectricityReadings(WEDNESDAY);
        List<ElectricityReading> thursdayReadings = getDailyElectricityReadings(THURSDAY);
        List<ElectricityReading> fridayReadings = getDailyElectricityReadings(FRIDAY);
        List<ElectricityReading> saturdayReadings = getDailyElectricityReadings(SATURDAY);

        DayOfWeekCost sundayCost = buildDayOfWeekCost(sundayReadings, 100.0, DayOfWeek.SUNDAY);
        DayOfWeekCost mondayCost = buildDayOfWeekCost(mondayReadings, 120.0, DayOfWeek.MONDAY);
        DayOfWeekCost tuesdayCost = buildDayOfWeekCost(tuesdayReadings, 140.0, DayOfWeek.TUESDAY);
        DayOfWeekCost wednesdayCost = buildDayOfWeekCost(wednesdayReadings, 160.0, DayOfWeek.WEDNESDAY);
        DayOfWeekCost thursdayCost = buildDayOfWeekCost(thursdayReadings, 180.0, DayOfWeek.THURSDAY);
        DayOfWeekCost fridayCost = buildDayOfWeekCost(fridayReadings, 110.0, DayOfWeek.FRIDAY);
        DayOfWeekCost saturdayCost = buildDayOfWeekCost(saturdayReadings, 130.0, DayOfWeek.SATURDAY);
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
                .andExpect(jsonPath("$.dailyCosts[0].dayOfWeek").value(DayOfWeek.SUNDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[0].cost").value(100.0))
                .andExpect(jsonPath("$.dailyCosts[1].dayOfWeek").value(DayOfWeek.MONDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[1].cost").value(120.0))
                .andExpect(jsonPath("$.dailyCosts[2].dayOfWeek").value(DayOfWeek.TUESDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[2].cost").value(140.0))
                .andExpect(jsonPath("$.dailyCosts[3].dayOfWeek").value(DayOfWeek.WEDNESDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[3].cost").value(160.0))
                .andExpect(jsonPath("$.dailyCosts[4].dayOfWeek").value(DayOfWeek.THURSDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[4].cost").value(180.0))
                .andExpect(jsonPath("$.dailyCosts[5].dayOfWeek").value(DayOfWeek.FRIDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[5].cost").value(110.0))
                .andExpect(jsonPath("$.dailyCosts[6].dayOfWeek").value(DayOfWeek.SATURDAY.name()))
                .andExpect(jsonPath("$.dailyCosts[6].cost").value(130.0));
    }

    @Test
    void shouldReturnRankOfWhatPricePlanUsedWithDayOfWeekCostsWhenGivenSmartMeterId() throws Exception {
        List<ElectricityReading> sundayReadings = getDailyElectricityReadings(SUNDAY);

        DayOfWeekCost sundayCost = DayOfWeekCost.builder()
                .dayOfWeek(DayOfWeek.SUNDAY)
                .cost(BigDecimal.valueOf(100.0))
                .currentPricePlanRank(3)
                .dailyElectricityReadings(sundayReadings)
                .build();

        List<DayOfWeekCost> daysOfWeekCosts = List.of(sundayCost);
        when(meterReadingCostService.getDayOfWeekCost(SMART_METER_ID)).thenReturn(daysOfWeekCosts);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/smart-meters/" + SMART_METER_ID + "/daily-cost"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.smartMeterId").value(SMART_METER_ID))
                .andExpect(jsonPath("$.dailyCosts[0].currentPricePlanRank").value(3));
    }
}