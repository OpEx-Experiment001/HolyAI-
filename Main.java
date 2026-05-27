import service.AICodeAssistant;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        AICodeAssistant assistant = new AICodeAssistant();
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("==================================================");
        System.out.println("🤖 Advanced AI Assistant Started (Memory + RAG + RL + Tools)");
        System.out.println("Type 'exit' to quit.");
        System.out.println("==================================================");

        String lastQuery = null;
        String lastResponse = null;

        while (true) {
            System.out.print("\nYou: ");
            String userInput = scanner.nextLine().trim();

            if (userInput.equalsIgnoreCase("exit")) {
                System.out.println("Goodbye!");
                break;
            }

            if (userInput.equals("+") || userInput.equals("-")) {
                if (lastQuery != null) {
                    boolean isPositive = userInput.equals("+");
                    assistant.saveFeedback(lastQuery, lastResponse, isPositive);
                    System.out.println("[System] Feedback saved! AI will learn from this.");
                } else {
                    System.out.println("[System] No previous interaction to rate.");
                }
                continue;
            }

            if (userInput.isEmpty()) continue;

            System.out.println("\nAI is thinking...");
            String response = assistant.ask(userInput);
            
            while (response.startsWith("[TOOL_REQUEST]")) {
                String toolJson = response.replace("[TOOL_REQUEST]", "").trim();
                System.out.println("\n⚠️ [SECURITY] HolyAI wants to execute a tool:");
                System.out.println(toolJson);
                System.out.print("Allow execution? (y/n): ");
                
                String permission = scanner.nextLine().trim();
                System.out.println("Executing... waiting for AI to analyze results...");
                if (permission.equalsIgnoreCase("y")) {
                    response = assistant.executeToolAndContinue(toolJson);
                } else {
                    response = assistant.denyToolAndContinue();
                }
            }

            System.out.println("\n=== AI RESPONSE ===");
            System.out.println(response);
            System.out.println("===================");
            System.out.println("(Type '+' for Good Answer, '-' for Bad Answer)");

            lastQuery = userInput;
            lastResponse = response;
        }
        
        scanner.close();
    }
}