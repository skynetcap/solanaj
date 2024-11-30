package org.p2p.solanaj.rpc.types.config;

import lombok.Setter;

public class TokenAccountConfig {

    @Setter
    private String mint;

    @Setter
    private String owner = null;

    @Setter
    private Integer page = null;

    @Setter
    private Integer limit = null;

    @Setter
    private String cursor = null;

    @Setter
    private String before = null;

    @Setter
    private String after = null;

    public TokenAccountConfig() {}

    public TokenAccountConfig(String mint, Integer limit, String cursor) {
        this.mint = mint;
        this.limit = limit;
        this.cursor = cursor;
    }

    public TokenAccountConfig(String mint, Integer limit) {
        this.mint = mint;
        this.cursor = cursor;
    }

    public TokenAccountConfig(String mint) {
        this.mint = mint;
    }
}