package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class EpochSchedule {

    @JsonProperty("slotsPerEpoch")
    private long slotsPerEpoch;

    @JsonProperty("leaderScheduleSlotOffset")
    private long leaderScheduleSlotOffset;

    @JsonProperty("warmup")
    private boolean warmup;

    @JsonProperty("firstNormalEpoch")
    private long firstNormalEpoch;

    @JsonProperty("firstNormalSlot")
    private long firstNormalSlot;
}
