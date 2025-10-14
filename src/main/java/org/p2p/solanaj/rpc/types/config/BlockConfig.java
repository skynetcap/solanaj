package org.p2p.solanaj.rpc.types.config;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BlockConfig {

    private String encoding = "json";

    private String transactionDetails = "full";

    private Boolean rewards = true;

    private String commitment;

    private Integer maxSupportedTransactionVersion = null;
}