package com.search.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 2000)
    private String url;

    @Column(length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private int wordCount;

    @Column(updatable = false)
    private LocalDateTime indexedAt = LocalDateTime.now();

    public Document() {}

    public Document(String url, String title, String content) {
        this.url = url;
        this.title = title;
        this.content = content;
        this.wordCount = content != null ? content.split("\\s+").length : 0;
    }

    // Getters and setters
    public Long getId()                   { return id; }
    public String getUrl()                { return url; }
    public String getTitle()              { return title; }
    public String getContent()            { return content; }
    public int getWordCount()             { return wordCount; }
    public LocalDateTime getIndexedAt()   { return indexedAt; }
    public void setId(Long id)            { this.id = id; }
    public void setUrl(String url)        { this.url = url; }
    public void setTitle(String title)    { this.title = title; }
    public void setContent(String c)      { this.content = c; }
    public void setWordCount(int wc)      { this.wordCount = wc; }
}
