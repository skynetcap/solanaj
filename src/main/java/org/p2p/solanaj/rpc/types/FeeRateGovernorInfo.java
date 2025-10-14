package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FeeRateGovernorInfo extends RpcResultObject
{
    @Getter
    @ToString
    public static class FeeRateGovernor {
        @JsonProperty("burnPercent")
        private int burnPercent;

        @JsonProperty("maxLamportsPerSignature")
        private double maxLamportsPerSignature;

        @JsonProperty("minLamportsPerSignature")
        private double minLamportsPerSignature;

        @JsonProperty("targetLamportsPerSignature")
        private double targetLamportsPerSignature;

        @JsonProperty("targetSignaturesPerSlot")
        private double targetSignaturesPerSlot;
    }

    @Getter
    @ToString
    public static class Value {

        @JsonProperty("feeRateGovernor")
        private FeeRateGovernor feeRateGovernor;
    }

    @JsonProperty("value")
    private Value value;
}
