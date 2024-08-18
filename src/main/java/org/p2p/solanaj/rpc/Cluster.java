package org.p2p.solanaj.rpc;

import lombok.Getter;

@Getter
public enum Cluster {
    DEVNET("https://api.devnet.solana.com"),
    TESTNET("https://api.testnet.solana.com"),
    MAINNET("https://api.mainnet-beta.solana.com"),
    ANKR("https://rpc.ankr.com/solana");

    private final String endpoint;

    Cluster(String endpoint) {
        this.endpoint = endpoint;
    }

}
