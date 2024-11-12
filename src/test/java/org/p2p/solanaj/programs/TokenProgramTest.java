package org.p2p.solanaj.programs;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.util.Arrays;
import java.util.List;

/**
 * Test class for TokenProgram
 * 
 * These tests verify the correct creation of TransactionInstructions for various
 * SPL Token operations, including initialization, transfers, and account management.
 */
public class TokenProgramTest {

    /**
     * Tests the initializeMint instruction creation for TokenProgram.
     * This instruction is used to create a new SPL Token mint.
     */
    @Test
    public void testInitializeMint() {
        PublicKey mintPubkey = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        int decimals = 9;
        PublicKey mintAuthority = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey freezeAuthority = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");

        TransactionInstruction instruction = TokenProgram.initializeMint(mintPubkey, decimals, mintAuthority, freezeAuthority);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(2, instruction.getKeys().size());
        assertEquals(mintPubkey, instruction.getKeys().get(0).getPublicKey());
        assertFalse(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertEquals(TokenProgram.SYSVAR_RENT_PUBKEY, instruction.getKeys().get(1).getPublicKey());

        byte[] actualData = instruction.getData();
        assertEquals(67, actualData.length);
        assertEquals(0, actualData[0]);  // Instruction type: InitializeMint
        assertEquals(9, actualData[1]);  // Decimals
        // The next byte (actualData[2]) is part of the 64-byte authority data
        assertEquals(-35, actualData[2]); // First byte of authority data (unsigned: 221)
    }

    /**
     * Tests the initializeMultisig instruction creation for TokenProgram.
     * This instruction is used to create a new multisig account for SPL Tokens.
     */
    @Test
    public void testInitializeMultisig() {
        PublicKey multisigPubkey = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        List<PublicKey> signerPubkeys = Arrays.asList(
                new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT"),
                new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM")
        );
        int m = 2;

        TransactionInstruction instruction = TokenProgram.initializeMultisig(multisigPubkey, signerPubkeys, m);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(4, instruction.getKeys().size());
        assertEquals(multisigPubkey, instruction.getKeys().get(0).getPublicKey());
        assertFalse(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertEquals(TokenProgram.SYSVAR_RENT_PUBKEY, instruction.getKeys().get(1).getPublicKey());

        byte[] expectedData = new byte[]{2, 2};  // [Instruction type: InitializeMultisig, Number of signers (m)]
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Tests the approve instruction creation for TokenProgram.
     * This instruction is used to approve a delegate to transfer tokens from an account.
     */
    @Test
    public void testApprove() {
        PublicKey sourcePubkey = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey delegatePubkey = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey ownerPubkey = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");
        long amount = 1000000000;  // 1 billion (assuming 9 decimals)

        TransactionInstruction instruction = TokenProgram.approve(sourcePubkey, delegatePubkey, ownerPubkey, amount);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(sourcePubkey, instruction.getKeys().get(0).getPublicKey());
        assertFalse(instruction.getKeys().get(0).isSigner());
        assertTrue(instruction.getKeys().get(0).isWritable());
        assertEquals(delegatePubkey, instruction.getKeys().get(1).getPublicKey());
        assertFalse(instruction.getKeys().get(1).isSigner());
        assertFalse(instruction.getKeys().get(1).isWritable());
        assertEquals(ownerPubkey, instruction.getKeys().get(2).getPublicKey());
        assertTrue(instruction.getKeys().get(2).isSigner());
        assertFalse(instruction.getKeys().get(2).isWritable());

        byte[] actualData = instruction.getData();
        assertEquals(9, actualData.length);
        assertEquals(4, actualData[0]);  // Instruction type: Approve
        assertEquals(0, actualData[1]);  // First byte of amount (little-endian)
        assertEquals(-54, actualData[2]);  // Second byte of amount (unsigned: 202)
        // Full 8-byte representation of 1000000000: [0, 202, 154, 59, 0, 0, 0, 0]
    }

    /**
     * Tests the transfer instruction creation for TokenProgram.
     * This instruction is used to transfer tokens between accounts.
     */
    @Test
    public void testTransfer() {
        PublicKey source = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey destination = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        long amount = 1000000000;  // 1 billion (assuming 9 decimals)
        PublicKey owner = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");

        TransactionInstruction instruction = TokenProgram.transfer(source, destination, amount, owner);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(source, instruction.getKeys().get(0).getPublicKey());
        assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
        assertEquals(owner, instruction.getKeys().get(2).getPublicKey());

        byte[] actualData = instruction.getData();
        assertEquals(9, actualData.length);
        assertEquals(3, actualData[0]);  // Instruction type: Transfer
        assertEquals(0, actualData[1]);  // First byte of amount (little-endian)
        assertEquals(-54, actualData[2]);  // Second byte of amount (unsigned: 202)
        // Full 8-byte representation of 1000000000: [0, 202, 154, 59, 0, 0, 0, 0]
    }

    /**
     * Tests the burn instruction creation for TokenProgram.
     * This instruction is used to burn (destroy) tokens.
     */
    @Test
    public void testBurn() {
        PublicKey account = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey mint = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey owner = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");
        long amount = 500000000;  // 500 million (assuming 9 decimals)

        TransactionInstruction instruction = TokenProgram.burn(account, mint, owner, amount);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        assertEquals(owner, instruction.getKeys().get(2).getPublicKey());

        byte[] actualData = instruction.getData();
        assertEquals(9, actualData.length);
        assertEquals(8, actualData[0]);  // Instruction type: Burn
        assertEquals(0, actualData[1]);  // First byte of amount (little-endian)
        assertEquals(101, actualData[2]);  // Second byte of amount
        // Full 8-byte representation of 500000000: [0, 101, 205, 29, 0, 0, 0, 0]
    }

    /**
     * Tests the mintTo instruction creation for TokenProgram.
     * This instruction is used to mint new tokens to an account.
     */
    @Test
    public void testMintTo() {
        PublicKey mint = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey destination = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey authority = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");
        long amount = 750000000;  // 750 million (assuming 9 decimals)

        TransactionInstruction instruction = TokenProgram.mintTo(mint, destination, authority, amount);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(mint, instruction.getKeys().get(0).getPublicKey());
        assertEquals(destination, instruction.getKeys().get(1).getPublicKey());
        assertEquals(authority, instruction.getKeys().get(2).getPublicKey());

        byte[] actualData = instruction.getData();
        assertEquals(9, actualData.length);
        assertEquals(7, actualData[0]);  // Instruction type: MintTo
        assertEquals(-128, actualData[1]);  // First byte of amount (unsigned: 128)
        assertEquals(23, actualData[2]);  // Second byte of amount
        // Full 8-byte representation of 750000000: [128, 23, 223, 44, 0, 0, 0, 0]
    }

    /**
     * Tests the freezeAccount instruction creation for TokenProgram.
     * This instruction is used to freeze an account, preventing transfers.
     */
    @Test
    public void testFreezeAccount() {
        PublicKey account = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey mint = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey authority = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");

        TransactionInstruction instruction = TokenProgram.freezeAccount(account, mint, authority);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        assertEquals(authority, instruction.getKeys().get(2).getPublicKey());

        byte[] expectedData = new byte[]{0x0A};  // Instruction type: FreezeAccount
        assertArrayEquals(expectedData, instruction.getData());
    }

    /**
     * Tests the thawAccount instruction creation for TokenProgram.
     * This instruction is used to thaw a frozen account, allowing transfers.
     */
    @Test
    public void testThawAccount() {
        PublicKey account = new PublicKey("Gh9ZwEmdLJ8DscKNTkTqPbNwLNNBjuSzaG9Vp2KGtKJr");
        PublicKey mint = new PublicKey("FuLFkNQzNEAzZ2dEgXVUqVVLxJYLYhbSgpZf9RVVXZuT");
        PublicKey authority = new PublicKey("HNGVuL5kqjDehw7KR63w9gxow32sX6xzRNgLb8GkbwCM");

        TransactionInstruction instruction = TokenProgram.thawAccount(account, mint, authority);

        assertEquals(TokenProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(3, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        assertEquals(mint, instruction.getKeys().get(1).getPublicKey());
        assertEquals(authority, instruction.getKeys().get(2).getPublicKey());

        byte[] expectedData = new byte[]{0x0B};  // Instruction type: ThawAccount
        assertArrayEquals(expectedData, instruction.getData());
    }
}