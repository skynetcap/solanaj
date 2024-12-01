package org.p2p.solanaj.rpc.types.config;

import lombok.Setter;

@Setter
public class TokenAccountsConfig {

    private String mint;

    private String owner = null;

    private Integer page = null;

    private Integer limit = null;

    private String cursor = null;

    private String before = null;

    private String after = null;

    public TokenAccountsConfig() {}

    public TokenAccountsConfig(String mint, Integer limit, String cursor) {
        this.mint = mint;
        this.limit = limit;
        this.cursor = cursor;
    }

    public TokenAccountsConfig(String mint, Integer limit) {
        this.mint = mint;
        this.limit = limit;
    }

    public TokenAccountsConfig(String mint) {
        this.mint = mint;
    }
}
