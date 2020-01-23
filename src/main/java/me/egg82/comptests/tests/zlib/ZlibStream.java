package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibStream extends BaseByteTest {
    private final Inflater inflater = new Inflater();
    private final Deflater deflater;

    public ZlibStream(int level) { deflater = new Deflater(level); }

    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (DeflaterOutputStream compressionStream = new DeflaterOutputStream(outputStream, deflater)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        deflater.reset();
        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                InflaterInputStream decompressionStream = new InflaterInputStream(inputStream, inflater)
        ) {
            while (decompressionStream.read(decompressionBuffer) > -1) { }
        }
        inflater.reset();
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
                InflaterInputStream decompressionStream = new InflaterInputStream(inputStream, inflater)
        ) {
            int decompressedBytes;
            while ((decompressedBytes = decompressionStream.read(decompressionBuffer)) > -1) {
                outputStream.write(decompressionBuffer, 0, decompressedBytes);
            }
        }
        outputStream.close();
        inflater.reset();
        return outputStream.toByteArray();
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        try (DeflaterOutputStream compressionStream = new DeflaterOutputStream(outputStream, deflater)) {
            compressionStream.write(decompressedData);
        }
        outputStream.close();
        deflater.reset();
        return outputStream.toByteArray();
    }
}
