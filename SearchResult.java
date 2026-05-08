package com.search.dto;

public class SearchResult {
    private Long   id;
    private String url;
    private String title;
    private String snippet;
    private double score;

    public SearchResult(Long id, String url, String title, String snippet, double score) {
        this.id      = id;
        this.url     = url;
        this.title   = title;
        this.snippet = snippet;
        this.score   = score;
    }

    public Long   getId()      { return id; }
    public String getUrl()     { return url; }
    public String getTitle()   { return title; }
    public String getSnippet() { return snippet; }
    public double getScore()   { return score; }
}
