package org.p2p.solanaj.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class TransactionInstruction {

    private PublicKey programId;

    private List<AccountMeta> keys;

    private byte[] data;
}
