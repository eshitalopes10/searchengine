package com.search.crawler;

import com.search.indexer.InvertedIndexBuilder;
import com.search.indexer.Tokenizer;
import com.search.model.Document;
import com.search.repository.DocumentRepository;
import org.jsoup.Jsoup;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class WebCrawler {

    private final DocumentRepository docRepo;
    private final InvertedIndexBuilder indexBuilder;
    private final Tokenizer tokenizer;

    // Track crawl progress (thread-safe)
    private final AtomicInteger crawledCount = new AtomicInteger(0);
    private volatile boolean crawling = false;

    public WebCrawler(DocumentRepository docRepo,
                      InvertedIndexBuilder indexBuilder,
                      Tokenizer tokenizer) {
        this.docRepo      = docRepo;
        this.indexBuilder = indexBuilder;
        this.tokenizer    = tokenizer;
    }

    @Async
    public void crawl(String seedUrl, int maxPages) {
        crawling = true;
        crawledCount.set(0);

        Queue<String> queue  = new LinkedList<>();
        Set<String> visited  = new HashSet<>();
        queue.add(seedUrl);

        String domain = extractDomain(seedUrl);

        while (!queue.isEmpty() && crawledCount.get() < maxPages) {
            String url = queue.poll();
            if (url == null || visited.contains(url)) continue;
            visited.add(url);

            try {
                org.jsoup.nodes.Document html = Jsoup.connect(url)
                    .userAgent("SearchBot/1.0 (educational project)")
                    .timeout(8000)
                    .followRedirects(true)
                    .get();

                String title   = html.title();
                String content = html.body().text();

                if (content.length() < 50) continue; // skip thin pages

                // Skip if already indexed
                if (docRepo.findByUrl(url).isPresent()) continue;

                Document doc = new Document(url, title, content);
                docRepo.save(doc);

                List<String> tokens = tokenizer.tokenize(content);
                indexBuilder.index(doc, tokens);

                crawledCount.incrementAndGet();

                // Queue same-domain links only
                html.select("a[href]").stream()
                    .map(a -> a.absUrl("href"))
                    .filter(link -> !link.isBlank())
                    .filter(link -> link.startsWith("http"))
                    .filter(link -> extractDomain(link).equals(domain))
                    .filter(link -> !visited.contains(link))
                    .limit(20)
                    .forEach(queue::add);

                Thread.sleep(300); // be polite to servers

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                System.err.println("Skipping " + url + " — " + e.getMessage());
            }
        }

        crawling = false;
        System.out.println("Crawl complete. Indexed " + crawledCount.get() + " pages.");
    }

    public int getCrawledCount() { return crawledCount.get(); }
    public boolean isCrawling()  { return crawling; }

    private String extractDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getHost() != null ? uri.getHost() : "";
        } catch (Exception e) {
            return "";
        }
    }
}
