package org.p2p.solanaj.rpc.types;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcRequest {

    @JsonProperty("jsonrpc")
    private String jsonrpc = "2.0";

    @JsonProperty("method")
    private String method;

    @JsonProperty("params")
    private List<Object> params;

    @JsonProperty("id")
    private String id = UUID.randomUUID().toString();

    public RpcRequest(String method) {
        this(method, null);
    }

    public RpcRequest(String method, List<Object> params) {
        this.method = method;
        this.params = params;
    }
}
