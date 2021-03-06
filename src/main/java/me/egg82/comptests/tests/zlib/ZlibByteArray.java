package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.*;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibByteArray extends BaseByteTest {
    private final Inflater inflater = new Inflater();
    private final Deflater deflater;

    public ZlibByteArray(int level) { deflater = new Deflater(level); }

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
        int power = 1;
        byte[] outBuf = new byte[1024 * 64 * power];
        int totalBytes = 0;

        inflater.setInput(compressedData, 0, compressedData.length);
        int decompressedBytes;
        while (!inflater.finished()) {
            boolean resize = false;

            try {
                decompressedBytes = inflater.inflate(decompressionBuffer);
            } catch (DataFormatException ex) {
                throw new IOException("Could not inflate data.", ex);
            }

            while (decompressedBytes > 1024 * 64 * power - totalBytes) {
                power++;
                resize = true;
            }
            if (resize) {
                byte[] tmp = outBuf;
                outBuf = new byte[1024 * 64 * power];
                System.arraycopy(tmp, 0, outBuf, 0, totalBytes);
            }

            System.arraycopy(decompressionBuffer, 0, outBuf, totalBytes, decompressedBytes);
            totalBytes += decompressedBytes;
        }
        inflater.reset();

        byte[] out = new byte[totalBytes];
        System.arraycopy(outBuf, 0, out, 0, totalBytes);
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        int power = 1;
        byte[] outBuf = new byte[1024 * 64 * power];
        int totalBytes = 0;

        inflater.setInput(compressedData, 0, compressedData.length);
        int decompressedBytes;
        while (!inflater.finished()) {
            boolean resize = false;

            try {
                decompressedBytes = inflater.inflate(decompressionBuffer);
            } catch (DataFormatException ex) {
                throw new IOException("Could not inflate data.", ex);
            }

            while (decompressedBytes > 1024 * 64 * power - totalBytes) {
                power++;
                resize = true;
            }
            if (resize) {
                byte[] tmp = outBuf;
                outBuf = new byte[1024 * 64 * power];
                System.arraycopy(tmp, 0, outBuf, 0, totalBytes);
            }

            System.arraycopy(decompressionBuffer, 0, outBuf, totalBytes, decompressedBytes);
            totalBytes += decompressedBytes;
        }
        inflater.reset();

        byte[] out = new byte[totalBytes];
        System.arraycopy(outBuf, 0, out, 0, totalBytes);
        return out;
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
