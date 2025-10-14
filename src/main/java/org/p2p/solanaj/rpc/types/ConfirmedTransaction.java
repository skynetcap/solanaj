package org.p2p.solanaj.rpc.types;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ConfirmedTransaction {

    @Getter
    @ToString
    public static class Header {

        @JsonProperty("numReadonlySignedAccounts")
        private long numReadonlySignedAccounts;

        @JsonProperty("numReadonlyUnsignedAccounts")
        private long numReadonlyUnsignedAccounts;

        @JsonProperty("numRequiredSignatures")
        private long numRequiredSignatures;
    }

    @Getter
    @ToString
    public static class Instruction {

        @JsonProperty("accounts")
        private List<Long> accounts;

        @JsonProperty("data")
        private String data;

        @JsonProperty("programIdIndex")
        private long programIdIndex;
    }

    @Getter
    @ToString
    public static class Message {

        @JsonProperty("accountKeys")
        private List<String> accountKeys;

        @JsonProperty("header")
        private Header header;

        @JsonProperty("instructions")
        private List<Instruction> instructions;

        @JsonProperty("recentBlockhash")
        private String recentBlockhash;
    }

    @Getter
    @ToString
    public static class Status {

        @JsonProperty("Ok")
        private Object ok;
    }

    @Getter
    @ToString
    public static class TokenBalance {

        @JsonProperty("accountIndex")
        private Double accountIndex;

        @JsonProperty("mint")
        private String mint;

        @JsonProperty("uiTokenAmount")
        private TokenResultObjects.TokenAmountInfo uiTokenAmount;
    }

    @Getter
    @ToString
    public static class Meta {

        @JsonProperty("err")
        private Object err;

        @JsonProperty("fee")
        private long fee;

        @JsonProperty("innerInstructions")
        private List<Object> innerInstructions;

        @JsonProperty("preTokenBalances")
        private List<TokenBalance> preTokenBalances;

        @JsonProperty("postTokenBalances")
        private List<TokenBalance> postTokenBalances;

        @JsonProperty("postBalances")
        private List<Long> postBalances;

        @JsonProperty("preBalances")
        private List<Long> preBalances;

        @JsonProperty("status")
        private Status status;
    }

    @Getter
    @ToString
    public static class Transaction {

        @JsonProperty("message")
        private Message message;

        @JsonProperty("signatures")
        private List<String> signatures;

        
        @JsonProperty("blockTime")
        private String blocktime;


    }

    @JsonProperty("meta")
    private Meta meta;

    @JsonProperty("slot")
    private long slot;

    @JsonProperty("transaction")
    private Transaction transaction;
}
