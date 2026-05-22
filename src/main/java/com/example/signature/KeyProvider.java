package com.example.signature;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
@RequiredArgsConstructor
public class KeyProvider {

    private final SignatureProperties props;

    private PrivateKey cachedPrivateKey;
    private PublicKey cachedPublicKey;

    public synchronized PrivateKey getPrivateKey() {
        if (cachedPrivateKey == null) loadKeys();
        return cachedPrivateKey;
    }

    public synchronized PublicKey getPublicKey() {
        if (cachedPublicKey == null) loadKeys();
        return cachedPublicKey;
    }

    private void loadKeys() {
        try {
            KeyStore ks = KeyStore.getInstance(props.getKeyStoreType());
            var resource = new DefaultResourceLoader()
                    .getResource(props.getKeyStorePath());
            try (InputStream is = resource.getInputStream()) {
                ks.load(is, props.getKeyStorePassword().toCharArray());
            }
            String keyPass = props.getKeyPassword() != null
                    ? props.getKeyPassword()
                    : props.getKeyStorePassword();
            cachedPrivateKey = (PrivateKey) ks.getKey(
                    props.getKeyAlias(),
                    keyPass.toCharArray()
            );
            cachedPublicKey = ks.getCertificate(
                    props.getKeyAlias()
            ).getPublicKey();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load keys", e);
        }
    }
}
