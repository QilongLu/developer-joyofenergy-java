package uk.tw.energy.adapter.SmartMeter.dto.response;

import lombok.Builder;
import lombok.Getter;
import uk.tw.energy.domain.DayOfWeekCost;

import java.util.List;

@Builder
@Getter
public class SmartMeterDailyCostsResponse {
    private String smartMeterId;
    private List<DayOfWeekCost> dailyCosts;

    private Integer currentPricePlanRank;
}
