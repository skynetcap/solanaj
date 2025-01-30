package org.p2p.solanaj.rpc.types;

import java.util.List;

import com.squareup.moshi.Json;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class TokenAccountsResponse {

    @Json(name = "total")
    private int total;

    @Json(name = "limit")
    private int limit;

    @Json(name = "cursor")
    private String cursor;

    @Json(name = "token_accounts")
    private List<TokenAccount> tokenAccounts;

    @Getter
    @ToString
    public static class TokenAccount {
        @Json(name = "address")
        private String address;

        @Json(name = "mint")
        private String mint;

        @Json(name = "owner")
        private String owner;

        @Json(name = "amount")
        private Long amount;

        @Json(name = "delegated_amount")
        private Long delegatedAmount;

        @Json(name = "frozen")
        private Boolean frozen;
    }
}
