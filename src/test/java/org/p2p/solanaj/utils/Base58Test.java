package org.p2p.solanaj.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

public class Base58Test {

    @Test
    public void testEmptyInput() {
        assertEquals("", Base58.encode(new byte[0]));
        assertArrayEquals(new byte[0], Base58.decode(""));
    }

    @Test
    public void testSingleZeroByte() {
        assertEquals("1", Base58.encode(new byte[]{0}));
        assertArrayEquals(new byte[]{0}, Base58.decode("1"));
    }

    @Test
    public void testMultipleZeroBytes() {
        assertEquals("111", Base58.encode(new byte[]{0, 0, 0}));
        assertArrayEquals(new byte[]{0, 0, 0}, Base58.decode("111"));
    }

    @Test
    public void testLeadingZeros() {
        byte[] input = new byte[]{0, 0, 1, 2, 3};
        String encoded = Base58.encode(input);
        assertTrue(encoded.startsWith("11")); // Two leading zeros become two '1's
        assertArrayEquals(input, Base58.decode(encoded));
    }

    @Test
    public void testSimpleEncoding() {
        // Test "Hello World"
        byte[] input = "Hello World".getBytes(StandardCharsets.UTF_8);
        String encoded = Base58.encode(input);
        assertEquals("JxF12TrwUP45BMd", encoded);
        assertArrayEquals(input, Base58.decode(encoded));
    }

    @Test
    public void testKnownVectors() {
        // Test vectors from Bitcoin
        testEncodeDecode("", "");
        testEncodeDecode("61", "2g");
        testEncodeDecode("626262", "a3gV");
        testEncodeDecode("636363", "aPEr");
        testEncodeDecode("73696d706c792061206c6f6e6720737472696e67", "2cFupjhnEsSn59qHXstmK2ffpLv2");
        testEncodeDecode("00eb15231dfceb60925886b67d065299925915aeb172c06647", "1NS17iag9jJgTHD1VXjvLCEnZuQ3rJDE9L");
        testEncodeDecode("516b6fcd0f", "ABnLTmg");
        testEncodeDecode("bf4f89001e670274dd", "3SEo3LWLoPntC");
        testEncodeDecode("572e4794", "3EFU7m");
        testEncodeDecode("ecac89cad93923c02321", "EJDM8drfXA6uyA");
        testEncodeDecode("10c8511e", "Rt5zm");
        testEncodeDecode("00000000000000000000", "1111111111");
    }

    @Test
    public void testLargeData() {
        // Test with 1KB of random-ish data
        byte[] largeData = new byte[1024];
        for (int i = 0; i < largeData.length; i++) {
            largeData[i] = (byte) (i % 256);
        }
        String encoded = Base58.encode(largeData);
        assertArrayEquals(largeData, Base58.decode(encoded));
    }

    @Test
    public void testAllByteValues() {
        // Test encoding/decoding all possible byte values
        byte[] allBytes = new byte[256];
        for (int i = 0; i < 256; i++) {
            allBytes[i] = (byte) i;
        }
        String encoded = Base58.encode(allBytes);
        assertArrayEquals(allBytes, Base58.decode(encoded));
    }

    @Test
    public void testNullEncode() {
        assertThrows(IllegalArgumentException.class, () -> Base58.encode(null));
    }

    @Test
    public void testNullDecode() {
        assertThrows(IllegalArgumentException.class, () -> Base58.decode(null));
    }

    @Test
    public void testInvalidCharacter() {
        assertThrows(IllegalArgumentException.class, () -> Base58.decode("Invalid0Character")); // '0' is not in Base58 alphabet
    }

    @Test
    public void testInvalidCharacterO() {
        assertThrows(IllegalArgumentException.class, () -> Base58.decode("InvalidOCharacter")); // 'O' is not in Base58 alphabet
    }

    @Test
    public void testInvalidCharacterI() {
        assertThrows(IllegalArgumentException.class, () -> Base58.decode("InvalidICharacter")); // 'I' is not in Base58 alphabet
    }

    @Test
    public void testInvalidCharacterl() {
        assertThrows(IllegalArgumentException.class, () -> Base58.decode("Invalidlcharacter")); // 'l' (lowercase L) is not in Base58 alphabet
    }

    @Test
    public void testRoundTripRandomData() {
        // Test multiple round trips with random-ish data
        for (int size : new int[]{1, 5, 10, 32, 64, 128, 256, 512}) {
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = (byte) ((i * 31 + 17) % 256);
            }
            String encoded = Base58.encode(data);
            byte[] decoded = Base58.decode(encoded);
            assertArrayEquals(data, decoded, "Failed for size " + size);
        }
    }

    @Test
    public void testSolanaAddresses() {
        // Test some real Solana addresses
        String[] addresses = {
            "11111111111111111111111111111111",
            "So11111111111111111111111111111111111111112",
            "4Nd1mBQtrMJVYVfKf2PJy9NZUZdTAsp7D4xWLs4gDB4T",
            "EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v",
            "Es9vMFrzaCERmJfrF4H2FYD4KCoNkY11McCe8BenwNYB"
        };
        
        for (String address : addresses) {
            byte[] decoded = Base58.decode(address);
            String reencoded = Base58.encode(decoded);
            assertEquals(address, reencoded, "Failed for address " + address);
        }
    }

    private void testEncodeDecode(String hex, String expectedBase58) {
        byte[] bytes = hexToBytes(hex);
        String encoded = Base58.encode(bytes);
        assertEquals(expectedBase58, encoded);
        assertArrayEquals(bytes, Base58.decode(encoded));
    }

    private byte[] hexToBytes(String hex) {
        if (hex.isEmpty()) return new byte[0];
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i+1), 16));
        }
        return data;
    }
}
