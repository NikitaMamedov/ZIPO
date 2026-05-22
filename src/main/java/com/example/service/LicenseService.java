package com.example.service;

import com.example.dto.Ticket;
import com.example.dto.TicketResponse;
import com.example.model.License;
import com.example.model.LicenseHistory;
import com.example.model.LicenseStatus;
import com.example.repository.LicenseHistoryRepository;
import com.example.repository.LicenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LicenseService {

    private final LicenseRepository licenseRepository;
    private final LicenseHistoryRepository historyRepository;
    private final SignatureService signatureService;

    // Создание лицензии
    public License createLicense(Long userId,
                                 String description,
                                 Integer maxDevices) {

        String licenseKey =
                "LIC-" + UUID.randomUUID()
                        .toString()
                        .substring(0, 16)
                        .toUpperCase();

        License license = new License();

        license.setLicenseKey(licenseKey);
        license.setUserId(userId);
        license.setDescription(description);

        license.setMaxDevices(
                maxDevices != null ? maxDevices : 1
        );

        // лицензия на 1 год
        license.setExpiresAt(
                Instant.now().plusSeconds(365L * 24 * 3600)
        );

        License saved = licenseRepository.save(license);

        addHistory(saved, "CREATE", null);

        return saved;
    }

    // Активация лицензии
    public License activateLicense(String licenseKey,
                                   String deviceId) {

        License license = licenseRepository
                .findByLicenseKey(licenseKey)
                .orElseThrow(() ->
                        new RuntimeException("License not found")
                );

        if (license.getStatus() != LicenseStatus.CREATED) {
            throw new RuntimeException(
                    "License cannot be activated"
            );
        }

        license.setStatus(LicenseStatus.ACTIVATED);
        license.setActivatedAt(Instant.now());
        license.setDeviceId(deviceId);

        License saved = licenseRepository.save(license);

        addHistory(saved, "ACTIVATE", deviceId);

        return saved;
    }

    // Проверка лицензии
    public boolean checkLicense(String licenseKey,
                                String deviceId) {

        Optional<License> optionalLicense =
                licenseRepository.findByLicenseKey(licenseKey);

        if (optionalLicense.isEmpty()) {
            return false;
        }

        License license = optionalLicense.get();

        if (license.getStatus() != LicenseStatus.ACTIVATED) {
            return false;
        }

        // проверка срока действия
        if (license.getExpiresAt().isBefore(Instant.now())) {

            license.setStatus(LicenseStatus.EXPIRED);

            licenseRepository.save(license);

            return false;
        }

        addHistory(license, "CHECK", deviceId);

        return true;
    }

    // Продление лицензии
    public License renewLicense(String licenseKey,
                                int days) {

        License license = licenseRepository
                .findByLicenseKey(licenseKey)
                .orElseThrow(() ->
                        new RuntimeException("License not found")
                );

        license.setExpiresAt(
                license.getExpiresAt()
                        .plusSeconds(days * 24L * 3600)
        );

        if (license.getStatus() == LicenseStatus.EXPIRED) {
            license.setStatus(LicenseStatus.ACTIVATED);
        }

        License saved = licenseRepository.save(license);

        addHistory(saved, "RENEW", null);

        return saved;
    }

    // Генерация тикета
    public TicketResponse generateTicket(String licenseKey) {

        License license = licenseRepository
                .findByLicenseKey(licenseKey)
                .orElseThrow(() ->
                        new RuntimeException("License not found")
                );

        Ticket ticket = new Ticket();

        ticket.setServerTime(Instant.now());

        // время жизни тикета 1 час
        ticket.setTtl(3600L);

        ticket.setActivatedAt(
                license.getActivatedAt()
        );

        ticket.setExpiresAt(
                license.getExpiresAt()
        );

        ticket.setUserId(
                license.getUserId()
        );

        ticket.setDeviceId(
                license.getDeviceId()
        );

        ticket.setBlocked(
                license.getStatus() == LicenseStatus.BLOCKED
                        || license.getStatus() == LicenseStatus.REVOKED
        );

        String signature =
                signatureService.signTicket(ticket);

        return new TicketResponse(ticket, signature);
    }

    // Получение лицензий пользователя
    public List<License> getUserLicenses(Long userId) {

        return licenseRepository.findByUserId(userId);
    }

    // История операций
    private void addHistory(License license,
                            String action,
                            String deviceId) {

        LicenseHistory history = new LicenseHistory();

        history.setLicense(license);
        history.setAction(action);
        history.setDeviceId(deviceId);

        historyRepository.save(history);
    }
}