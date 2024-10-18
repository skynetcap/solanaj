package org.p2p.solanaj.core;

import org.p2p.solanaj.programs.AddressLookupTableProgram;

import java.util.List;
import java.util.Objects;

/**
 * Builder class for constructing Transactions.
 */
public class TransactionBuilder {

    private final Transaction transaction;

    /**
     * Constructs a new TransactionBuilder instance.
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
     * Sets the recent blockhash for the transaction.
     *
     * @param recentBlockhash The recent blockhash to set
     * @return This builder for method chaining
     * @throws NullPointerException if the recentBlockhash is null
     */
    public TransactionBuilder setRecentBlockHash(String recentBlockhash) {
        transaction.setRecentBlockHash(recentBlockhash);
        return this;
    }

    /**
     * Sets the fee payer and signs the transaction with the provided signers.
     *
     * @param signers The list of signers; the first signer is the fee payer
     * @return This builder for method chaining
     * @throws NullPointerException     if the signers list is null
     * @throws IllegalArgumentException if the signers list is empty
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
     * @param tablePubkey      the public key of the address table
     * @param writableIndexes  the list of writable indexes
     * @param readonlyIndexes  the list of readonly indexes
     * @return this builder for method chaining
     */
    public TransactionBuilder addAddressTableLookup(PublicKey tablePubkey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        transaction.addAddressTableLookup(tablePubkey, writableIndexes, readonlyIndexes);
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