package me.egg82.comptests.tests.zstd;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZstdByteArray extends BaseByteTest {
    private final int level;

    public ZstdByteArray(int level) { this.level = level; }

    protected long compress(byte[] decompressedData) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) Zstd.compressBound(decompressedData.length));
        byte[] compressedBytes = Zstd.compress(decompressedData, level);
        outputStream.write(compressedBytes, 0, compressedBytes.length);
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

            decompressedBytes = Zstd.decompress(outBuf, compressedData);
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

            decompressedBytes = Zstd.decompress(outBuf, compressedData);
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
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) Zstd.compressBound(decompressedData.length));
        byte[] compressedBytes = Zstd.compress(decompressedData, level);
        outputStream.write(compressedBytes, 0, compressedBytes.length);
        outputStream.close();

        return outputStream.toByteArray();
    }
}
