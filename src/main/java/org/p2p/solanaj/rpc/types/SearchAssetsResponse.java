package org.p2p.solanaj.rpc.types;

import com.squareup.moshi.Json;
import lombok.Getter;
import lombok.ToString;
import org.p2p.solanaj.rpc.types.config.SearchAssetsConfig;

import java.util.List;

@Getter
@ToString
public class SearchAssetsResponse {
    private Integer total;
    private Integer limit;
    private Integer page;
    @Json(name = "items")
    private List<Asset> items;
    private String cursor;
    private NativeBalance nativeBalance;

    @Getter
    @ToString
    public static class Asset {
        private SearchAssetsConfig.Interface iterface;
        private String id;
        private Object content;
        private List<Object> authorities;
        private Object compression;
        private Object grouping;
        private Object royalty;
        private List<Object> creators;
        private Object ownership;
        private Boolean mutable;
        private Boolean burnt;
        @Json(name = "mint_extensions")
        private MintExtensions mintExtensions;
        private Supply supply = null;
        @Json(name = "token_info")
        private TokenInfo tokenInfo;
        private Inscription inscription;
        private Object spl20;
    }

    @Getter
    @ToString
    static class MintExtensions {
        @Json(name = "confidential_transfer_mint")
        private ConfidentialTransferMint confidentialTransferMint;
        @Json(name = "confidential_transfer_fee_config")
        private ConfidentialTransferFeeConfig confidentialTransferFeeConfig;
        @Json(name = "transfer_fee_config")
        private TransferFeeConfig transferFeeConfig;
        @Json(name = "metadata_pointer")
        private MetadataPointer metadataPointer;
        @Json(name = "mint_close_authority")
        private MintCloseAuthority mintCloseAuthority;
        @Json(name = "permanent_delegate")
        private PermanentDelegate permanentDelegate;
        @Json(name = "transfer_hook")
        private TransferHook transferHook;
        @Json(name = "interest_bearing_config")
        private InterestBearingConfig interestBearingConfig;
        @Json(name = "default_account_state")
        private DefaultAccountState defaultAccountState;
        @Json(name = "confidential_transfer_account")
        private ConfidentialTransferAccount confidentialTransferAccount;
        @Json(name = "metadataobject")
        private Metadata metadata;
    }

    @Getter
    @ToString
    static class ConfidentialTransferMint {
        @Json(name = "authority")
        private String authority;
        @Json(name = "auto_approve_new_accounts")
        private Boolean autoApproveNewAccounts;
        @Json(name = "auditor_elgamal_pubkey")
        private String auditorElgamalPubkey;
    }

    @Getter
    @ToString
    static class ConfidentialTransferFeeConfig {
        @Json(name = "authority")
        private String authority;
        @Json(name = "withdraw_withheld_authority_elgamal_pubkey")
        private String withdrawWithheldAuthorityElgamalPubkey;
        @Json(name = "harvest_to_mint_enabled")
        private Boolean harvestToMintEnabled;
        @Json(name = "withheld_amount")
        private String withheldAmount;
    }

    @Getter
    @ToString
    static class TransferFeeConfig {
        @Json(name = "transfer_fee_config_authority")
        private String transferFeeConfigAuthority;
        @Json(name = "withdraw_withheld_authority")
        private String withdrawWithheldAuthority;
        @Json(name = "withheld_amount")
        private Double withheldAmount;
        @Json(name = "older_transfer_fee")
        private OlderTransferFee olderTransferFee;
        @Json(name = "newer_transfer_fee")
        private NewerTransferFee newerTransferFee;
    }

    @Getter
    @ToString
    static class OlderTransferFee {
        private String epoch;
        @Json(name = "maximum_fee")
        private String maximumFee;
        @Json(name = "transfer_fee_basis_points")
        private String transferFeeBasisPoints;
    }

    @Getter
    @ToString
    static class NewerTransferFee {
        private String epoch;
    }

    @Getter
    @ToString
    static class MetadataPointer {
        private String authority;
        @Json(name = "metadata_address")
        private String metadataAddress;
    }

    @Getter
    @ToString
    static class MintCloseAuthority {
        @Json(name = "close_authority")
        private String closeAuthority;
    }

    @Getter
    @ToString
    static class PermanentDelegate {
        private String delegate;
    }

    @Getter
    @ToString
    static class TransferHook {
        private String authority;
        @Json(name = "program_id")
        private String programId;
    }

    @Getter
    @ToString
    static class InterestBearingConfig {
        @Json(name = "rate_authority")
        private String rateAuthority;
        @Json(name = "initialization_timestamp")
        private Double initializationTimestamp;
        @Json(name = "pre_update_average_rate")
        private Double preUpdateAverageRate;
        @Json(name = "last_update_timestamp")
        private Double lastUpdateTimestamp;
        @Json(name = "current_rate")
        private Double currentRate;
    }

    @Getter
    @ToString
    static class DefaultAccountState {
        private String state;
    }

    @Getter
    @ToString
    static class ConfidentialTransferAccount {
        private Boolean approved;
        @Json(name = "elgamal_pubkey")
        private String elgamalPubkey;
        @Json(name = "pending_balance_lo")
        private String pendingBalanceLo;
        @Json(name = "pending_balance_hi")
        private String pendingBalanceHi;
        @Json(name = "available_balance")
        private String availableBalance;
        @Json(name = "decryptable_available_balance")
        private String decryptableAvailableBalance;
        @Json(name = "allow_confidential_credits")
        private Boolean allowConfidentialCredits;
        @Json(name = "allow_non_confidential_credits")
        private Boolean allowNonConfidentialCredits;
        @Json(name = "pending_balance_credit_counter")
        private Integer pendingBalanceCreditCounter;
        @Json(name = "maximum_pending_balance_credit_counter")
        private Integer maximumPendingBalanceCreditCounter;
        @Json(name = "expected_pending_balance_credit_counter")
        private Integer expectedPendingBalanceCreditCounter;
        @Json(name = "actual_pending_balance_credit_counter")
        private Integer actualPendingBalanceCreditCounter;
    }

    @Getter
    @ToString
    static class Metadata {
        @Json(name = "update_authority")
        private String updateAuthority;
        private String mint;
        private String name;
        private String symbol;
        private String uri;
        @Json(name = "additional_metadata")
        private List<KeyValue> additionalMetadata;
    }

    @Getter
    @ToString
    static class KeyValue {
        private String key;
        private String value;
    }

    @Getter
    @ToString
    static class Supply {
        @Json(name = "print_max_supply")
        private Long printMaxSupply;
        @Json(name = "print_current_supply")
        private Long printCurrentSupply;
        @Json(name = "edition_nonce")
        private Integer editionNonce;
        @Json(name = "edition_number")
        private Integer editionNumber = null;
    }

    @Getter
    @ToString
    static class TokenInfo {
        private String symbol;
        private Long balance;
        private Long supply;
        private Integer decimals;
        @Json(name = "token_program")
        private String tokenProgram;
        @Json(name = "associated_token_address")
        private String associatedTokenAddress;
        @Json(name = "price_info")
        private PriceInfo priceInfo;
    }

    @Getter
    @ToString
    static class PriceInfo {
        @Json(name = "price_per_token")
        private Double pricePerToken;
        @Json(name = "total_price")
        private Double totalPrice;
        private String currency;
    }

    @Getter
    @ToString
    static class Inscription {
        private Integer order;
        private Integer size;
        private String contentType;
        private String encoding;
        private String validationHash;
        private String inscriptionDataAccount;
        private String authority;
    }

    @Getter
    @ToString
    static class NativeBalance {
        private Long lamports;
        @Json(name = "price_per_sol")
        private Double pricePerSol;
        @Json(name = "total_price")
        private Double totalPrice;
    }
}
