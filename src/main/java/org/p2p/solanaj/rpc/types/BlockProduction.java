package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString
public class BlockProduction {

    @Getter
    @ToString
    public static class BlockProductionRange {
        @JsonProperty("firstSlot")
        private double firstSlot;

        @JsonProperty("lastSlot")
        private double lastSlot;

    }

    @Getter
    @ToString
    public static class BlockProductionValue {
        @JsonProperty("byIdentity")
        private Map<String, List<Double>> byIdentity;

        public Map<String, List<Double>> getByIdentity() {
            return byIdentity;
        }

        @JsonProperty("range")
        private BlockProductionRange blockProductionRange;

    }

    @JsonProperty("value")
    private BlockProductionValue value;
}
