package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class Supply extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @JsonProperty("total")
        private long total;

        @JsonProperty("circulating")
        private long circulating;

        @JsonProperty("nonCirculating")
        private long nonCirculating;

        @JsonProperty("nonCirculatingAccounts")
        private List<String> nonCirculatingAccounts;
    }

    @JsonProperty("value")
    private Value value;
}
