package org.p2p.solanaj.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.EqualsAndHashCode;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class TransactionInstruction {

    private PublicKey programId;

    private List<AccountMeta> keys;

    private byte[] data;
}
