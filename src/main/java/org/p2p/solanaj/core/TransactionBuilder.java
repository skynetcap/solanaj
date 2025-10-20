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

    public TransactionBuilder setFeePayer(PublicKey feePayer) {
        transaction.setFeePayer(feePayer);
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

}
