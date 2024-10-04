package org.p2p.solanaj.programs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * Unit tests for AddressLookupTableProgram.
 */
public class AddressLookupTableProgramTest {

    /**
     * Test for creating a lookup table.
     */
    @Test
    public void testCreateLookupTable() {
        PublicKey authority = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey payer = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        long recentSlot = 123456789L;

        TransactionInstruction instruction = AddressLookupTableProgram.createLookupTable(authority, payer, recentSlot);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size()); // Check number of keys

        // Validate data
        byte[] expectedData = new byte[9];
        expectedData[0] = 0; // CREATE_LOOKUP_TABLE
        ByteBuffer buffer = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(recentSlot);
        System.arraycopy(buffer.array(), 0, expectedData, 1, 8);
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test for freezing a lookup table.
     */
    @Test
    public void testFreezeLookupTable() {
        PublicKey authority = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey lookupTable = new PublicKey("BPFLoaderUpgradeab1e11111111111111111111111");

        TransactionInstruction instruction = AddressLookupTableProgram.freezeLookupTable(lookupTable, authority);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size()); // Check number of keys

        // Validate data
        byte[] expectedData = new byte[]{1}; // FREEZE_LOOKUP_TABLE
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test for extending a lookup table.
     */
    @Test
    public void testExtendLookupTable() {
        PublicKey lookupTable = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
        PublicKey payer = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        PublicKey authority = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        List<PublicKey> addresses = List.of(
                new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5"),
                new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo")
        );

        TransactionInstruction instruction = AddressLookupTableProgram.extendLookupTable(lookupTable, payer, authority, addresses);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(1 + 4 + addresses.size() * 32, instruction.getData().length);

        // Validate data
        ByteBuffer data = ByteBuffer.wrap(instruction.getData()).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(2, data.get()); // EXTEND_LOOKUP_TABLE
        assertEquals(addresses.size(), data.getInt());

        for (PublicKey address : addresses) {
            byte[] addrBytes = new byte[32];
            data.get(addrBytes);
            assertArrayEquals(address.toByteArray(), addrBytes);
        }
    }

    /**
     * Test for deactivating a lookup table.
     */
    @Test
    public void testDeactivateLookupTable() {
        PublicKey lookupTable = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
        PublicKey authority = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");

        TransactionInstruction instruction = AddressLookupTableProgram.deactivateLookupTable(lookupTable, authority);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());

        // Validate data
        byte[] expectedData = new byte[]{3}; // DEACTIVATE_LOOKUP_TABLE
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Test for closing a lookup table.
     */
    @Test
    public void testCloseLookupTable() {
        PublicKey lookupTable = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
        PublicKey authority = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");
        PublicKey recipient = new PublicKey("skynetDj29GH6o6bAqoixCpDuYtWqi1rm8ZNx1hB3vq");

        TransactionInstruction instruction = AddressLookupTableProgram.closeLookupTable(lookupTable, authority, recipient);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());

        // Validate data
        byte[] expectedData = new byte[]{4}; // CLOSE_LOOKUP_TABLE
        assertArrayEquals(expectedData, instruction.getData());
    }
}