package org.p2p.solanaj.rpc.types;

import java.util.List;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ConfirmedTransaction {

    @Getter
    @ToString
    public static class Header {

        @Json(name = "numReadonlySignedAccounts")
        private long numReadonlySignedAccounts;

        @Json(name = "numReadonlyUnsignedAccounts")
        private long numReadonlyUnsignedAccounts;

        @Json(name = "numRequiredSignatures")
        private long numRequiredSignatures;
    }

    @Getter
    @ToString
    public static class Instruction {

        @Json(name = "accounts")
        private List<Long> accounts;

        @Json(name = "data")
        private String data;

        @Json(name = "programIdIndex")
        private long programIdIndex;
    }

    @Getter
    @ToString
    public static class Message {

        @Json(name = "accountKeys")
        private List<String> accountKeys;

        @Json(name = "header")
        private Header header;

        @Json(name = "instructions")
        private List<Instruction> instructions;

        @Json(name = "recentBlockhash")
        private String recentBlockhash;
    }

    @Getter
    @ToString
    public static class Status {

        @Json(name = "Ok")
        private Object ok;
    }

    @Getter
    @ToString
    public static class TokenBalance {

        @Json(name = "accountIndex")
        private Double accountIndex;

        @Json(name = "mint")
        private String mint;

        @Json(name = "uiTokenAmount")
        private TokenResultObjects.TokenAmountInfo uiTokenAmount;
    }

	@Getter
	@ToString
	public static class LoadedAddresses {

		@Json(name = "readonly")
		public List<String> readOnly;

		@Json(name = "writable")
		public List<String> writable;
	}

	@Getter
	@ToString
	public static class Meta {

		@Json(name = "err")
		private Object err;

		@Json(name = "fee")
		private long fee;

		@Json(name = "innerInstructions")
		private List<Object> innerInstructions;

		@Json(name = "preTokenBalances")
		private List<TokenBalance> preTokenBalances;

		@Json(name = "postTokenBalances")
		private List<TokenBalance> postTokenBalances;

		@Json(name = "postBalances")
		private List<Long> postBalances;

		@Json(name = "preBalances")
		private List<Long> preBalances;

		@Json(name = "status")
		private Status status;

		@Json(name = "loadedAddresses")
		private LoadedAddresses loadedAddresses;
	}

    @Getter
    @ToString
    public static class Transaction {

        @Json(name = "message")
        private Message message;

        @Json(name = "signatures")
        private List<String> signatures;


        @Json(name = "blockTime")
        private String blocktime;


    }

    @Json(name = "meta")
    private Meta meta;

    @Json(name = "slot")
    private long slot;

    @Json(name = "transaction")
    private Transaction transaction;
}
