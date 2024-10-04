package org.p2p.solanaj.rpc.types;

import org.p2p.solanaj.core.AddressTableLookup;
import org.p2p.solanaj.core.PublicKey;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

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

        @Json(name = "version")
        private byte version;

        @Json(name = "addressTableLookups")
        private List<AddressTableLookup> addressTableLookups;

        @Json(name = "accountKeys")
        private List<String> accountKeyStrings;

        @Json(name = "header")
        private Header header;

        @Json(name = "instructions")
        private List<Instruction> instructions;

        @Json(name = "recentBlockhash")
        private String recentBlockhash;

        /**
         * Gets the list of account keys involved in the transaction as PublicKey objects.
         * @return The list of account keys as PublicKey objects.
         */
        public List<PublicKey> getAccountKeys() {
            return accountKeyStrings.stream()
                .map(PublicKey::new)
                .collect(Collectors.toList());
        }

        /**
         * Gets the list of address table lookups.
         * @return The list of address table lookups.
         */
        public List<AddressTableLookup> getAddressTableLookups() {
            return addressTableLookups != null ? addressTableLookups : Collections.emptyList();
        }
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

    public Transaction getTransaction() {
        return transaction;
    }
}
