package me.egg82.comptests.tests.lz4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.egg82.comptests.tests.generic.BaseByteTest;
import net.jpountz.lz4.*;

public class LZ4Stream extends BaseByteTest {
    private final LZ4FastDecompressor decompressor = LZ4Factory.fastestJavaInstance().fastDecompressor();
    private final LZ4Compressor compressor = LZ4Factory.fastestJavaInstance().fastCompressor();

    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (LZ4BlockOutputStream compressionStream = new LZ4BlockOutputStream(outputStream, 32 * 1024, compressor)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        try (LZ4BlockInputStream decompressionStream = new LZ4BlockInputStream(inputStream, decompressor)) {
            while (decompressionStream.read(decompressionBuffer) > -1) { }
        }
        inputStream.close();
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        try (LZ4BlockInputStream decompressionStream = new LZ4BlockInputStream(inputStream, decompressor)) {
            int decompressedBytes;
            while ((decompressedBytes = decompressionStream.read(decompressionBuffer)) > -1) {
                outputStream.write(decompressionBuffer, 0, decompressedBytes);
            }
        }
        inputStream.close();
        outputStream.close();
        return outputStream.toByteArray();
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (LZ4BlockOutputStream compressionStream = new LZ4BlockOutputStream(outputStream, 32 * 1024, compressor)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
}
