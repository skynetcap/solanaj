package org.p2p.solanaj.utils.bip32.wallet;


import lombok.Getter;
import org.p2p.solanaj.utils.bip32.wallet.key.HdPrivateKey;
import org.p2p.solanaj.utils.bip32.wallet.key.HdPublicKey;

/**
 * An HD pub/private key
 */
public class HdAddress {

    @Getter
    private final HdPrivateKey privateKey;
    @Getter
    private final HdPublicKey publicKey;
    private final SolanaCoin solanaCoin;
    @Getter
    private final String path;

    public HdAddress(HdPrivateKey privateKey, HdPublicKey publicKey, SolanaCoin solanaCoin, String path) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.solanaCoin = solanaCoin;
        this.path = path;
    }

    public SolanaCoin getCoinType() {
        return solanaCoin;
    }

}
