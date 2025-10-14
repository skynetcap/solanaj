package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SnapshotSlot {

    @JsonProperty("full")
    private Long fullSnapshotSlot;

    @JsonProperty("incremental")
    private Long incrementalSnapshotSlot;

}
