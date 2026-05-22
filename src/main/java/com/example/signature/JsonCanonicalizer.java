package com.example.signature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Component
public class JsonCanonicalizer {

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public byte[] canonicalize(Object payload) {
        try {
            // Конвертируем в Map и сортируем ключи
            String json = mapper.writeValueAsString(payload);
            Map<?, ?> map = mapper.readValue(json, TreeMap.class);
            String canonical = mapper.writeValueAsString(map);
            return canonical.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Canonicalization failed", e);
        }
    }
}