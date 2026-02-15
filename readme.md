# Job Search Engine System

A scalable full-stack job search application combining search-engine retrieval
with backend rule-based ranking and fault-tolerant fallback mechanisms.

# Project Overview

This project simulates how real-world job platforms handle search at scale.

Instead of relying only on database queries, the system uses:
- A search engine for fast candidate retrieval
- Backend services for business-driven relevance ranking
- A fallback mechanism to ensure reliability

# System Architecture

Frontend (React)
        ↓
Backend API (Spring Boot)
        ↓
Primary: OpenSearch (retrieval)
        ↓
Fallback: CSV-based in-memory search (if OpenSearch fails)

##  Core Features

### Large-Scale Data Ingestion
- Processed ~20,000 real job records using Python
- Normalized job titles, descriptions, and salary fields
- Built an indexing pipeline to ingest data into OpenSearch

### Search Engine–Based Retrieval
- Keyword-based retrieval using OpenSearch
- Multi-field search across title and description
- Result size limiting for performance

### Backend Rule-Based Ranking
- Custom relevance scoring in Java
- Title matches weighted higher than description matches
- Salary used as a secondary boost

### Fallback Strategy
- Automatic fallback to in-memory CSV search
- Ensures uninterrupted search when OpenSearch is unavailable

##  Tech Stack

**Backend:** Java 17, Spring Boot, REST APIs, Maven  
**Search:** OpenSearch  
**Frontend:** React, HTML, CSS, JavaScript  
**Data Processing:** Python, Pandas  
**Tools:** Git, GitHub, IntelliJ IDEA, VS Code

## Running the Project

### Start OpenSearch
``bash
bin/opensearch

Run Backend:
cd backend
mvn spring-boot:run

Index Jobs:
POST http://localhost:8080/index

Run Frontend:
cd frontend
npm install
npm run dev



##  Author

**Mahidhar Agraharapu**  
B.Tech – Artificial Intelligence & Machine Learning  
GitHub: https://github.com/mahidhar2026



