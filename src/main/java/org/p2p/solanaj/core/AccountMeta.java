package org.p2p.solanaj.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountMeta {

    private PublicKey publicKey;

    private boolean isSigner;

    private boolean isWritable;

    /**
     * Sorting based on isSigner and isWritable cannot fully meet the requirements. This value can be used for custom sorting, because if the order is incorrect during serialization, it may lead to failed method calls.
     */
    private int sort = Integer.MAX_VALUE;

    public AccountMeta(PublicKey publicKey, boolean isSigner, boolean isWritable) {
        this.publicKey = publicKey;
        this.isSigner = isSigner;
        this.isWritable = isWritable;
    }

}