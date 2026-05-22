package com.example.service;

import com.example.dto.Ticket;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class SignatureService {

    private static final String SECRET = "SUPER_SECRET_KEY";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public String signTicket(Ticket ticket) {

        try {

            String data =
                    objectMapper.writeValueAsString(ticket);

            Mac sha256Hmac =
                    Mac.getInstance("HmacSHA256");

            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            SECRET.getBytes(),
                            "HmacSHA256"
                    );

            sha256Hmac.init(secretKey);

            byte[] signedBytes =
                    sha256Hmac.doFinal(data.getBytes());

            return Base64.getEncoder()
                    .encodeToString(signedBytes);

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while signing ticket",
                    e
            );
        }
    }
}