package com.search.service;

import com.search.dto.SearchResult;
import com.search.indexer.Tokenizer;
import com.search.model.Document;
import com.search.model.IndexEntry;
import com.search.repository.DocumentRepository;
import com.search.repository.IndexRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {

    private final IndexRepository indexRepo;
    private final DocumentRepository docRepo;
    private final Tokenizer tokenizer;

    @Value("${search.fuzzy.max-edit-distance:2}")
    private int maxEditDistance;

    public SearchService(IndexRepository indexRepo,
                         DocumentRepository docRepo,
                         Tokenizer tokenizer) {
        this.indexRepo = indexRepo;
        this.docRepo   = docRepo;
        this.tokenizer = tokenizer;
    }

    public List<SearchResult> search(String query, int limit) {
        List<String> queryTerms = tokenizer.tokenize(query);
        if (queryTerms.isEmpty()) return Collections.emptyList();

        Map<Long, Double> docScores = new HashMap<>();

        for (String term : queryTerms) {
            List<IndexEntry> entries = indexRepo.findByTerm(term);

            // Fuzzy fallback if no exact match
            if (entries.isEmpty()) {
                entries = fuzzySearch(term);
            }

            for (IndexEntry e : entries) {
                Long docId = e.getDocument().getId();
                docScores.merge(docId, e.getTfIdf(), Double::sum);
            }
        }

        // Boost documents matching more query terms
        List<SearchResult> results = docScores.entrySet().stream()
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .map(e -> buildResult(e.getKey(), e.getValue(), query))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return results;
    }

    private SearchResult buildResult(Long docId, double score, String query) {
        return docRepo.findById(docId).map(doc -> {
            String snippet = extractSnippet(doc.getContent(), query);
            return new SearchResult(
                doc.getId(),
                doc.getUrl(),
                doc.getTitle(),
                snippet,
                score
            );
        }).orElse(null);
    }

    // Extract a 200-char snippet around the first query term hit
    private String extractSnippet(String content, String query) {
        if (content == null) return "";
        String lower   = content.toLowerCase();
        String term    = query.toLowerCase().split("\\s+")[0];
        int idx        = lower.indexOf(term);
        int start      = Math.max(0, idx - 80);
        int end        = Math.min(content.length(), start + 200);
        String snippet = content.substring(start, end).trim();
        if (start > 0)          snippet = "..." + snippet;
        if (end < content.length()) snippet = snippet + "...";
        return snippet;
    }

    // Levenshtein fuzzy search — finds terms within edit distance threshold
    private List<IndexEntry> fuzzySearch(String queryTerm) {
        List<String> allTerms = indexRepo.findAllDistinctTerms();
        return allTerms.stream()
            .filter(t -> Math.abs(t.length() - queryTerm.length()) <= maxEditDistance)
            .filter(t -> levenshtein(queryTerm, t) <= maxEditDistance)
            .flatMap(t -> indexRepo.findByTerm(t).stream())
            .collect(Collectors.toList());
    }

    // Classic DP Levenshtein distance
    public int levenshtein(String a, String b) {
        int m = a.length(), n = b.length();
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 0; i <= m; i++) dp[i][0] = i;
        for (int j = 0; j <= n; j++) dp[0][j] = j;
        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                dp[i][j] = a.charAt(i - 1) == b.charAt(j - 1)
                    ? dp[i - 1][j - 1]
                    : 1 + Math.min(dp[i - 1][j - 1], Math.min(dp[i - 1][j], dp[i][j - 1]));
            }
        }
        return dp[m][n];
    }
}
