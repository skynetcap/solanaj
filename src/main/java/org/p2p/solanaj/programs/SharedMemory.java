package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class SharedMemory extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("ABbdZW8gJcXEB9XkRZLwDDuGmom3hBwWEsG2y49bHv45");

    public static TransactionInstruction initializeBuffer(final PublicKey accountToWrite,
                                                          byte[] data,
                                                          int offset) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(accountToWrite, false, true));

        ByteBuffer result = ByteBuffer.allocate(8 + data.length);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.putInt(0, offset);
        result.put(8, data);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                result.array()
        );
    }

}
