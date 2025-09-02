package org.p2p.solanaj.programs;

import java.util.List;
import java.util.Arrays;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;
import org.p2p.solanaj.core.AccountMeta;

import static org.bitcoinj.core.Utils.*;

/**
 * Represents the System Program on the Solana blockchain.
 * This class provides methods to create various system instructions.
 */
public class SystemProgram {
    /** The program ID for the System Program */
    public static final PublicKey PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    private static final int PROGRAM_INDEX_CREATE_ACCOUNT = 0;
    private static final int PROGRAM_INDEX_ASSIGN = 1;
    private static final int PROGRAM_INDEX_TRANSFER = 2;

    private static final int UINT32_SIZE = 4;
    private static final int INT64_SIZE = 8;
    private static final int PUBKEY_SIZE = 32;

    private SystemProgram() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates a transfer instruction.
     *
     * @param fromPublicKey The public key of the account to transfer from
     * @param toPublicKey The public key of the account to transfer to
     * @param lamports The number of lamports to transfer
     * @return A TransactionInstruction object representing the transfer instruction
     * @throws IllegalArgumentException if lamports is negative
     */
    public static TransactionInstruction transfer(PublicKey fromPublicKey, PublicKey toPublicKey, long lamports) {
        if (lamports < 0) {
            throw new IllegalArgumentException("Lamports must be non-negative");
        }

        List<AccountMeta> keys = Arrays.asList(
            new AccountMeta(fromPublicKey, true, true),
            new AccountMeta(toPublicKey, false, true)
        );

        byte[] data = new byte[UINT32_SIZE + INT64_SIZE]; // 4 bytes for program index, 8 bytes for lamports
        uint32ToByteArrayLE(PROGRAM_INDEX_TRANSFER, data, 0);
        int64ToByteArrayLE(lamports, data, UINT32_SIZE);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }

    /** Write 8 bytes to the byte array (starting at the offset) as signed 64-bit integer in little endian format. */
    public static void int64ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
        out[offset + 4] = (byte) (0xFF & (val >> 32));
        out[offset + 5] = (byte) (0xFF & (val >> 40));
        out[offset + 6] = (byte) (0xFF & (val >> 48));
        out[offset + 7] = (byte) (0xFF & (val >> 56));
    }


    /** Write 4 bytes to the byte array (starting at the offset) as unsigned 32-bit integer in little endian format. */
    public static void uint32ToByteArrayLE(long val, byte[] out, int offset) {
        out[offset] = (byte) (0xFF & val);
        out[offset + 1] = (byte) (0xFF & (val >> 8));
        out[offset + 2] = (byte) (0xFF & (val >> 16));
        out[offset + 3] = (byte) (0xFF & (val >> 24));
    }    

    /**
     * Creates an instruction to create a new account.
     *
     * @param fromPublicKey The public key of the account funding the new account
     * @param newAccountPublicKey The public key of the new account to be created
     * @param lamports The number of lamports to transfer to the new account
     * @param space The amount of space in bytes to allocate to the new account
     * @param programId The program id to assign as the owner of the new account
     * @return A TransactionInstruction object representing the create account instruction
     * @throws IllegalArgumentException if lamports or space is negative
     */
    public static TransactionInstruction createAccount(PublicKey fromPublicKey, PublicKey newAccountPublicKey,
            long lamports, long space, PublicKey programId) {
        if (lamports < 0 || space < 0) {
            throw new IllegalArgumentException("Lamports and space must be non-negative");
        }

        List<AccountMeta> keys = Arrays.asList(
            new AccountMeta(fromPublicKey, true, true),
            new AccountMeta(newAccountPublicKey, true, true)
        );

        byte[] data = new byte[UINT32_SIZE + INT64_SIZE + INT64_SIZE + PUBKEY_SIZE]; // 4 + 8 + 8 + 32 = 52 bytes
        uint32ToByteArrayLE(PROGRAM_INDEX_CREATE_ACCOUNT, data, 0);
        int64ToByteArrayLE(lamports, data, UINT32_SIZE);
        int64ToByteArrayLE(space, data, UINT32_SIZE + INT64_SIZE);
        System.arraycopy(programId.toByteArray(), 0, data, UINT32_SIZE + INT64_SIZE + INT64_SIZE, PUBKEY_SIZE);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }

    /**
     * Creates an instruction to assign a new owner to an account.
     *
     * @param owner The current owner of the account
     * @param newOwner The new owner to assign to the account
     * @return A TransactionInstruction object representing the assign instruction
     */
    public static TransactionInstruction assign(PublicKey owner, PublicKey newOwner) {
        List<AccountMeta> keys = List.of(new AccountMeta(owner, true, true));

        byte[] data = new byte[UINT32_SIZE + PUBKEY_SIZE]; // 4 + 32 = 36 bytes
        uint32ToByteArrayLE(PROGRAM_INDEX_ASSIGN, data, 0);
        System.arraycopy(newOwner.toByteArray(), 0, data, UINT32_SIZE, PUBKEY_SIZE);

        return new TransactionInstruction(PROGRAM_ID, keys, data);
    }
}
