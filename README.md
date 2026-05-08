# SearchX — Full-Text Search Engine

Built from scratch with Java Spring Boot, PostgreSQL, and React.

## Features
- Inverted index built in Java (no Elasticsearch)
- TF-IDF ranking with per-document scoring
- Porter-lite stemmer and stop-word removal
- Levenshtein fuzzy matching (handles typos, edit distance ≤ 2)
- Multi-threaded async web crawler (Jsoup)
- React frontend with real-time search, timing display, and crawl panel

## Quick Start

### 1. Start PostgreSQL
```bash
docker-compose up -d
```

### 2. Start backend
```bash
cd backend
./mvnw spring-boot:run
```

### 3. Start frontend
```bash
cd frontend
npm install
npm run dev
```

### 4. Crawl some pages
```bash
curl -X POST "http://localhost:8080/api/crawl?url=https://en.wikipedia.org/wiki/Java_(programming_language)&maxPages=100"
```

### 5. Search
Open http://localhost:5173 and search!

Or via API:
```bash
curl "http://localhost:8080/api/search?q=concurreny&limit=5"
# Works even with the typo — fuzzy matching catches it
```

## API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/search?q=query | Search indexed documents |
| POST | /api/crawl?url=URL | Start crawling a URL |
| GET | /api/crawl/status | Crawl progress |
| GET | /api/stats | Index statistics |
| GET | /api/health | Health check |

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.2, Spring Data JPA
- **Database**: PostgreSQL 16
- **Crawler**: Jsoup
- **Frontend**: React 18, Vite
- **Algorithms**: Inverted index, TF-IDF, Levenshtein distance, Porter stemmer
