package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class SignatureStatuses extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @JsonProperty("slot")
        private long slot;

        @JsonProperty("confirmations")
        private Long confirmations;

        @JsonProperty("confirmationStatus")
        private String confirmationStatus;
    }

    @JsonProperty("value")
    private List<Value> value;
}
