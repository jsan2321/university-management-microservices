package com.panadi.ums.identityservice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "provisioning_requests")
class ProvisioningRecord {
    @Id UUID id;
    @Column(name = "idempotency_key", nullable = false, unique = true) String idempotencyKey;
    @Column(name = "profile_type", nullable = false) String profileType;
    @Column(name = "user_id") UUID userId;
    @Column(name = "profile_id") UUID profileId;
    @Column(nullable = false) String status;
    @Column(name = "error_message") String errorMessage;
    @Column(name = "created_at", nullable = false) LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) LocalDateTime updatedAt;

    static ProvisioningRecord pending(String key, String profileType) {
        ProvisioningRecord value = new ProvisioningRecord();
        value.id = UUID.randomUUID();
        value.idempotencyKey = key;
        value.profileType = profileType;
        value.status = "PENDING";
        value.createdAt = LocalDateTime.now();
        value.updatedAt = value.createdAt;
        return value;
    }

    void update(String status, UUID userId, UUID profileId, String errorMessage) {
        this.status = status;
        this.userId = userId;
        this.profileId = profileId;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
    }
}
