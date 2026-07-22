ALTER TABLE teachers
    ADD CONSTRAINT chk_teachers_user_id_required
    CHECK (user_id IS NOT NULL) NOT VALID;
