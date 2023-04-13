package uk.tw.energy.controller;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/smart-meters")
public class MeterReadingCostController {

    private final MeterReadingCostService meterReadingCostService;

    public MeterReadingCostController(MeterReadingCostService meterReadingCostService) {
        this.meterReadingCostService = meterReadingCostService;
    }

    @GetMapping("/{smartMeterId}/costs")
    public ResponseEntity<BigDecimal> getLastWeekOfDateCosts(@PathVariable("smartMeterId") String smartMeterId,
                                                            @RequestParam(value = "enteredDate", required = false)
                                             @DateTimeFormat(pattern = "yyyy-MM-dd")
                                             LocalDate enteredDate) {
        if (enteredDate == null) {
            enteredDate = LocalDate.now();
        }
        Instant date = enteredDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        BigDecimal lastWeekCosts = meterReadingCostService.getLastWeekCostOfTheDate(smartMeterId, date);
        return ResponseEntity.ok().body(lastWeekCosts);
    }
}
