package tools.impl;

import tools.Tool;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReadFileTool implements Tool {
    @Override
    public String getName() { return "read_file"; }

    @Override
    public String getDescription() { return "Reads the content of a specific file, or if a directory is provided, reads all Java files within it. args: path."; }

    @Override
    public String execute(String args) throws Exception {
        try {
            // Clean up the path from LLM hallucinated quotes
            String path = args.trim().replace("'", "").replace("\"", "");
            if (path.endsWith("\\}")) path = path.substring(0, path.length() - 2); // fix for phi3 json escape bug
            
            File file = new File(path);
            if (!file.exists()) {
                // Try resolving relative to current directory if absolute fails
                file = new File(".", path);
                if (!file.exists()) return "Failed to read: File or directory not found.";
            }

            if (file.isDirectory()) {
                StringBuilder allCode = new StringBuilder();
                scanForJava(file, allCode);
                if (allCode.length() == 0) return "No Java files found in directory.";
                return allCode.toString();
            } else {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (Exception e) {
            return "Failed to read file: " + e.getMessage();
        }
    }

    private void scanForJava(File dir, StringBuilder allCode) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory() && !f.getName().startsWith(".") && !f.getName().equals("target") && !f.getName().equals("docs")) {
                scanForJava(f, allCode);
            } else if (f.getName().endsWith(".java")) {
                try {
                    allCode.append("\n\n=== FILE: ").append(f.getPath()).append(" ===\n");
                    allCode.append(new String(Files.readAllBytes(f.toPath())));
                } catch (Exception e) {}
            }
        }
    }
}
