package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibStreamDict extends BaseByteTest {
    private final Deflater deflater;
    private final Inflater inflater = new Inflater();

    private final byte[] dictionary;

    public ZlibStreamDict(byte[] dictionary, int level) {
        this.dictionary = dictionary;
        deflater = new Deflater(level);
        deflater.setDictionary(dictionary);
    }

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
            int decompressedBytes;
            // This whole mess is needed because while inflater.inflate() returns 0 with needsDictionary and -1 for end-of-stream,
            // InflaterInputStream.read() returns -1 with needsDictionary and end-of-stream
            do {
                decompressedBytes = decompressionStream.read(decompressionBuffer);
                if (decompressedBytes <= 0) {
                    if (inflater.needsDictionary()) {
                        inflater.setDictionary(dictionary);
                    } else {
                        break;
                    }
                }
            } while (true);
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
            // This whole mess is needed because while inflater.inflate() returns 0 with needsDictionary and -1 for end-of-stream,
            // InflaterInputStream.read() returns -1 with needsDictionary and end-of-stream
            do {
                decompressedBytes = decompressionStream.read(decompressionBuffer);
                if (decompressedBytes <= 0) {
                    if (inflater.needsDictionary()) {
                        inflater.setDictionary(dictionary);
                    } else {
                        break;
                    }
                } else {
                    outputStream.write(decompressionBuffer, 0, decompressedBytes);
                }
            } while (true);
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
