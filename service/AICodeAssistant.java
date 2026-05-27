package service;

import client.AIFactory;
import client.AIProvider;
import memory.ChatSession;
import memory.Message;
import rag.DocumentLoader;
import rag.Retriever;
import rl.Feedback;
import rl.FeedbackStore;
import tools.Tool;
import tools.ToolRegistry;
import java.util.List;

public class AICodeAssistant {

    private AIProvider ai;
    private ChatSession chatSession;
    private Retriever retriever;
    private FeedbackStore feedbackStore;
    private ToolRegistry toolRegistry;

    public AICodeAssistant() {
        this.ai = AIFactory.getConfiguredProvider();
        this.chatSession = new ChatSession();
        
        DocumentLoader loader = new DocumentLoader("docs");
        this.retriever = new Retriever(loader);
        
        this.feedbackStore = new FeedbackStore();
        this.toolRegistry = new ToolRegistry();
    }

    public String ask(String userInput) {
        if (userInput != null && !userInput.isEmpty()) {
            List<String> contextDocs = retriever.retrieveContext(userInput, 2);
            Feedback bestExample = feedbackStore.findRelevantGoodExample(userInput);

            StringBuilder systemContent = new StringBuilder();
            systemContent.append("You are an advanced Java coding assistant. CRITICAL RULE: Never write paragraphs of filler text when executing tools. Get straight to the point. However, if the user explicitly asks for a 'detailed explanation' or a 'comprehensive analysis', you may provide a thorough and detailed response.\n");
            systemContent.append(toolRegistry.getSystemPromptExtension());
            
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

            chatSession.addSystemMessage(systemContent.toString());
            chatSession.addUserMessage(userInput);
        }

        String rawResponse = ai.generateResponse(chatSession.getMessages());
        String finalAnswer = extractJsonContent(rawResponse);

        int toolStart = finalAnswer.indexOf("<tool_call>");
        int toolEnd = finalAnswer.indexOf("</tool_call>");
        if (toolStart != -1 && toolEnd != -1) {
            String toolJson = finalAnswer.substring(toolStart + 11, toolEnd).trim();
            return "[TOOL_REQUEST] " + toolJson;
        }

        int jsonStart = finalAnswer.indexOf("{\"tool\":");
        if (jsonStart != -1) {
            int braceCount = 0;
            int jsonEnd = -1;
            for (int i = jsonStart; i < finalAnswer.length(); i++) {
                if (finalAnswer.charAt(i) == '{') braceCount++;
                else if (finalAnswer.charAt(i) == '}') braceCount--;
                
                if (braceCount == 0) {
                    jsonEnd = i;
                    break;
                }
            }
            if (jsonEnd > jsonStart) {
                String toolJson = finalAnswer.substring(jsonStart, jsonEnd + 1).trim();
                return "[TOOL_REQUEST] " + toolJson;
            }
        }

        chatSession.addAssistantMessage(finalAnswer);
        return finalAnswer;
    }

    public String executeToolAndContinue(String toolJson) {
        try {
            String toolName = extractJsonValue(toolJson, "tool");
            String args = extractJsonValue(toolJson, "args");
            
            Tool tool = toolRegistry.getTool(toolName);
            if (tool == null) {
                chatSession.addSystemMessage("Tool execution failed: Tool '" + toolName + "' not found.");
            } else {
                String result = tool.execute(args);
                chatSession.addSystemMessage("Tool '" + toolName + "' executed successfully. Results:\n" + result);
            }
        } catch (Exception e) {
            chatSession.addSystemMessage("Tool execution failed: " + e.getMessage());
        }
        
        return ask(null); 
    }

    public String denyToolAndContinue() {
        chatSession.addSystemMessage("The user denied permission to execute the requested tool.");
        return ask(null);
    }

    public void saveFeedback(String query, String response, boolean isPositive) {
        feedbackStore.saveFeedback(query, response, isPositive);
    }

    private String extractJsonContent(String raw) {
        if (raw.startsWith("[ERROR]")) return raw;
        
        StringBuilder result = new StringBuilder();
        String search = "\"content\":";
        int index = raw.indexOf(search);
        while (index != -1) {
            int startQuote = raw.indexOf("\"", index + search.length());
            if (startQuote == -1) break;
            
            int endQuote = startQuote + 1;
            while (endQuote < raw.length()) {
                if (raw.charAt(endQuote) == '\"' && raw.charAt(endQuote - 1) != '\\') {
                    break;
                }
                endQuote++;
            }
            
            if (endQuote < raw.length()) {
                result.append(raw.substring(startQuote + 1, endQuote));
            }
            index = raw.indexOf(search, endQuote);
        }
        
        if (result.length() == 0) return raw;
        return result.toString().replace("\\n", "\n").replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) {
            search = "\"" + key + "\": ";
            start = json.indexOf(search);
            if (start == -1) return "";
        }
        start += search.length();
        
        int quoteStart = json.indexOf("\"", start);
        if (quoteStart == -1) return "";
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return "";
        return json.substring(quoteStart + 1, quoteEnd);
    }
}