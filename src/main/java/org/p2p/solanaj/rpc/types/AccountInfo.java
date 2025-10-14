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
            this.lamports = (double) am.get("lamports");
            this.owner = (String) am.get("owner");
            this.rentEpoch = (double) am.get("rentEpoch");
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
