package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class InflationGovernor {

    @JsonProperty("initial")
    private float initial;

    @JsonProperty("terminal")
    private float terminal;

    @JsonProperty("taper")
    private float taper;

    @JsonProperty("foundation")
    private float foundation;

    @JsonProperty("foundationTerm")
    private long foundationTerm;
}
