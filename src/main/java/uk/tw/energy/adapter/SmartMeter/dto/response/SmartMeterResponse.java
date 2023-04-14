package uk.tw.energy.adapter.SmartMeter.dto.response;

import java.math.BigDecimal;

public class SmartMeterResponse {
    private final String smartMeterId;
    private final BigDecimal costs;

    public SmartMeterResponse(Builder builder) {
        this.smartMeterId = builder.smartMeterId;
        this.costs = builder.costs;
    }
    public String getSmartMeterId() {
        return smartMeterId;
    }

    public BigDecimal getCosts() {
        return costs;
    }

    public static class Builder{
        private final String smartMeterId;
        private final BigDecimal costs;

        public Builder(String smartMeterId, BigDecimal costs) {
            this.smartMeterId = smartMeterId;
            this.costs = costs;
        }

        public SmartMeterResponse build() {
            return new SmartMeterResponse(this);
        }
    }
}
