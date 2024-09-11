package org.p2p.solanaj.programs;

import org.junit.Test;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.util.Collections;

import static org.junit.Assert.*;

public class AddressLookupTableProgramTest {

    private static final PublicKey AUTHORITY = new PublicKey("AuthorityPublicKeyHere");
    private static final PublicKey PAYER = new PublicKey("PayerPublicKeyHere");
    private static final PublicKey LOOKUP_TABLE = new PublicKey("LookupTablePublicKeyHere");
    private static final long RECENT_SLOT = 123456;

    /**
     * Test for creating a lookup table.
     */
    @Test
    public void testCreateLookupTable() {
        TransactionInstruction instruction = AddressLookupTableProgram.createLookupTable(AUTHORITY, PAYER, RECENT_SLOT);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size()); // Check number of keys
    }

    /**
     * Test for freezing a lookup table.
     */
    @Test
    public void testFreezeLookupTable() {
        TransactionInstruction instruction = AddressLookupTableProgram.freezeLookupTable(LOOKUP_TABLE, AUTHORITY);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size()); // Check number of keys
    }

    /**
     * Test for extending a lookup table.
     */
    @Test
    public void testExtendLookupTable() {
        PublicKey addressToAdd = new PublicKey("AddressToAddPublicKeyHere");
        TransactionInstruction instruction = AddressLookupTableProgram.extendLookupTable(LOOKUP_TABLE, AUTHORITY, PAYER, Collections.singletonList(addressToAdd));
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size()); // Check number of keys
    }

    /**
     * Test for deactivating a lookup table.
     */
    @Test
    public void testDeactivateLookupTable() {
        TransactionInstruction instruction = AddressLookupTableProgram.deactivateLookupTable(LOOKUP_TABLE, AUTHORITY);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size()); // Check number of keys
    }

    /**
     * Test for closing a lookup table.
     */
    @Test
    public void testCloseLookupTable() {
        PublicKey recipient = new PublicKey("RecipientPublicKeyHere");
        TransactionInstruction instruction = AddressLookupTableProgram.closeLookupTable(LOOKUP_TABLE, AUTHORITY, recipient);
        assertNotNull(instruction);
        assertEquals(AddressLookupTableProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size()); // Check number of keys
    }
}