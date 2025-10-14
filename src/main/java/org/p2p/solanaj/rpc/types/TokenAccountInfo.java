package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class TokenAccountInfo extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {

        @JsonProperty("account")
        private TokenResultObjects.Value account;

        @JsonProperty("pubkey")
        private String pubkey;
    }

    @JsonProperty("value")
    private List<Value> value;
}
