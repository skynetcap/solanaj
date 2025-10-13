package org.p2p.solanaj.programs;

import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.utils.Base58Utils;

public class SystemProgramTest {

    @Test
    public void testTransferInstruction() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        long lamports = 3000;

        TransactionInstruction instruction = SystemProgram.transfer(fromPublicKey, toPublicKey, lamports);

        assertEquals(SystemProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());
        assertEquals(fromPublicKey, instruction.getKeys().get(0).getPublicKey());
        assertTrue(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertEquals(toPublicKey, instruction.getKeys().get(1).getPublicKey());
        assertFalse(instruction.getKeys().get(1).isSigner());
        assertTrue(instruction.getKeys().get(1).isWritable());

        byte[] expectedData = new byte[]{2, 0, 0, 0, -72, 11, 0, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedData, instruction.getData());
    }

    @Test
    public void testTransferInstructionWithNegativeLamports() {
        assertThrows(IllegalArgumentException.class, () -> {
            PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
            PublicKey toPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
            long negativeLamports = -1;

            SystemProgram.transfer(fromPublicKey, toPublicKey, negativeLamports);
        });
    }

    @Test
    public void testCreateAccountInstruction() {
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey newAccountPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        long lamports = 2039280;
        long space = 165;
        PublicKey programId = SystemProgram.PROGRAM_ID;

        TransactionInstruction instruction = SystemProgram.createAccount(fromPublicKey, newAccountPublicKey, lamports, space, programId);

        assertEquals(SystemProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());
        assertEquals(fromPublicKey, instruction.getKeys().get(0).getPublicKey());
        assertTrue(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertEquals(newAccountPublicKey, instruction.getKeys().get(1).getPublicKey());
        assertTrue(instruction.getKeys().get(1).isSigner());
        assertTrue(instruction.getKeys().get(1).isWritable());

        String expectedDataBase58 = "11119os1e9qSs2u7TsThXqkBSRUo9x7kpbdqtNNbTeaxHGPdWbvoHsks9hpp6mb2ed1NeB";
        assertEquals(expectedDataBase58, Base58Utils.encode(instruction.getData()));
    }

    @Test
    public void testCreateAccountInstructionWithNegativeLamports() {
        assertThrows(IllegalArgumentException.class, () -> {
            PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
            PublicKey newAccountPublicKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
            long negativeLamports = -1;
            long space = 165;
            PublicKey programId = SystemProgram.PROGRAM_ID;
    
            SystemProgram.createAccount(fromPublicKey, newAccountPublicKey, negativeLamports, space, programId);    
        });
    }

    @Test
    public void testAssignInstruction() {
        PublicKey owner = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey newOwner = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");

        TransactionInstruction instruction = SystemProgram.assign(owner, newOwner);

        assertEquals(SystemProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(1, instruction.getKeys().size());
        assertEquals(owner, instruction.getKeys().get(0).getPublicKey());
        assertTrue(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());

        byte[] expectedData = new byte[36];
        expectedData[0] = 1; // PROGRAM_INDEX_ASSIGN
        System.arraycopy(newOwner.toByteArray(), 0, expectedData, 4, 32);
        assertArrayEquals(expectedData, instruction.getData());
    }
}
