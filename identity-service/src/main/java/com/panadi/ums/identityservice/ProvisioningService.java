package com.panadi.ums.identityservice;

import com.panadi.ums.identityservice.ProvisioningDtos.LinkExistingRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisionStudentRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisionTeacherRequest;
import com.panadi.ums.identityservice.ProvisioningDtos.ProvisioningResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
class ProvisioningService {
    private final ProvisioningRepository records;
    private final KeycloakAdminClient keycloak;
    private final TeacherProfileClient teachers;
    private final StudentProfileClient students;

    ProvisioningService(ProvisioningRepository records, KeycloakAdminClient keycloak,
                        TeacherProfileClient teachers, StudentProfileClient students) {
        this.records = records;
        this.keycloak = keycloak;
        this.teachers = teachers;
        this.students = students;
    }

    ProvisioningResponse provisionTeacher(String key, ProvisionTeacherRequest request) {
        return provision(key, "TEACHER", request.username(), request.email(), request.firstName(), request.lastName(),
                request.temporaryPassword(), userId -> teachers.create(new TeacherProfileRequest(
                        request.departmentId(), userId, request.teacherCode(), request.firstName(), request.lastName(),
                        request.email(), request.phone(), request.hireDate())));
    }

    ProvisioningResponse provisionStudent(String key, ProvisionStudentRequest request) {
        return provision(key, "STUDENT", request.username(), request.email(), request.firstName(), request.lastName(),
                request.temporaryPassword(), userId -> students.create(new StudentProfileRequest(
                        userId, request.studentCode(), request.firstName(), request.lastName(), request.gender(),
                        request.dateOfBirth(), request.email(), request.phone(), request.address(), request.programId(),
                        request.admissionDate())));
    }

    ProvisioningResponse linkTeacher(String key, LinkExistingRequest request) {
        return link(key, "TEACHER", request, () -> teachers.link(request.profileId(), request.userId()));
    }

    ProvisioningResponse linkStudent(String key, LinkExistingRequest request) {
        return link(key, "STUDENT", request, () -> students.link(request.profileId(), request.userId()));
    }

    private ProvisioningResponse provision(String key, String role, String username, String email, String firstName,
                                           String lastName, String password, ProfileCreator creator) {
        ProvisioningRecord existing = records.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if ("COMPLETED".equals(existing.status)) return response(existing);
            if ("PROFILE_CREATED".equals(existing.status) && existing.userId != null && existing.profileId != null) {
                keycloak.enable(existing.userId);
                existing.update("COMPLETED", existing.userId, existing.profileId, null);
                return response(records.save(existing));
            }
            throw new ProvisioningConflictException("This idempotency key is already associated with an incomplete provisioning request");
        }
        ProvisioningRecord record = records.save(ProvisioningRecord.pending(key, role));
        UUID userId = null;
        UUID profileId = null;
        try {
            userId = keycloak.createDisabledUser(username, email, firstName, lastName, password, role);
            record.update("KEYCLOAK_CREATED", userId, null, null);
            records.save(record);
            ProfileResponse profile = creator.create(userId);
            profileId = profile.id();
            record.update("PROFILE_CREATED", userId, profileId, null);
            records.save(record);
            keycloak.enable(userId);
            record.update("COMPLETED", userId, profileId, null);
            return response(records.save(record));
        } catch (RuntimeException exception) {
            compensate(record, userId, profileId, exception);
            throw exception instanceof ProvisioningException provisioning ? provisioning
                    : new ProvisioningException("Profile provisioning failed", exception);
        }
    }

    private ProvisioningResponse link(String key, String role, LinkExistingRequest request, ProfileLinker linker) {
        ProvisioningRecord existing = records.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if ("COMPLETED".equals(existing.status)) return response(existing);
            throw new ProvisioningConflictException("This idempotency key is already associated with an incomplete link request");
        }
        ProvisioningRecord record = records.save(ProvisioningRecord.pending(key, role));
        try {
            keycloak.requireRole(request.userId(), role);
            ProfileResponse profile = linker.link();
            record.update("COMPLETED", request.userId(), profile.id(), null);
            return response(records.save(record));
        } catch (RuntimeException exception) {
            record.update("FAILED", request.userId(), request.profileId(), exception.getMessage());
            records.save(record);
            throw exception instanceof ProvisioningException provisioning ? provisioning
                    : new ProvisioningException("Existing identity link failed", exception);
        }
    }

    private void compensate(ProvisioningRecord record, UUID userId, UUID profileId, RuntimeException cause) {
        if (profileId != null) {
            record.update("PROFILE_CREATED", userId, profileId, cause.getMessage());
            records.save(record);
            return;
        }
        String status = "FAILED";
        if (userId != null) {
            try {
                keycloak.delete(userId);
            } catch (RuntimeException cleanupFailure) {
                status = "COMPENSATION_FAILED";
            }
        }
        record.update(status, userId, null, cause.getMessage());
        records.save(record);
    }

    private ProvisioningResponse response(ProvisioningRecord value) {
        return new ProvisioningResponse(value.id, value.userId, value.profileId, value.profileType, value.status);
    }

    private interface ProfileCreator { ProfileResponse create(UUID userId); }
    private interface ProfileLinker { ProfileResponse link(); }
}
