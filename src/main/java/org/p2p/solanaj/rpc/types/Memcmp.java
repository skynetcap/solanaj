package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Memcmp {

    @JsonProperty("offset")
    private long offset;

    @JsonProperty("bytes")
    private String bytes;
}