package com.example.binary;

import com.example.malware.MalwareSignature;
import com.example.malware.MalwareSignatureRepository;
import com.example.malware.SignatureStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/binary/signatures")
@RequiredArgsConstructor
public class BinarySignatureController {

    private final MalwareSignatureRepository signatureRepo;
    private final ManifestBinSerializer manifestSerializer;
    private final DataBinSerializer dataSerializer;

    // 1. Полная база
    @GetMapping("/full")
    public ResponseEntity<?> getFull() {
        try {
            List<MalwareSignature> sigs = signatureRepo
                    .findByStatus(SignatureStatus.ACTUAL);
            return buildMultipart(sigs, 1, -1L);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    // 2. Инкремент
    @GetMapping("/increment")
    public ResponseEntity<?> getIncrement(@RequestParam String since) {
        try {
            Instant sinceInstant = Instant.parse(since);
            List<MalwareSignature> sigs = signatureRepo
                    .findByUpdatedAtAfter(sinceInstant);
            return buildMultipart(sigs, 2, sinceInstant.toEpochMilli());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Неверный формат since");
        }
    }

    // 3. По списку UUID
    @PostMapping("/by-ids")
    public ResponseEntity<?> getByIds(
            @RequestBody Map<String, List<UUID>> body) {
        try {
            List<MalwareSignature> sigs = signatureRepo
                    .findByIdIn(body.get("ids"));
            return buildMultipart(sigs, 3, -1L);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(e.getMessage());
        }
    }

    private ResponseEntity<MultiValueMap<String, Object>> buildMultipart(
            List<MalwareSignature> sigs,
            int exportType,
            long sinceEpochMillis) throws Exception {

        byte[] dataBin = dataSerializer.serialize(sigs);
        byte[] manifestBin = manifestSerializer.serialize(
                sigs, dataBin, exportType, sinceEpochMillis);

        HttpHeaders manifestHeaders = new HttpHeaders();
        manifestHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        manifestHeaders.setContentDisposition(
                ContentDisposition.attachment().filename("manifest.bin").build());

        HttpHeaders dataHeaders = new HttpHeaders();
        dataHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        dataHeaders.setContentDisposition(
                ContentDisposition.attachment().filename("data.bin").build());

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("manifest", new HttpEntity<>(manifestBin, manifestHeaders));
        parts.add("data", new HttpEntity<>(dataBin, dataHeaders));

        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.MULTIPART_MIXED);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(parts);
    }
}