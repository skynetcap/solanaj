package org.p2p.solanaj.core;

import java.util.List;
import java.util.Objects;

/**
 * Builder for constructing {@link Transaction} objects to be used in sendTransaction.
 */
public class TransactionBuilder {

    private final Transaction transaction;

    /**
     * Constructs a new TransactionBuilder.
     */
    public TransactionBuilder() {
        this.transaction = new Transaction();
    }

    /**
     * Adds a single instruction to the transaction.
     *
     * @param transactionInstruction the instruction to add
     * @return this builder for method chaining
     * @throws NullPointerException if transactionInstruction is null
     */
    public TransactionBuilder addInstruction(TransactionInstruction transactionInstruction) {
        Objects.requireNonNull(transactionInstruction, "Transaction instruction cannot be null");
        transaction.addInstruction(transactionInstruction);
        return this;
    }

    /**
     * Adds multiple instructions to the transaction.
     *
     * @param instructions the list of instructions to add
     * @return this builder for method chaining
     * @throws NullPointerException if instructions is null
     */
    public TransactionBuilder addInstructions(List<TransactionInstruction> instructions) {
        Objects.requireNonNull(instructions, "Instructions list cannot be null");
        instructions.forEach(this::addInstruction);
        return this;
    }

    /**
     * Sets the recent block hash for the transaction.
     *
     * @param recentBlockHash the recent block hash to set
     * @return this builder for method chaining
     * @throws NullPointerException if recentBlockHash is null
     */
    public TransactionBuilder setRecentBlockHash(String recentBlockHash) {
        Objects.requireNonNull(recentBlockHash, "Recent block hash cannot be null");
        transaction.setRecentBlockHash(recentBlockHash);
        return this;
    }

    /**
     * Sets the signers for the transaction and signs it.
     *
     * @param signers the list of signers
     * @return this builder for method chaining
     * @throws NullPointerException if signers is null
     * @throws IllegalArgumentException if signers is empty
     */
    public TransactionBuilder setSigners(List<Account> signers) {
        Objects.requireNonNull(signers, "Signers list cannot be null");
        if (signers.isEmpty()) {
            throw new IllegalArgumentException("Signers list cannot be empty");
        }
        transaction.sign(signers);
        return this;
    }

    /**
     * Builds and returns the constructed Transaction object.
     *
     * @return the built Transaction
     */
    public Transaction build() {
        return transaction;
    }

    /**
     * Sets the version for the transaction.
     *
     * @param version the version to set
     * @return this builder for method chaining
     */
    public TransactionBuilder setVersion(byte version) {
        transaction.setVersion(version);
        return this;
    }

    /**
     * Adds an address table lookup to the transaction.
     *
     * @param tablePubkey the public key of the address table
     * @param writableIndexes the list of writable indexes
     * @param readonlyIndexes the list of readonly indexes
     * @return this builder for method chaining
     */
    public TransactionBuilder addAddressTableLookup(PublicKey tablePubkey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        transaction.addAddressTableLookup(tablePubkey, writableIndexes, readonlyIndexes);
        return this;
    }

}
