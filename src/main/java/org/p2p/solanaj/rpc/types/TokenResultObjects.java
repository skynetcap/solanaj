package org.p2p.solanaj.rpc.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.AbstractMap;
import java.util.List;

public class TokenResultObjects {

    @Getter
    @ToString
    @NoArgsConstructor
    public static class TokenAmountInfo {

        @JsonProperty("amount")
        private String amount;

        @JsonProperty("decimals")
        private int decimals;

        @JsonProperty("uiAmount")
        private Double uiAmount;

        @JsonProperty("uiAmountString")
        private String uiAmountString;

        @SuppressWarnings({ "rawtypes" })
        public TokenAmountInfo(AbstractMap am) {
            this.amount = (String) am.get("amount");
            Object decimalsObj = am.get("decimals");
            this.decimals = decimalsObj instanceof Number ? ((Number) decimalsObj).intValue() : 0;
            Object uiAmountObj = am.get("uiAmount");
            this.uiAmount = uiAmountObj instanceof Number ? ((Number) uiAmountObj).doubleValue() : null;
            this.uiAmountString = (String) am.get("uiAmountString");
        }
    }

    @Getter
    @ToString
    @NoArgsConstructor
    public static class TokenAccount extends TokenAmountInfo {

        @JsonProperty("address")
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

        @JsonProperty("isNative")
        private Boolean isNative;

        @JsonProperty("mint")
        private String mint;

        @JsonProperty("owner")
        private String owner;

        @JsonProperty("state")
        private String state;

        @JsonProperty("tokenAmount")
        private TokenAmountInfo tokenAmount;

        @JsonProperty("decimals")
        private int decimals;

        @JsonProperty("freezeAuthority")
        private String freezeAuthority;

        @JsonProperty("mintAuthority")
        private String mintAuthority;

        @JsonProperty("supply")
        private String supply;  

        @JsonProperty("isInitialized")
        private boolean isInitialized;

        // Optional extensions for token2022
        @JsonProperty("extensions")
        private List<Extension> extensions;
    }

    @Getter
    @ToString
    public static class ParsedData {

        @JsonProperty("info")
        private TokenInfo info;

        @JsonProperty("type")
        private String type;
    }

    @Getter
    @ToString
    public static class Data {

        @JsonProperty("parsed")
        private ParsedData parsed;

        @JsonProperty("program")
        private String program;

        @JsonProperty("space")
        private Integer space;
    }

    @Getter
    @ToString
    public static class Value {

        @JsonProperty("data")
        private Data data;

        @JsonProperty("executable")
        private boolean executable;

        @JsonProperty("lamports")
        private double lamports;

        @JsonProperty("owner")
        private String owner;

        @JsonProperty("rentEpoch")
        private double rentEpoch;
    }

    @Getter
    @ToString
    public static class Extension {

        @JsonProperty("extension")
        private String extensionType;

        @JsonProperty("state")
        private ExtensionState state;
    }

    @Getter
    @ToString
    public static class ExtensionState {
        
        // For "metadataPointer" extension
        @JsonProperty("authority")
        private String authority;

        @JsonProperty("metadataAddress")
        private String metadataAddress;

        // For "tokenMetadata" extension
        @JsonProperty("additionalMetadata")
        private List<Object> additionalMetadata;

        @JsonProperty("mint")
        private String mint;

        @JsonProperty("name")
        private String name;

        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("updateAuthority")
        private String updateAuthority;

        @JsonProperty("uri")
        private String uri;
    }
}
