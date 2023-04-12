package uk.tw.energy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.controller.exception.ReadingsNotFoundException;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/smart-meters")
public class MeterReadingCostController {

    private final MeterReadingCostService meterReadingCostService;

    public MeterReadingCostController(MeterReadingCostService meterReadingCostService) {
        this.meterReadingCostService = meterReadingCostService;
    }

    @GetMapping("/{smartMeterId}/last-week/costs")
    @ResponseStatus(HttpStatus.OK)
    public BigDecimal getLastWeekCosts(@PathVariable String smartMeterId) throws ReadingsNotFoundException {
        return meterReadingCostService.getLastWeekCost(smartMeterId);
    }
}
