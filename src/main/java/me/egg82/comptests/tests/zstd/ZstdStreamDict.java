package me.egg82.comptests.tests.zstd;

import com.github.luben.zstd.ZstdDictCompress;
import com.github.luben.zstd.ZstdDictDecompress;
import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZstdStreamDict extends BaseByteTest {
    private final ZstdDictCompress compressor;
    private final ZstdDictDecompress decompressor;

    public ZstdStreamDict(byte[] dictionary, int level) {
        compressor = new ZstdDictCompress(dictionary, level);
        decompressor = new ZstdDictDecompress(dictionary);
    }

    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (ZstdOutputStream compressionStream = new ZstdOutputStream(outputStream).setDict(compressor)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                ZstdInputStream decompressionStream = new ZstdInputStream(inputStream).setDict(decompressor)
        ) {
            while (decompressionStream.read(decompressionBuffer) > -1) { }
        }
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                ZstdInputStream decompressionStream = new ZstdInputStream(inputStream).setDict(decompressor)
        ) {
            int decompressedBytes;
            while ((decompressedBytes = decompressionStream.read(decompressionBuffer)) > -1) {
                outputStream.write(decompressionBuffer, 0, decompressedBytes);
            }
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (ZstdOutputStream compressionStream = new ZstdOutputStream(outputStream).setDict(compressor)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
}
