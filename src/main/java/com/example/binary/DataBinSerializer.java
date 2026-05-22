package com.example.binary;

import com.example.malware.MalwareSignature;
import org.springframework.stereotype.Component;

import java.util.HexFormat;
import java.util.List;

@Component
public class DataBinSerializer {

    private static final String MAGIC = "DB-Mamedov";
    private static final int VERSION = 1;

    public byte[] serialize(List<MalwareSignature> signatures) {
        BinaryWriter writer = new BinaryWriter();

        // Заголовок
        writer.writeRawBytes(MAGIC.getBytes());
        writer.writeU16(VERSION);
        writer.writeU32(signatures.size());

        // Записи
        for (MalwareSignature sig : signatures) {
            writer.writeString(sig.getThreatName());
            writer.writeBytes(HexFormat.of().parseHex(sig.getFirstBytesHex()));
            writer.writeBytes(HexFormat.of().parseHex(sig.getRemainderHashHex()));
            writer.writeI64(sig.getRemainderLength());
            writer.writeString(sig.getFileType());
            writer.writeI64(sig.getOffsetStart());
            writer.writeI64(sig.getOffsetEnd());
        }

        return writer.toByteArray();
    }
}