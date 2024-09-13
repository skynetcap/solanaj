package org.p2p.solanaj.programs;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import static org.junit.jupiter.api.Assertions.*;
import org.p2p.solanaj.core.PublicKey;
import org.p2p.solanaj.core.TransactionInstruction;

import java.nio.charset.StandardCharsets;

public class MemoProgramTest {

    @Test
    public void testWriteUtf8_ValidInput() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        String memo = "Test memo";

        TransactionInstruction instruction = MemoProgram.writeUtf8(account, memo);

        assertNotNull(instruction);
        assertEquals(MemoProgram.PROGRAM_ID, instruction.getProgramId());
        assertEquals(1, instruction.getKeys().size());
        assertEquals(account, instruction.getKeys().get(0).getPublicKey());
        assertTrue(instruction.getKeys().get(0).isSigner());
        assertFalse(instruction.getKeys().get(0).isWritable());
        assertArrayEquals(memo.getBytes(StandardCharsets.UTF_8), instruction.getData());
    }

    @Test
    public void testWriteUtf8_NullAccount() {
        assertThrows(IllegalArgumentException.class, () -> MemoProgram.writeUtf8(null, "Test memo"));
    }

    @Test
    public void testWriteUtf8_NullMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        assertThrows(IllegalArgumentException.class, () -> MemoProgram.writeUtf8(account, null));
    }

    @Test
    public void testWriteUtf8_EmptyMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        assertThrows(IllegalArgumentException.class, () -> MemoProgram.writeUtf8(account, ""));
    }

    @Test
    public void testWriteUtf8_LongMemo() {
        PublicKey account = new PublicKey("11111111111111111111111111111111");
        String longMemo = String.join("", java.util.Collections.nCopies(1000, "A"));

        TransactionInstruction instruction = MemoProgram.writeUtf8(account, longMemo);

        assertNotNull(instruction);
        assertEquals(MemoProgram.PROGRAM_ID, instruction.getProgramId());
        assertArrayEquals(longMemo.getBytes(StandardCharsets.UTF_8), instruction.getData());
    }
}
