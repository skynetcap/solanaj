package org.p2p.solanaj.core;

import org.p2p.solanaj.utils.ShortvecEncoding;

import java.nio.ByteBuffer;
import java.util.List;

public class AddressTableLookup {
    private final PublicKey tablePubkey;
    private final List<Byte> writableIndexes;
    private final List<Byte> readonlyIndexes;

    public AddressTableLookup(PublicKey tablePubkey, List<Byte> writableIndexes, List<Byte> readonlyIndexes) {
        this.tablePubkey = tablePubkey;
        this.writableIndexes = writableIndexes;
        this.readonlyIndexes = readonlyIndexes;
    }

    public int getSerializedSize() {
        return PublicKey.PUBLIC_KEY_LENGTH
            + ShortvecEncoding.decodeLength(writableIndexes)
            + writableIndexes.size()
            + ShortvecEncoding.decodeLength(readonlyIndexes)
            + readonlyIndexes.size();
    }

    public byte[] serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(getSerializedSize());
        buffer.put(tablePubkey.toByteArray());
        buffer.put(ShortvecEncoding.encodeLength(writableIndexes.size()));
        for (Byte index : writableIndexes) {
            buffer.put(index);
        }
        buffer.put(ShortvecEncoding.encodeLength(readonlyIndexes.size()));
        for (Byte index : readonlyIndexes) {
            buffer.put(index);
        }
        return buffer.array();
    }
}

