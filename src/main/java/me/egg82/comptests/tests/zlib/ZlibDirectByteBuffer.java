package me.egg82.comptests.tests.zlib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZlibDirectByteBuffer extends BaseByteTest {
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
        int power = 1;
        ByteBuffer outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);
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
