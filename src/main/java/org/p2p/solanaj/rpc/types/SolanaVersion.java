package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SolanaVersion {

    @JsonProperty("solana-core")
    private String solanaCore;

    @JsonProperty("feature-set")
    private String featureSet;
}
