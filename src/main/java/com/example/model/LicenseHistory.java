package com.example.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "license_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LicenseHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "license_id", nullable = false)
    private License license;

    @Column(nullable = false)
    private String action; // CREATE, ACTIVATE, CHECK, RENEW, BLOCK, REVOKE

    @Column(name = "device_id")
    private String deviceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
