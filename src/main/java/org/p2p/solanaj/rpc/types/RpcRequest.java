package org.p2p.solanaj.rpc.types;

import java.util.List;
import java.util.UUID;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class RpcRequest {

    @Json(name = "jsonrpc")
    private final String jsonrpc = "2.0";

    @Json(name = "method")
    private final String method;

    @Json(name = "params")
    private final Object params;

    @Json(name = "id")
    private final String id = UUID.randomUUID().toString();

    public RpcRequest(String method) {
        this(method, null);
    }

    public RpcRequest(String method, Object params) {
        this.method = method;
        this.params = params;
    }
}
