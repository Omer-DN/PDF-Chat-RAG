-- טבלאות נדרשות לאפליקציית RAG PDF
-- הרץ PostgreSQL (מסד rag_llm): psql -U postgres -d rag_llm -f schema.sql
-- או דרך כלי ניהול DB.
-- הערה: הסקריפט לא מוחק את users או נתוני משתמשים.

-- מחיקת טבלאות ישנות שלא בשימוש (לפני יצירת הטבלאות הנדרשות)
DROP TABLE IF EXISTS pdf_chunk_embeddings;
DROP TABLE IF EXISTS pdf_document;

-- הרחבת וקטורים (נדרשת ל-pdf_chunks)
CREATE EXTENSION IF NOT EXISTS vector;

-- טבלת קבצי PDF שהועלו (user_id = בעלות)
CREATE TABLE IF NOT EXISTS pdf_files (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    filename        VARCHAR(255) NOT NULL,
    data            BYTEA NOT NULL,
    uploaded_at     TIMESTAMP NOT NULL
);
-- אם הטבלה כבר קיימת בלי user_id:
-- ALTER TABLE pdf_files ADD COLUMN IF NOT EXISTS user_id BIGINT;
-- UPDATE pdf_files SET user_id = 1 WHERE user_id IS NULL;

-- טבלת מקטעי טקסט + embedding (לחיפוש RAG)
CREATE TABLE IF NOT EXISTS pdf_chunks (
    id           BIGSERIAL PRIMARY KEY,
    pdf_id       BIGINT NOT NULL,
    text         TEXT NOT NULL,
    chunk_number INT NOT NULL,
    embedding    vector(768)
);

CREATE INDEX IF NOT EXISTS idx_pdf_chunks_pdf_id ON pdf_chunks(pdf_id);
-- אופציונלי: אחרי שיש נתונים ב-pdf_chunks, אפשר להוסיף לאיצור חיפוש:
-- CREATE INDEX idx_pdf_chunks_embedding ON pdf_chunks USING ivfflat (embedding vector_cosine_ops) WITH (lists = 100);

-- טבלת משתמשים
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    google_sub    VARCHAR(255) UNIQUE,
    created_at    TIMESTAMP NOT NULL
);

-- טבלת שאלות ותשובות (לכל משתמש ומסמך)
CREATE TABLE IF NOT EXISTS question_answers (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    pdf_id     BIGINT NOT NULL,
    question   TEXT NOT NULL,
    answer     TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_question_answers_user_id ON question_answers(user_id);
CREATE INDEX IF NOT EXISTS idx_question_answers_user_pdf ON question_answers(user_id, pdf_id);
