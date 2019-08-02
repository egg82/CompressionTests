package me.egg82.comptests.tests.zstd;

import com.github.luben.zstd.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZstdByteArrayDict extends BaseByteTest {
    private final ZstdDictCompress compressor;
    private final ZstdDictDecompress decompressor;

    public ZstdByteArrayDict(byte[] dictionary) {
        compressor = new ZstdDictCompress(dictionary, 1);
        decompressor = new ZstdDictDecompress(dictionary);
    }

    protected long compress(byte[] decompressedData) throws IOException {
        byte[] output = new byte[(int) Zstd.compressBound(decompressedData.length)];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(output.length);
        long compressedBytes = Zstd.compressFastDict(output, 0, decompressedData, 0, decompressedData.length, compressor);
        if (Zstd.isError(compressedBytes)) {
            throw new ZstdException(compressedBytes);
        }
        outputStream.write(output, 0, (int) compressedBytes);
        outputStream.close();

        return outputStream.size();
    }

    protected void decompress(byte[] compressedData) {
        int power = 1;
        byte[] outBuf;

        long decompressedBytes;
        boolean resize;
        do {
            resize = false;
            outBuf = new byte[1024 * 64 * power];

            decompressedBytes = Zstd.decompressFastDict(outBuf, 0, compressedData, 0, compressedData.length, decompressor);
            if (Zstd.isError(decompressedBytes)) {
                if (Zstd.getErrorCode(decompressedBytes) == Zstd.errDstSizeTooSmall()) {
                    resize = true;
                } else {
                    throw new ZstdException(decompressedBytes);
                }
            }

            if (resize) {
                power++;
            }
        } while (resize);

        byte[] out = new byte[(int) decompressedBytes];
        System.arraycopy(outBuf, 0, out, 0, (int) decompressedBytes);
    }

    public byte[] getDecompressedData(byte[] compressedData) {
        int power = 1;
        byte[] outBuf;

        long decompressedBytes;
        boolean resize;
        do {
            resize = false;
            outBuf = new byte[1024 * 64 * power];

            decompressedBytes = Zstd.decompressFastDict(outBuf, 0, compressedData, 0, compressedData.length, decompressor);
            if (Zstd.isError(decompressedBytes)) {
                if (Zstd.getErrorCode(decompressedBytes) == Zstd.errDstSizeTooSmall()) {
                    resize = true;
                } else {
                    throw new ZstdException(decompressedBytes);
                }
            }

            if (resize) {
                power++;
            }
        } while (resize);

        byte[] out = new byte[(int) decompressedBytes];
        System.arraycopy(outBuf, 0, out, 0, (int) decompressedBytes);
        return out;
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        byte[] output = new byte[(int) Zstd.compressBound(decompressedData.length)];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(output.length);
        long compressedBytes = Zstd.compressFastDict(output, 0, decompressedData, 0, decompressedData.length, compressor);
        if (Zstd.isError(compressedBytes)) {
            throw new ZstdException(compressedBytes);
        }
        outputStream.write(output, 0, (int) compressedBytes);
        outputStream.close();

        return outputStream.toByteArray();
    }
}
