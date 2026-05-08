package com.search.controller;

import com.search.crawler.WebCrawler;
import com.search.dto.SearchResult;
import com.search.repository.DocumentRepository;
import com.search.repository.IndexRepository;
import com.search.service.SearchService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class SearchController {

    private final SearchService searchService;
    private final WebCrawler crawler;
    private final DocumentRepository docRepo;
    private final IndexRepository indexRepo;

    @Value("${search.results.default-limit:10}")
    private int defaultLimit;

    public SearchController(SearchService searchService,
                             WebCrawler crawler,
                             DocumentRepository docRepo,
                             IndexRepository indexRepo) {
        this.searchService = searchService;
        this.crawler       = crawler;
        this.docRepo       = docRepo;
        this.indexRepo     = indexRepo;
    }

    // GET /api/search?q=java+programming&limit=10
    @GetMapping("/search")
    public ResponseEntity<List<SearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        long start = System.currentTimeMillis();
        List<SearchResult> results = searchService.search(q, Math.min(limit, 50));
        long elapsed = System.currentTimeMillis() - start;

        return ResponseEntity.ok()
            .header("X-Search-Time-Ms", String.valueOf(elapsed))
            .header("X-Result-Count", String.valueOf(results.size()))
            .body(results);
    }

    // POST /api/crawl?url=https://example.com&maxPages=50
    @PostMapping("/crawl")
    public ResponseEntity<Map<String, String>> crawl(
            @RequestParam String url,
            @RequestParam(defaultValue = "50") int maxPages) {

        if (crawler.isCrawling()) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "A crawl is already in progress"));
        }

        crawler.crawl(url, maxPages);
        return ResponseEntity.accepted()
            .body(Map.of(
                "status",   "started",
                "url",      url,
                "maxPages", String.valueOf(maxPages)
            ));
    }

    // GET /api/crawl/status
    @GetMapping("/crawl/status")
    public ResponseEntity<Map<String, Object>> crawlStatus() {
        return ResponseEntity.ok(Map.of(
            "crawling",      crawler.isCrawling(),
            "crawledCount",  crawler.getCrawledCount()
        ));
    }

    // GET /api/stats
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> stats() {
        return ResponseEntity.ok(Map.of(
            "totalDocuments", docRepo.count(),
            "totalTerms",     indexRepo.countDistinctTerms(),
            "lastIndexed",    docRepo.findLatestIndexedAt() != null
                                ? docRepo.findLatestIndexedAt().toString()
                                : "never"
        ));
    }

    // GET /api/health
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
