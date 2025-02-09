package clases;

import java.util.*;

public class RegexConverter {
    private static final Set<String> OPERATORS = Set.of("|", "?", "+", "*", "#", "/*", "/+");
    private static final Map<String, Integer> PRECEDENCE = Map.of(
            "#", 4,  // Diferencia de conjuntos (mayor precedencia)
            "*", 3,  // Cerradura Kleene
            "+", 3,  // Cerradura Positiva
            "?", 3,  // Existencia opcional
            "^", 2,  // Concatenación implícita
            "|", 1   // Alternancia
    );

    public static String toPostfix(String infix) {
        //infix = "(" + infix + ").";
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

        return postfix.toString().replaceAll("[()]", ""); // Elimina paréntesis y concatenación implícita incorrecta
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
                if (c1.equals("[")) {
                    int j = i;
                    while (j < length && regex.charAt(j) != ']') j++;
                    c1 = regex.substring(i, j + 1);
                    i = j;
                } else if (c1.equals("\"")) {
                    int j = i + 1;
                    while (j < length && regex.charAt(j) != '"') j++;
                    c1 = regex.substring(i, j + 1);
                    i = j;
                } else if (i < length - 1 && c1.equals("/")) {
                    char next = regex.charAt(i + 1);
                    if (next == '*' || next == '+') {
                        c1 = "/" + next;
                        i++;
                    }
                }

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
