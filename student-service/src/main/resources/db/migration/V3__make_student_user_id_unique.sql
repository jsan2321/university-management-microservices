CREATE UNIQUE INDEX uq_students_user_id
    ON students(user_id)
    WHERE user_id IS NOT NULL;
