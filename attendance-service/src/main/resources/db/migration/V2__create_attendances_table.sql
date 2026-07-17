CREATE TABLE attendances (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    attendance_session_id UUID NOT NULL,
    student_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL,
    recorded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance_student_session UNIQUE(attendance_session_id, student_id)
);

CREATE INDEX idx_attendances_session_id ON attendances(attendance_session_id);
CREATE INDEX idx_attendances_student_id ON attendances(student_id);
CREATE INDEX idx_attendances_status ON attendances(status);
