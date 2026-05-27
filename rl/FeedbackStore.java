package rl;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeedbackStore {
    private static final String FILE_PATH = "feedback.csv";
    private List<Feedback> feedbackHistory;

    public FeedbackStore() {
        feedbackHistory = new ArrayList<>();
        loadFeedback();
    }

    private void loadFeedback() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                Feedback fb = Feedback.fromCsvLine(line);
                if (fb != null) {
                    feedbackHistory.add(fb);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load feedback: " + e.getMessage());
        }
    }

    public void saveFeedback(String query, String response, boolean isPositive) {
        Feedback fb = new Feedback(query, response, isPositive);
        feedbackHistory.add(fb);
        
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            pw.println(fb.toCsvLine());
        } catch (IOException e) {
            System.err.println("Failed to save feedback: " + e.getMessage());
        }
    }

    /**
     * Finds a past good interaction to use as a Few-Shot Example.
     */
    public Feedback findRelevantGoodExample(String currentQuery) {
        Set<String> queryWords = tokenize(currentQuery);
        Feedback bestMatch = null;
        double bestScore = 0.0;

        for (Feedback fb : feedbackHistory) {
            if (!fb.isPositive()) continue; // Only learn from positive feedback

            Set<String> pastWords = tokenize(fb.getQuery());
            Set<String> intersection = new HashSet<>(queryWords);
            intersection.retainAll(pastWords);
            
            Set<String> union = new HashSet<>(queryWords);
            union.addAll(pastWords);

            double score = (double) intersection.size() / (union.isEmpty() ? 1 : union.size());
            
            if (score > bestScore && score > 0.1) { // Threshold
                bestScore = score;
                bestMatch = fb;
            }
        }
        return bestMatch;
    }

    private Set<String> tokenize(String text) {
        String[] words = text.toLowerCase().replaceAll("[^a-z0-9\\s]", "").split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }
}
