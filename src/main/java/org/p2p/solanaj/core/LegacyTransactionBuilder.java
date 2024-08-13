package org.p2p.solanaj.core;

import java.util.List;

/**
 * Builder for constructing {@link LegacyTransaction} objects to be used in sendLegacyTransaction.
 */
public class LegacyTransactionBuilder {

    private final LegacyTransaction legacyTransaction;

    public LegacyTransactionBuilder() {
        legacyTransaction = new LegacyTransaction();
    }

    public LegacyTransactionBuilder addInstruction(TransactionInstruction transactionInstruction) {
        legacyTransaction.addInstruction(transactionInstruction);
        return this;
    }

    public LegacyTransactionBuilder setRecentBlockHash(String recentBlockHash) {
        legacyTransaction.setRecentBlockHash(recentBlockHash);
        return this;
    }

    public LegacyTransactionBuilder setSigners(List<Account> signers) {
        legacyTransaction.sign(signers);
        return this;
    }

    public LegacyTransaction build() {
        return legacyTransaction;
    }

}
