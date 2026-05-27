package rag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Retriever {
    private DocumentLoader loader;

    public Retriever(DocumentLoader loader) {
        this.loader = loader;
    }

    /**
     * Finds the top N most relevant chunks for a given query based on Jaccard Similarity (keyword overlap).
     */
    public List<String> retrieveContext(String query, int topN) {
        List<String> docs = loader.getDocuments();
        if (docs.isEmpty()) return new ArrayList<>();

        Set<String> queryWords = tokenize(query);

        // Simple scoring based on matching words
        List<DocScore> scores = new ArrayList<>();
        for (String doc : docs) {
            Set<String> docWords = tokenize(doc);
            
            // Intersection size
            Set<String> intersection = new HashSet<>(queryWords);
            intersection.retainAll(docWords);
            
            // Union size
            Set<String> union = new HashSet<>(queryWords);
            union.addAll(docWords);
            
            double score = (double) intersection.size() / (union.size() == 0 ? 1 : union.size());
            scores.add(new DocScore(doc, score));
        }

        // Sort descending by score
        scores.sort((a, b) -> Double.compare(b.score, a.score));

        // Return top N
        List<String> results = new ArrayList<>();
        for (int i = 0; i < Math.min(topN, scores.size()); i++) {
            if (scores.get(i).score > 0.01) { // Only return if there is at least *some* overlap
                results.add(scores.get(i).doc);
            }
        }
        return results;
    }

    private Set<String> tokenize(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }

    private static class DocScore {
        String doc;
        double score;
        DocScore(String doc, double score) {
            this.doc = doc;
            this.score = score;
        }
    }
}
