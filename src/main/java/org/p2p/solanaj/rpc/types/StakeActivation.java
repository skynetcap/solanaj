package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class StakeActivation {

    @JsonProperty("active")
    private long active;

    @JsonProperty("inactive")
    private long inactive;

    @JsonProperty("state")
    private String state;
}
