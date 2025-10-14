package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Reward {

    @JsonProperty("pubkey")
    private String pubkey;

    @JsonProperty("lamports")
    private double lamports;

    @JsonProperty("postBalance")
    private String postBalance;

    @JsonProperty("rewardType")
    private RewardType rewardType;
}
