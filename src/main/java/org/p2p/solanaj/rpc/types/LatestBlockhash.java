package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LatestBlockhash extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @Json(name = "blockhash")
        private String blockhash;

        @Json(name = "lastValidBlockHeight")
        private long lastValidBlockHeight;
    }

    @Json(name = "value")
    private Value value;
}
