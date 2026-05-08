package com.search.indexer;

import com.search.model.Document;
import com.search.model.IndexEntry;
import com.search.repository.DocumentRepository;
import com.search.repository.IndexRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InvertedIndexBuilder {

    private final IndexRepository indexRepo;
    private final DocumentRepository docRepo;

    public InvertedIndexBuilder(IndexRepository indexRepo, DocumentRepository docRepo) {
        this.indexRepo = indexRepo;
        this.docRepo   = docRepo;
    }

    @Transactional
    public void index(Document doc, List<String> tokens) {
        if (tokens.isEmpty()) return;

        // Count term frequencies in this document
        Map<String, Long> termFreq = tokens.stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        long totalDocs = docRepo.count();

        termFreq.forEach((term, freq) -> {
            long docsWithTerm = indexRepo.countByTerm(term) + 1;

            // TF: how often term appears in this doc (normalized by doc length)
            double tf = (double) freq / tokens.size();

            // IDF: how rare this term is across all docs
            double idf = Math.log((double)(totalDocs + 1) / docsWithTerm);

            double tfidf = tf * idf;

            IndexEntry entry = new IndexEntry(term, doc, freq.intValue(), tfidf);
            indexRepo.save(entry);
        });
    }
}
