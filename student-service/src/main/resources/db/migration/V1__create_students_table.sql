CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE students (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          student_code VARCHAR(30) NOT NULL UNIQUE,
                          first_name VARCHAR(100) NOT NULL,
                          last_name VARCHAR(100) NOT NULL,
                          gender VARCHAR(20),
                          date_of_birth DATE,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          phone VARCHAR(30),
                          address TEXT,
                          program_id UUID NOT NULL,
                          admission_date DATE NOT NULL,
                          status VARCHAR(30) NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_students_program_id
    ON students(program_id);

CREATE INDEX idx_students_email
    ON students(email);

CREATE INDEX idx_students_status
    ON students(status);