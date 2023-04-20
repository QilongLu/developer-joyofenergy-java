package uk.tw.energy.adapter.SmartMeter.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class SmartMeterResponse<T> {
    private String smartMeterId;
    private T bill;
}
