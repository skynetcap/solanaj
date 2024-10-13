package org.p2p.solanaj.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder class for constructing VersionedTransaction instances.
 */
public class VersionedTransactionBuilder {
    private VersionedMessage message;
    private List<Account> signers;

    /**
     * Initializes the builder with default values.
     */
    public VersionedTransactionBuilder() {
        this.message = new VersionedMessage();
        this.signers = new ArrayList<>();
    }

    /**
     * Adds a transaction instruction to the message.
     *
     * @param instruction The TransactionInstruction to add
     * @return The current builder instance
     */
    public VersionedTransactionBuilder addInstruction(TransactionInstruction instruction) {
        Objects.requireNonNull(instruction, "Instruction cannot be null");
        this.message.addInstruction(instruction);
        return this;
    }

    /**
     * Adds an Address Lookup Table to the message.
     *
     * @param lookupTable The AddressTableLookup to add
     * @return The current builder instance
     */
    public VersionedTransactionBuilder addAddressTableLookup(AddressTableLookup lookupTable) {
        Objects.requireNonNull(lookupTable, "LookupTable cannot be null");
        this.message.addAddressTableLookup(lookupTable);
        return this;
    }

    /**
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @return The current builder instance
     */
    public VersionedTransactionBuilder setRecentBlockhash(String recentBlockhash) {
        Objects.requireNonNull(recentBlockhash, "RecentBlockhash cannot be null");
        this.message.setRecentBlockhash(recentBlockhash);
        return this;
    }

    /**
     * Adds signers to the transaction.
     *
     * @param signers The list of Accounts that will sign the transaction
     * @return The current builder instance
     */
    public VersionedTransactionBuilder setSigners(List<Account> signers) {
        Objects.requireNonNull(signers, "Signers cannot be null");
        this.signers.addAll(signers);
        return this;
    }

    /**
     * Builds the VersionedTransaction instance.
     *
     * @return The constructed VersionedTransaction
     */
    public VersionedTransaction build() {
        VersionedTransaction vt = new VersionedTransaction(this.message);
        vt.sign(this.signers);
        return vt;
    }
}