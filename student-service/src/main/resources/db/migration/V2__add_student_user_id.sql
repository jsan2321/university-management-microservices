ALTER TABLE students
    ADD COLUMN user_id UUID;

CREATE INDEX idx_students_user_id
    ON students(user_id);
