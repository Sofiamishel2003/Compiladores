package clases;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Función para interpretar caracteres escapados
    private String handleEscapedChars(String input) {
        return input
                .replace("\\t", "\t")
                .replace("\\n", "\n")
                .replace("eof", "\\u0000");  // EOF se transforma a \u0000
    }

    public List<Rule> parseYAL(String filepath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean inRuleSection = false;

            // Mejorado para detectar listas y rangos
            Pattern pattern = Pattern.compile("([\\[\\]'a-zA-Z0-9\\-+*/()\\\\]+|eof)\\s*\\{\\s*return\\s*\"(\\w+)\";\\s*\\}");

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("rule")) {
                    inRuleSection = true; // Comenzamos a procesar las reglas
                } else if (inRuleSection) {
                    Matcher matcher = pattern.matcher(line);
                    if (matcher.find()) {
                        String rawRegex = matcher.group(1);
                        String action = matcher.group(2);

                        // Manejar caracteres escapados
                        String processedRegex = handleEscapedChars(rawRegex);

                        rules.add(new Rule(processedRegex, action));
                    }
                }
            }
        }
        return rules;
    }

    // Combina todas las reglas en una sola regex con el dummy token
    public String combineRegex() {
        StringBuilder combinedRegex = new StringBuilder();
        int index = 1;

        for (Rule rule : rules) {
            if (combinedRegex.length() > 0) {
                combinedRegex.append("|"); // Separador de regex
            }
            combinedRegex.append("(").append(rule.regex).append(")#").append(index);
            index++;
        }

        // Dummy token (\u0000)
        combinedRegex.append("|(\\u0000)#0");
        return combinedRegex.toString();
    }
}





