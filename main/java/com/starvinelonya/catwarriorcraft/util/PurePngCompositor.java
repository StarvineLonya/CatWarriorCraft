package com.starvinelonya.catwarriorcraft.util;

import com.mojang.blaze3d.platform.NativeImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

/**
 * 零AWT、零第三方库的PNG合成器（NeoForge 1.21 适配版）
 * 仅支持 color_type=6 (RGBA)、bit_depth=8、非隔行扫描的PNG
 */
public class PurePngCompositor {

    // ==================== PNG 数据结构 ====================
    public static class PngImage {
        public final int width, height;
        public final byte[] pixels; // RGBA 原始像素，每像素4字节，已去除滤镜

        public PngImage(int w, int h, byte[] px) {
            if (w <= 0 || h <= 0) throw new IllegalArgumentException("Invalid dimensions: " + w + "x" + h);
            if (px.length != w * h * 4) throw new IllegalArgumentException("Pixel array size mismatch");
            this.width = w;
            this.height = h;
            this.pixels = px;
        }

        /**
         * 【新增】直接转换为 Minecraft NativeImage，用于动态纹理注册
         * 注意：NativeImage 内部使用 ABGR 格式，此方法已处理通道交换
         */
        public NativeImage toNativeImage() {
            NativeImage image = new NativeImage(width, height, true);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int idx = (y * width + x) * 4;
                    int r = pixels[idx] & 0xFF;
                    int g = pixels[idx + 1] & 0xFF;
                    int b = pixels[idx + 2] & 0xFF;
                    int a = pixels[idx + 3] & 0xFF;
                    // NativeImage.setPixelRGBA 期望 ABGR 格式
                    image.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
                }
            }
            return image;
        }
    }

    // ==================== 1. PNG 解码 ====================
    public static PngImage decodePng(InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream(in);

        byte[] sig = new byte[8];
        dis.readFully(sig);
        if (sig[0] != (byte) 0x89 || sig[1] != 0x50 || sig[2] != 0x4E || sig[3] != 0x47)
            throw new IOException("Invalid PNG signature");

        int width = 0, height = 0;
        List<byte[]> idatChunks = new ArrayList<>();
        boolean ihdrFound = false;

        while (true) {
            int length = dis.readInt();
            byte[] typeBytes = new byte[4];
            dis.readFully(typeBytes);
            String type = new String(typeBytes, StandardCharsets.US_ASCII);

            if (length < 0) throw new IOException("Invalid chunk length: " + length);
            byte[] data = new byte[length];
            dis.readFully(data);
            int storedCrc = dis.readInt();

            CRC32 crc = new CRC32();
            crc.update(typeBytes);
            crc.update(data);
            if ((int) crc.getValue() != storedCrc)
                throw new IOException("CRC mismatch in chunk: " + type);

            switch (type) {
                case "IHDR":
                    if (length < 13) throw new IOException("IHDR chunk too short");
                    ByteBuffer ihdr = ByteBuffer.wrap(data);
                    width = ihdr.getInt();
                    height = ihdr.getInt();
                    int bitDepth = ihdr.get() & 0xFF;
                    int colorType = ihdr.get() & 0xFF;
                    ihdr.get(); // compression
                    ihdr.get(); // filter
                    int interlace = ihdr.get() & 0xFF;
                    if (bitDepth != 8 || colorType != 6 || interlace != 0)
                        throw new IOException("Only RGBA8 non-interlaced PNG supported. Got: depth="
                                + bitDepth + " type=" + colorType + " interlace=" + interlace);
                    ihdrFound = true;
                    break;
                case "IDAT":
                    if (!ihdrFound) throw new IOException("IDAT before IHDR");
                    idatChunks.add(data);
                    break;
                case "IEND":
                    if (!ihdrFound || idatChunks.isEmpty())
                        throw new IOException("Missing IHDR or IDAT");
                    int totalLen = idatChunks.stream().mapToInt(b -> b.length).sum();
                    byte[] compressed = new byte[totalLen];
                    int offset = 0;
                    for (byte[] chunk : idatChunks) {
                        System.arraycopy(chunk, 0, compressed, offset, chunk.length);
                        offset += chunk.length;
                    }
                    byte[] raw = inflate(compressed);
                    byte[] pixels = unfilter(raw, width, height, 4);
                    return new PngImage(width, height, pixels);
            }
        }
    }

    // ==================== 2. zlib 解压/压缩 ====================
    private static byte[] inflate(byte[] data) throws IOException {
        Inflater inf = new Inflater();
        inf.setInput(data);
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length * 4);
        byte[] buf = new byte[8192];
        try {
            while (!inf.finished()) {
                int count = inf.inflate(buf);
                if (count == 0 && inf.needsInput()) break;
                out.write(buf, 0, count);
            }
        } catch (DataFormatException e) {
            throw new IOException("zlib decompression failed", e);
        } finally {
            inf.end();
        }
        return out.toByteArray();
    }

    private static byte[] deflate(byte[] data) throws IOException {
        Deflater def = new Deflater(Deflater.DEFAULT_COMPRESSION);
        def.setInput(data);
        def.finish();
        ByteArrayOutputStream out = new ByteArrayOutputStream(data.length);
        byte[] buf = new byte[8192];
        while (!def.finished()) {
            int count = def.deflate(buf);
            out.write(buf, 0, count);
        }
        def.end();
        return out.toByteArray();
    }

    // ==================== 3. PNG 行滤镜还原 ====================
    private static byte[] unfilter(byte[] raw, int width, int height, int bpp) throws IOException {
        int stride = width * bpp;
        int expectedSize = (stride + 1) * height;
        if (raw.length < expectedSize)
            throw new IOException("Raw data too short: expected " + expectedSize + ", got " + raw.length);

        byte[] out = new byte[stride * height];
        int srcOffset = 0;

        for (int y = 0; y < height; y++) {
            int filterType = raw[srcOffset++] & 0xFF;
            int dstRowStart = y * stride;

            for (int x = 0; x < stride; x++) {
                int rawByte = raw[srcOffset++] & 0xFF;
                int a = (x >= bpp) ? (out[dstRowStart + x - bpp] & 0xFF) : 0;
                int b = (y > 0) ? (out[dstRowStart - stride + x] & 0xFF) : 0;
                int c = (x >= bpp && y > 0) ? (out[dstRowStart - stride + x - bpp] & 0xFF) : 0;

                int val;
                switch (filterType) {
                    case 0: val = rawByte; break;
                    case 1: val = rawByte + a; break;
                    case 2: val = rawByte + b; break;
                    case 3: val = rawByte + (a + b) / 2; break;
                    case 4: val = rawByte + paeth(a, b, c); break;
                    default: throw new IOException("Unknown filter type: " + filterType);
                }
                out[dstRowStart + x] = (byte) (val & 0xFF);
            }
        }
        return out;
    }

    private static int paeth(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a), pb = Math.abs(p - b), pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) return a;
        if (pb <= pc) return b;
        return c;
    }

    // ==================== 4. Alpha 混合（精度修复）====================
    public static PngImage composite(PngImage bg, PngImage fg) {
        if (bg.width != fg.width || bg.height != fg.height)
            throw new IllegalArgumentException("Images must have same dimensions: "
                    + bg.width + "x" + bg.height + " vs " + fg.width + "x" + fg.height);

        int len = bg.width * bg.height * 4;
        byte[] out = new byte[len];

        for (int i = 0; i < len; i += 4) {
            int srcR = fg.pixels[i] & 0xFF, srcG = fg.pixels[i + 1] & 0xFF;
            int srcB = fg.pixels[i + 2] & 0xFF, srcA = fg.pixels[i + 3] & 0xFF;
            int dstR = bg.pixels[i] & 0xFF, dstG = bg.pixels[i + 1] & 0xFF;
            int dstB = bg.pixels[i + 2] & 0xFF, dstA = bg.pixels[i + 3] & 0xFF;

            if (srcA == 0) {
                out[i] = bg.pixels[i]; out[i + 1] = bg.pixels[i + 1];
                out[i + 2] = bg.pixels[i + 2]; out[i + 3] = bg.pixels[i + 3];
                continue;
            }
            if (srcA == 255) {
                out[i] = fg.pixels[i]; out[i + 1] = fg.pixels[i + 1];
                out[i + 2] = fg.pixels[i + 2]; out[i + 3] = fg.pixels[i + 3];
                continue;
            }

            // 【修复】使用浮点运算避免整数截断导致的色带
            float fSrcA = srcA / 255f;
            float fInvSrcA = 1f - fSrcA;
            float fDstA = dstA / 255f;
            float outAf = fSrcA + fDstA * fInvSrcA;

            if (outAf <= 0f) {
                out[i] = out[i + 1] = out[i + 2] = out[i + 3] = 0;
            } else {
                out[i]     = (byte) Math.round((srcR * fSrcA + dstR * fDstA * fInvSrcA) / outAf);
                out[i + 1] = (byte) Math.round((srcG * fSrcA + dstG * fDstA * fInvSrcA) / outAf);
                out[i + 2] = (byte) Math.round((srcB * fSrcA + dstB * fDstA * fInvSrcA) / outAf);
                out[i + 3] = (byte) Math.round(outAf * 255f);
            }
        }
        return new PngImage(bg.width, bg.height, out);
    }

    // ==================== 5. PNG 编码 ====================
    public static void encodePng(PngImage img, OutputStream out) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        dos.write(new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A});

        ByteBuffer ihdr = ByteBuffer.allocate(13);
        ihdr.putInt(img.width).putInt(img.height);
        ihdr.put((byte) 8).put((byte) 6).put((byte) 0).put((byte) 0).put((byte) 0);
        writeChunk(dos, "IHDR", ihdr.array());

        int stride = img.width * 4;
        byte[] filtered = new byte[(stride + 1) * img.height];
        for (int y = 0; y < img.height; y++) {
            filtered[y * (stride + 1)] = 0;
            System.arraycopy(img.pixels, y * stride, filtered, y * (stride + 1) + 1, stride);
        }
        byte[] compressed = deflate(filtered);
        writeChunk(dos, "IDAT", compressed);

        writeChunk(dos, "IEND", new byte[0]);
        dos.flush();
    }

    private static void writeChunk(DataOutputStream dos, String type, byte[] data) throws IOException {
        dos.writeInt(data.length);
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        dos.write(typeBytes);
        dos.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        dos.writeInt((int) crc.getValue());
    }
}