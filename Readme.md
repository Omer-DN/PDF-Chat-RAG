# RAG AI PDF Assistant

## Overview

This project is a Full-Stack Retrieval-Augmented Generation (RAG) system. 
It enables users to upload PDF documents, which are then processed, "chunked", and converted into vector embeddings using the Gemini API. 
Users can then perform semantic searches and ask complex questions based on the content of their uploaded documents.

---

## Features

* User Authentication: Full registration and login system with persistent history.

* Persistent Sidebar: Automatically retrieves and displays the user's previously uploaded PDF history from the database upon login.

* Smart PDF Processing: - Text extraction using PDFBox.

 - Intelligent text splitting into manageable chunks.

 - Real-time vector embedding generation via Google Gemini API.

* Vector Storage: Integrated with PostgreSQL (and prepared for pgvector) to store document chunks and their associated embeddings.

* Interactive Chat: Modern React UI for chatting with documents, featuring loading states and real-time feedback.

---

## Technologies Used

Backend (Java/Spring Boot)
* Spring Boot 3.x & Spring Data JPA

* PostgreSQL (Database)

* PDFBox (PDF processing)

* Gemini API (LLM & Embeddings)

* Jackson (JSON handling with @JsonIgnore for optimized performance)

Frontend (React)
* React.js (Vite)

* Tailwind CSS (Styling)

* Lucide React (Iconography)

* Axios (API communication)

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
Backend:
    mvn clean install
    mvn spring-boot:run

Frontend:
    npm install
    npm run dev

### API Endpoints
Authentication
* POST /api/users/register - Create a new account.

* POST /api/users/login - Authenticate and get user details.

PDF Management
* POST /api/pdf/upload - Upload PDF and generate embeddings.

* GET /api/pdf/user/{userId} - (New) Retrieve all files belonging to a specific user.

RAG Operations
* POST /api/rag/ask - Send a question and get an AI answer based on a specific PDF.



### Future Work / Roadmap

[ ] Transition to pgvector for native vector similarity search.

[ ] Add support for multiple PDF selection in a single chat.

[ ] Implement chat history persistence (saving the messages themselves).

[x] Implement user-specific file history.
