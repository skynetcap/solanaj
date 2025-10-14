package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

public class RpcResultTypes {

    @Getter
    @ToString
    public static class ValueLong extends RpcResultObject {
        @JsonProperty("value")
        private Long value;
    }

}
