package com.example.binary;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BinaryWriter {

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    public void writeU8(int value) {
        out.write(value & 0xFF);
    }

    public void writeU16(int value) {
        out.write((value >>> 8) & 0xFF);
        out.write(value & 0xFF);
    }

    public void writeU32(long value) {
        out.write((int) ((value >>> 24) & 0xFF));
        out.write((int) ((value >>> 16) & 0xFF));
        out.write((int) ((value >>> 8) & 0xFF));
        out.write((int) (value & 0xFF));
    }

    public void writeI64(long value) {
        for (int i = 7; i >= 0; i--) {
            out.write((int) ((value >>> (i * 8)) & 0xFF));
        }
    }

    public void writeBytes(byte[] bytes) {
        writeU32(bytes.length);
        out.writeBytes(bytes);
    }

    public void writeString(String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeU32(bytes.length);
        out.writeBytes(bytes);
    }

    public void writeUUID(UUID uuid) {
        writeI64(uuid.getMostSignificantBits());
        writeI64(uuid.getLeastSignificantBits());
    }

    public void writeRawBytes(byte[] bytes) {
        out.writeBytes(bytes);
    }

    public byte[] toByteArray() {
        return out.toByteArray();
    }
}