package uk.tw.energy.adapter.SmartMeter.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.adapter.SmartMeter.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.adapter.SmartMeter.dto.response.SmartMeterDailyCostsResponse;
import uk.tw.energy.adapter.SmartMeter.dto.response.SmartMeterWeeklyCostsResponse;
import uk.tw.energy.domain.DayOfWeekCost;
import uk.tw.energy.service.MeterReadingCostService;

import javax.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/smart-meters")
@Validated
public class MeterReadingCostController {

    private final MeterReadingCostService meterReadingCostService;

    public MeterReadingCostController(MeterReadingCostService meterReadingCostService) {
        this.meterReadingCostService = meterReadingCostService;
    }

    @GetMapping("/{smartMeterId}/costs")
    public ResponseEntity<SmartMeterWeeklyCostsResponse> getLastWeekOfDateCosts(
            @PathVariable("smartMeterId") String smartMeterId,
            @RequestParam(value = "enteredDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            @PastOrPresent(message = "must be a date in the past or in the present")
            LocalDate enteredDate,
            @RequestParam(value = "duration")
            String duration
    ) {
        if (duration.matches("(?i)^last.*week$")) {
            if (enteredDate == null) {
                enteredDate = LocalDate.now();
            }
            Instant date = enteredDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            BigDecimal lastWeekCostOfTheDate = meterReadingCostService.getLastWeekCostOfTheDate(smartMeterId, date);
            SmartMeterWeeklyCostsResponse smartMeterWeeklyCostsResponse = SmartMeterWeeklyCostsResponse.builder()
                    .smartMeterId(smartMeterId)
                    .costs(lastWeekCostOfTheDate)
                    .build();
            return ResponseEntity.ok(smartMeterWeeklyCostsResponse);
        }
        else {
            throw new ReadingsNotFoundException();
        }
    }

    @GetMapping("{smartMeterId}/daily-cost")
    public ResponseEntity<SmartMeterDailyCostsResponse> getDayOfWeekCost(@PathVariable("smartMeterId") String smartMeterId) {
        List<DayOfWeekCost> daysOfWeekCosts = meterReadingCostService.getDayOfWeekCost(smartMeterId);
        SmartMeterDailyCostsResponse smartMeterDailyCostsResponse = SmartMeterDailyCostsResponse.builder()
                .smartMeterId(smartMeterId)
                .dailyCosts(daysOfWeekCosts)
                .build();
        return ResponseEntity.ok(smartMeterDailyCostsResponse);
    }
}
