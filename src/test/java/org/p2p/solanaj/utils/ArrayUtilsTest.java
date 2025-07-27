package org.p2p.solanaj.utils;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArrayUtilsTest {

    @Test
    public void guardedSplice(){
        int[] serialize = new int[] { 1, 0, 1, 3, 6, 26, 217, 208, 83, 135, 21, 72, 83, 126, 222, 62, 38, 24, 73, 163,
                223, 183, 253, 2, 250, 188, 117, 178, 35, 200, 228, 106, 219, 133, 61, 12, 235, 122, 188, 208, 216, 117,
                235, 194, 109, 161, 177, 129, 163, 51, 155, 62, 242, 163, 22, 149, 187, 122, 189, 188, 103, 130, 115,
                188, 173, 205, 229, 170, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 203, 226, 136, 193, 153, 148, 240, 50, 230, 98, 9, 79, 221, 179, 243, 174, 90, 67,
                104, 169, 6, 187, 165, 72, 36, 156, 19, 57, 132, 38, 69, 245, 1, 2, 2, 0, 1, 12, 2, 0, 0, 0, 184, 11, 0,
                0, 0, 0, 0, 0 };
        List<Byte> bytes = ByteUtils.toByteList(ByteUtils.intToByteArray(serialize));

        byte[] splice = ArrayUtils.guardedSplice(bytes, 1, 5);
        System.out.println(Arrays.toString(splice));

        assertEquals(bytes.size(), serialize.length - 5);
        assertArrayEquals(splice, new byte[]{0, 1, 3, 6, 26});

    }


}
