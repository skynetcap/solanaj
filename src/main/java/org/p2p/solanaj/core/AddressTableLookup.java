package org.p2p.solanaj.core;

import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

/**
 * Represents an Address Lookup Table (ALT) for Solana versioned transactions.
 * ALTs allow transactions to reference additional addresses required for execution,
 * enabling transactions that exceed the maximum number of accounts.
 */
public class AddressTableLookup {
    private final PublicKey accountKey;
    private final List<Integer> writableIndexes;
    private final List<Integer> readonlyIndexes;

    /**
     * Constructs an AddressTableLookup with the given parameters.
     *
     * @param accountKey      The public key of the address table
     * @param writableIndexes The list of writable address indexes
     * @param readonlyIndexes The list of readonly address indexes
     * @throws NullPointerException if any parameter is null
     */
    public AddressTableLookup(PublicKey accountKey, List<Integer> writableIndexes, List<Integer> readonlyIndexes) {
        this.accountKey = Objects.requireNonNull(accountKey, "AccountKey cannot be null");
        this.writableIndexes = Objects.requireNonNull(writableIndexes, "WritableIndexes cannot be null");
        this.readonlyIndexes = Objects.requireNonNull(readonlyIndexes, "ReadonlyIndexes cannot be null");
    }

    /**
     * Serializes the Address Lookup Table into a byte array.
     *
     * @return The serialized byte array
     */
    public byte[] serialize() {
        byte[] accountKeyBytes = accountKey.toByteArray();
        byte[] writableIndexesLength = ShortvecEncoding.encodeLength(writableIndexes.size());
        ByteBuffer writableBuffer = ByteBuffer.allocate(writableIndexes.size());
        for (Integer index : writableIndexes) {
            writableBuffer.put(index.byteValue());
        }

        byte[] readonlyIndexesLength = ShortvecEncoding.encodeLength(readonlyIndexes.size());
        ByteBuffer readonlyBuffer = ByteBuffer.allocate(readonlyIndexes.size());
        for (Integer index : readonlyIndexes) {
            readonlyBuffer.put(index.byteValue());
        }

        ByteBuffer buffer = ByteBuffer.allocate(
                accountKeyBytes.length +
                writableIndexesLength.length + writableIndexes.size() +
                readonlyIndexesLength.length + readonlyIndexes.size()
        );

        buffer.put(accountKeyBytes);
        buffer.put(writableIndexesLength);
        buffer.put(writableBuffer.array());
        buffer.put(readonlyIndexesLength);
        buffer.put(readonlyBuffer.array());

        return buffer.array();
    }

    /**
     * Gets the account key of the address lookup table.
     *
     * @return The account key
     */
    public PublicKey getAccountKey() {
        return accountKey;
    }

    /**
     * Gets the list of writable indexes.
     *
     * @return List of writable indexes
     */
    public List<Integer> getWritableIndexes() {
        return writableIndexes;
    }

    /**
     * Gets the list of readonly indexes.
     *
     * @return List of readonly indexes
     */
    public List<Integer> getReadonlyIndexes() {
        return readonlyIndexes;
    }
}   