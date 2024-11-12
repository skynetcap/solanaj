package org.p2p.solanaj.core;

import lombok.Getter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Arrays;

/**
 * Represents an instruction to be executed by a Solana program.
 */
@Getter
public class TransactionInstruction {

    private final PublicKey programId;
    private final List<AccountMeta> keys;
    private final byte[] data;

    public TransactionInstruction(PublicKey programId, List<AccountMeta> keys, byte[] data) {
        this.programId = Objects.requireNonNull(programId, "Program ID cannot be null");
        this.keys = Collections.unmodifiableList(Objects.requireNonNull(keys, "Keys cannot be null"));
        this.data = Arrays.copyOf(Objects.requireNonNull(data, "Data cannot be null"), data.length);
    }

    /**
     * Creates a new builder for TransactionInstruction.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Compares this TransactionInstruction with another object for equality.
     *
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionInstruction that = (TransactionInstruction) o;
        return Objects.equals(programId, that.programId) &&
               Objects.equals(keys, that.keys) &&
               Arrays.equals(data, that.data);
    }

    /**
     * Generates a hash code for this TransactionInstruction.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        int result = Objects.hash(programId, keys);
        result = 31 * result + Arrays.hashCode(data);
        return result;
    }

    /**
     * Returns a string representation of this TransactionInstruction.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "TransactionInstruction{" +
               "programId=" + programId +
               ", keys=" + keys +
               ", data=" + Arrays.toString(data) +
               '}';
    }

    /**
     * Builder class for creating TransactionInstruction instances.
     */
    public static class Builder {
        private PublicKey programId;
        private List<AccountMeta> keys;
        private byte[] data;

        /**
         * Sets the program ID for this instruction.
         *
         * @param programId the PublicKey of the program to execute this instruction
         * @return this Builder instance
         */
        public Builder programId(PublicKey programId) {
            this.programId = programId;
            return this;
        }

        /**
         * Sets the list of account keys for this instruction.
         *
         * @param keys the list of AccountMeta objects representing the accounts
         * @return this Builder instance
         */
        public Builder keys(List<AccountMeta> keys) {
            this.keys = keys;
            return this;
        }

        /**
         * Sets the instruction data.
         *
         * @param data the byte array containing the instruction data
         * @return this Builder instance
         */
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        /**
         * Builds and returns a new TransactionInstruction instance.
         *
         * @return a new TransactionInstruction instance
         * @throws NullPointerException if programId, keys, or data is null
         */
        public TransactionInstruction build() {
            return new TransactionInstruction(programId, keys, data);
        }
    }
}
