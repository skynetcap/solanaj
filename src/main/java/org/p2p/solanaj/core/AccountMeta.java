package org.p2p.solanaj.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountMeta {

    private PublicKey publicKey;

    private boolean isSigner;

    private boolean isWritable;

    @Override
    public String toString() {
        return "AccountMeta{" +
                "publicKey=" + publicKey +
                ", isSigner=" + isSigner +
                ", isWritable=" + isWritable +
                '}';
    }
}