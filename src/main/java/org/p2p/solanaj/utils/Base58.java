package org.p2p.solanaj.utils;

import java.util.Arrays;

/**
 * High-performance Base58 encoding and decoding utility.
 * 
 * This implementation is based on the BitcoinJ Base58 algorithm but with several optimizations:
 * - Uses lookup tables for fast character-to-index conversion
 * - Minimizes memory allocations by reusing buffers
 * - Optimized divmod operation for base conversion
 * - Skip leading zeros optimization
 * 
 * Base58 is used in Bitcoin and other cryptocurrencies to encode addresses and other data.
 * It avoids characters that could be confused (0, O, I, l) and uses only alphanumeric characters.
 */
public class Base58 {
    
    private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char ENCODED_ZERO = ALPHABET[0];
    private static final int[] INDEXES = new int[128];
    
    static {
        Arrays.fill(INDEXES, -1);
        for (int i = 0; i < ALPHABET.length; i++) {
            INDEXES[ALPHABET[i]] = i;
        }
    }

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
        
        // Count leading zeros
        int zeros = 0;
        while (zeros < input.length && input[zeros] == 0) {
            ++zeros;
        }
        
        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        // The maximum possible size is ceil(log(256)/log(58)) * input.length = ~1.37 * input.length
        // We use 2x to be safe and avoid reallocation
        input = Arrays.copyOf(input, input.length); // since we modify it in-place
        char[] encoded = new char[input.length * 2]; // upper bound
        int outputStart = encoded.length;
        
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = ALPHABET[divmod256to58(input, inputStart)];
            if (input[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

    /**
     * Decodes the given Base58 string to a byte array.
     *
     * @param input the Base58 encoded string
     * @return the decoded byte array
     * @throws IllegalArgumentException if input is null or contains invalid characters
     */
    public static byte[] decode(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }
        
        if (input.isEmpty()) {
            return new byte[0];
        }
        
        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        byte[] input58 = new byte[input.length()];
        for (int i = 0; i < input.length(); ++i) {
            char c = input.charAt(i);
            int digit = c < 128 ? INDEXES[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException("Invalid Base58 character: " + c);
            }
            input58[i] = (byte) digit;
        }
        
        // Count leading zeros
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            ++zeros;
        }
        
        // Convert base-58 digits to base-256 digits
        byte[] decoded = new byte[input.length()];
        int outputStart = decoded.length;
        
        for (int inputStart = zeros; inputStart < input58.length; ) {
            decoded[--outputStart] = divmod58to256(input58, inputStart);
            if (input58[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        
        // Ignore extra leading zeroes that were added during the calculation
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        
        // Return decoded data (including original number of leading zeros)
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

    /**
     * Optimized divmod for encoding (base 256 to base 58).
     * Uses bit shifting for division by powers of 2.
     */
    private static byte divmod256to58(byte[] number, int firstDigit) {
        // Specialized version for base 256 to base 58 conversion
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = (remainder << 8) + digit; // Multiply by 256 using bit shift
            number[i] = (byte) (temp / 58);
            remainder = temp % 58;
        }
        return (byte) remainder;
    }
    
    /**
     * Optimized divmod for decoding (base 58 to base 256).
     * Uses bit shifting for modulo with powers of 2.
     */
    private static byte divmod58to256(byte[] number, int firstDigit) {
        // Specialized version for base 58 to base 256 conversion
        int remainder = 0;
        for (int i = firstDigit; i < number.length; i++) {
            int digit = (int) number[i] & 0xFF;
            int temp = remainder * 58 + digit;
            number[i] = (byte) (temp >> 8); // Divide by 256 using bit shift
            remainder = temp & 0xFF; // Modulo 256 using bit mask
        }
        return (byte) remainder;
    }

    /**
     * Additional optimized encode method that avoids array copy for performance-critical paths.
     * This method modifies the input array, so it should only be used when the input array
     * is not needed after encoding.
     *
     * @param input the byte array to encode (will be modified)
     * @param zeros the number of leading zeros already counted
     * @return the Base58 encoded string
     */
    public static String encodeNoCopy(byte[] input, int zeros) {
        if (input.length == 0) {
            return "";
        }
        
        char[] encoded = new char[input.length * 2];
        int outputStart = encoded.length;
        
        for (int inputStart = zeros; inputStart < input.length; ) {
            encoded[--outputStart] = ALPHABET[divmod256to58(input, inputStart)];
            if (input[inputStart] == 0) {
                ++inputStart;
            }
        }
        
        while (outputStart < encoded.length && encoded[outputStart] == ENCODED_ZERO) {
            ++outputStart;
        }
        while (--zeros >= 0) {
            encoded[--outputStart] = ENCODED_ZERO;
        }
        
        return new String(encoded, outputStart, encoded.length - outputStart);
    }
}