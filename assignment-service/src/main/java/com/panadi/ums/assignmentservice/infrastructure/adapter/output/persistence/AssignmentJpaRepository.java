package com.panadi.ums.assignmentservice.infrastructure.adapter.output.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

interface AssignmentJpaRepository extends JpaRepository<AssignmentEntity, UUID>, JpaSpecificationExecutor<AssignmentEntity> { }
