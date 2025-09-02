package org.p2p.solanaj.programs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import org.p2p.solanaj.core.AccountMeta;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.util.List;

public class AssociatedTokenProgramTest {

    // Using real Solana addresses
    private static final PublicKey FUNDING_ACCOUNT = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
    private static final PublicKey WALLET_ADDRESS = new PublicKey("6sbzC1eH4FTujJXWj51eQe25cYvr4xfXbJ1vAj7j2k5J");
    private static final PublicKey MINT = new PublicKey("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"); // USDC mint
    private static final PublicKey NESTED_ACCOUNT = new PublicKey("4zMMC9srt5Ri5X14GAgXhaHii3GnPAEERYPJgZJDncDU");
    private static final PublicKey NESTED_MINT = new PublicKey("So11111111111111111111111111111111111111112"); // Wrapped SOL mint
    private static final PublicKey DESTINATION_ACCOUNT = new PublicKey("5omQJtDUHA3gMFdHEQg1zZSvcBUVzey5WaKWYRmqF1Vj");
    private static final PublicKey OWNER_ACCOUNT = new PublicKey("7UX2i7SucgLMQcfZ75s3VXmZZY4YRUyJN9X1RgfMoDUi");
    private static final PublicKey OWNER_MINT = new PublicKey("mSoLzYCxHdYgdzU16g5QSh3i5K3z3KZK7ytfqcJm7So"); // Marinade staked SOL mint

    @Test
    public void testCreate() {
        TransactionInstruction instruction = AssociatedTokenProgram.create(FUNDING_ACCOUNT, WALLET_ADDRESS, MINT, TokenProgram.PROGRAM_ID);

        assertEquals(AssociatedTokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(6, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(0, instruction.getData()[0]);

        verifyCommonAccountMetas(instruction.getKeys());
    }

    @Test
    public void testCreateIdempotent() {
        TransactionInstruction instruction = AssociatedTokenProgram.createIdempotent(FUNDING_ACCOUNT, WALLET_ADDRESS, MINT, TokenProgram.PROGRAM_ID);

        assertEquals(AssociatedTokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(6, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(1, instruction.getData()[0]);

        verifyCommonAccountMetas(instruction.getKeys());
    }

    @Test
    public void testRecoverNested() {
        TransactionInstruction instruction = AssociatedTokenProgram.recoverNested(
                NESTED_ACCOUNT, NESTED_MINT, DESTINATION_ACCOUNT, OWNER_ACCOUNT, OWNER_MINT, WALLET_ADDRESS, TokenProgram.PROGRAM_ID);

        assertEquals(AssociatedTokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(7, instruction.getKeys().size());
        assertEquals(1, instruction.getData().length);
        assertEquals(2, instruction.getData()[0]);

        List<AccountMeta> keys = instruction.getKeys();
        assertEquals(NESTED_ACCOUNT, keys.get(0).getPublicKey());
        assertEquals(NESTED_MINT, keys.get(1).getPublicKey());
        assertEquals(DESTINATION_ACCOUNT, keys.get(2).getPublicKey());
        assertEquals(OWNER_ACCOUNT, keys.get(3).getPublicKey());
        assertEquals(OWNER_MINT, keys.get(4).getPublicKey());
        assertEquals(WALLET_ADDRESS, keys.get(5).getPublicKey());
        assertEquals(TokenProgram.PROGRAM_ID, keys.get(6).getPublicKey());
    }

    private void verifyCommonAccountMetas(List<AccountMeta> keys) {
        assertEquals(FUNDING_ACCOUNT, keys.get(0).getPublicKey());
        assertTrue(keys.get(0).isSigner());
        assertTrue(keys.get(0).isWritable());

        // PDA account
        assertFalse(keys.get(1).isSigner());
        assertTrue(keys.get(1).isWritable());

        assertEquals(WALLET_ADDRESS, keys.get(2).getPublicKey());
        assertFalse(keys.get(2).isSigner());
        assertFalse(keys.get(2).isWritable());

        assertEquals(MINT, keys.get(3).getPublicKey());
        assertFalse(keys.get(3).isSigner());
        assertFalse(keys.get(3).isWritable());

        assertEquals(SystemProgram.PROGRAM_ID, keys.get(4).getPublicKey());
        assertFalse(keys.get(4).isSigner());
        assertFalse(keys.get(4).isWritable());

        assertEquals(TokenProgram.PROGRAM_ID, keys.get(5).getPublicKey());
        assertFalse(keys.get(5).isSigner());
        assertFalse(keys.get(5).isWritable());
    }
}