package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.AbstractMap;

public class TokenResultObjects {

    @Getter
    @ToString
    @NoArgsConstructor
    public static class TokenAmountInfo {

        @Json(name = "amount")
        private String amount;

        @Json(name = "decimals")
        private int decimals;

        @Json(name = "uiAmount")
        private Double uiAmount;

        @Json(name = "uiAmountString")
        private String uiAmountString;

        @SuppressWarnings({ "rawtypes" })
        public TokenAmountInfo(AbstractMap am) {
            this.amount = (String) am.get("amount");
            this.decimals = (int) (double) am.get("decimals");
            this.uiAmount = (Double) am.get("uiAmount");
            this.uiAmountString = (String) am.get("uiAmountString");
        }
    }

    @Getter
    @ToString
    @NoArgsConstructor
    public static class TokenAccount extends TokenAmountInfo {

        @Json(name = "address")
        private String address;

        @SuppressWarnings({ "rawtypes" })
        public TokenAccount(AbstractMap am) {
            super(am);
            this.address = (String) am.get("address");
        }
    }

    @Getter
    @ToString
    public static class TokenInfo {

        @Json(name = "isNative")
        private Boolean isNative;

        @Json(name = "mint")
        private String mint;

        @Json(name = "owner")
        private String owner;

        @Json(name = "state")
        private String state;

        @Json(name = "tokenAmount")
        private TokenAmountInfo tokenAmount;

        @Json(name = "decimals")
        private Integer decimals;

        @Json(name = "freezeAuthority")
        private String freezeAuthority;

        @Json(name = "isInitialized")
        private Boolean isInitialized;

        @Json(name = "mintAuthority")
        private String mintAuthority;

        @Json(name = "supply")
        private String supply;
    }

    @Getter
    @ToString
    public static class ParsedData {

        @Json(name = "info")
        private TokenInfo info;

        @Json(name = "type")
        private String type;
    }

    @Getter
    @ToString
    public static class Data {

        @Json(name = "parsed")
        private ParsedData parsed;

        @Json(name = "program")
        private String program;

        @Json(name = "space")
        private Integer space;
    }

    @Getter
    @ToString
    public static class Value {

        @Json(name = "data")
        private Data data;

        @Json(name = "executable")
        private boolean executable;

        @Json(name = "lamports")
        private double lamports;

        @Json(name = "owner")
        private String owner;

        @Json(name = "rentEpoch")
        private double rentEpoch;
    }
}
