package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class ConfirmedBlock {

    @Json(name = "blockTime")
    private int blockTime;

    @Json(name = "blockhash")
    private String blockhash;

    @Json(name = "parentSlot")
    private int parentSlot;

    @Json(name = "previousBlockhash")
    private String previousBlockhash;

    @Json(name = "transactions")
    private List<ConfirmedTransaction> transactions;
}
