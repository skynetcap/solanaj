package org.p2p.solanaj.rpc.types.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RpcSendTransactionConfig {

    public static enum Encoding {
        base64("base64"),
        base58("base58");

        private String enc;

        Encoding(String enc) {
            this.enc = enc;
        }

        public String getEncoding() {
            return enc;
        }

    }

    @JsonProperty("encoding")
    @Builder.Default
    private Encoding encoding = Encoding.base64;

    @JsonProperty("skipPreflight")
    @Builder.Default
    private boolean skipPreflight = true;

    @JsonProperty("maxRetries")
    @Builder.Default
    private long maxRetries = 0;

}
