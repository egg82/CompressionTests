package me.egg82.comptests.tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibStreamDict extends BaseByteTest {
    private final Deflater deflater = new Deflater();
    private final Inflater inflater = new Inflater();

    private final byte[] dictionary;

    public ZlibStreamDict(byte[] dictionary) {
        this.dictionary = dictionary;
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
        ByteArrayInputStream inputStream = new ByteArrayInputStream(compressedData);
        try (InflaterInputStream decompressionStream = new InflaterInputStream(inputStream)) {
            int decompressedBytes;
            while ((decompressedBytes = decompressionStream.read(decompressionBuffer)) > -1) {
                if (decompressedBytes == 0) {
                    if (inflater.needsDictionary()) {
                        inflater.setDictionary(dictionary);
                    }
                    if (inflater.needsInput()) {
                        throw new IOException("Inflater reached end of stream prematurely.");
                    }
                }
            }
        }
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
