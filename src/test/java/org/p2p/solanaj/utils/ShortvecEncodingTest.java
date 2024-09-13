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
        assertArrayEquals(new byte[] { -1, -1, 1 } /* [0xff, 0xff, 0x01] */, ShortvecEncoding.encodeLength(32767)); // 0x7fff
        assertArrayEquals(new byte[] { -128, -128, -128, 1 } /* [0x80, 0x80, 0x80, 0x01] */,
                ShortvecEncoding.encodeLength(2097152)); // 0x200000
    }
}
