package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class SnapshotSlot {

    @Json(name = "full")
    private Long fullSnapshotSlot;

    @Json(name = "incremental")
    private Long incrementalSnapshotSlot;

}
