package com.panadi.ums.enrollmentservice.application.service;

import com.panadi.ums.enrollmentservice.application.ApplicationException;
import com.panadi.ums.enrollmentservice.application.PageResult;
import com.panadi.ums.enrollmentservice.application.ResourceNotFoundException;
import com.panadi.ums.enrollmentservice.application.command.CreateEnrollmentCommand;
import com.panadi.ums.enrollmentservice.application.port.in.EnrollmentUseCase;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.ScheduleSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.SectionSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.AcademicCatalogLookupPort.SubjectSnapshot;
import com.panadi.ums.enrollmentservice.application.port.out.EnrollmentRepositoryPort;
import com.panadi.ums.enrollmentservice.application.port.out.StudentLookupPort;
import com.panadi.ums.enrollmentservice.domain.model.Enrollment;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentDetail;
import com.panadi.ums.enrollmentservice.domain.model.EnrollmentStatus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class EnrollmentService implements EnrollmentUseCase {
    private static final int MAX_CREDITS = 22;

    private final EnrollmentRepositoryPort enrollments;
    private final StudentLookupPort students;
    private final AcademicCatalogLookupPort academic;

    public EnrollmentService(EnrollmentRepositoryPort enrollments, StudentLookupPort students, AcademicCatalogLookupPort academic) {
        this.enrollments = enrollments;
        this.students = students;
        this.academic = academic;
    }

    @Override
    public Enrollment createEnrollment(CreateEnrollmentCommand command) {
        requireSectionIds(command.sectionIds());
        StudentLookupPort.StudentSnapshot student = students.getStudent(command.studentId());
        if (!student.isActive()) {
            throw new ApplicationException("Student is not active");
        }
        AcademicCatalogLookupPort.SemesterSnapshot semester = academic.getSemester(command.semesterId());
        if (!semester.isActive()) {
            throw new ApplicationException("Semester is not active");
        }
        if (enrollments.existsActiveEnrollment(command.studentId(), command.semesterId())) {
            throw new ApplicationException("Student already has an active enrollment for this semester");
        }

        List<SectionSnapshot> sections = command.sectionIds().stream().map(academic::getSection).toList();
        List<SubjectSnapshot> subjects = sections.stream().map(section -> academic.getSubject(section.subjectId())).toList();
        validateAcademicSnapshots(command.semesterId(), student.programId(), sections, subjects);
        validateCapacity(sections);
        validateSchedules(sections);

        List<EnrollmentDetail> details = new ArrayList<>();
        for (int i = 0; i < sections.size(); i++) {
            SectionSnapshot section = sections.get(i);
            SubjectSnapshot subject = subjects.get(i);
            details.add(EnrollmentDetail.create(section.id(), subject.id(), subject.credits()));
        }

        Enrollment enrollment = Enrollment.create(command.studentId(), command.semesterId(), details);
        if (enrollment.totalCredits() > MAX_CREDITS) {
            throw new ApplicationException("Enrollment exceeds maximum semester credits");
        }
        return enrollments.saveEnrollment(enrollment);
    }

    @Override
    public Enrollment getEnrollment(UUID id) {
        return enrollments.findEnrollmentById(id).orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));
    }

    @Override
    public PageResult<Enrollment> listEnrollments(UUID studentId, UUID semesterId, EnrollmentStatus status, int page, int size) {
        return enrollments.findEnrollments(studentId, semesterId, status, page, size);
    }

    @Override
    public List<UUID> listActiveStudentIdsBySection(UUID sectionId) {
        return enrollments.findActiveStudentIdsBySectionId(sectionId);
    }

    @Override
    public Enrollment cancelEnrollment(UUID id) {
        return enrollments.saveEnrollment(getEnrollment(id).cancel());
    }

    private void requireSectionIds(List<UUID> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) {
            throw new ApplicationException("At least one section is required");
        }
        Set<UUID> unique = new HashSet<>(sectionIds);
        if (unique.size() != sectionIds.size()) {
            throw new ApplicationException("Duplicate sections are not allowed");
        }
    }

    private void validateAcademicSnapshots(UUID semesterId, UUID studentProgramId, List<SectionSnapshot> sections, List<SubjectSnapshot> subjects) {
        for (SectionSnapshot section : sections) {
            if (!section.isActive()) {
                throw new ApplicationException("Section is not active");
            }
            if (!semesterId.equals(section.semesterId())) {
                throw new ApplicationException("Section does not belong to the requested semester");
            }
        }
        for (SubjectSnapshot subject : subjects) {
            if (!subject.isActive()) {
                throw new ApplicationException("Subject is not active");
            }
            if (!studentProgramId.equals(subject.programId())) {
                throw new ApplicationException("Subject does not belong to the student's program");
            }
        }
        if (subjects.stream().map(SubjectSnapshot::id).distinct().count() != subjects.size()) {
            throw new ApplicationException("Student cannot enroll in multiple sections of the same subject");
        }
    }

    private void validateCapacity(List<SectionSnapshot> sections) {
        for (SectionSnapshot section : sections) {
            if (enrollments.countActiveEnrollmentDetailsBySectionId(section.id()) >= section.capacity()) {
                throw new ApplicationException("Section capacity has been reached");
            }
        }
    }

    private void validateSchedules(List<SectionSnapshot> sections) {
        List<ScheduleSnapshot> seen = new ArrayList<>();
        for (SectionSnapshot section : sections) {
            for (ScheduleSnapshot schedule : section.schedules()) {
                if (seen.stream().anyMatch(schedule::overlaps)) {
                    throw new ApplicationException("Section schedules conflict");
                }
                seen.add(schedule);
            }
        }
    }
}
