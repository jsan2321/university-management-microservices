package com.panadi.ums.auditservice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditRecordRepository repository;

    public AuditController(AuditRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditRecord> getAudits(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String producer,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String aggregateType
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "occurredAt"));
        Specification<AuditRecord> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (producer != null && !producer.isBlank()) {
                predicates.add(cb.equal(root.get("producer"), producer));
            }
            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(root.get("eventType"), eventType));
            }
            if (aggregateType != null && !aggregateType.isBlank()) {
                predicates.add(cb.equal(root.get("aggregateType"), aggregateType));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }
}
