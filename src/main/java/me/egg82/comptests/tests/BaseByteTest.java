package me.egg82.comptests.tests;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public abstract class BaseByteTest implements ByteTest {
    private long compressionTime = -1L;
    private long decompressionTime = -1L;
    private double compressionRatio = -1.0d;

    public final long getCompressionTime(TimeUnit unit) { return unit.convert(compressionTime, TimeUnit.NANOSECONDS); }

    public final long getDecompressionTime(TimeUnit unit) { return unit.convert(decompressionTime, TimeUnit.NANOSECONDS); }

    public final double getCompressionRatio() { return compressionRatio; }

    public final void testCompress(byte[] decompressedData, int iterations) throws IOException {
        long totalTime = 0L;
        long totalSize = 0L;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            totalSize += compress(decompressedData);
            long end = System.nanoTime();
            totalTime += end - start;
        }

        compressionTime = totalTime / iterations;
        if (totalSize > 0) {
            compressionRatio = (double) decompressedData.length / ((double) totalSize / (double) iterations);
        }
    }

    protected abstract long compress(byte[] decompressedData) throws IOException;

    public final void testDecompress(byte[] decompressedData, int iterations) throws IOException {
        long totalTime = 0L;
        for (int i = 0; i < iterations; i++) {
            long start = System.nanoTime();
            decompress(decompressedData);
            long end = System.nanoTime();
            totalTime += end - start;
        }

        decompressionTime = totalTime / iterations;
    }

    protected abstract void decompress(byte[] compressedData) throws IOException;
}
