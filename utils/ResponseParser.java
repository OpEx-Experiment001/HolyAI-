package utils;

public class ResponseParser {

    public static String clean(String json) {
        try {
            String raw = json.split("\"response\":\"")[1].split("\"")[0];
            raw = raw.replace("\\n", "\n");

            // Extract useful lines
            StringBuilder cleaned = new StringBuilder();

            for (String line : raw.split("\n")) {
                line = line.trim();

                if (line.contains(";") || line.startsWith("int") || line.startsWith("String")) {
                    cleaned.append(line).append("\n");
                }
            }

            return cleaned.toString();

        } catch (Exception e) {
            return "Parsing Error";
        }
    }
}