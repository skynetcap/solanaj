package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcResultObject {

    @Getter
    @ToString
    public static class Context {
        @JsonProperty("slot")
        private long slot;
    }

    @JsonProperty("context")
    protected Context context;
}
