package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BlockCommitment {

    @JsonProperty("commitment")
    private long[] commitment;

    @JsonProperty("totalStake")
    private long totalStake;
}
