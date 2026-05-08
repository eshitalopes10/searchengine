package com.search.model;

import jakarta.persistence.*;

@Entity
@Table(name = "index_entries",
       indexes = {
           @Index(name = "idx_term", columnList = "term"),
           @Index(name = "idx_doc_id", columnList = "document_id")
       })
public class IndexEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    private int frequency;
    private double tfIdf;

    public IndexEntry() {}

    public IndexEntry(String term, Document document, int frequency, double tfIdf) {
        this.term = term;
        this.document = document;
        this.frequency = frequency;
        this.tfIdf = tfIdf;
    }

    public Long getId()           { return id; }
    public String getTerm()       { return term; }
    public Document getDocument() { return document; }
    public int getFrequency()     { return frequency; }
    public double getTfIdf()      { return tfIdf; }
    public void setTfIdf(double s){ this.tfIdf = s; }
}
