package tools.impl;

import tools.Tool;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodebaseMapperTool implements Tool {
    @Override
    public String getName() { return "map_codebase"; }

    @Override
    public String getDescription() { return "Maps the current Java project directory to extract classes and public methods. args: ignore."; }

    @Override
    public String execute(String args) throws Exception {
        StringBuilder map = new StringBuilder();
        File root = new File(".");
        scanDirectory(root, map);
        if (map.length() == 0) return "No java files found.";
        return map.toString();
    }

    private void scanDirectory(File dir, StringBuilder map) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                if (!f.getName().startsWith(".") && !f.getName().equals("target") && !f.getName().equals("docs")) {
                    scanDirectory(f, map);
                }
            } else if (f.getName().endsWith(".java")) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())));
                    map.append("\nFile: ").append(f.getPath()).append("\n");
                    extractStructure(content, map);
                } catch (Exception e) {}
            }
        }
    }

    private void extractStructure(String content, StringBuilder map) {
        Matcher mClass = Pattern.compile("(public\\s+(class|interface)\\s+\\w+)").matcher(content);
        if (mClass.find()) {
            map.append("  ").append(mClass.group(1)).append("\n");
        }
        Matcher mMethod = Pattern.compile("public\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\([^)]*\\)").matcher(content);
        while (mMethod.find()) {
            map.append("    - ").append(mMethod.group()).append("\n");
        }
    }
}
