package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.AbstractMap;

@Getter
@ToString
@NoArgsConstructor
public class InflationReward {

    @JsonProperty("epoch")
    private double epoch;

    @JsonProperty("effectiveSlot")
    private double effectiveSlot;

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("postBalance")
    private double postBalance;

    // Constructor for deserializing into List
    @SuppressWarnings({ "rawtypes" })
    public InflationReward(AbstractMap pa) {
        this.epoch = (Double) pa.get("epoch");
        this.effectiveSlot = (Double) pa.get("effectiveSlot");
        this.amount = (Double) pa.get("amount");
        this.postBalance = (Double) pa.get("postBalance");
    }
}
