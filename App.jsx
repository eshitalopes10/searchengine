import { useState, useEffect, useRef } from "react";

const API = "/api";

export default function App() {
  const [query,    setQuery]    = useState("");
  const [results,  setResults]  = useState([]);
  const [stats,    setStats]    = useState(null);
  const [timing,   setTiming]   = useState(null);
  const [count,    setCount]    = useState(null);
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState(null);
  const [crawlUrl, setCrawlUrl] = useState("");
  const [crawling, setCrawling] = useState(false);
  const [crawlMsg, setCrawlMsg] = useState("");
  const [searched, setSearched] = useState(false);
  const inputRef = useRef(null);

  useEffect(() => {
    fetch(`${API}/stats`)
      .then(r => r.json())
      .then(setStats)
      .catch(() => {});
    inputRef.current?.focus();
  }, []);

  const search = async () => {
    if (!query.trim()) return;
    setLoading(true);
    setError(null);
    setSearched(true);
    try {
      const res = await fetch(`${API}/search?q=${encodeURIComponent(query)}&limit=10`);
      if (!res.ok) throw new Error("Search failed");
      setTiming(res.headers.get("X-Search-Time-Ms"));
      setCount(res.headers.get("X-Result-Count"));
      setResults(await res.json());
    } catch (e) {
      setError("Could not reach the search API. Is the backend running?");
    } finally {
      setLoading(false);
    }
  };

  const startCrawl = async () => {
    if (!crawlUrl.trim()) return;
    setCrawling(true);
    setCrawlMsg("Crawling started...");
    try {
      const res = await fetch(`${API}/crawl?url=${encodeURIComponent(crawlUrl)}&maxPages=50`, { method: "POST" });
      const data = await res.json();
      setCrawlMsg(data.error || `Crawling ${data.url} in background (up to ${data.maxPages} pages)`);
    } catch {
      setCrawlMsg("Failed to start crawl.");
    } finally {
      setCrawling(false);
    }
  };

  return (
    <div style={{ minHeight: "100vh", padding: "0 20px" }}>
      {/* Header */}
      <header style={{ maxWidth: 720, margin: "0 auto", padding: "40px 0 0" }}>
        <h1 style={{ fontSize: 36, fontWeight: 700, letterSpacing: -1, marginBottom: 4 }}>
          Search<span style={{ color: "#4285f4" }}>X</span>
        </h1>
        {stats && (
          <p style={{ fontSize: 13, color: "#5f6368" }}>
            {Number(stats.totalDocuments).toLocaleString()} documents &middot;&nbsp;
            {Number(stats.totalTerms).toLocaleString()} indexed terms
          </p>
        )}
      </header>

      {/* Search bar */}
      <div style={{ maxWidth: 720, margin: "24px auto 0" }}>
        <div style={{ display: "flex", gap: 10 }}>
          <input
            ref={inputRef}
            value={query}
            onChange={e => setQuery(e.target.value)}
            onKeyDown={e => e.key === "Enter" && search()}
            placeholder="Search anything... (typos ok!)"
            style={{
              flex: 1, padding: "12px 16px", fontSize: 16,
              border: "1px solid #dfe1e5", borderRadius: 24,
              outline: "none", background: "#fff",
              boxShadow: "0 1px 6px rgba(32,33,36,.1)"
            }}
          />
          <button
            onClick={search}
            disabled={loading}
            style={{
              padding: "12px 24px", background: "#4285f4", color: "#fff",
              border: "none", borderRadius: 24, cursor: "pointer",
              fontSize: 15, fontWeight: 500,
              opacity: loading ? 0.7 : 1
            }}
          >
            {loading ? "..." : "Search"}
          </button>
        </div>

        {/* Timing */}
        {searched && !loading && (
          <p style={{ fontSize: 13, color: "#70757a", margin: "10px 0 0 4px" }}>
            {error ? "" : `About ${count || 0} results (${timing || "?"}ms)`}
          </p>
        )}
      </div>

      {/* Error */}
      {error && (
        <div style={{ maxWidth: 720, margin: "16px auto", padding: "12px 16px", background: "#fce8e6", borderRadius: 8, fontSize: 14, color: "#c5221f" }}>
          {error}
        </div>
      )}

      {/* Results */}
      <div style={{ maxWidth: 720, margin: "16px auto 40px" }}>
        {results.map(r => (
          <div key={r.id} style={{ marginBottom: 28 }}>
            <div style={{ fontSize: 13, color: "#202124", marginBottom: 2, opacity: 0.7 }}>
              {r.url?.length > 60 ? r.url.substring(0, 60) + "..." : r.url}
            </div>
            <a href={r.url} target="_blank" rel="noreferrer"
               style={{ fontSize: 20, color: "#1a0dab", fontWeight: 400 }}>
              {r.title || r.url}
            </a>
            {r.snippet && (
              <p style={{ fontSize: 14, color: "#4d5156", marginTop: 4, lineHeight: 1.58 }}>
                {r.snippet}
              </p>
            )}
            <span style={{ fontSize: 12, color: "#aaa" }}>
              relevance score: {r.score?.toFixed(4)}
            </span>
          </div>
        ))}

        {searched && !loading && results.length === 0 && !error && (
          <div style={{ textAlign: "center", padding: "40px 0", color: "#5f6368" }}>
            <p style={{ fontSize: 18, marginBottom: 8 }}>No results found for "{query}"</p>
            <p style={{ fontSize: 14 }}>Try crawling some pages first using the panel below.</p>
          </div>
        )}
      </div>

      {/* Crawl Panel */}
      <div style={{
        maxWidth: 720, margin: "0 auto 60px",
        padding: 20, background: "#fff",
        border: "1px solid #dfe1e5", borderRadius: 12
      }}>
        <h3 style={{ fontSize: 15, fontWeight: 600, marginBottom: 12, color: "#202124" }}>
          Index new pages
        </h3>
        <div style={{ display: "flex", gap: 10 }}>
          <input
            value={crawlUrl}
            onChange={e => setCrawlUrl(e.target.value)}
            placeholder="https://en.wikipedia.org/wiki/Java_(programming_language)"
            style={{
              flex: 1, padding: "10px 14px", fontSize: 14,
              border: "1px solid #dfe1e5", borderRadius: 8, outline: "none"
            }}
          />
          <button
            onClick={startCrawl}
            disabled={crawling}
            style={{
              padding: "10px 20px", background: "#34a853", color: "#fff",
              border: "none", borderRadius: 8, cursor: "pointer", fontSize: 14
            }}
          >
            {crawling ? "Starting..." : "Crawl"}
          </button>
        </div>
        {crawlMsg && (
          <p style={{ fontSize: 13, color: "#5f6368", marginTop: 10 }}>{crawlMsg}</p>
        )}
        <p style={{ fontSize: 12, color: "#aaa", marginTop: 8 }}>
          Crawls up to 50 pages from the given URL. Runs in the background.
        </p>
      </div>
    </div>
  );
}
