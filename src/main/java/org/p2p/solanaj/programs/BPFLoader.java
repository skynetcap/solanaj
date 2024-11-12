package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * BPFLoader program instructions.
 * 
 * This class implements the instructions for the BPF Loader program as specified in:
 * https://github.com/solana-labs/solana/blob/master/sdk/program/src/loader_upgradeable_instruction.rs
 */
public class BPFLoader extends Program {

    public static final PublicKey PROGRAM_ID = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");

    // Hardcoded public keys for system accounts
    public static final PublicKey SYSVAR_RENT_PUBKEY = new PublicKey("SysvarRent111111111111111111111111111111111");
    public static final PublicKey SYSVAR_CLOCK_PUBKEY = new PublicKey("SysvarC1ock11111111111111111111111111111111");
    public static final PublicKey SYSTEM_PROGRAM_ID = new PublicKey("11111111111111111111111111111111");

    /**
     * Initialize a Buffer account.
     *
     * @param newAccount      The account to initialize as a buffer.
     * @param bufferAuthority The authority of the buffer (optional).
     * @return TransactionInstruction for initializing the buffer.
     */
    public static TransactionInstruction initializeBuffer(final PublicKey newAccount,
                                                          final PublicKey bufferAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(newAccount, false, true));
        if (bufferAuthority != null) {
            keys.add(new AccountMeta(bufferAuthority, false, false));
        }

        ByteBuffer data = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 0); // Instruction index for InitializeBuffer

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Write program data into a Buffer account (previous version without offset).
     *
     * @param writeableBuffer The buffer account to write program data to.
     * @param bufferAuthority The authority of the buffer.
     * @param data            The program data to write.
     * @return TransactionInstruction for writing to the buffer.
     */
    public static TransactionInstruction write(final PublicKey writeableBuffer,
                                               final PublicKey bufferAuthority,
                                               final byte[] data) {
        return write(writeableBuffer, bufferAuthority, 0, data);
    }

    /**
     * Write program data into a Buffer account.
     *
     * @param writeableBuffer The buffer account to write program data to.
     * @param bufferAuthority The authority of the buffer.
     * @param offset          The offset at which to write the data.
     * @param data            The program data to write.
     * @return TransactionInstruction for writing to the buffer.
     */
    public static TransactionInstruction write(final PublicKey writeableBuffer,
                                               final PublicKey bufferAuthority,
                                               final long offset,
                                               final byte[] data) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(writeableBuffer, false, true));
        keys.add(new AccountMeta(bufferAuthority, true, false));

        ByteBuffer instructionData = ByteBuffer.allocate(5 + data.length).order(ByteOrder.LITTLE_ENDIAN);
        instructionData.put((byte) 1); // Instruction index for Write
        instructionData.putInt((int) offset);
        instructionData.put(data);

        return createTransactionInstruction(PROGRAM_ID, keys, instructionData.array());
    }

    /**
     * Deploy an executable program.
     *
     * @param payer           The account paying for the deployment.
     * @param programData     The program data account.
     * @param program         The program account.
     * @param buffer          The buffer containing the program data.
     * @param programAuthority The program's authority.
     * @param maxDataLen      The maximum length of the program data.
     * @return TransactionInstruction for deploying the program.
     */
    public static TransactionInstruction deployWithMaxDataLen(final PublicKey payer,
                                                              final PublicKey programData,
                                                              final PublicKey program,
                                                              final PublicKey buffer,
                                                              final PublicKey programAuthority,
                                                              final long maxDataLen) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(payer, true, true));
        keys.add(new AccountMeta(programData, false, true));
        keys.add(new AccountMeta(program, false, true));
        keys.add(new AccountMeta(buffer, false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));
        keys.add(new AccountMeta(SYSVAR_CLOCK_PUBKEY, false, false));
        keys.add(new AccountMeta(SYSTEM_PROGRAM_ID, false, false));
        keys.add(new AccountMeta(programAuthority, true, false));

        ByteBuffer data = ByteBuffer.allocate(9).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 2); // Instruction index for DeployWithMaxDataLen
        data.putLong(maxDataLen);

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Upgrade a program.
     *
     * @param programData     The program data account.
     * @param program         The program account.
     * @param buffer          The buffer containing the updated program data.
     * @param spillAccount    The account to receive leftover lamports.
     * @param programAuthority The program's authority.
     * @return TransactionInstruction for upgrading the program.
     */
    public static TransactionInstruction upgrade(final PublicKey programData,
                                                 final PublicKey program,
                                                 final PublicKey buffer,
                                                 final PublicKey spillAccount,
                                                 final PublicKey programAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(programData, false, true));
        keys.add(new AccountMeta(program, false, true));
        keys.add(new AccountMeta(buffer, false, true));
        keys.add(new AccountMeta(spillAccount, false, true));
        keys.add(new AccountMeta(SYSVAR_RENT_PUBKEY, false, false));
        keys.add(new AccountMeta(SYSVAR_CLOCK_PUBKEY, false, false));
        keys.add(new AccountMeta(programAuthority, true, false));

        ByteBuffer data = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 3); // Instruction index for Upgrade

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Set a new authority for a buffer or program.
     *
     * @param account         The buffer or program data account.
     * @param currentAuthority The current authority.
     * @param newAuthority    The new authority (optional).
     * @return TransactionInstruction for setting the authority.
     */
    public static TransactionInstruction setAuthority(final PublicKey account,
                                                      final PublicKey currentAuthority,
                                                      final PublicKey newAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(currentAuthority, true, false));
        if (newAuthority != null) {
            keys.add(new AccountMeta(newAuthority, false, false));
        }

        ByteBuffer data = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 4); // Instruction index for SetAuthority

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Close a buffer or program account.
     *
     * @param account         The account to close.
     * @param recipient       The account to receive the lamports.
     * @param authority       The account's authority (optional).
     * @param programAccount  The associated program account (optional).
     * @return TransactionInstruction for closing the account.
     */
    public static TransactionInstruction close(final PublicKey account,
                                               final PublicKey recipient,
                                               final PublicKey authority,
                                               final PublicKey programAccount) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(recipient, false, true));
        if (authority != null) {
            keys.add(new AccountMeta(authority, true, false));
        }
        if (programAccount != null) {
            keys.add(new AccountMeta(programAccount, false, true));
        }

        ByteBuffer data = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 5); // Instruction index for Close

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Extend a program's data length.
     *
     * @param programData     The program data account.
     * @param program         The program account.
     * @param payer           The account paying for the extension (optional).
     * @param additionalBytes The number of bytes to extend.
     * @return TransactionInstruction for extending the program.
     */
    public static TransactionInstruction extendProgram(final PublicKey programData,
                                                       final PublicKey program,
                                                       final PublicKey payer,
                                                       final int additionalBytes) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(programData, false, true));
        keys.add(new AccountMeta(program, false, true));
        keys.add(new AccountMeta(SYSTEM_PROGRAM_ID, false, false));
        if (payer != null) {
            keys.add(new AccountMeta(payer, true, true));
        }

        ByteBuffer data = ByteBuffer.allocate(5).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 6); // Instruction index for ExtendProgram
        data.putInt(additionalBytes);

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }

    /**
     * Set a new authority for a buffer or program with authority check.
     *
     * @param account         The buffer or program data account.
     * @param currentAuthority The current authority.
     * @param newAuthority    The new authority.
     * @return TransactionInstruction for setting the authority with a check.
     */
    public static TransactionInstruction setAuthorityChecked(final PublicKey account,
                                                             final PublicKey currentAuthority,
                                                             final PublicKey newAuthority) {
        final List<AccountMeta> keys = new ArrayList<>();
        keys.add(new AccountMeta(account, false, true));
        keys.add(new AccountMeta(currentAuthority, true, false));
        keys.add(new AccountMeta(newAuthority, true, false));

        ByteBuffer data = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
        data.put((byte) 7); // Instruction index for SetAuthorityChecked

        return createTransactionInstruction(PROGRAM_ID, keys, data.array());
    }
}
