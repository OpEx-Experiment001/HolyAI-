package rl;

public class Feedback {
    private String query;
    private String response;
    private boolean isPositive;

    public Feedback(String query, String response, boolean isPositive) {
        this.query = query;
        this.response = response;
        this.isPositive = isPositive;
    }

    public String getQuery() { return query; }
    public String getResponse() { return response; }
    public boolean isPositive() { return isPositive; }

    // Serialization for CSV
    public String toCsvLine() {
        return escapeCsv(query) + "," + escapeCsv(response) + "," + isPositive;
    }

    public static Feedback fromCsvLine(String line) {
        // Basic CSV parsing (ignores commas inside quotes)
        String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length >= 3) {
            String q = unescapeCsv(parts[0]);
            String r = unescapeCsv(parts[1]);
            boolean p = Boolean.parseBoolean(parts[2]);
            return new Feedback(q, r, p);
        }
        return null;
    }

    private static String escapeCsv(String val) {
        return "\"" + val.replace("\"", "\"\"").replace("\n", "\\n") + "\"";
    }

    private static String unescapeCsv(String val) {
        if (val.startsWith("\"") && val.endsWith("\"")) {
            val = val.substring(1, val.length() - 1);
        }
        return val.replace("\"\"", "\"").replace("\\n", "\n");
    }
}
