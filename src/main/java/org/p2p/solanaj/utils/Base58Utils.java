package org.p2p.solanaj.utils;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Modern Base58 encoding and decoding utility.
 * This replaces the bitcoinj Base58 implementation with a custom, actively maintained implementation.
 * 
 * Base58 is used in Bitcoin and other cryptocurrencies to encode addresses and other data.
 * It avoids characters that could be confused (0, O, I, l) and uses only alphanumeric characters.
 */
public class Base58Utils {
    
    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger BASE = BigInteger.valueOf(58);
    private static final char[] ALPHABET_ARRAY = ALPHABET.toCharArray();

    /**
     * Encodes the given byte array to a Base58 string.
     *
     * @param input the byte array to encode
     * @return the Base58 encoded string
     * @throws IllegalArgumentException if input is null
     */
    public static String encode(byte[] input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        if (input.length == 0) {
            return "";
        }

        // Convert to BigInteger (treating as unsigned)
        BigInteger num = new BigInteger(1, input);
        StringBuilder encoded = new StringBuilder();

        // Convert to base 58
        while (num.compareTo(BigInteger.ZERO) > 0) {
            BigInteger[] divmod = num.divideAndRemainder(BASE);
            num = divmod[0];
            encoded.insert(0, ALPHABET_ARRAY[divmod[1].intValue()]);
        }

        // Handle leading zeros (they become '1' in Base58)
        for (byte b : input) {
            if (b == 0) {
                encoded.insert(0, ALPHABET_ARRAY[0]);
            } else {
                break;
            }
        }

        return encoded.toString();
    }

    /**
     * Decodes the given Base58 string to a byte array.
     *
     * @param input the Base58 encoded string
     * @return the decoded byte array
     * @throws IllegalArgumentException if input is null, empty, or contains invalid characters
     */
    public static byte[] decode(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        if (input.isEmpty()) {
            return new byte[0];
        }

        BigInteger num = BigInteger.ZERO;
        BigInteger multiplier = BigInteger.ONE;

        // Process string from right to left
        for (int i = input.length() - 1; i >= 0; i--) {
            char c = input.charAt(i);
            int index = ALPHABET.indexOf(c);
            
            if (index == -1) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            
            num = num.add(multiplier.multiply(BigInteger.valueOf(index)));
            multiplier = multiplier.multiply(BASE);
        }

        // Convert BigInteger to byte array
        byte[] decoded = num.toByteArray();
        
        // Remove leading zero if present (BigInteger adds it for positive numbers)
        if (decoded.length > 0 && decoded[0] == 0) {
            decoded = Arrays.copyOfRange(decoded, 1, decoded.length);
        }

        // Handle leading zeros from original input
        int leadingZeros = 0;
        for (char c : input.toCharArray()) {
            if (c == ALPHABET_ARRAY[0]) {
                leadingZeros++;
            } else {
                break;
            }
        }

        if (leadingZeros > 0) {
            byte[] result = new byte[leadingZeros + decoded.length];
            System.arraycopy(decoded, 0, result, leadingZeros, decoded.length);
            return result;
        }

        return decoded;
    }
}
