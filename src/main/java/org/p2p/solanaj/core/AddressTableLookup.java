package org.p2p.solanaj.core;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Represents an Address Lookup Table (ALT) in Solana.
 * <p>
 * ALTs allow transactions to reference additional addresses required for execution,
 * enabling transactions that exceed the maximum number of accounts.
 * </p>
 */
@AllArgsConstructor
public class AddressTableLookup {
    @Json(name = "accountKey")
    private String accountKey;

    @Getter
    @Json(name = "writableIndexes")
    private List<Byte> writableIndexes;

    @Getter
    @Json(name = "readonlyIndexes")
    private List<Byte> readonlyIndexes;

    /**
     * Constructs an AddressTableLookup with the given parameters.
     *
     * @param accountKey      The public key of the address table
     * @param writableIndexes The list of writable indexes
     * @param readonlyIndexes The list of readonly indexes
     */
    public AddressTableLookup(PublicKey accountKey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        this.accountKey = accountKey.toBase58();
        this.writableIndexes = writableIndexes;
        this.readonlyIndexes = readonlyIndexes;
    }

    /**
     * Gets the account key as a PublicKey object.
     *
     * @return the account key
     */
    public PublicKey getAccountKey() {
        return new PublicKey(accountKey);
    }

    /**
     * Calculates the serialized size of the AddressTableLookup.
     *
     * @return the size in bytes
     */
    public int getSerializedSize() {
        return 32 + // PublicKey size
               ShortvecEncoding.decodeLength(writableIndexes) +
               writableIndexes.size() +
               ShortvecEncoding.decodeLength(readonlyIndexes) +
               readonlyIndexes.size();
    }

    /**
     * Serializes the AddressTableLookup into a byte array.
     *
     * @return the serialized byte array
     */
    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.put(getAccountKey().toByteArray());
        buffer.put(ShortvecEncoding.encodeLength(writableIndexes.size()));
        for (Byte index : writableIndexes) {
            buffer.put(index.byteValue());
        }
        buffer.put(ShortvecEncoding.encodeLength(readonlyIndexes.size()));
        for (Byte index : readonlyIndexes) {
            buffer.put(index.byteValue());
        }
        return buffer.array();
    }
}