package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class BPFLoader extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");

    public static TransactionInstruction initializeBuffer(final PublicKey newAccount,
                                                          final PublicKey bufferAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(newAccount, false, true));
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

    public static TransactionInstruction write(final PublicKey writeableBuffer,
                                               final PublicKey bufferAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();

        keys.add(new AccountMeta(writeableBuffer, false, true));
        if (bufferAuthority != null) {
            keys.add(new AccountMeta(bufferAuthority, true, false));
        }

        byte[] payload = new byte[]{89, 0, 0, 0, 0, 0, 0, 0, 95, 102, 97, 107, 101, 95, 115, 101, 114, 117, 109, 95,
                111, 112, 101, 110, 95, 111, 114, 100, 101, 114, 115, 95, 97, 99, 99, 111, 117, 110, 116, 115, 95, 119, 105, 116, 104, 95, 97, 95, 109, 97, 115, 115, 105, 118, 101, 95, 111, 114, 100, 101, 114, 95, 116, 104, 97, 116, 95, 100, 114, 97, 105, 110, 115, 95, 101, 118, 101, 114, 121, 116, 104, 105, 110, 103, 95, 119, 104, 101, 110, 95, 99, 114, 97, 110, 107, 101, 100};

        ByteBuffer result = ByteBuffer.allocate(8 + payload.length);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put((byte) 1);
        result.put(4, (byte) 0); // offset 0 always for now
        result.put(8, payload);

        System.out.println(Arrays.toString(result.array()));


        return createTransactionInstruction(
                PROGRAM_ID,
                keys,
                result.array()
        );
    }
}
