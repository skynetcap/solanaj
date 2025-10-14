package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LatestBlockhash extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @JsonProperty("blockhash")
        private String blockhash;

        @JsonProperty("lastValidBlockHeight")
        private long lastValidBlockHeight;
    }

    @JsonProperty("value")
    private Value value;
}
