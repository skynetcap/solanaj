package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class SimulatedTransaction extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {
        @JsonProperty("accounts")
        private List<AccountInfo.Value> accounts;

        @JsonProperty("logs")
        private List<String> logs;
    }

    @JsonProperty("value")
    private Value value;
}
