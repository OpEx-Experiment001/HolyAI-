package rag;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DocumentLoader {
    private List<String> documents;

    public DocumentLoader(String directoryPath) {
        documents = new ArrayList<>();
        loadDocuments(directoryPath);
    }

    private void loadDocuments(String directoryPath) {
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs(); // Create docs directory if it doesn't exist
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".txt") || name.endsWith(".md"));
        if (files == null) return;

        for (File file : files) {
            try {
                String content = new String(Files.readAllBytes(file.toPath()));
                // Split by double newline to create paragraph chunks
                String[] chunks = content.split("\n\\s*\n");
                for (String chunk : chunks) {
                    if (!chunk.trim().isEmpty()) {
                        documents.add(chunk.trim());
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to read document: " + file.getName());
            }
        }
    }

    public List<String> getDocuments() {
        return documents;
    }
}
