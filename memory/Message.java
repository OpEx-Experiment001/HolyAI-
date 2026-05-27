package memory;

public class Message {
    private String role; // "system", "user", "assistant"
    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }

    // Escapes text for JSON payload
    public String getEscapedContent() {
        return content.replace("\\", "\\\\")
                      .replace("\"", "\\\"")
                      .replace("\n", "\\n")
                      .replace("\r", "");
    }
}
