package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
public class SignatureInformation {

    @JsonProperty("err")
    private Object err;

    @JsonProperty("memo")
    private Object memo;

    @JsonProperty("signature")
    private String signature;

    @JsonProperty("slot")
    private double slot;

    @JsonProperty("blockTime")
    private double blockTime;

    @SuppressWarnings({ "rawtypes" })
    public SignatureInformation(AbstractMap info) {
        this.err = info.get("err");
        this.memo = info.get("memo");
        this.signature = (String) info.get("signature");
        this.slot = (Double) info.get("slot");
        this.blockTime = (Double) info.get("blockTime");
    }
}
