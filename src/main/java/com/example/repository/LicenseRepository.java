package com.example.repository;

import com.example.model.License;
import com.example.model.LicenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {

    Optional<License> findByLicenseKey(String licenseKey);

    List<License> findByUserId(Long userId);

    List<License> findByUserIdAndStatus(Long userId, LicenseStatus status);

    List<License> findByDeviceId(String deviceId);
}