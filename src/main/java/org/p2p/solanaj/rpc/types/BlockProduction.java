package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
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
        @Json(name = "firstSlot")
        private double firstSlot;

        @Json(name = "lastSlot")
        private double lastSlot;

    }

    @Getter
    @ToString
    public static class BlockProductionValue {
        @Getter
        @Json(name = "byIdentity")
        private Map<String, List<Double>> byIdentity;

        @Json(name = "range")
        private BlockProductionRange blockProductionRange;

    }

    @Json(name = "value")
    private BlockProductionValue value;
}
