package com.example.service;

import com.example.dto.Ticket;
import com.example.signature.JsonCanonicalizer;
import com.example.signature.KeyProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Signature;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final KeyProvider keyProvider;
    private final JsonCanonicalizer canonicalizer;

    public String signTicket(Ticket ticket) {
        try {
            byte[] data = canonicalizer.canonicalize(ticket);

            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyProvider.getPrivateKey());
            signer.update(data);
            byte[] signatureBytes = signer.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (Exception e) {
            throw new RuntimeException("Error while signing ticket", e);
        }
    }
}