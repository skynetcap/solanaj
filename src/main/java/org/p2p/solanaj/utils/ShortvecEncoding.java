package org.p2p.solanaj.utils;

/**
 * Utility class for short vector encoding as per Solana's requirements.
 */
public class ShortvecEncoding {

    /**
     * Encodes a length using Solana's short vector encoding.
     *
     * @param length The length to encode
     * @return The encoded byte array
     */
    public static byte[] encodeLength(int length) {
        byte[] buffer = new byte[5];
        int i = 0;
        while (length > 127) {
            buffer[i++] = (byte) ((length & 0x7F) | 0x80);
            length >>>= 7;
        }
        buffer[i++] = (byte) (length & 0x7F);
        byte[] result = new byte[i];
        System.arraycopy(buffer, 0, result, 0, i);
        return result;
    }

    /**
     * Decodes a short vector encoded length.
     *
     * @param bytes The byte array containing the encoded length
     * @return The decoded length
     */
    public static int decodeLength(byte[] bytes) {
        int length = 0;
        int shift = 0;
        for (byte b : bytes) {
            length |= (b & 0x7F) << shift;
            if ((b & 0x80) == 0) {
                break;
            }
            shift += 7;
        }
        return length;
    }
}