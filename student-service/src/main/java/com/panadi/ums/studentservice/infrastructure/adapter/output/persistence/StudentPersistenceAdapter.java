package com.panadi.ums.studentservice.infrastructure.adapter.output.persistence;

import com.panadi.ums.studentservice.application.PageResult;
import com.panadi.ums.studentservice.application.port.out.StudentRepositoryPort;
import com.panadi.ums.studentservice.domain.model.Student;
import com.panadi.ums.studentservice.domain.model.StudentStatus;
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
class StudentPersistenceAdapter implements StudentRepositoryPort {
    private final StudentJpaRepository repository;

    StudentPersistenceAdapter(StudentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Student saveStudent(Student student) {
        return toDomain(repository.save(toEntity(student)));
    }

    @Override
    public Optional<Student> findStudentById(UUID id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Student> findStudentByUserId(UUID userId) {
        return repository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public PageResult<Student> findStudents(StudentStatus status, UUID programId, int page, int size) {
        return toPage(repository.findAll(studentSpec(status, programId), PageRequest.of(page, size)), this::toDomain);
    }

    @Override
    public boolean existsByStudentCode(String studentCode, UUID excludedId) {
        return excludedId == null ? repository.existsByStudentCode(studentCode) : repository.existsByStudentCodeAndIdNot(studentCode, excludedId);
    }

    @Override
    public boolean existsByEmail(String email, UUID excludedId) {
        return excludedId == null ? repository.existsByEmail(email) : repository.existsByEmailAndIdNot(email, excludedId);
    }

    @Override
    public boolean existsByUserId(UUID userId, UUID excludedId) {
        return excludedId == null ? repository.existsByUserId(userId) : repository.existsByUserIdAndIdNot(userId, excludedId);
    }

    private StudentEntity toEntity(Student student) {
        StudentEntity entity = new StudentEntity();
        entity.id = student.id() == null ? UUID.randomUUID() : student.id();
        entity.userId = student.userId();
        entity.studentCode = student.studentCode();
        entity.firstName = student.firstName();
        entity.lastName = student.lastName();
        entity.gender = student.gender();
        entity.dateOfBirth = student.dateOfBirth();
        entity.email = student.email();
        entity.phone = student.phone();
        entity.address = student.address();
        entity.programId = student.programId();
        entity.admissionDate = student.admissionDate();
        entity.status = student.status();
        entity.createdAt = student.createdAt() == null ? LocalDateTime.now() : student.createdAt();
        entity.updatedAt = student.updatedAt() == null ? LocalDateTime.now() : student.updatedAt();
        return entity;
    }

    private Student toDomain(StudentEntity entity) {
        return new Student(entity.id, entity.userId, entity.studentCode, entity.firstName, entity.lastName, entity.gender, entity.dateOfBirth, entity.email, entity.phone, entity.address, entity.programId, entity.admissionDate, entity.status, entity.createdAt, entity.updatedAt);
    }

    private Specification<StudentEntity> studentSpec(StudentStatus status, UUID programId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (programId != null) {
                predicates.add(cb.equal(root.get("programId"), programId));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private <T, R> PageResult<R> toPage(Page<T> page, Function<T, R> mapper) {
        return new PageResult<>(page.getContent().stream().map(mapper).toList(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
