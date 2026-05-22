package com.example.repository;

import com.example.model.LicenseHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseHistoryRepository extends JpaRepository<LicenseHistory, Long> {

    List<LicenseHistory> findByLicenseIdOrderByCreatedAtDesc(Long licenseId);
}