package client;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AIFactory {

    /**
     * Reads the config file and returns the universal AIProvider.
     */
    public static AIProvider getConfiguredProvider() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            System.err.println("Warning: config.properties not found. Using defaults.");
        }

        String url = props.getProperty("ai.url", "http://localhost:11434/api/chat");
        String model = props.getProperty("ai.model", "phi3");
        String apiKey = props.getProperty("ai.api_key", "");
        String startupCommand = props.getProperty("ai.startup_command", "");

        return new UniversalAIProvider(url, model, apiKey, startupCommand);
    }
}
