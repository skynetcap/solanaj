package org.p2p.solanaj.utils.bip32.wallet.key;

import lombok.Getter;
import lombok.Setter;

/**
 * Defines a key with a given public key
 */
@Setter
@Getter
public class HdPublicKey extends HdKey {
    private byte[] publicKey;

}
