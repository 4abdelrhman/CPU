import java.io.*;
import java.util.*;

class TestCaseLoader {

    public AGInput loadAG(String path) throws Exception {
        String content = readFile(path);
        AGInput in = new AGInput();
        in.processes = new ArrayList<>();

        // Parse JSON manually
        String inputSection = extractSection(content, "\"input\"");
        String processesSection = extractSection(inputSection, "\"processes\"");

        List<String> processBlocks = extractArrayObjects(processesSection);
        for (String block : processBlocks) {
            String name = extractStringValue(block, "name");
            int arrival = extractIntValue(block, "arrival");
            int burst = extractIntValue(block, "burst");
            int priority = extractIntValue(block, "priority");
            int quantum = extractIntValue(block, "quantum");

            in.processes.add(new Process(name, arrival, burst, priority, quantum));
        }

        return in;
    }

    public MultiInput loadMulti(String path) throws Exception {
        String content = readFile(path);
        MultiInput in = new MultiInput();

        String inputSection = extractSection(content, "\"input\"");

        in.contextSwitch = extractIntValue(inputSection, "contextSwitch");
        in.rrQuantum = extractIntValue(inputSection, "rrQuantum");
        in.agingInterval = extractIntValue(inputSection, "agingInterval");

        String processesSection = extractSection(inputSection, "\"processes\"");
        in.processes = new ArrayList<>();

        List<String> processBlocks = extractArrayObjects(processesSection);
        for (String block : processBlocks) {
            String name = extractStringValue(block, "name");
            int arrival = extractIntValue(block, "arrival");
            int burst = extractIntValue(block, "burst");
            int priority = extractIntValue(block, "priority");

            in.processes.add(new Process(name, arrival, burst, priority, 0));
        }

        return in;
    }

    // Helper methods for simple JSON parsing
    private String readFile(String path) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append(" ");
        }
        br.close();
        return sb.toString();
    }

    private String extractSection(String json, String key) {
        int start = json.indexOf(key);
        if (start == -1) return "";

        start = json.indexOf(":", start) + 1;

        // Find matching brace or bracket
        char firstChar = ' ';
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        if (start >= json.length()) return "";

        firstChar = json.charAt(start);

        if (firstChar == '{') {
            int count = 1;
            int i = start + 1;
            while (i < json.length() && count > 0) {
                if (json.charAt(i) == '{') count++;
                if (json.charAt(i) == '}') count--;
                i++;
            }
            return json.substring(start, i);
        } else if (firstChar == '[') {
            int count = 1;
            int i = start + 1;
            while (i < json.length() && count > 0) {
                if (json.charAt(i) == '[') count++;
                if (json.charAt(i) == ']') count--;
                i++;
            }
            return json.substring(start, i);
        }

        return "";
    }

    private List<String> extractArrayObjects(String arraySection) {
        List<String> objects = new ArrayList<>();

        // Remove leading [ and trailing ]
        String content = arraySection.trim();
        if (content.startsWith("[")) content = content.substring(1);
        if (content.endsWith("]")) content = content.substring(0, content.length() - 1);

        int i = 0;
        while (i < content.length()) {
            // Skip whitespace
            while (i < content.length() && Character.isWhitespace(content.charAt(i))) {
                i++;
            }

            if (i >= content.length()) break;

            if (content.charAt(i) == '{') {
                int count = 1;
                int start = i;
                i++;
                while (i < content.length() && count > 0) {
                    if (content.charAt(i) == '{') count++;
                    if (content.charAt(i) == '}') count--;
                    i++;
                }
                objects.add(content.substring(start, i));
            }

            // Skip comma
            while (i < content.length() && (content.charAt(i) == ',' || Character.isWhitespace(content.charAt(i)))) {
                i++;
            }
        }

        return objects;
    }

    private String extractStringValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int start = json.indexOf(pattern);
        if (start == -1) return "";

        start = json.indexOf(":", start) + 1;
        start = json.indexOf("\"", start) + 1;
        int end = json.indexOf("\"", start);

        return json.substring(start, end);
    }

    private int extractIntValue(String json, String key) {
        String pattern = "\"" + key + "\"";
        int start = json.indexOf(pattern);
        if (start == -1) return 0;

        start = json.indexOf(":", start) + 1;

        // Skip whitespace
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }

        // Extract number
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }

        return Integer.parseInt(json.substring(start, end).trim());
    }
}
