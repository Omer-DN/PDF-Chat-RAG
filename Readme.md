# RAG LLM PDF Backend

## Overview

**Java Spring Boot** backend for a **RAG (Retrieval-Augmented Generation)** system over PDFs.  
Upload PDFs → extract text → split into chunks → generate **Gemini embeddings** → store in **PostgreSQL + pgvector**.  
Ask questions over your documents via RAG: semantic search on embeddings + **Gemini** for answers.  
Includes **JWT auth**, user-scoped files, and optional **React (Vite)** UI in `rag-ui`.

---

## Features

- **Auth**: Register (שם משתמש, אימייל, סיסמה) / **Login רק עם שם משתמש וסיסמה** (JWT). אימייל נשמר לצרכים בהמשך.
- **PDF**: Upload, list, delete (per user)
- **RAG**: Real embeddings (Gemini), stored in pgvector (768 dims)
- **Q&A**: `POST /api/pdf/{pdfId}/ask` and **streaming** `POST /api/pdf/{pdfId}/ask-stream` (SSE)
- **History**: Per-document Q&A history
- **Re-embed**: Fix documents with zero vectors (`POST /api/pdf/{pdfId}/reembed`)
- **React UI**: Dev server can start with backend (`rag.start-react-dev=true`)

---

## Technologies

- Java 17+
- Spring Boot 3.2.x
- Spring Data JPA
- PostgreSQL + **pgvector** (vector 768)
- **Gemini API** (embeddings + chat)
- Maven
- PDFBox
- SpringDoc OpenAPI (Swagger)
- Docker
- JWT (auth)
- Lombok

---

## Docker – PostgreSQL + pgvector

```bash
docker run -d \
  --name rag-postgres \
  -p 5432:5432 \
  -v postgresdata:/var/lib/postgresql/data \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=rag_llm \
  ankane/pgvector:latest
```

Then run the schema (once):

```bash
psql -U postgres -d rag_llm -f src/main/resources/schema.sql
```

Or use your DB tool; the app uses `spring.jpa.hibernate.ddl-auto=update` for existing tables.

---

## Configuration

Set environment variables (do **not** put secrets in `application.properties`):

| Variable            | Description                    |
|---------------------|--------------------------------|
| `GEMINI_API_KEY`    | Google AI Studio API key       |
| `JWT_SECRET`        | JWT signing secret (min 32 chars; optional, has dev default) |
Optional in `application.properties`:

- `rag.start-react-dev=true` – start React (Vite) dev server with the app
- `rag.react-ui-path=rag-ui` – path to React UI folder

---

## Build & Run

```bash
mvn clean install
mvn spring-boot:run
```

Backend: `http://localhost:8080`.  
Swagger (if enabled): `http://localhost:8080/swagger-ui.html`.

---

## API Endpoints

### Auth (no JWT)

| Method | Path                | Description   |
|--------|---------------------|---------------|
| POST   | `/api/auth/register`| Register (username, email, password; סיסמה נשמרת מוצפנת) |
| POST   | `/api/auth/login`   | **Login רק עם שם משתמש וסיסמה** (returns JWT) |

### PDF (require `Authorization: Bearer <token>`)

| Method | Path                         | Description                          |
|--------|------------------------------|--------------------------------------|
| POST   | `/api/pdf/upload`            | Upload PDF (multipart, field: `file`) |
| GET    | `/api/pdf/list`              | List current user's PDFs            |
| GET    | `/api/pdf/{pdfId}/chunks`    | Get chunks for a PDF                 |
| POST   | `/api/pdf/{pdfId}/ask`       | RAG Q&A (JSON body: `{"question":"..."}`) |
| POST   | `/api/pdf/{pdfId}/ask-stream`| RAG Q&A streaming (SSE)             |
| GET    | `/api/pdf/{pdfId}/history`   | Q&A history for document            |
| POST   | `/api/pdf/{pdfId}/reembed`   | Regenerate embeddings for document   |
| DELETE | `/api/pdf/{pdfId}`           | Delete PDF and related data         |
| DELETE | `/api/pdf/all`               | Delete all current user's data      |

### Example responses

**Upload**

```json
{
  "message": "PDF uploaded successfully with embeddings",
  "pdfId": 1,
  "numChunks": 5
}
```

**Ask**

```json
{
  "answer": "Based on the document..."
}
```

**List**

```json
[
  { "id": 1, "filename": "example.pdf", "uploadedAt": "2025-02-16T..." }
]
```

---

## Future / Roadmap

- IVFFlat/HNSW index on `pdf_chunks.embedding` for faster similarity search (see `schema.sql` comment)
- Stronger input validation and error handling
- Optional auth (e.g. API keys) for server-to-server
