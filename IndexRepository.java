package com.search.repository;

import com.search.model.IndexEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface IndexRepository extends JpaRepository<IndexEntry, Long> {
    List<IndexEntry> findByTerm(String term);

    long countByTerm(String term);

    @Query("SELECT DISTINCT e.term FROM IndexEntry e")
    List<String> findAllDistinctTerms();

    @Query("SELECT COUNT(DISTINCT e.term) FROM IndexEntry e")
    long countDistinctTerms();
}
