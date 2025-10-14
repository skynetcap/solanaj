package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class InflationRate {

    @JsonProperty("total")
    private float total;

    @JsonProperty("validator")
    private float validator;

    @JsonProperty("foundation")
    private float foundation;

    @JsonProperty("epoch")
    private long epoch;
}
