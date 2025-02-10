package clases;

import java.util.*;

public class RegexConverter {
    private static final Set<String> OPERATORS = Set.of("|", "?", "+", "*", "#", "/*", "/+");
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "#", 4,  // Diferencia de conjuntos (mayor precedencia)
            "*", 3,  // Cerradura Kleene
            "+", 3,  // Cerradura Positiva (convertida a aa*)
            "?", 3,  // Existencia opcional (convertida a a|ε)
            "^", 2,  // Concatenación implícita
            "|", 1   // Alternancia
    );

    public static String preprocessRegex(String regex) {
        StringBuilder processed = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c = regex.charAt(i);
            if (c == '+') {
                processed.append(processed.charAt(processed.length() - 1)).append("*");
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
                charSet.append(")"); // Aplicar * correctamente a todo el conjunto
                processed.append(charSet);
                i = j;
            } else {
                processed.append(c);
            }
        }
        return processed.append(".").toString(); // Agregar punto final
    }

    public static String toPostfix(String infix) {
        infix = preprocessRegex(infix);
        System.out.println("Preprocessed Regex: " + infix);
        System.out.println(infix);
        List<Symbol> formattedRegex = tokenize(infix);
        StringBuilder postfix = new StringBuilder();
        Stack<Symbol> stack = new Stack<>();

        for (Symbol symbol : formattedRegex) {
            String value = symbol.getValue();

            if (!symbol.isOperator()) {
                postfix.append(value);
            } else if (value.equals("(")) {
                stack.push(symbol);
            } else if (value.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().getValue().equals("(")) {
                    postfix.append(stack.pop().getValue());
                }
                stack.pop(); // Quitar '('
            } else {
                while (!stack.isEmpty() && getPrecedence(stack.peek().getValue()) >= getPrecedence(value)) {
                    postfix.append(stack.pop().getValue());
                }
                stack.push(symbol);
            }
        }

        while (!stack.isEmpty()) {
            postfix.append(stack.pop().getValue());
        }

        return postfix.toString().replaceAll("[()^]", ""); // Elimina paréntesis y concatenación implícita incorrecta
    }

    private static int getPrecedence(String operator) {
        return PRECEDENCE.getOrDefault(operator, -1);
    }

    private static List<Symbol> tokenize(String regex) {
        List<Symbol> result = new ArrayList<>();
        boolean escapeNext = false;
        int length = regex.length();

        for (int i = 0; i < length; i++) {
            String c1 = String.valueOf(regex.charAt(i));

            if (c1.equals("\\")) {
                escapeNext = !escapeNext;
                result.add(new Symbol(c1, false));
            } else if (!escapeNext) {
                boolean isOperator = OPERATORS.contains(c1);
                if (!isOperator && !result.isEmpty()) {
                    Symbol prev = result.get(result.size() - 1);
                    if (!prev.isOperator() && !prev.getValue().equals("(")) {
                        result.add(new Symbol("^", true));
                    }
                }
                result.add(new Symbol(c1, isOperator));
                escapeNext = false;
            } else {
                result.add(new Symbol(c1, false));
                escapeNext = false;
            }
        }

        return result;
    }
}
