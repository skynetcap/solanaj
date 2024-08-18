package org.p2p.solanaj.programs;

import java.util.ArrayList;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.AccountMeta;

import static org.bitcoinj.core.Utils.*;

public class SystemProgram extends Program {
    public static final PublicKey PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    public static final int PROGRAM_INDEX_CREATE_ACCOUNT = 0;
    public static final int PROGRAM_INDEX_TRANSFER = 2;

    public static TransactionInstruction transfer(PublicKey fromPublicKey, PublicKey toPublickKey, long lamports) {
        ArrayList<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(toPublickKey, false, true));

        // 4 byte instruction index + 8 bytes lamports
        byte[] data = new byte[4 + 8];
        uint32ToByteArrayLE(PROGRAM_INDEX_TRANSFER, data, 0);
        int64ToByteArrayLE(lamports, data, 4);

        return createTransactionInstruction(PROGRAM_ID, keys, data);
    }

    public static TransactionInstruction createAccount(PublicKey fromPublicKey, PublicKey newAccountPublikkey,
            long lamports, long space, PublicKey programId) {
        ArrayList<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(fromPublicKey, true, true));
        keys.add(new AccountMeta(newAccountPublikkey, true, true));

        byte[] data = new byte[4 + 8 + 8 + 32];
        uint32ToByteArrayLE(PROGRAM_INDEX_CREATE_ACCOUNT, data, 0);
        int64ToByteArrayLE(lamports, data, 4);
        int64ToByteArrayLE(space, data, 12);
        System.arraycopy(programId.toByteArray(), 0, data, 20, 32);

        return createTransactionInstruction(PROGRAM_ID, keys, data);
    }

    public static TransactionInstruction assign(PublicKey owner, PublicKey newOwner) {
        ArrayList<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(owner, true, true));

        byte[] data = new byte[4 + 32];
        uint32ToByteArrayLE(1, data, 0);
        System.arraycopy(newOwner.toByteArray(), 0, data, 4, 32);

        return createTransactionInstruction(PROGRAM_ID, keys, data);
    }
}
