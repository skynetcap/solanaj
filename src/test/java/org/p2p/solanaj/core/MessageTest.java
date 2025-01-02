package org.p2p.solanaj.core;

import org.bitcoinj.core.Base58;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.p2p.solanaj.programs.SystemProgram;
import org.p2p.solanaj.utils.ByteUtils;

import java.util.Arrays;
import java.util.List;

public class MessageTest {
    Account signer = new Account(Base58
            .decode("4Z7cXSyeFR8wNGMVXUE1TwtKn5D5Vu7FzEv69dokLv7KrQk7h6pu4LF8ZRR9yQBhc7uSM6RTTZtU1fmaxiNrxXrs"));

    private TransactionInstruction transfer(){
        PublicKey fromPublicKey = new PublicKey("QqCCvshxtqMAL2CVALqiJB7uEeE5mjSPsseQdDzsRUo");
        PublicKey toPublickKey = new PublicKey("GrDMoeqMLFjeXQ24H56S1RLgT4R76jsuWCd6SvXyGPQ5");
        int lamports = 3000;

        return SystemProgram.transfer(fromPublicKey, toPublickKey, lamports);
    }

    @Test
    public void serializeMessage() {
        Message message = new Message();
        message.setFeePayer(signer.getPublicKey());
        message.addInstruction(transfer());
        message.setRecentBlockHash("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn");

        assertArrayEquals(new int[] { 1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
                223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
                235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
                188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
                104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
                0, 0, 0, 0, 0 }, toUnsignedByteArray(message.serialize()));
    }

    @Test
    public void deserialize(){
        int[] serialize = new int[] { 1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
                223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
                235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
                188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
                104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
                0, 0, 0, 0, 0 };
        List<Byte> bytes = ByteUtils.toByteList(intToByteArray(serialize));
        Message message = Message.deserialize(bytes);
        assertEquals("Eit7RCyhUixAe2hGBS8oqnw59QK3kgMMjfLME5bm9wRn", message.getRecentBlockhash());
//        assertArrayEquals(new int[]{1, 0, 1}, toUnsignedByteArray(message.getMessageHeader().toByteArray()));
        assertEquals(1, message.getInstructions().size());

        TransactionInstruction transfer = transfer();
        TransactionInstruction transferNew = message.getInstructions().get(0);
        assertEquals(transfer.getProgramId(), transferNew.getProgramId());
        assertArrayEquals(transfer.getData(), transferNew.getData());

        for (int i = 0; i < transfer.getKeys().size(); i++) {
            assertEquals(transfer.getKeys().get(i).getPublicKey().toBase58(), transferNew.getKeys().get(i).getPublicKey().toBase58());
        }
        int[] serializeNew = toUnsignedByteArray(message.serialize());
        System.out.println(Arrays.toString(serializeNew));

        assertArrayEquals(serialize, serializeNew);
    }


    @Test
    public void int_byte_convert(){
        int[] serialize = new int[] { 1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
                223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
                235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
                188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
                104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
                0, 0, 0, 0, 0 };

        byte[] bytes = intToByteArray(serialize);

        int[] serializeNew = toUnsignedByteArray(bytes);

        byte[] bytesNew = intToByteArray(serializeNew);

        assertArrayEquals(serialize, serializeNew);
        assertArrayEquals(bytes, bytesNew);
    }

    int[] toUnsignedByteArray(byte[] in) {
        int[] out = new int[in.length];

        for (int i = 0; i < in.length; i++) {
            out[i] = in[i] & 0xff;
        }

        return out;
    }

    public static byte[] intToByteArray(int[] in) {
        byte[] out = new byte[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (byte) (in[i] & 0xff);
        }
        return out;
    }

}
