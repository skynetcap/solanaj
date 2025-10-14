package org.p2p.solanaj.utils;


import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class ByteUtils {
    public static final int UINT_32_LENGTH = 4;
    public static final int UINT_64_LENGTH = 8;
    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] readBytes(byte[] buf, int offset, int length) {
        byte[] b = new byte[length];
        System.arraycopy(buf, offset, b, 0, length);
        return b;
    }

    public static BigInteger readUint64(byte[] buf, int offset) {
        return new BigInteger(reverseBytes(readBytes(buf, offset, UINT_64_LENGTH)));
    }

    public static byte[] reverseBytes(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - 1 - i];
        }
        return reversed;
    }

    public static BigInteger readUint64Price(byte[] buf, int offset) {
        return new BigInteger(readBytes(buf, offset, UINT_64_LENGTH));
    }

    public static void uint64ToByteStreamLE(BigInteger val, OutputStream stream) throws IOException {
        byte[] bytes = val.toByteArray();
        if (bytes.length > 8) {
            if (bytes[0] == 0) {
                bytes = readBytes(bytes, 1, bytes.length - 1);
            } else {
                throw new RuntimeException("Input too large to encode into a uint64");
            }
        }
        bytes = reverseBytes(bytes);
        stream.write(bytes);
        if (bytes.length < 8) {
            for (int i = 0; i < 8 - bytes.length; i++)
                stream.write(0);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] trim(byte[] bytes)
    {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] == 0)
        {
            --i;
        }

        return Arrays.copyOf(bytes, i + 1);
    }

    public static int getBit(byte[] data, int pos) {
        int posByte = pos/8;
        int posBit = pos%8;
        byte valByte = data[posByte];
        int valInt = (valByte >> posBit) & 1;
        return valInt;
    }


    public static int readUint16(ByteBuffer buf) {
        return Short.toUnsignedInt(buf.order(ByteOrder.LITTLE_ENDIAN).getShort());
    }

    public static int readUint16(byte[] bytes, int offset) {
        return readUint16(ByteBuffer.wrap(bytes, offset, bytes.length - offset));
    }

}
