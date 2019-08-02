package me.egg82.comptests.tests.zstd;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import me.egg82.comptests.tests.generic.BaseByteTest;

public class ZstdDirectByteBuffer extends BaseByteTest {
    protected long compress(byte[] decompressedData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) Zstd.compressBound(decompressedData.length));
        ByteBuffer inBuffer = ByteBuffer.allocateDirect(decompressedData.length);
        inBuffer.put(decompressedData);
        inBuffer.clear();
        Zstd.compressDirectByteBuffer(buffer, 0, buffer.remaining(), inBuffer, 0, inBuffer.remaining(), 1);
        byte[] out = new byte[buffer.position()];
        buffer.clear();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(out.length);
        buffer.get(out);
        outputStream.write(out);
        outputStream.close();

        return outputStream.size();
    }

    protected void decompress(byte[] compressedData) {
        int power = 1;
        ByteBuffer outBuf;
        ByteBuffer inBuf = ByteBuffer.allocateDirect(compressedData.length);
        inBuf.put(compressedData, 0, compressedData.length);
        inBuf.clear();

        long decompressedBytes;
        boolean resize;
        do {
            resize = false;
            outBuf = ByteBuffer.allocateDirect(1024 * 64 * power);

            decompressedBytes = Zstd.decompressDirectByteBuffer(outBuf, 0, outBuf.remaining(), inBuf, 0, inBuf.remaining());
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
        outBuf.rewind();
        outBuf.get(out);
    }

    public byte[] getCompressedData(byte[] decompressedData) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect((int) Zstd.compressBound(decompressedData.length));
        ByteBuffer inBuffer = ByteBuffer.allocateDirect(decompressedData.length);
        inBuffer.put(decompressedData);
        inBuffer.clear();
        Zstd.compressDirectByteBuffer(buffer, 0, buffer.remaining(), inBuffer, 0, inBuffer.remaining(), 1);
        byte[] out = new byte[buffer.position()];
        buffer.clear();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(out.length);
        buffer.get(out);
        outputStream.write(out);
        outputStream.close();

        return outputStream.toByteArray();
    }
}
