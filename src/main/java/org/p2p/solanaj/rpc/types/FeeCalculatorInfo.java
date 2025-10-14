package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class FeeCalculatorInfo extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {

        @JsonProperty("feeCalculator")
        private RecentBlockhash.FeeCalculator feeCalculator;
    }

    @JsonProperty("value")
    private Value value;
}

