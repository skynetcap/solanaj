package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AccountInfo extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {

        public Value() {
            // Default constructor for Jackson
        }

        public Value(AbstractMap am) {
            this.data = (List) am.get("data");
            this.executable = (boolean) am.get("executable");
            Object lamportsObj = am.get("lamports");
            this.lamports = lamportsObj instanceof Number ? ((Number) lamportsObj).doubleValue() : 0.0;
            this.owner = (String) am.get("owner");
            Object rentEpochObj = am.get("rentEpoch");
            this.rentEpoch = rentEpochObj instanceof Number ? ((Number) rentEpochObj).doubleValue() : 0.0;
        }

        @JsonProperty("data")
        private List<String> data;

        @JsonProperty("executable")
        private boolean executable;

        @JsonProperty("lamports")
        private double lamports;

        @JsonProperty("owner")
        private String owner;

        @JsonProperty("rentEpoch")
        private double rentEpoch;
    }

    @JsonProperty("value")
    private Value value;

    public byte[] getDecodedData() {
        return Base64.getDecoder().decode(getValue().getData().get(0).getBytes());
    }
}
