package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.*;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibDirectByteBufferDict extends BaseByteTest {
    private final Inflater inflater = new Inflater();
    private final Deflater deflater = new Deflater();

    private final byte[] dictionary;

    public ZlibDirectByteBufferDict(byte[] dictionary) {
        this.dictionary = dictionary;
        deflater.setDictionary(dictionary);
    }

    private byte[] compressionBuffer = new byte[1024 * 64];
    protected long compress(byte[] decompressedData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(decompressedData.length);
        deflater.setInput(decompressedData, 0, decompressedData.length);
        deflater.finish();
        while (!deflater.finished()) {
            buffer.put(compressionBuffer, 0, deflater.deflate(compressionBuffer));
        }
        byte[] out = new byte[buffer.position()];
        buffer.clear();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(out.length);
        buffer.get(out);
        outputStream.write(out);
        outputStream.close();
        deflater.reset();

        return outputStream.size();
    }

    private byte[] decompressionBuffer = new byte[1024 * 64];
    protected void decompress(byte[] compressedData) throws IOException {
        int power = 1;
        ByteBuffer outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);
        int totalBytes = 0;

        inflater.setInput(compressedData, 0, compressedData.length);
        int decompressedBytes;
        while (!inflater.finished()) {
            boolean resize = false;

            try {
                decompressedBytes = inflater.inflate(decompressionBuffer);
                if (decompressedBytes == 0) {
                    if (inflater.needsDictionary()) {
                        inflater.setDictionary(dictionary);
                        decompressedBytes = inflater.inflate(decompressionBuffer);
                    }
                    if (inflater.needsInput()) {
                        throw new IOException("Inflater reached end of stream prematurely.");
                    }
                }
            } catch (DataFormatException ex) {
                throw new IOException("Could not inflate data.", ex);
            }

            while (decompressedBytes > 1024 * 64 * power - totalBytes) {
                power++;
                resize = true;
            }
            if (resize) {
                ByteBuffer tmp = outBuf;
                outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);
                byte[] tmpBytes = new byte[totalBytes];
                tmp.rewind();
                tmp.get(tmpBytes);
                outBuf.put(tmpBytes);
            }

            outBuf.put(decompressionBuffer, 0, decompressedBytes);
            totalBytes += decompressedBytes;
        }
        inflater.reset();

        byte[] out = new byte[totalBytes];
        outBuf.rewind();
        outBuf.get(out);
    }

    public byte[] getDecompressedData(byte[] compressedData) throws IOException {
        int power = 1;
        ByteBuffer outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);
        int totalBytes = 0;

        inflater.setInput(compressedData, 0, compressedData.length);
        int decompressedBytes;
        while (!inflater.finished()) {
            boolean resize = false;

            try {
                decompressedBytes = inflater.inflate(decompressionBuffer);
                if (decompressedBytes == 0) {
                    if (inflater.needsDictionary()) {
                        inflater.setDictionary(dictionary);
                        decompressedBytes = inflater.inflate(decompressionBuffer);
                    }
                    if (inflater.needsInput()) {
                        throw new IOException("Inflater reached end of stream prematurely.");
                    }
                }
            } catch (DataFormatException ex) {
                throw new IOException("Could not inflate data.", ex);
            }

            while (decompressedBytes > 1024 * 64 * power - totalBytes) {
                power++;
                resize = true;
            }
            if (resize) {
                ByteBuffer tmp = outBuf;
                outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);
                byte[] tmpBytes = new byte[totalBytes];
                tmp.rewind();
                tmp.get(tmpBytes);
                outBuf.put(tmpBytes);
            }

            outBuf.put(decompressionBuffer, 0, decompressedBytes);
            totalBytes += decompressedBytes;
        }
        inflater.reset();

        byte[] out = new byte[totalBytes];
        outBuf.rewind();
        outBuf.get(out);
        return out;
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(decompressedData.length);
        deflater.setInput(decompressedData, 0, decompressedData.length);
        deflater.finish();
        while (!deflater.finished()) {
            buffer.put(compressionBuffer, 0, deflater.deflate(compressionBuffer));
        }
        byte[] out = new byte[buffer.position()];
        buffer.clear();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(out.length);
        buffer.get(out);
        outputStream.write(out);
        outputStream.close();
        deflater.reset();

        return outputStream.toByteArray();
    }
}
