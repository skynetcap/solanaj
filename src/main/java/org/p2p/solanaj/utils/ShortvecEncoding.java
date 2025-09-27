package org.p2p.solanaj.utils;

import java.util.List;

import static org.bitcoinj.core.Utils.uint16ToByteArrayLE;

public class ShortvecEncoding {

    public static byte[] encodeLength(int len) {
        byte[] out = new byte[10];
        int remLen = len;
        int cursor = 0;

        for (;;) {
            int elem = remLen & 0x7f;
            remLen >>= 7;
            if (remLen == 0) {
                uint16ToByteArrayLE(elem, out, cursor);
                break;
            } else {
                elem |= 0x80;
                uint16ToByteArrayLE(elem, out, cursor);
                cursor += 1;
            }
        }

        byte[] bytes = new byte[cursor + 1];
        System.arraycopy(out, 0, bytes, 0, cursor + 1);

        return bytes;
    }

    public static int decodeLength(List<Byte> dataBytesList) {
        int len = 0;
        int size = 0;

        for (;;) {
            int elem = (int) dataBytesList.remove(0);
            len |= (elem & 0x7f) << (size * 7);
            size += 1;
            if ((elem & 0x80) == 0) {
                break;
            }
        }
        return len;
    }

}
