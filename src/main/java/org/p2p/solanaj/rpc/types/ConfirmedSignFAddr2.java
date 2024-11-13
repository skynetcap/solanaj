package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import org.p2p.solanaj.rpc.types.config.Commitment;

public class ConfirmedSignFAddr2 {

    @Json(name = "limit")
    private final long limit;

    @Json(name = "before")
    private String before;

    @Json(name = "until")
    private String until;

    @Json(name = "commitment")
    private final String commitment;

    public ConfirmedSignFAddr2(int limit, Commitment commitment) {
        this.limit = limit;
        this.commitment = commitment.getValue();
    }

    public ConfirmedSignFAddr2(String before, int limit, Commitment commitment) {
        this.before = before;
        this.limit = limit;
        this.commitment = commitment.getValue();
    }

    @Override
    public String toString() {
        return "ConfirmedSignFAddr2{" +
                "limit=" + limit +
                ", before='" + before + '\'' +
                ", until='" + until + '\'' +
                ", commitment='" + commitment + '\'' +
                '}';
    }
}