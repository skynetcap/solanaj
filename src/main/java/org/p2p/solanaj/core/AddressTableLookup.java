package org.p2p.solanaj.core;

import com.squareup.moshi.Json;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.List;

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

    public AddressTableLookup(PublicKey accountKey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        this.accountKey = accountKey.toBase58();
        this.writableIndexes = writableIndexes;
        this.readonlyIndexes = readonlyIndexes;
    }

    // Getters
    public PublicKey getAccountKey() {
        return new PublicKey(accountKey);
    }

    public int getSerializedSize() {
        return 32 + // PublicKey size
               ShortvecEncoding.decodeLength(writableIndexes) +
               writableIndexes.size() +
               ShortvecEncoding.decodeLength(readonlyIndexes) +
               readonlyIndexes.size();
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.put(getAccountKey().toByteArray());
        buffer.put(ShortvecEncoding.encodeLength(writableIndexes.size()));
        writableIndexes.forEach(index -> buffer.put(index.byteValue()));
        buffer.put(ShortvecEncoding.encodeLength(readonlyIndexes.size()));
        readonlyIndexes.forEach(index -> buffer.put(index.byteValue()));
        return buffer.array();
    }
}