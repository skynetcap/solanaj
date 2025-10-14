package org.p2p.solanaj.rpc.types.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignatureStatusConfig {
    @JsonProperty("searchTransactionHistory")
    private final boolean searchTransactionHistory;
}
