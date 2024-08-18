package org.p2p.solanaj.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class ShortvecTest {

    @Test
    public void encodeLength() {
        assertArrayEquals(new byte[] { 0 } /* [0] */, Shortvec.encodeLength(0));
        assertArrayEquals(new byte[] { 1 } /* [1] */, Shortvec.encodeLength(1));
        assertArrayEquals(new byte[] { 5 } /* [5] */, Shortvec.encodeLength(5));
        assertArrayEquals(new byte[] { 127 } /* [0x7f] */, Shortvec.encodeLength(127)); // 0x7f
        assertArrayEquals(new byte[] { -128, 1 }/* [0x80, 0x01] */, Shortvec.encodeLength(128)); // 0x80
        assertArrayEquals(new byte[] { -1, 1 } /* [0xff, 0x01] */, Shortvec.encodeLength(255)); // 0xff
        assertArrayEquals(new byte[] { -128, 2 } /* [0x80, 0x02] */, Shortvec.encodeLength(256)); // 0x100
        assertArrayEquals(new byte[] { -1, -1, 1 } /* [0xff, 0xff, 0x01] */, Shortvec.encodeLength(32767)); // 0x7fff
        assertArrayEquals(new byte[] { -128, -128, -128, 1 } /* [0x80, 0x80, 0x80, 0x01] */,
                Shortvec.encodeLength(2097152)); // 0x200000
    }
}
