package me.egg82.comptests.tests;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibByteArray extends BaseByteTest {
    private final Inflater inflater = new Inflater();
    private final Deflater deflater = new Deflater();

    private byte[] compressionBuffer = new byte[1024 * 64];
    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        deflater.setInput(decompressedData, 0, decompressedData.length);
        deflater.finish();
        while (!deflater.finished()) {
            outputStream.write(compressionBuffer, 0, deflater.deflate(compressionBuffer));
        }
        outputStream.close();
        deflater.reset();

        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        inflater.setInput(compressedData, 0, compressedData.length);
        while (!inflater.finished()) {
            try {
                inflater.inflate(decompressionBuffer);
            } catch (DataFormatException ex) {
                throw new IOException("Could not inflate data.", ex);
            }
        }
        inflater.reset();
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(decompressedData.length);
        deflater.setInput(decompressedData, 0, decompressedData.length);
        deflater.finish();
        while (!deflater.finished()) {
            outputStream.write(compressionBuffer, 0, deflater.deflate(compressionBuffer));
        }
        outputStream.close();
        deflater.reset();

        return outputStream.toByteArray();
    }
}
