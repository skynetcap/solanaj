package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EpochInfo {

    @JsonProperty("absoluteSlot")
    private long absoluteSlot;

    @JsonProperty("blockHeight")
    private long blockHeight;

    @JsonProperty("epoch")
    private long epoch;

    @JsonProperty("slotIndex")
    private long slotIndex;

    @JsonProperty("slotsInEpoch")
    private long slotsInEpoch;
}
    