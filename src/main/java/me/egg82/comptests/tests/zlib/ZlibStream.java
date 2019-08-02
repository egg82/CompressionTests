package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibStream extends BaseByteTest {
    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (DeflaterOutputStream compressionStream = new DeflaterOutputStream(outputStream)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        try (InflaterInputStream decompressionStream = new InflaterInputStream(inputStream)) {
            while (decompressionStream.read(decompressionBuffer) > -1) { }
        }
        inputStream.close();
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        try (InflaterInputStream decompressionStream = new InflaterInputStream(inputStream)) {
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
        try (DeflaterOutputStream compressionStream = new DeflaterOutputStream(outputStream)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        return outputStream.toByteArray();
    }
}
