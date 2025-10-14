package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class Block {

    @JsonProperty("blockTime")
    private int blockTime;

    @JsonProperty("blockHeight")
    private int blockHeight;

    @JsonProperty("blockhash")
    private String blockHash;

    @JsonProperty("parentSlot")
    private int parentSlot;

    @JsonProperty("previousBlockhash")
    private String previousBlockhash;

    @JsonProperty("transactions")
    private List<ConfirmedTransaction> transactions;

    @JsonProperty("rewards")
    private List<Reward> rewards;
}
