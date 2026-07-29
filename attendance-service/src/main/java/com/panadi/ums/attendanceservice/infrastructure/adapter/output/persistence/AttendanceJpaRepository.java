package com.panadi.ums.attendanceservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.attendanceservice.domain.model.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

interface AttendanceJpaRepository extends JpaRepository<AttendanceEntity, UUID>, JpaSpecificationExecutor<AttendanceEntity> {
    Optional<AttendanceEntity> findByAttendanceSessionIdAndStudentId(UUID attendanceSessionId, UUID studentId);

    @Query("""
            select count(attendance)
            from AttendanceEntity attendance, AttendanceSessionEntity session
            where attendance.attendanceSessionId = session.id
              and attendance.studentId = :studentId
              and session.sectionId = :sectionId
              and attendance.status = :status
            """)
    long countByStudentIdAndSectionIdAndStatus(@Param("studentId") UUID studentId, @Param("sectionId") UUID sectionId, @Param("status") AttendanceStatus status);

    @Query("""
            select count(attendance)
            from AttendanceEntity attendance, AttendanceSessionEntity session
            where attendance.attendanceSessionId = session.id
              and attendance.studentId = :studentId
              and session.sectionId = :sectionId
            """)
    long countByStudentIdAndSectionId(@Param("studentId") UUID studentId, @Param("sectionId") UUID sectionId);
}
