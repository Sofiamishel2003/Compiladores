package clases;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegexConverter {
    private static final Set<String> OPERATORS = Set.of("|", "^", "?", "+", "*", "/*");
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "*", 3,   // Cerradura de Kleene
            "+", 3,   // Cerradura positiva
            "?", 3,   // Opcionalidad
            "/*", 3,  // Operador especial
            "^", 2,   // Concatenación explícita
            "|", 1    // Alternancia
    );

    /**
     * Preprocesa la regex, agregando operadores de concatenación (`^`)
     * y transformando los conjuntos como [0-9] en (0|1|2|...|9).
     */
    public static String preprocessRegex(String regex) {
        StringBuilder processed = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            if (c == '+') {
                int lastIndex = processed.length() - 1;
    
                // Find the start of the last complete sub-expression
                if (processed.charAt(lastIndex) == ')') {
                    int openParenIndex = lastIndex;
                    int balance = 1;
                    while (openParenIndex > 0 && balance > 0) {
                        openParenIndex--;
                        if (processed.charAt(openParenIndex) == ')') balance++;
                        if (processed.charAt(openParenIndex) == '(') balance--;
                    }
        
                    if (balance == 0) {
                        processed.append("^").append(processed.substring(openParenIndex, lastIndex + 1)).append("*");
                    }
                } else {
                    processed.append("^").append(processed.charAt(lastIndex)).append("*");
                }
            } else if (c == '?') {
                processed.append("|ε");
            } else if (c == '[') {
                int j = i + 1;
                StringBuilder charSet = new StringBuilder("(");
                while (j < regex.length() && regex.charAt(j) != ']') {
                    if (j < regex.length() - 2 && regex.charAt(j + 1) == '-') {
                        char start = regex.charAt(j);
                        char end = regex.charAt(j + 2);
                        for (char ch = start; ch <= end; ch++) {
                            charSet.append(ch).append("|");
                        }
                        j += 3;
                    } else {
                        charSet.append(regex.charAt(j)).append("|");
                        j++;
                    }
                }
                if (charSet.charAt(charSet.length() - 1) == '|') {
                    charSet.deleteCharAt(charSet.length() - 1);
                }
                charSet.append(")");
                processed.append(charSet);
                i = j;
            } else {
                processed.append(c);
            }

            // Insertar operador de concatenación `^` cuando sea necesario
            if (i < regex.length() - 1) {
                char next = regex.charAt(i + 1);
                
                // Agregar ^ entre dos símbolos que requieren concatenación
                if ((Character.isLetterOrDigit(c) || c == '*' || c == '?' || c == ')' || c == ']' || c == '}' || c == '+') &&
                    (Character.isLetterOrDigit(next) || next == '(')) {
                    processed.append("^");
                }
            }

        }
        return processed.append("^.").toString(); // Agregar punto final
    }

    /**
     * Convierte una expresión regular infija a postfija usando Shunting Yard.
     */
    public static String toPostfix(String infix) {
        infix = preprocessRegex(infix);
        System.out.println("Preprocessed Regex: " + infix);

        List<String> output = new ArrayList<>();
        Stack<String> stack = new Stack<>();
        List<String> tokens = tokenize(infix);

        for (String token : tokens) {
            if (!OPERATORS.contains(token) && !token.equals("(") && !token.equals(")")) {
                output.add(token);
            } else if (token.equals("(")) {
                stack.push(token);
            } else if (token.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    output.add(stack.pop());
                }
                stack.pop(); // Quitar '('
            } else {
                while (!stack.isEmpty() && !stack.peek().equals("(") &&
                        PRECEDENCE.get(stack.peek()) >= PRECEDENCE.get(token)) {
                    output.add(stack.pop());
                }
                stack.push(token);
            }
        }

        while (!stack.isEmpty()) {
            output.add(stack.pop());
        }

        return String.join("", output);
    }

    /**
     * Tokeniza la expresión regular, asegurando que los operadores como "/*" se manejen correctamente.
     */
    private static List<String> tokenize(String regex) {
        List<String> tokens = new ArrayList<>();
        boolean escapeNext = false;

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            if (escapeNext) {
                tokens.add("\\" + c);
                escapeNext = false;
            } else if (c == '\\') {
                escapeNext = true;
            } else if (i < regex.length() - 1 && c == '/' && regex.charAt(i + 1) == '*') {
                tokens.add("/*");
                i++;
            } else if (OPERATORS.contains(String.valueOf(c)) || c == '(' || c == ')') {
                tokens.add(String.valueOf(c));
            } else {
                tokens.add(String.valueOf(c));
            }
        }
        return tokens;
    }
}
