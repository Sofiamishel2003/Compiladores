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
     * Preprocesa la regex combinada:
     * - Maneja secuencias escapadas (\t, \n, \u0000).
     * - Expande rangos ([a-z] → (a|b|c|...|z)).
     * - Transforma operadores como + → (exp)^(exp)*.
     * - Respeta las etiquetas #n.
     * - Inserta concatenaciones explícitas (^).
     */
    public static String preprocessRegex(String regex) {
        StringBuilder processed = new StringBuilder();
        boolean insideCharClass = false; // Estamos dentro de [ ]

        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);

            // Detectar inicio de una clase de caracteres [ ]
            // Detectar inicio de una clase de caracteres [ ]
            if (c == '[') {
                insideCharClass = true;
                StringBuilder charSet = new StringBuilder("(");

                i++; // Saltar el '['

                // Expandir contenido del conjunto
                while (i < regex.length() && regex.charAt(i) != ']') {
                    if (i + 6 < regex.length()
                            && regex.charAt(i) == '\''
                            && regex.charAt(i + 2) == '\''
                            && regex.charAt(i + 3) == '-'
                            && regex.charAt(i + 5) == '\''
                            && regex.charAt(i + 6) != ']') {
                        char start = regex.charAt(i + 1);
                        char end = regex.charAt(i + 4);
                        for (char ch = start; ch <= end; ch++) {
                            charSet.append(ch).append("|");
                        }
                        i += 6;
                    }
                    // Rango sin comillas como: 0-9
                    else if (i + 2 < regex.length() && regex.charAt(i + 1) == '-') {
                        char start = regex.charAt(i);
                        char end = regex.charAt(i + 2);
                        for (char ch = start; ch <= end; ch++) {
                            charSet.append(ch).append("|");
                        }
                        i += 2;
                    }
                    // Escapados como \t, \n
                    else if (regex.charAt(i) == '\\' && i + 1 < regex.length()) {
                        charSet.append("\\").append(regex.charAt(i + 1)).append("|");
                        i++;
                    }
                    // Ignorar comillas simples
                    else if (regex.charAt(i) != '\'') {
                        charSet.append(regex.charAt(i)).append("|");
                    }
                    i++;
                }

                // Limpiar el último |
                if (charSet.charAt(charSet.length() - 1) == '|') {
                    charSet.deleteCharAt(charSet.length() - 1);
                }

                charSet.append(")");
                processed.append(charSet);
                insideCharClass = false;
                continue;
            }


            // Detectar el fin de un conjunto
            if (c == ']') {
                insideCharClass = false;
                continue;
            }

            // Manejar secuencias escapadas (\t, \n, \r, \\, \u0000)
            if (c == '\\' && i + 1 < regex.length()) {
                char next = regex.charAt(i + 1);

                if (next == 't' || next == 'n' || next == 'r' || next == '\\') {
                    processed.append("\\").append(next);
                    i++;
                    continue;
                }

                // Manejar secuencias Unicode (\u0000)
                if (next == 'u' && i + 5 < regex.length() && regex.substring(i + 2, i + 6).matches("[0-9a-fA-F]{4}")) {
                    processed.append("\\u").append(regex, i + 2, i + 6);
                    i += 5;
                    continue;
                }
            }

            // Si encuentra una etiqueta #n, la copia sin modificar
            if (c == '#' && i + 1 < regex.length() && Character.isDigit(regex.charAt(i + 1))) {
                processed.append(c);
                while (i + 1 < regex.length() && Character.isDigit(regex.charAt(i + 1))) {
                    processed.append(regex.charAt(++i));
                }
                continue;
            }

            // Manejar el operador +
            else if (c == '+' && !insideCharClass) {
                int lastIndex = processed.length() - 1;
            
                // Caso: si el último carácter es ')', buscamos el grupo completo
                if (processed.charAt(lastIndex) == ')') {
                    int openParenIndex = lastIndex;
                    int balance = 1;
            
                    // Usar una pila para encontrar el paréntesis abierto correspondiente
                    while (openParenIndex > 0 && balance > 0) {
                        openParenIndex--;
                        if (processed.charAt(openParenIndex) == ')') balance++;
                        if (processed.charAt(openParenIndex) == '(') balance--;
                    }
            
                    // Si encontramos el grupo correctamente, aplicamos la expansión
                    if (balance == 0) {
                        String subExpr = processed.substring(openParenIndex, lastIndex + 1);
                        processed.append("^").append(subExpr).append("*");
                    }
                } 
            
                // Caso: si el último carácter es una secuencia escapada (como \d)
                else if (processed.charAt(lastIndex) == '\\') {
                    processed.append(c); // No modificar, ya está escapado
                }
            
                // Caso: un solo carácter (como a+ → a^(a)*)
                else {
                    char prevChar = processed.charAt(lastIndex);
                    processed.deleteCharAt(lastIndex); // Eliminamos el último carácter
                    processed.append("(").append(prevChar).append(")").append("^(").append(prevChar).append(")*");
                }
            
                continue; // Saltar al siguiente carácter
            }
            

            // Manejar el operador ?
            else if (c == '?' && !insideCharClass) {
                processed.append("|ε");
                continue;
            }

            // Copiar otros caracteres
            processed.append(c);

            // Insertar ^ para concatenación (fuera de [ ])
            if (!insideCharClass && i < regex.length() - 1) {
                char next = regex.charAt(i + 1);

                if ((Character.isLetterOrDigit(c) || c == '*' || c == '?' || c == ')') &&
                    (Character.isLetterOrDigit(next) || next == '(' || next == '\\')) {

                    // No insertar ^ antes de una etiqueta #n
                    if (!(next == '#' && (i + 2) < regex.length() && Character.isDigit(regex.charAt(i + 2)))) {
                        processed.append("^");
                    }
                }
            }
        }

        // Agregar el dummy token final
        return processed.append("^.").toString();
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
