package com.search.indexer;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class Tokenizer {

    private static final Set<String> STOP_WORDS = Set.of(
        "the","is","at","which","on","a","an","and","or","in","to","of","for",
        "with","as","by","from","it","its","was","be","this","that","are","were",
        "been","has","have","had","do","does","did","but","not","if","so","up",
        "out","about","into","than","then","their","there","they","what","when",
        "where","who","will","would","could","should","can","may","might","shall"
    );

    public List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return Collections.emptyList();
        return Arrays.stream(
                text.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")
                    .split("\\s+"))
            .filter(w -> w.length() > 2)
            .filter(w -> !STOP_WORDS.contains(w))
            .map(this::stem)
            .collect(Collectors.toList());
    }

    // Porter-lite stemmer — handles most English suffixes
    public String stem(String word) {
        if (word.length() <= 3) return word;
        if (word.endsWith("ing")  && word.length() > 6) return word.substring(0, word.length() - 3);
        if (word.endsWith("tion") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ness") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ment") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("able") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ible") && word.length() > 6) return word.substring(0, word.length() - 4);
        if (word.endsWith("ed")   && word.length() > 5) return word.substring(0, word.length() - 2);
        if (word.endsWith("ly")   && word.length() > 5) return word.substring(0, word.length() - 2);
        if (word.endsWith("er")   && word.length() > 5) return word.substring(0, word.length() - 2);
        if (word.endsWith("est")  && word.length() > 5) return word.substring(0, word.length() - 3);
        if (word.endsWith("s")    && word.length() > 4 && !word.endsWith("ss")) return word.substring(0, word.length() - 1);
        return word;
    }
}
