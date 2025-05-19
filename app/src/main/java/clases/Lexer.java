package clases;

import java.util.*;

public class Lexer {
    private String input;
    private int position;
    private static final Map<Set<Integer>, Map<String, Set<Integer>>> transitionTable = new HashMap<>();
    private static final Map<Set<Integer>, String> finalStates = new HashMap<>();

    private static final Set<Integer> startState = new HashSet<>(Arrays.asList(32, 1, 2, 34, 3, 36, 5, 38, 7, 8, 40, 9, 10, 42, 11, 12, 13, 14, 15, 16, 28, 30));

    static {
        Map<String, Set<Integer>> tempTransitions;
        tempTransitions = new HashMap<>();
        tempTransitions.put("\\u0000", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#10", new HashSet<>(Arrays.asList()));
        tempTransitions.put("\\t", new HashSet<>(Arrays.asList()));
        tempTransitions.put("\\n", new HashSet<>(Arrays.asList()));
        tempTransitions.put(" ", new HashSet<>(Arrays.asList()));
        tempTransitions.put("(", new HashSet<>(Arrays.asList()));
        tempTransitions.put(")", new HashSet<>(Arrays.asList()));
        tempTransitions.put("*", new HashSet<>(Arrays.asList()));
        tempTransitions.put("+", new HashSet<>(Arrays.asList()));
        tempTransitions.put("-", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#0", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#1", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#2", new HashSet<>(Arrays.asList()));
        tempTransitions.put("/", new HashSet<>(Arrays.asList()));
        tempTransitions.put("0", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#3", new HashSet<>(Arrays.asList()));
        tempTransitions.put("1", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#4", new HashSet<>(Arrays.asList()));
        tempTransitions.put("2", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#5", new HashSet<>(Arrays.asList()));
        tempTransitions.put("3", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#6", new HashSet<>(Arrays.asList()));
        tempTransitions.put("4", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#7", new HashSet<>(Arrays.asList()));
        tempTransitions.put("5", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#8", new HashSet<>(Arrays.asList()));
        tempTransitions.put("6", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#9", new HashSet<>(Arrays.asList()));
        tempTransitions.put("7", new HashSet<>(Arrays.asList()));
        tempTransitions.put("8", new HashSet<>(Arrays.asList()));
        tempTransitions.put("9", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList()), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#6", new HashSet<>(Arrays.asList()));
        tempTransitions.put("*", new HashSet<>(Arrays.asList(32, 33)));
        transitionTable.put(new HashSet<>(Arrays.asList(32, 33)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("0", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("#3", new HashSet<>(Arrays.asList()));
        tempTransitions.put("1", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("2", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("3", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("4", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("5", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("6", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("7", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("8", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("9", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        transitionTable.put(new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#7", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(35)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#1", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(4)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#10", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#0", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(41, 43)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#8", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(37)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put(" ", new HashSet<>(Arrays.asList(4)));
        tempTransitions.put("\\u0000", new HashSet<>(Arrays.asList(41, 43)));
        tempTransitions.put("(", new HashSet<>(Arrays.asList(37)));
        tempTransitions.put("\\t", new HashSet<>(Arrays.asList(4)));
        tempTransitions.put(")", new HashSet<>(Arrays.asList(39)));
        tempTransitions.put("*", new HashSet<>(Arrays.asList(32, 33)));
        tempTransitions.put("\\n", new HashSet<>(Arrays.asList(6)));
        tempTransitions.put("+", new HashSet<>(Arrays.asList(29)));
        tempTransitions.put("-", new HashSet<>(Arrays.asList(31)));
        tempTransitions.put("/", new HashSet<>(Arrays.asList(35)));
        tempTransitions.put("0", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("1", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("2", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("3", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("4", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("5", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("6", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("7", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("8", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        tempTransitions.put("9", new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)));
        transitionTable.put(new HashSet<>(Arrays.asList(32, 1, 2, 34, 3, 36, 5, 38, 7, 8, 40, 9, 10, 42, 11, 12, 13, 14, 15, 16, 28, 30)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#2", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(6)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#9", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(39)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#4", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(29)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#5", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(31)), tempTransitions);
    }

    static {
        finalStates.put(new HashSet<>(Arrays.asList(32, 33)), "TIMES");
        finalStates.put(new HashSet<>(Arrays.asList(17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)), "NUM");
        finalStates.put(new HashSet<>(Arrays.asList(35)), "DIV");
        finalStates.put(new HashSet<>(Arrays.asList(4)), "WHITESPACE");
        finalStates.put(new HashSet<>(Arrays.asList(41, 43)), "EOF");
        finalStates.put(new HashSet<>(Arrays.asList(37)), "LPAREN");
        finalStates.put(new HashSet<>(Arrays.asList(6)), "EOL");
        finalStates.put(new HashSet<>(Arrays.asList(39)), "RPAREN");
        finalStates.put(new HashSet<>(Arrays.asList(29)), "PLUS");
        finalStates.put(new HashSet<>(Arrays.asList(31)), "MINUS");
    }

    public Lexer(String input) {
        this.input = input;
        this.position = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (position < input.length()) {
            Token token = nextToken();
            if (token != null) {
                System.out.println("token: "+token);
                tokens.add(token);
            } else {
                System.err.println("Error léxico en posición " + position);
                position++;
            }
        }
        return tokens;
    }

    private Token nextToken() {
        Set<Integer> state = new HashSet<>(startState);
        int start = position;
        int lastAcceptingPos = -1;
        String lastAcceptingType = null;

        while (position < input.length()) {
            String current = String.valueOf(input.charAt(position));
            if (!transitionTable.containsKey(state) || !transitionTable.get(state).containsKey(current)) {
                break;
            }
            state = transitionTable.get(state).get(current);
            position++;

            if (finalStates.containsKey(state)) {
                lastAcceptingPos = position;
                lastAcceptingType = finalStates.get(state);
            }
        }
        if (lastAcceptingType != null) {
            position = lastAcceptingPos;
            return new Token(lastAcceptingType, input.substring(start, position));
        }
        return null;
    }

    public static class Token {
        public String type;
        private String value;

        public Token(String type, String value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return "[" + type + ": \"" + value + "\"]";
        }
    }
}
