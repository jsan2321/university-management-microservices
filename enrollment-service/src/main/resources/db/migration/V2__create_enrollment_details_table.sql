CREATE TABLE enrollment_details (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    enrollment_id UUID NOT NULL,
    section_id UUID NOT NULL,
    subject_id UUID NOT NULL,
    credits INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_enrollment_detail_section UNIQUE(enrollment_id, section_id)
);

CREATE INDEX idx_enrollment_details_enrollment_id ON enrollment_details(enrollment_id);
CREATE INDEX idx_enrollment_details_section_id ON enrollment_details(section_id);
CREATE INDEX idx_enrollment_details_subject_id ON enrollment_details(subject_id);
