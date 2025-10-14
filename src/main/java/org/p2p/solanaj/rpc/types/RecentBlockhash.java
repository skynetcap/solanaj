package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RecentBlockhash extends RpcResultObject {

    @Getter
    @ToString
    public static class FeeCalculator {

        @JsonProperty("lamportsPerSignature")
        private double lamportsPerSignature;
    }

    @Getter
    @ToString
    public static class Value {
        @JsonProperty("blockhash")
        private String blockhash;

        @JsonProperty("feeCalculator")
        private FeeCalculator feeCalculator;
    }

    @JsonProperty("value")
    private Value value;
}
