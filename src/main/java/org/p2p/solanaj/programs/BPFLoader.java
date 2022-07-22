package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class BPFLoader extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");

    public static TransactionInstruction initializeBuffer(final PublicKey newAccount,
                                                          final PublicKey bufferAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(newAccount,false, true));
        if (bufferAuthority != null) {
            keys.add(new AccountMeta(bufferAuthority, false, false));
        }

        ByteBuffer result = ByteBuffer.allocate(4);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put((byte) 0);

        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                result.array()
        );
    }
}
