package me.egg82.comptests.tests.generic;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public interface ByteTest {
    long getCompressionTime(TimeUnit unit);
    long getDecompressionTime(TimeUnit unit);
    double getCompressionRatio();

    void testCompress(byte[] decompressedData, int iterations) throws IOException;
    void testDecompress(byte[] compressedData, int iterations) throws IOException;

    byte[] getDecompressedData(byte[] compressedData) throws IOException;
    byte[] getCompressedData(byte[] decompressedData) throws IOException;
}
