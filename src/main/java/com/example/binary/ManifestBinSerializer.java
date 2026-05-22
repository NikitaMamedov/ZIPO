package com.example.binary;

import com.example.malware.MalwareSignature;
import com.example.malware.SignatureStatus;
import com.example.service.SignatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ManifestBinSerializer {

    private static final String MAGIC = "MF-Mamedov";
    private static final int VERSION = 1;

    private final SignatureService signatureService;

    public byte[] serialize(List<MalwareSignature> signatures,
                            byte[] dataBin,
                            int exportType,
                            long sinceEpochMillis) throws Exception {

        BinaryWriter writer = new BinaryWriter();

        // Заголовок
        writer.writeRawBytes(MAGIC.getBytes());
        writer.writeU16(VERSION);
        writer.writeU8(exportType);
        writer.writeI64(System.currentTimeMillis());
        writer.writeI64(sinceEpochMillis);
        writer.writeU32(signatures.size());

        // SHA-256 от data.bin
        byte[] dataSha256 = MessageDigest.getInstance("SHA-256").digest(dataBin);
        writer.writeRawBytes(dataSha256); // ровно 32 байта

        // Вычисляем смещения
        int offset = 0;
        DataBinSerializer dataSerializer = new DataBinSerializer();

        for (MalwareSignature sig : signatures) {
            // Временно сериализуем одну запись чтобы узнать длину
            BinaryWriter entryWriter = new BinaryWriter();
            java.util.HexFormat hex = java.util.HexFormat.of();
            entryWriter.writeString(sig.getThreatName());
            entryWriter.writeBytes(hex.parseHex(sig.getFirstBytesHex()));
            entryWriter.writeBytes(hex.parseHex(sig.getRemainderHashHex()));
            entryWriter.writeI64(sig.getRemainderLength());
            entryWriter.writeString(sig.getFileType());
            entryWriter.writeI64(sig.getOffsetStart());
            entryWriter.writeI64(sig.getOffsetEnd());
            int entryLength = entryWriter.toByteArray().length;

            // Декодируем подпись записи
            byte[] sigBytes = sig.getDigitalSignatureBase64() != null
                    ? Base64.getDecoder().decode(sig.getDigitalSignatureBase64())
                    : new byte[0];

            // Запись манифеста
            writer.writeUUID(sig.getId());
            writer.writeU8(sig.getStatus() == SignatureStatus.ACTUAL ? 1 : 0);
            writer.writeI64(sig.getUpdatedAt().toEpochMilli());
            writer.writeI64(offset);
            writer.writeI64(entryLength);
            writer.writeU32(sigBytes.length);
            writer.writeRawBytes(sigBytes);

            offset += entryLength;
        }

        // Подписываем неподписанную часть манифеста
        byte[] unsignedManifest = writer.toByteArray();
        byte[] manifestSignature = signatureService.signBytes(unsignedManifest);

        // Дописываем подпись манифеста
        BinaryWriter finalWriter = new BinaryWriter();
        finalWriter.writeRawBytes(unsignedManifest);
        finalWriter.writeU32(manifestSignature.length);
        finalWriter.writeRawBytes(manifestSignature);

        return finalWriter.toByteArray();
    }
}