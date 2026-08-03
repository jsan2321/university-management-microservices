package com.panadi.ums.identityservice;

import org.springframework.data.jpa.repository.JpaRepository;

interface IdentifierSequenceRepository extends JpaRepository<IdentifierSequence, String> {}
