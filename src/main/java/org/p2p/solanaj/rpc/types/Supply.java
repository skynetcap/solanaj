package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class Supply extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @Json(name = "total")
        private long total;

        @Json(name = "circulating")
        private long circulating;

        @Json(name = "nonCirculating")
        private long nonCirculating;

        @Json(name = "nonCirculatingAccounts")
        private List<String> nonCirculatingAccounts;
    }

    @Json(name = "value")
    private Value value;
}
