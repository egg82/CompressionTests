package me.egg82.comptests.tests.zstd;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZstdStream extends BaseByteTest {
    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (ZstdOutputStream compressionStream = new ZstdOutputStream(outputStream, 1)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                ZstdInputStream decompressionStream = new ZstdInputStream(inputStream)
        ) {
            while (decompressionStream.read(decompressionBuffer) > -1) { }
        }
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                ZstdInputStream decompressionStream = new ZstdInputStream(inputStream)
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
        try (ZstdOutputStream compressionStream = new ZstdOutputStream(outputStream, 1)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
}
