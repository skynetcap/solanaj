package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcNotificationResult {

    @Getter
    @ToString
    public static class Result extends RpcResultObject {

        @JsonProperty("value")
        private Object value;
    }

    @Getter
    @ToString
    public static class Params {

        @JsonProperty("result")
        private Result result;

        @JsonProperty("subscription")
        private long subscription;
    }

    @JsonProperty("jsonrpc")
    private String jsonrpc;

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private Params params;
}
