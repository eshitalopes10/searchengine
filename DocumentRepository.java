package com.search.repository;

import com.search.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByUrl(String url);

    @Query("SELECT MAX(d.indexedAt) FROM Document d")
    LocalDateTime findLatestIndexedAt();
}
