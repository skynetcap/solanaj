package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;
import java.util.Base64;
import java.util.List;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AccountInfo extends RpcResultObject {

    @Getter
    @ToString
    public static class Value {

        public Value(AbstractMap am) {
            this.data = (List) am.get("data");
            this.executable = (boolean) am.get("executable");
            this.lamports = (double) am.get("lamports");
            this.owner = (String) am.get("owner");
            this.rentEpoch = (double) am.get("rentEpoch");
        }

        @Json(name = "data")
        private List<String> data;

        @Json(name = "executable")
        private boolean executable;

        @Json(name = "lamports")
        private double lamports;

        @Json(name = "owner")
        private String owner;

        @Json(name = "rentEpoch")
        private double rentEpoch;
    }

    @Json(name = "value")
    private Value value;

    public byte[] getDecodedData() {
        return Base64.getDecoder().decode(getValue().getData().get(0).getBytes());
    }
}
