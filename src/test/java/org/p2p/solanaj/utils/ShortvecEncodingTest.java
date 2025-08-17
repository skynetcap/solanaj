package org.p2p.solanaj.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ShortvecEncodingTest {

    @Test
    public void encodeLength() {
        assertArrayEquals(new byte[] { 0 } /* [0] */, ShortvecEncoding.encodeLength(0));
        assertArrayEquals(new byte[] { 1 } /* [1] */, ShortvecEncoding.encodeLength(1));
        assertArrayEquals(new byte[] { 5 } /* [5] */, ShortvecEncoding.encodeLength(5));
        assertArrayEquals(new byte[] { 127 } /* [0x7f] */, ShortvecEncoding.encodeLength(127)); // 0x7f
        assertArrayEquals(new byte[] { -128, 1 }/* [0x80, 0x01] */, ShortvecEncoding.encodeLength(128)); // 0x80
        assertArrayEquals(new byte[] { -1, 1 } /* [0xff, 0x01] */, ShortvecEncoding.encodeLength(255)); // 0xff
        assertArrayEquals(new byte[] { -128, 2 } /* [0x80, 0x02] */, ShortvecEncoding.encodeLength(256)); // 0x100
        assertArrayEquals(new byte[]{ (byte) 0b10101100, (byte) 0b00000010}, ShortvecEncoding.encodeLength(300)); //
        assertArrayEquals(new byte[] { -1, -1, 1 } /* [0xff, 0xff, 0x01] */, ShortvecEncoding.encodeLength(32767)); // 0x7fff
        assertArrayEquals(new byte[] { -128, -128, -128, 1 } /* [0x80, 0x80, 0x80, 0x01] */,
                ShortvecEncoding.encodeLength(2097152)); // 0x200000
    }

    @Test
    public void decodeLength(){

//        assertEquals(0, ShortvecEncoding.decodeLength(new byte[]{0}));
//        assertEquals(1, ShortvecEncoding.decodeLength(new byte[]{1}));
//        assertEquals(5, ShortvecEncoding.decodeLength(new byte[]{5}));
//        assertEquals(127, ShortvecEncoding.decodeLength(new byte[]{127}));
//        assertEquals(128, ShortvecEncoding.decodeLength(new byte[]{-128, 1}));
//        assertEquals(255, ShortvecEncoding.decodeLength(new byte[]{-1, 1}));
//        assertEquals(256, ShortvecEncoding.decodeLength(new byte[]{-128, 2}));
//        assertEquals(32767, ShortvecEncoding.decodeLength(new byte[]{-1, -1, 1}));
//        assertEquals(2097152, ShortvecEncoding.decodeLength(new byte[]{-128, -128, -128, 1}));

    }
    @Test
    public void decodeLength2(){

        assertEquals(0, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{0})));
        assertEquals(1, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{1})));
        assertEquals(5, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{5})));
        assertEquals(127, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{127})));
        assertEquals(128, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{-128, 1})));
        assertEquals(255, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{-1, 1})));
        assertEquals(256, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{-128, 2})));
        assertEquals(300, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{ (byte) 0b10101100, (byte) 0b00000010})));
        assertEquals(32767, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{-1, -1, 1})));
        assertEquals(2097152, ShortvecEncoding.decodeLength(ByteUtils.toByteList(new byte[]{-128, -128, -128, 1})));

    }
}
