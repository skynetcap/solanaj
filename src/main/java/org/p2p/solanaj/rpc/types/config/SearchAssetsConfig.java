package org.p2p.solanaj.rpc.types.config;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
public class SearchAssetsConfig {

    private Integer page = null;

    private String cursor = null;

    private String authorityAddress = null;

    private Integer limit = null;

    @Json(name = "sortBy")
    private SortByObject sortBy = null;

    private Boolean compressed = null;

    private Boolean compressible = null;

    private Integer delegate = null;

    private String creatorAddress = null;

    private Boolean creatorVerified = null;

    private List<String> grouping = null;

    private Long supply = null;

    private String supplyMint = null;

    private Boolean frozen = null;

    private Boolean burnt = null;

    @Json(name = "interface")
    private Interface anInterface = null;

    private String ownerAddress = null;

    private String royaltyTargetType = null;

    private Integer royanltyTarget = null;

    private Integer royaltyAmount = null;

    private Integer ownerType = null;

    private String before = null;

    private String after = null;

    private Options options = null;

    private String tokenType = TokenType.FUNGIBLE.getName();

    public SearchAssetsConfig() {}

    public SearchAssetsConfig(String ownerAddress, int limit, int page) {
        this.ownerAddress = ownerAddress;
        this.limit = limit;
        this.page = page;
    }

    public SearchAssetsConfig(String ownerAddress, int limit, String cursor) {
        this.ownerAddress = ownerAddress;
        this.limit = limit;
        this.cursor = cursor;
    }

    public SearchAssetsConfig(String ownerAddress, int limit) {
        this.ownerAddress = ownerAddress;
        this.limit = limit;
    }

    public SearchAssetsConfig(String ownerAddress) {
        this.ownerAddress = ownerAddress;
    }

    @Getter
    @Setter
    static class SortByObject {
        @Getter
        @AllArgsConstructor
        enum SortBy {
            CREATED("created"),
            RECENT_ACTION("recent_action"),
            UPDATED("updated"),
            NONE("none");
            private final String value;
        }
        @Getter
        @AllArgsConstructor
        enum SortDirection {
            ASC("asc"),
            DESC("desc");
            private final String value;
        }
        private SortBy sortBy = SortBy.NONE;
        private SortDirection sortDirection = SortDirection.ASC;
    }

    @Getter
    @Setter
    static class Options {
        private Boolean showUnverifiedCollections = null;
        private Boolean showCollectionMetadata = null;
        private Boolean showGrandTotal = null;
        private Boolean showNativeBalance = null;
        private Boolean showInscription = null;
        private Boolean showZeroBalance = null;
    }

    @Getter
    @AllArgsConstructor
    public enum Interface {
        V1_NFT("V1_NFT"),
        V1_PRINT("V1_PRINT"),
        LEGACY_NFT("LEGACY_NFT"),
        V2_NFT("V2_NFT"),
        FUNGIBLE_ASSET("FungibleAsset"),
        FUNGIBLE_TOKEN("FungibleToken"),
        CUSTOM("Custom"),
        IDENTITY("Identity"),
        EXECUTABLE("Executable"),
        PROGRAMMABLE_NFT("ProgrammableNFT");
        private final String value;
    }

    @Getter
    @AllArgsConstructor
    enum TokenType {
        FUNGIBLE("fungible"),
        NON_FUNGIBLE("nonFungible"),
        REGULAR_NFT("regularNFT"),
        COMPRESSED_NFT("compressedNFT"),
        ALL("all");
        @Json
        private final String name;
    }
}
