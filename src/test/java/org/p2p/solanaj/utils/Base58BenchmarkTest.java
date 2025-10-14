package org.p2p.solanaj.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Random;

/**
 * Performance benchmark for Base58 encoding/decoding.
 * This test is disabled by default as it's a benchmark, not a unit test.
 * Run it manually to see performance characteristics.
 */
@Disabled("This is a benchmark test, run manually")
public class Base58BenchmarkTest {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;
    
    @Test
    public void benchmarkBase58Performance() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Test with different data sizes
        int[] sizes = {32, 64, 128, 256, 512, 1024};
        
        for (int size : sizes) {
            System.out.println("\n=== Testing with " + size + " byte data ===");
            
            // Generate test data
            byte[][] testData = new byte[BENCHMARK_ITERATIONS][];
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                testData[i] = new byte[size];
                random.nextBytes(testData[i]);
            }
            
            // Warmup
            for (int i = 0; i < WARMUP_ITERATIONS; i++) {
                String encoded = Base58.encode(testData[i % testData.length]);
                Base58.decode(encoded);
            }
            
            // Benchmark encoding
            long encodeStart = System.nanoTime();
            String[] encoded = new String[BENCHMARK_ITERATIONS];
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                encoded[i] = Base58.encode(testData[i]);
            }
            long encodeTime = System.nanoTime() - encodeStart;
            
            // Benchmark decoding
            long decodeStart = System.nanoTime();
            for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
                Base58.decode(encoded[i]);
            }
            long decodeTime = System.nanoTime() - decodeStart;
            
            // Print results
            System.out.printf("Encode: %.2f µs/op (%.2f MB/s)\n", 
                encodeTime / 1000.0 / BENCHMARK_ITERATIONS,
                (size * BENCHMARK_ITERATIONS * 1000.0) / encodeTime);
            System.out.printf("Decode: %.2f µs/op (%.2f MB/s)\n", 
                decodeTime / 1000.0 / BENCHMARK_ITERATIONS,
                (size * BENCHMARK_ITERATIONS * 1000.0) / decodeTime);
        }
        
        // Test edge cases
        System.out.println("\n=== Edge Case Performance ===");
        
        // All zeros (worst case for encoding)
        byte[] allZeros = new byte[256];
        long start = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            Base58.encode(allZeros);
        }
        long time = System.nanoTime() - start;
        System.out.printf("All zeros encode: %.2f µs/op\n", time / 1000.0 / BENCHMARK_ITERATIONS);
        
        // All 0xFF (best case for encoding)
        byte[] allFF = new byte[256];
        for (int i = 0; i < allFF.length; i++) {
            allFF[i] = (byte) 0xFF;
        }
        start = System.nanoTime();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            Base58.encode(allFF);
        }
        time = System.nanoTime() - start;
        System.out.printf("All 0xFF encode: %.2f µs/op\n", time / 1000.0 / BENCHMARK_ITERATIONS);
    }
}
