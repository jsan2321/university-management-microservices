ALTER TABLE students
    ADD CONSTRAINT chk_students_user_id_required
    CHECK (user_id IS NOT NULL) NOT VALID;
