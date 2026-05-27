package memory;

import java.util.ArrayList;
import java.util.List;

public class ChatSession {
    private List<Message> messages;

    public ChatSession() {
        this.messages = new ArrayList<>();
    }

    public void addSystemMessage(String content) {
        // Replace existing system message if it exists, or insert at beginning
        messages.removeIf(m -> m.getRole().equals("system"));
        messages.add(0, new Message("system", content));
    }

    public void addUserMessage(String content) {
        messages.add(new Message("user", content));
    }

    public void addAssistantMessage(String content) {
        messages.add(new Message("assistant", content));
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void clear() {
        messages.clear();
    }
}
