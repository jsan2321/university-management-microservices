CREATE UNIQUE INDEX uq_teachers_user_id
    ON teachers(user_id)
    WHERE user_id IS NOT NULL;
