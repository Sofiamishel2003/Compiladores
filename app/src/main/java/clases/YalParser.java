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

    private String handleEscapedChars(String input) {
        StringBuilder result = new StringBuilder();
        boolean insideBrackets = false;   // Rastrea si estamos dentro de [ ]
        boolean afterGroupOrSet = false;  // Rastrea si estamos después de ] o )
    
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
    
            // Detectar inicio y fin de un conjunto [ ]
            if (c == '[') {
                insideBrackets = true;
            } else if (c == ']') {
                insideBrackets = false;
                afterGroupOrSet = true; // Estamos después de un conjunto
            }
    
            // Detectar fin de un grupo ( )
            if (c == ')') {
                afterGroupOrSet = true; // Estamos después de un grupo
            }
    
            // Reemplazar "eof" por \u0000
            if (input.startsWith("eof", i)) {
                result.append("\\u0000");
                i += 2; // Saltar "of"
                continue;
            }
    
            // Escapar caracteres especiales si no estamos dentro de un conjunto [ ]
            if (!insideBrackets) {
                // Siempre escapar estos caracteres: ( ) / -
                if ("()/-".indexOf(c) != -1) {
                    result.append("\\");
                }
                // Escapar + o * solo si NO están después de ] o )
                else if ((c == '+' || c == '*') && !afterGroupOrSet) {
                    result.append("\\");
                }
            }
    
            // No añadir comillas simples
            if (c != '\'') {
                result.append(c);
            }
    
            // Restablecer el estado después de cualquier carácter que no sea cuantificador
            if (afterGroupOrSet && (c == '+' || c == '*')) {
                afterGroupOrSet = false;
            }
        }
    
        return result.toString();
    }        

    public List<Rule> parseYAL(String filepath) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line;
            boolean inRuleSection = false;

            // Mejorado para detectar listas y rangos
            Pattern pattern = Pattern.compile("((?:\\[[^\\]]*\\]|[a-zA-Z0-9+*/()\\\\'\\-]+|eof)+)\\s*\\{\\s*return\\s*\"(\\w+)\";\\s*\\}");

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





