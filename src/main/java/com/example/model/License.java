package com.example.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

@Entity
@Table(name = "licenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class License {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_key", nullable = false, unique = true)
    private String licenseKey;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "device_id")
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LicenseStatus status = LicenseStatus.CREATED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "max_devices", nullable = false)
    private Integer maxDevices = 1;

    private String description;
}