package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Factory class for creating ComputeBudget program instructions.
 */
public class ComputeBudgetProgram extends Program {

    /** The program ID for the ComputeBudget program */
    public static final PublicKey PROGRAM_ID =
            PublicKey.valueOf("ComputeBudget111111111111111111111111111111");

    private static final byte REQUEST_HEAP_FRAME = 0x01;
    private static final byte SET_COMPUTE_UNIT_LIMIT = 0x02;
    private static final byte SET_COMPUTE_UNIT_PRICE = 0x03;
    private static final byte SET_LOADED_ACCOUNTS_DATA_SIZE_LIMIT = 0x04;

    /**
     * Creates an instruction to set the compute unit price.
     *
     * @param microLamports The desired price of a compute unit in micro-lamports.
     * @return A TransactionInstruction to set the compute unit price.
     */
    public static TransactionInstruction setComputeUnitPrice(int microLamports) {
        byte[] transactionData = encodeSetComputeUnitPriceTransaction(microLamports);
        return createTransactionInstruction(PROGRAM_ID, Collections.emptyList(), transactionData);
    }

    /**
     * Creates an instruction to set the compute unit limit.
     *
     * @param units The desired maximum number of compute units.
     * @return A TransactionInstruction to set the compute unit limit.
     */
    public static TransactionInstruction setComputeUnitLimit(int units) {
        byte[] transactionData = encodeSetComputeUnitLimitTransaction(units);
        return createTransactionInstruction(PROGRAM_ID, Collections.emptyList(), transactionData);
    }

    /**
     * Creates an instruction to set the loaded accounts data size limit.
     *
     * @param bytes The desired maximum loaded accounts data size in bytes.
     * @return A TransactionInstruction to set the loaded accounts data size limit.
     */
    public static TransactionInstruction setLoadedAccountsDataSizeLimit(int bytes) {
        byte[] transactionData = encodeSetLoadedAccountsDataSizeLimitTransaction(bytes);
        return createTransactionInstruction(PROGRAM_ID, Collections.emptyList(), transactionData);
    }

    /**
     * Creates an instruction to request a heap frame.
     *
     * @param bytes The desired heap frame size in bytes.
     * @return A TransactionInstruction to request a heap frame.
     */
    public static TransactionInstruction requestHeapFrame(int bytes) {
        byte[] data = new byte[]{REQUEST_HEAP_FRAME, (byte) (bytes & 0xFF), (byte) ((bytes >> 8) & 0xFF), (byte) ((bytes >> 16) & 0xFF), (byte) ((bytes >> 24) & 0xFF)};
        return new TransactionInstruction(PROGRAM_ID, new ArrayList<>(), data);
    }

    private static byte[] encodeSetComputeUnitPriceTransaction(int microLamports) {
        ByteBuffer result = ByteBuffer.allocate(9);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(SET_COMPUTE_UNIT_PRICE);
        result.putLong(microLamports);

        return result.array();
    }

    private static byte[] encodeSetComputeUnitLimitTransaction(int units) {
        ByteBuffer result = ByteBuffer.allocate(5);
        result.order(ByteOrder.LITTLE_ENDIAN);

        result.put(SET_COMPUTE_UNIT_LIMIT);
        result.putInt(units);

        return result.array();
    }

    private static byte[] encodeSetLoadedAccountsDataSizeLimitTransaction(int bytes) {
        ByteBuffer result = ByteBuffer.allocate(5);
        result.order(ByteOrder.LITTLE_ENDIAN);
        result.put(SET_LOADED_ACCOUNTS_DATA_SIZE_LIMIT);
        result.putInt(bytes);
        return result.array();
    }

}
