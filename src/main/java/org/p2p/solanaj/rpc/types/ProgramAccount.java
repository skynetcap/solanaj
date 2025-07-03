package org.p2p.solanaj.rpc.types;

import java.util.AbstractMap;
import java.util.List;
import java.util.Base64;

import com.squareup.moshi.Json;

import lombok.Getter;
import lombok.ToString;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.rpc.types.config.RpcSendTransactionConfig.Encoding;

import org.bitcoinj.base.Base58;

@Getter
@ToString
public class ProgramAccount {

    @Getter
    @ToString
    public static class Account {
        @Json(name = "data")
        private String data;

        @Json(name = "executable")
        private boolean executable;

        @Json(name = "lamports")
        private double lamports;

        @Json(name = "owner")
        private String owner;

        @Json(name = "rentEpoch")
        private double rentEpoch;

        private String encoding;

        @SuppressWarnings({ "rawtypes", "unchecked" })
        public Account(Object acc) {
            AbstractMap account = (AbstractMap) acc;

            Object rawData = account.get("data");
            if (rawData instanceof List) {
                List<String> dataList = ((List<String>) rawData);

                this.data = dataList.get(0);
                this.encoding = (String) dataList.get(1);
            } else if (rawData instanceof String) {
                this.data = (String) rawData;
            }

            this.executable = (boolean) account.get("executable");
            this.lamports = (double) account.get("lamports");
            this.owner = (String) account.get("owner");
            this.rentEpoch = (double) account.get("rentEpoch");
        }

        public byte[] getDecodedData() {
            if (encoding != null && encoding.equals(Encoding.base64.toString())) {
                return Base64.getDecoder().decode(data);
            }

            return Base58.decode(data);
        }
    }

    @Json(name = "account")
    private Account account;

    @Json(name = "pubkey")
    private String pubkey;

    public PublicKey getPublicKey() {
        return new PublicKey(pubkey);
    }

    @SuppressWarnings({ "rawtypes" })
    public ProgramAccount(AbstractMap pa) {
        this.account = new Account(pa.get("account"));
        this.pubkey = (String) pa.get("pubkey");
    }
}
