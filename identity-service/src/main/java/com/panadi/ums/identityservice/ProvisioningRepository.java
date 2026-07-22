package com.panadi.ums.identityservice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface ProvisioningRepository extends JpaRepository<ProvisioningRecord, UUID> {
    Optional<ProvisioningRecord> findByIdempotencyKey(String idempotencyKey);
}
