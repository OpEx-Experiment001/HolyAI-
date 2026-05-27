package tools;

import tools.impl.CodebaseMapperTool;
import tools.impl.ReadFileTool;
import tools.impl.WebSearchTool;

import java.util.HashMap;
import java.util.Map;

public class ToolRegistry {
    private Map<String, Tool> tools = new HashMap<>();

    public ToolRegistry() {
        register(new ReadFileTool());
        register(new CodebaseMapperTool());
        register(new WebSearchTool());
    }

    public void register(Tool tool) {
        tools.put(tool.getName(), tool);
    }

    public Tool getTool(String name) {
        if (name == null) return null;
        String lower = name.toLowerCase();
        if (lower.contains("map") || lower.contains("codebase")) return tools.get("map_codebase");
        if (lower.contains("search") || lower.contains("web")) return tools.get("web_search");
        if (lower.contains("read")) return tools.get("read_file");
        return tools.get(name);
    }

    public String getSystemPromptExtension() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== AVAILABLE TOOLS ===\n");
        sb.append("You have access to the following tools to help you answer the user's request. ");
        sb.append("To use a tool, you MUST wrap a valid JSON object inside <tool_call> tags.\n");
        sb.append("Example:\n");
        sb.append("<tool_call>\n");
        sb.append("{\"tool\": \"tool_name\", \"args\": \"arguments_as_a_single_string\"}\n");
        sb.append("</tool_call>\n\n");
        sb.append("Tools List:\n");
        for (Tool tool : tools.values()) {
            sb.append("- ").append(tool.getName()).append(": ").append(tool.getDescription()).append("\n");
        }
        sb.append("=======================\n");
        return sb.toString();
    }
}
