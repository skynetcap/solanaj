package org.p2p.solanaj.rpc.types.config;

import java.util.Map;

import lombok.Setter;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

import com.fasterxml.jackson.annotation.JsonProperty;

@Setter
public class SimulateTransactionConfig {

    @JsonProperty("encoding")
    private Encoding encoding = Encoding.base64;

    @JsonProperty("accounts")
    private Map accounts = null;

    @JsonProperty("commitment")
    private String commitment = "finalized";

    @JsonProperty("sigVerify")
    private Boolean sigVerify = false;

    @JsonProperty("replaceRecentBlockhash")
    private Boolean replaceRecentBlockhash = false;

    public SimulateTransactionConfig(Map accounts) {
        this.accounts = accounts;
    }

    public SimulateTransactionConfig(Encoding encoding) {
        this.encoding = encoding;
    }
}