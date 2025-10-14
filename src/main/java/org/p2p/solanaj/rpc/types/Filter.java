package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Filter {

    @JsonProperty("memcmp")
    private Memcmp memcmp;
}