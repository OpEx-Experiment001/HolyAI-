package tools.impl;

import tools.Tool;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;

public class WebSearchTool implements Tool {
    @Override
    public String getName() { return "web_search"; }

    @Override
    public String getDescription() { return "Searches Wikipedia for factual information. args: the search query."; }

    @Override
    public String execute(String args) throws Exception {
        String encodedQuery = URLEncoder.encode(args, "UTF-8");
        URL url = new URI("https://en.wikipedia.org/w/api.php?action=query&list=search&srsearch=" + encodedQuery + "&utf8=&format=json").toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        if (conn.getResponseCode() != 200) {
            return "Failed to fetch search results. HTTP " + conn.getResponseCode();
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        
        // Very basic zero-dependency JSON extraction for 'snippet'
        StringBuilder snippets = new StringBuilder();
        String json = response.toString();
        String[] parts = json.split("\"snippet\":\"");
        for (int i = 1; i < parts.length && i <= 3; i++) { // Get top 3 snippets
            String snippet = parts[i].split("\"")[0];
            // Remove basic HTML tags Wikipedia returns and unicode escapes
            snippet = snippet.replaceAll("<[^>]*>", "").replace("\\\"", "\"").replace("\\n", " ");
            snippets.append("- ").append(snippet).append("\n");
        }
        
        if (snippets.length() == 0) return "No results found.";
        return snippets.toString();
    }
}
