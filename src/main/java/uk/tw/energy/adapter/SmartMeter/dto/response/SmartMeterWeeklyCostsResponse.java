package uk.tw.energy.adapter.SmartMeter.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Builder
@Getter
public class SmartMeterWeeklyCostsResponse {
    private String smartMeterId;
    private BigDecimal costs;
}
