package clases;

import java.io.*;
import java.util.*;
import java.util.regex.*;

public class YalParser {

    public static class Rule {
        public String regex;
        public String action;

        public Rule(String regex, String action) {
            this.regex = regex;
            this.action = action;
        }

        @Override
        public String toString() {
            return "Regex: " + regex + " -> Acción: " + action;
        }
    }

    private final List<Rule> rules = new ArrayList<>();
    private final Map<String, String> definitions = new HashMap<>();
    private String headerCode = "";
    private String trailerCode = "";

    private String expandDefinition(String key, Set<String> visited) {
        if (!definitions.containsKey(key) || visited.contains(key)) {
            return key;
        }
        visited.add(key);
        String expanded = definitions.get(key);
        for (String subKey : definitions.keySet()) {
            expanded = expanded.replaceAll("\\b" + subKey + "\\b", "(" + expandDefinition(subKey, visited) + ")");
        }
        return expanded;
    }

    private String handleEscapedChars(String input) {
        StringBuilder result = new StringBuilder();
        boolean insideBrackets = false;
        boolean afterGroupOrSet = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '[') {
                insideBrackets = true;
            } else if (c == ']') {
                insideBrackets = false;
                afterGroupOrSet = true;
            }

            if (c == ')') {
                afterGroupOrSet = true;
            }

            if (input.startsWith("eof", i)) {
                result.append("\\u0000");
                i += 2;
                continue;
            }

            if (!insideBrackets) {
                if ("()/-".indexOf(c) != -1) {
                    result.append("\\");
                } else if ((c == '+' || c == '*') && !afterGroupOrSet) {
                    result.append("\\");
                }
            }

            if (c != '\'') {
                result.append(c);
            }

            if (afterGroupOrSet && (c == '+' || c == '*')) {
                afterGroupOrSet = false;
            }
        }

        return result.toString();
    }

    public List<Rule> parseYAL(String filepath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean inHeader = false, inTrailer = false, inRules = false;
            StringBuilder header = new StringBuilder();
            StringBuilder trailer = new StringBuilder();

            Pattern letPattern = Pattern.compile("let\\s+(\\w+)\\s*=\\s*(.+)");
            Pattern rulePattern = Pattern
                    .compile(
                            "((?:\\[[^\\]]*\\]|\\w+|[+*()/'\\\\\\\\\\-]+)+)\\s*\\{\\s*return\\s*\\\"(\\w+)\\\";\\s*\\}");

            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.equals("{") && !inRules) {
                    inHeader = true;
                    continue;
                } else if (line.equals("}") && inHeader) {
                    inHeader = false;
                    continue;
                } else if (line.equals("{") && inRules) {
                    inTrailer = true;
                    continue;
                } else if (line.equals("}") && inTrailer) {
                    inTrailer = false;
                    continue;
                }

                if (inHeader) {
                    header.append(line).append("\n");
                } else if (inTrailer) {
                    trailer.append(line).append("\n");
                } else if (line.startsWith("let ")) {
                    Matcher m = letPattern.matcher(line);
                    if (m.find()) {
                        definitions.put(m.group(1), m.group(2));
                    }
                } else if (line.startsWith("rule")) {
                    inRules = true;
                } else if (inRules) {
                    Matcher matcher = rulePattern.matcher(line);
                    if (matcher.find()) {
                        String rawRegex = matcher.group(1);
                        String action = matcher.group(2);

                        for (String defKey : definitions.keySet()) {
                            String expanded = expandDefinition(defKey, new HashSet<>());
                            rawRegex = rawRegex.replaceAll("\\b" + defKey + "\\b", "(" + expanded + ")");
                        }

                        String processedRegex = handleEscapedChars(rawRegex);
                        rules.add(new Rule(processedRegex, action));
                    }
                }
            }

            headerCode = header.toString();
            trailerCode = trailer.toString();
        }

        return rules;
    }

    public String getHeaderCode() {
        return headerCode;
    }

    public String getTrailerCode() {
        return trailerCode;
    }

    public String combineRegex() {
        StringBuilder combinedRegex = new StringBuilder();
        int index = 1;

        for (Rule rule : rules) {
            if (combinedRegex.length() > 0) {
                combinedRegex.append("|");
            }
            combinedRegex.append("(").append(rule.regex).append(")#").append(index);
            index++;
        }

        combinedRegex.append("|(\\u0000)#0");
        return combinedRegex.toString();
    }
}