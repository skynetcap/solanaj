package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.p2p.solanaj.rpc.types.config.Commitment;

public class ConfirmedSignFAddr2 {

    @JsonProperty("limit")
    private long limit;

    @JsonProperty("before")
    private String before;

    @JsonProperty("until")
    private String until;

    @JsonProperty("commitment")
    private String commitment;

    public ConfirmedSignFAddr2(int limit, Commitment commitment) {
        this.limit = limit;
        this.commitment = commitment.getValue();
    }

    public ConfirmedSignFAddr2(int limit, Commitment commitment, String before, String until) {
        this.limit = limit;
        this.commitment = commitment.getValue();
        this.before = before;
        this.until = until;
    }
}