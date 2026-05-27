package client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;
import memory.Message;

public class UniversalAIProvider implements AIProvider {
    private String urlStr;
    private String model;
    private String apiKey;
    private String startupCommand;

    public UniversalAIProvider(String url, String model, String apiKey, String startupCommand) {
        this.urlStr = url;
        this.model = model;
        this.apiKey = apiKey;
        this.startupCommand = startupCommand;
    }

    @Override
    public String generateResponse(List<Message> messages) {
        try {
            return sendHttpRequest(messages);
        } catch (java.net.ConnectException e) {
            if (startupCommand != null && !startupCommand.trim().isEmpty()) {
                System.out.println("Connection failed. Attempting to start local AI provider using: " + startupCommand);
                try {
                    ProcessBuilder pb = new ProcessBuilder(startupCommand.split(" "));
                    pb.start();
                    System.out.println("Waiting 5 seconds for the AI provider to start up...");
                    Thread.sleep(5000);
                    
                    return sendHttpRequest(messages);
                } catch (Exception retryException) {
                    return "[ERROR] Failed to auto-start the local AI provider: " + retryException.getMessage();
                }
            } else {
                return "[ERROR] Connection refused. The AI provider is unreachable.";
            }
        } catch (Exception e) {
            return "[ERROR] " + e.getMessage();
        }
    }

    private String sendHttpRequest(List<Message> messages) throws Exception {
        URL url = new URI(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        }
        
        conn.setDoOutput(true);

        // Build the messages JSON array manually
        StringBuilder msgJson = new StringBuilder();
        for (int i = 0; i < messages.size(); i++) {
            Message m = messages.get(i);
            msgJson.append("{ \"role\": \"").append(m.getRole()).append("\", \"content\": \"").append(m.getEscapedContent()).append("\" }");
            if (i < messages.size() - 1) {
                msgJson.append(",");
            }
        }

        String json = "{"
                + "\"model\": \"" + model + "\","
                + "\"messages\": [" + msgJson.toString() + "]"
                + "}";

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();

        if (conn.getResponseCode() != 200) {
            return "[ERROR] HTTP " + conn.getResponseCode();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line).append("\n");
        }
        return response.toString();
    }
}
