package uk.tw.energy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.domain.ElectricityReading;
import uk.tw.energy.service.AccountService;
import uk.tw.energy.service.MeterReadingCostService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/readings")
public class MeterReadingCostController {

    private final AccountService accountService;
    private final MeterReadingCostService meterReadingCostService;

    public MeterReadingCostController(AccountService accountService, MeterReadingCostService meterReadingCostService) {
        this.accountService = accountService;
        this.meterReadingCostService = meterReadingCostService;
    }

    @GetMapping("/last-week/{smartMeterId}")
    public ResponseEntity<List<ElectricityReading>> getLastWeekReadings(@PathVariable String smartMeterId) {
        String pricePlanId = accountService.getPricePlanIdForSmartMeterId(smartMeterId);
        if (pricePlanId==null) {
            throw new PricePlanNotMatchedException(HttpStatus.NOT_FOUND, "Price plan not matched.");
        }
        Optional<List<ElectricityReading>> readings = meterReadingCostService.getLastWeekReadings(smartMeterId);
        return readings
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @ExceptionHandler(PricePlanNotMatchedException.class)
    public ResponseEntity<String> handleMyException(PricePlanNotMatchedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
