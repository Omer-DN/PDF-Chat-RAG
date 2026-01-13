# RAG LLM PDF Backend

## Overview

This project is a **Java Spring Boot backend** for a **RAG (Retrieval-Augmented Generation)** system using PDF files.  
It allows uploading PDFs, extracting text, splitting it into chunks, generating embeddings, and storing everything in PostgreSQL.  
Later, embeddings will be used for semantic search and question-answering (RAG) via LangChain and Gemini API.

Currently, embeddings are dummy placeholders (`byte[]`). Future updates will integrate **Gemini embeddings**, **pgvector**, and **semantic search**.

---

## Features

- Upload PDF files via REST API
- Extract text from PDFs
- Split text into chunks
- Save PDF and chunks to PostgreSQL
- Generate dummy embeddings
- REST endpoints to retrieve chunks
- Future: Semantic search over PDFs using embeddings and RAG (LangChain)

---

## Technologies Used

- Java 17+
- Spring Boot 3.2.x
- Spring Data JPA
- PostgreSQL + pgvector
- Maven
- PDFBox
- SpringDoc OpenAPI (Swagger)
- Docker
- Gemini API (planned)
- LangChain / RAG (planned)
- Lombok (optional)

---

## Docker Setup

### 1. Run PostgreSQL with pgvector

```bash
docker run -d \
  -p 5432:5432 \
  -v postgresdata:/var/lib/postgresql/data \
  -e POSTGRES_DB=rag_llm \
  -e POSTGRES_PASSWORD=postgres \
  --name rag_postgres \
  postgres

```

### 2. Update application.properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/rag_llm
    spring.datasource.username=postgres
    spring.datasource.password=postgres
    spring.jpa.hibernate.ddl-auto=update

# Gemini API key placeholder
    gemini.api-key=YOUR_GEMINI_KEY


### 3. Build & Run the Backend
    mvn clean install
    mvn spring-boot:run

### API Endpoints
    Upload PDF
    POST /api/pdf/upload
    Content-Type: multipart/form-data
    Form field: file


### Response:
    {
    "message": "PDF uploaded successfully with embeddings",
    "pdfId": 1,
    "filename": "example.pdf",
    "numChunks": 5
    }

### Retrieve Chunks     
    GET /api/pdf/{pdfId}/chunks


### Response:
    {
    "pdfId": 1,
    "numChunks": 5,
    "chunks": [
    "First chunk text...",
    "Second chunk text..."
    ]
    }

Future Work / Roadmap

Integrate Gemini API to generate real embeddings.

Store embeddings as vector type using pgvector for similarity search.

Implement /ask endpoint for question-answering over PDFs using RAG / LangChain.

Improve error handling and input validation.

Add authentication/authorization.
