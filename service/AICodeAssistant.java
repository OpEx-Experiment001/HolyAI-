package service;

import client.AIFactory;
import client.AIProvider;
import memory.ChatSession;
import memory.Message;
import rag.DocumentLoader;
import rag.Retriever;
import rl.Feedback;
import rl.FeedbackStore;
import java.util.List;

public class AICodeAssistant {

    private AIProvider ai;
    private ChatSession chatSession;
    private Retriever retriever;
    private FeedbackStore feedbackStore;

    public AICodeAssistant() {
        this.ai = AIFactory.getConfiguredProvider();
        this.chatSession = new ChatSession();
        
        // Initialize RAG
        DocumentLoader loader = new DocumentLoader("docs");
        this.retriever = new Retriever(loader);
        
        // Initialize RL Feedback loop
        this.feedbackStore = new FeedbackStore();
    }

    public String ask(String userInput) {
        // 1. RAG Retrieval
        List<String> contextDocs = retriever.retrieveContext(userInput, 2);
        
        // 2. RL Feedback Example Retrieval (Few-Shot)
        Feedback bestExample = feedbackStore.findRelevantGoodExample(userInput);

        // 3. Build System Prompt
        StringBuilder systemContent = new StringBuilder();
        systemContent.append("You are an advanced Java coding assistant.\n");
        
        if (!contextDocs.isEmpty()) {
            systemContent.append("\n--- CONTEXT (Use this to answer if relevant) ---\n");
            for (String doc : contextDocs) {
                systemContent.append(doc).append("\n");
            }
            systemContent.append("----------------------------------------------\n");
        }

        if (bestExample != null) {
            systemContent.append("\n--- LEARNING EXAMPLE (This is how the user likes answers) ---\n");
            systemContent.append("Q: ").append(bestExample.getQuery()).append("\n");
            systemContent.append("A: ").append(bestExample.getResponse()).append("\n");
            systemContent.append("-----------------------------------------------------------\n");
        }

        // Update the system instructions for this turn
        chatSession.addSystemMessage(systemContent.toString());

        // Add user query to memory
        chatSession.addUserMessage(userInput);

        // Send full memory to AI
        String rawResponse = ai.generateResponse(chatSession.getMessages());
        String finalAnswer = extractJsonContent(rawResponse);

        // Add assistant response to memory
        chatSession.addAssistantMessage(finalAnswer);

        return finalAnswer;
    }

    public void saveFeedback(String query, String response, boolean isPositive) {
        feedbackStore.saveFeedback(query, response, isPositive);
    }

    private String extractJsonContent(String raw) {
        if (raw.startsWith("[ERROR]")) return raw;

        StringBuilder result = new StringBuilder();
        String[] lines = raw.split("\n");
        for (String line : lines) {
            if (line.contains("\"content\":\"")) {
                try {
                    String part = line.split("\"content\":\"")[1].split("\"")[0];
                    result.append(part);
                } catch (Exception ignored) {}
            }
        }
        
        if (result.length() == 0) return "Failed to parse JSON. Raw response:\n" + raw;
        return result.toString().replace("\\n", "\n").replace("\\\"", "\"");
    }
}