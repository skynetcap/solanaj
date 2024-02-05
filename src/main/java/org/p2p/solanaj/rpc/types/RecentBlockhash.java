package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RecentBlockhash extends RpcResultObject {

    @Getter
    @ToString
    public static class FeeCalculator {

        @Json(name = "lamportsPerSignature")
        private double lamportsPerSignature;
    }

    @Getter
    @ToString
    public static class Value {
        @Json(name = "blockhash")
        private String blockhash;

        @Json(name = "feeCalculator")
        private FeeCalculator feeCalculator;
    }

    @Json(name = "value")
    private Value value;
}
