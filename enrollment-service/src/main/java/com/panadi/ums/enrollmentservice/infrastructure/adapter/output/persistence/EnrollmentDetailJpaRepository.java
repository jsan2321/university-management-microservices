package com.panadi.ums.enrollmentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface EnrollmentDetailJpaRepository extends JpaRepository<EnrollmentDetailEntity, UUID> {
    @Query("""
            select count(detail)
            from EnrollmentDetailEntity detail
            where detail.sectionId = :sectionId
              and detail.enrollment.status = :status
            """)
    long countBySectionIdAndEnrollmentStatus(@Param("sectionId") UUID sectionId, @Param("status") EnrollmentStatus status);

    @Query("""
            select distinct detail.enrollment.studentId
            from EnrollmentDetailEntity detail
            where detail.sectionId = :sectionId
              and detail.enrollment.status = :status
            """)
    List<UUID> findStudentIdsBySectionIdAndEnrollmentStatus(@Param("sectionId") UUID sectionId, @Param("status") EnrollmentStatus status);
}
