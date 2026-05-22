package com.example.controller;

import com.example.dto.TicketResponse;
import com.example.model.License;
import com.example.service.LicenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/licenses")
@RequiredArgsConstructor
public class LicenseController {

    private final LicenseService licenseService;

    // 1. Создание лицензии
    @PostMapping("/create")
    public License createLicense(
            @RequestParam Long userId,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") Integer maxDevices
    ) {

        return licenseService.createLicense(
                userId,
                description,
                maxDevices
        );
    }

    // 2. Активация лицензии
    @PostMapping("/activate")
    public License activateLicense(
            @RequestParam String licenseKey,
            @RequestParam String deviceId
    ) {

        return licenseService.activateLicense(
                licenseKey,
                deviceId
        );
    }

    // 3. Проверка лицензии
    @GetMapping("/check")
    public boolean checkLicense(
            @RequestParam String licenseKey,
            @RequestParam String deviceId
    ) {

        return licenseService.checkLicense(
                licenseKey,
                deviceId
        );
    }

    // 4. Продление лицензии
    @PostMapping("/renew")
    public License renewLicense(
            @RequestParam String licenseKey,
            @RequestParam int days
    ) {

        return licenseService.renewLicense(
                licenseKey,
                days
        );
    }

    // 5. Получение тикета
    @GetMapping("/ticket/{licenseKey}")
    public TicketResponse getTicket(
            @PathVariable String licenseKey
    ) {

        return licenseService.generateTicket(
                licenseKey
        );
    }

    // 6. Список лицензий пользователя
    @GetMapping("/user/{userId}")
    public List<License> getUserLicenses(
            @PathVariable Long userId
    ) {

        return licenseService.getUserLicenses(userId);
    }
}