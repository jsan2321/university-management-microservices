package com.panadi.ums.attendanceservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.attendanceservice.application.PageResult;
import com.panadi.ums.attendanceservice.application.port.out.AttendanceRepositoryPort;
import com.panadi.ums.attendanceservice.domain.model.Attendance;
import com.panadi.ums.attendanceservice.domain.model.AttendanceSession;
import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Repository
@Transactional
class AttendancePersistenceAdapter implements AttendanceRepositoryPort {
    private final AttendanceSessionJpaRepository sessions;
    private final AttendanceJpaRepository attendances;

    AttendancePersistenceAdapter(AttendanceSessionJpaRepository sessions, AttendanceJpaRepository attendances) {
        this.sessions = sessions;
        this.attendances = attendances;
    }

    @Override
    public AttendanceSession saveSession(AttendanceSession session) {
        return toDomain(sessions.save(toEntity(session)));
    }

    @Override
    public Optional<AttendanceSession> findSessionById(UUID id) {
        return sessions.findById(id).map(this::toDomain);
    }

    @Override
    public PageResult<AttendanceSession> findSessions(UUID sectionId, int page, int size) {
        return toPage(sessions.findAll(sessionSpec(sectionId), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsSessionBySectionIdAndSessionNumber(UUID sectionId, int sessionNumber) {
        return sessions.existsBySectionIdAndSessionNumber(sectionId, sessionNumber);
    }

    @Override
    public Attendance saveAttendance(Attendance attendance) {
        return toDomain(attendances.save(toEntity(attendance)));
    }

    @Override
    public Optional<Attendance> findAttendance(UUID sessionId, UUID studentId) {
        return attendances.findByAttendanceSessionIdAndStudentId(sessionId, studentId).map(this::toDomain);
    }

    @Override
    public PageResult<Attendance> findAttendances(UUID sessionId, AttendanceStatus status, int page, int size) {
        return toPage(attendances.findAll(attendanceSpec(sessionId, status), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public long countSessionsBySectionId(UUID sectionId) {
        return sessions.countBySectionId(sectionId);
    }

    @Override
    public long countPresentByStudentIdAndSectionId(UUID studentId, UUID sectionId) {
        return attendances.countByStudentIdAndSectionIdAndStatus(studentId, sectionId, AttendanceStatus.PRESENT);
    }

    @Override
    public long countAttendancesByStudentIdAndSectionId(UUID studentId, UUID sectionId) {
        return attendances.countByStudentIdAndSectionId(studentId, sectionId);
    }

    private AttendanceSessionEntity toEntity(AttendanceSession domain) {
        AttendanceSessionEntity entity = new AttendanceSessionEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.sectionId = domain.sectionId();
        entity.sessionNumber = domain.sessionNumber();
        entity.date = domain.date();
        entity.topic = domain.topic();
        entity.createdAt = domain.createdAt() == null ? LocalDateTime.now() : domain.createdAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private AttendanceSession toDomain(AttendanceSessionEntity entity) {
        return new AttendanceSession(entity.id, entity.sectionId, entity.sessionNumber, entity.date, entity.topic, entity.createdAt, entity.updatedAt);
    }

    private AttendanceEntity toEntity(Attendance domain) {
        AttendanceEntity entity = new AttendanceEntity();
        entity.id = domain.id() == null ? UUID.randomUUID() : domain.id();
        entity.attendanceSessionId = domain.attendanceSessionId();
        entity.studentId = domain.studentId();
        entity.status = domain.status();
        entity.recordedAt = domain.recordedAt() == null ? LocalDateTime.now() : domain.recordedAt();
        entity.updatedAt = domain.updatedAt() == null ? LocalDateTime.now() : domain.updatedAt();
        return entity;
    }

    private Attendance toDomain(AttendanceEntity entity) {
        return new Attendance(entity.id, entity.attendanceSessionId, entity.studentId, entity.status, entity.recordedAt, entity.updatedAt);
    }

    private Specification<AttendanceSessionEntity> sessionSpec(UUID sectionId) {
        return (root, query, cb) -> sectionId == null ? cb.conjunction() : cb.equal(root.get("sectionId"), sectionId);
    }

    private Specification<AttendanceEntity> attendanceSpec(UUID sessionId, AttendanceStatus status) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("attendanceSessionId"), sessionId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private <T, R> PageResult<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
