package com.panadi.ums.identityservice;

import com.panadi.ums.identityservice.ProvisioningDtos.ProvisionTeacherRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProvisioningServiceTests {
    private final ProvisionTeacherRequest request = new ProvisionTeacherRequest(
            "ada.personal@example.com", "Ada", "Lovelace", UUID.randomUUID(), null, LocalDate.now());

    @Test
    void createsInvitationIdentityThenProfile() {
        ProvisioningRepository records = repository();
        KeycloakAdminClient keycloak = mock(KeycloakAdminClient.class);
        TeacherProfileClient teachers = mock(TeacherProfileClient.class);
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        when(keycloak.createProvisionedUser(any(), any(), any(), any(), any(), any())).thenReturn(userId);
        when(teachers.create(any())).thenReturn(new ProfileResponse(profileId, userId));

        var response = service(records, keycloak, teachers).provisionTeacher("request-1", request);

        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.userId()).isEqualTo(userId);
        assertThat(response.profileId()).isEqualTo(profileId);
        verify(keycloak, never()).enable(userId);
        verify(keycloak, never()).delete(userId);
    }

    @Test
    void deletesDisabledIdentityWhenProfileCreationFails() {
        ProvisioningRepository records = repository();
        KeycloakAdminClient keycloak = mock(KeycloakAdminClient.class);
        TeacherProfileClient teachers = mock(TeacherProfileClient.class);
        UUID userId = UUID.randomUUID();
        when(keycloak.createProvisionedUser(any(), any(), any(), any(), any(), any())).thenReturn(userId);
        when(teachers.create(any())).thenThrow(new RuntimeException("profile conflict"));

        assertThatThrownBy(() -> service(records, keycloak, teachers).provisionTeacher("request-2", request))
                .isInstanceOf(ProvisioningException.class);
        verify(keycloak).delete(userId);
        verify(keycloak, never()).enable(userId);
    }

    @Test
    void returnsCompletedResultForRepeatedIdempotencyKey() {
        ProvisioningRepository records = mock(ProvisioningRepository.class);
        ProvisioningRecord completed = ProvisioningRecord.pending("same-key", "TEACHER");
        completed.update("COMPLETED", UUID.randomUUID(), UUID.randomUUID(), null);
        when(records.findByIdempotencyKey("same-key")).thenReturn(Optional.of(completed));
        KeycloakAdminClient keycloak = mock(KeycloakAdminClient.class);

        var response = service(records, keycloak, mock(TeacherProfileClient.class)).provisionTeacher("same-key", request);

        assertThat(response.status()).isEqualTo("COMPLETED");
        verify(keycloak, never()).createProvisionedUser(any(), any(), any(), any(), any(), any());
    }

    private ProvisioningRepository repository() {
        ProvisioningRepository records = mock(ProvisioningRepository.class);
        when(records.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(records.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return records;
    }

    private ProvisioningService service(ProvisioningRepository records, KeycloakAdminClient keycloak, TeacherProfileClient teachers) {
        IdentityGenerator identities = mock(IdentityGenerator.class);
        when(identities.next(any(), any(), any())).thenReturn(new IdentityGenerator.IdentityBundle("TCH-2026-00001", "tch202600001", "tch202600001@ums.local"));
        return new ProvisioningService(records, keycloak, teachers, mock(StudentProfileClient.class), identities, mock(com.panadi.ums.auditcommon.AuditOutbox.class), mock(EmailService.class));
    }
}
