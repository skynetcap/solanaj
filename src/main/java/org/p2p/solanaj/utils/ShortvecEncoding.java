package org.p2p.solanaj.utils;


public class ShortvecEncoding {

    public static byte[] encodeLength(int len) {
        byte[] out = new byte[10];
        int remLen = len;
        int cursor = 0;

        for (;;) {
            int elem = remLen & 0x7f;
            remLen >>= 7;
            if (remLen == 0) {
                out[cursor] = (byte) elem;
                break;
            } else {
                elem |= 0x80;
                out[cursor] = (byte) elem;
                cursor += 1;
            }
        }

        byte[] bytes = new byte[cursor + 1];
        System.arraycopy(out, 0, bytes, 0, cursor + 1);

        return bytes;
    }
}
