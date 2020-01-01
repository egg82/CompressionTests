package me.egg82.comptests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;

public class Chunk {
    private final int x;
    private final int z;

    private byte compressionType;
    private final byte[] uncompressedData;

    private final int hash;

    public Chunk(int x, int z, byte compressionType, byte[] compressedData) throws IOException {
        this.x = x;
        this.z = z;

        this.compressionType = compressionType;

        if (compressionType == 1) {
            this.uncompressedData = gzipDecompress(compressedData);
        } else if (compressionType == 2) {
            this.uncompressedData = inflate(compressedData);
        } else if (compressionType == 3) {
            this.uncompressedData = compressedData;
        } else {
            this.uncompressedData = null;
        }

        this.hash = Objects.hash(x, z);
    }

    public int getX() { return x; }

    public int getZ() { return z; }

    public byte getCompressionType() { return compressionType; }

    public byte[] getUncompressedData() { return uncompressedData; }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chunk)) return false;
        Chunk chunk = (Chunk) o;
        return x == chunk.x &&
                z == chunk.z;
    }

    public int hashCode() { return hash; }

    private byte[] gzipDecompress(byte[] input) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(input.length);
        try (
                ByteArrayInputStream inputStream = new ByteArrayInputStream(input);
                GZIPInputStream decompressionStream = new GZIPInputStream(inputStream, 32 * 1024)
        ) {
            int decompressedBytes;
            while ((decompressedBytes = decompressionStream.read(decompressionBuffer)) > -1) {
                outputStream.write(decompressionBuffer, 0, decompressedBytes);
            }
        }
        outputStream.close();
        return outputStream.toByteArray();
    }

    private final Inflater zlibInflater = new Inflater();
    private final byte[] decompressionBuffer = new byte[1024 * 64];
    private byte[] inflate(byte[] input) throws IOException {
        int power = 1;
        byte[] outBuf = new byte[1024 * 64 * power];
        int totalBytes = 0;

        zlibInflater.setInput(input, 0, input.length);
        int decompressedBytes;
        while (!zlibInflater.finished()) {
            boolean resize = false;

            try {
                decompressedBytes = zlibInflater.inflate(decompressionBuffer);
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
        zlibInflater.reset();

        byte[] out = new byte[totalBytes];
        System.arraycopy(outBuf, 0, out, 0, totalBytes);
        return out;
    }
}
