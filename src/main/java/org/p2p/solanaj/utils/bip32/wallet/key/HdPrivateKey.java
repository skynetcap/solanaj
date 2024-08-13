package org.p2p.solanaj.utils.bip32.wallet.key;

import lombok.Getter;
import lombok.Setter;

/**
 * Defines a key with a given private key
 */
@Setter
@Getter
public class HdPrivateKey extends HdKey {
    private byte[] privateKey;

}
