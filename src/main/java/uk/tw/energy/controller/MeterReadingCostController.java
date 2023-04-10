package uk.tw.energy.controller;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.tw.energy.service.MeterReadingCostService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/readings")
public class MeterReadingCostController {

    private final MeterReadingCostService meterReadingCostService;

    public MeterReadingCostController(MeterReadingCostService meterReadingCostService) {
        this.meterReadingCostService = meterReadingCostService;
    }

    @GetMapping("/last-week/costs/{smartMeterId}")
    @ResponseStatus(HttpStatus.OK)
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "404", content = @Content)
            })
    public BigDecimal getLastWeekCosts(@PathVariable String smartMeterId) throws NotFoundException {
        return meterReadingCostService.getLastWeekCost(smartMeterId);
    }

    @ExceptionHandler(PricePlanNotMatchedException.class)
    public ResponseEntity<String> ExceptionHandler(PricePlanNotMatchedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> ExceptionHandler() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("0.0");
    }
}
