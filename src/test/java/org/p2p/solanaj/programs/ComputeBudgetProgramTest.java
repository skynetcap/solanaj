package org.p2p.solanaj.programs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.p2p.solanaj.core.TransactionInstruction;

/**
 * Test class for ComputeBudgetProgram.
 * This class contains unit tests for various methods in the ComputeBudgetProgram class.
 */
public class ComputeBudgetProgramTest {

    /**
     * Test the setComputeUnitPrice method of ComputeBudgetProgram.
     * Verifies that the instruction is created correctly with the right program ID, keys, and data.
     */
    @Test
    public void testSetComputeUnitPrice() {
        int microLamports = 1000;
        TransactionInstruction instruction = ComputeBudgetProgram.setComputeUnitPrice(microLamports);

        assertEquals(ComputeBudgetProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(0, instruction.getKeys().size());

        byte[] expectedData = new byte[]{0x03, (byte) 0xE8, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test the setComputeUnitLimit method of ComputeBudgetProgram.
     * Verifies that the instruction is created correctly with the right program ID, keys, and data.
     */
    @Test
    public void testSetComputeUnitLimit() {
        int units = 200000;
        TransactionInstruction instruction = ComputeBudgetProgram.setComputeUnitLimit(units);

        assertEquals(ComputeBudgetProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(0, instruction.getKeys().size());

        byte[] expectedData = new byte[]{0x02, 0x40, 0x0D, 0x03, 0x00};
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test the requestHeapFrame method of ComputeBudgetProgram.
     * Verifies that the instruction is created correctly with the right program ID, keys, and data.
     */
    @Test
    public void testRequestHeapFrame() {
        int bytes = 32768;
        TransactionInstruction instruction = ComputeBudgetProgram.requestHeapFrame(bytes);

        assertEquals(ComputeBudgetProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(0, instruction.getKeys().size());

        byte[] expectedData = new byte[]{0x01, 0x00, (byte) 0x80, 0x00, 0x00};
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test the setLoadedAccountsDataSizeLimit method of ComputeBudgetProgram.
     * Verifies that the instruction is created correctly with the right program ID, keys, and data.
     */
    @Test
    public void testSetLoadedAccountsDataSizeLimit() {
        int bytes = 65536;
        TransactionInstruction instruction = ComputeBudgetProgram.setLoadedAccountsDataSizeLimit(bytes);

        assertEquals(ComputeBudgetProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(0, instruction.getKeys().size());

        byte[] expectedData = new byte[]{0x04, 0x00, 0x00, 0x01, 0x00};
        assertArrayEquals(expectedData, instruction.getData());
    }
}
