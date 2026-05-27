package client;

import java.util.List;
import memory.Message;

public interface AIProvider {
    /**
     * Sends the conversation history to the AI and returns the raw response string.
     */
    String generateResponse(List<Message> messages);
}
