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
import uk.tw.energy.adapter.SmartMeter.dto.response.SmartMeterResponse;
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
    public ResponseEntity<SmartMeterResponse<BigDecimal>> getLastWeekOfDateCosts(
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
            SmartMeterResponse<BigDecimal> smartMeterResponse = SmartMeterResponse.<BigDecimal>builder()
                    .smartMeterId(smartMeterId)
                    .bill(lastWeekCostOfTheDate)
                    .build();
            return ResponseEntity.ok(smartMeterResponse);
        }
        else {
            throw new ReadingsNotFoundException();
        }
    }

    @GetMapping("{smartMeterId}/daily-cost")
    public ResponseEntity<SmartMeterResponse<List<DayOfWeekCost>>> getDayOfWeekCost(@PathVariable("smartMeterId") String smartMeterId) {
        List<DayOfWeekCost> daysOfWeekCosts = meterReadingCostService.getDayOfWeekCost(smartMeterId);
        SmartMeterResponse<List<DayOfWeekCost>> smartMeterResponse = SmartMeterResponse.<List<DayOfWeekCost>>builder()
                .smartMeterId(smartMeterId)
                .bill(daysOfWeekCosts)
                .build();
        return ResponseEntity.ok(smartMeterResponse);
    }
}
