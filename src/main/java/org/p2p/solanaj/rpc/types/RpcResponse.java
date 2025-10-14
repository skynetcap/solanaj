package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcResponse<T> {

    @Getter
    @ToString
    public static class Error {
        @JsonProperty("code")
        private long code;

        @JsonProperty("message")
        private String message;
    }

    @JsonProperty("jsonrpc")
    private String jsonrpc;

    @JsonProperty("result")
    private T result;

    @JsonProperty("error")
    private Error error;

    @JsonProperty("id")
    private String id;
}
