package clases;

import java.util.*;

public class Lexer {
    private String input;
    private int position;
    private static final Map<Set<Integer>, Map<String, Set<Integer>>> transitionTable = new HashMap<>();
    private static final Map<Set<Integer>, String> finalStates = new HashMap<>();
    public List<parser.Yapar.ErrorDetalle> erroresLexicos = new ArrayList<>();

    private static final Set<Integer> startState = new HashSet<>(Arrays.asList(193, 195, 197, 199, 201, 203, 205, 207, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 191));

    static {
        Map<String, Set<Integer>> tempTransitions;
        tempTransitions = new HashMap<>();
        tempTransitions.put("#5", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(192)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("\\u0000", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#10", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#11", new HashSet<>(Arrays.asList()));
        tempTransitions.put("\\t", new HashSet<>(Arrays.asList()));
        tempTransitions.put("\\n", new HashSet<>(Arrays.asList()));
        tempTransitions.put(" ", new HashSet<>(Arrays.asList()));
        tempTransitions.put("(", new HashSet<>(Arrays.asList()));
        tempTransitions.put(")", new HashSet<>(Arrays.asList()));
        tempTransitions.put("*", new HashSet<>(Arrays.asList()));
        tempTransitions.put("+", new HashSet<>(Arrays.asList()));
        tempTransitions.put("-", new HashSet<>(Arrays.asList()));
        tempTransitions.put("/", new HashSet<>(Arrays.asList()));
        tempTransitions.put("0", new HashSet<>(Arrays.asList()));
        tempTransitions.put("1", new HashSet<>(Arrays.asList()));
        tempTransitions.put("2", new HashSet<>(Arrays.asList()));
        tempTransitions.put("3", new HashSet<>(Arrays.asList()));
        tempTransitions.put("4", new HashSet<>(Arrays.asList()));
        tempTransitions.put("5", new HashSet<>(Arrays.asList()));
        tempTransitions.put("6", new HashSet<>(Arrays.asList()));
        tempTransitions.put("7", new HashSet<>(Arrays.asList()));
        tempTransitions.put("8", new HashSet<>(Arrays.asList()));
        tempTransitions.put("9", new HashSet<>(Arrays.asList()));
        tempTransitions.put("A", new HashSet<>(Arrays.asList()));
        tempTransitions.put("B", new HashSet<>(Arrays.asList()));
        tempTransitions.put("C", new HashSet<>(Arrays.asList()));
        tempTransitions.put("D", new HashSet<>(Arrays.asList()));
        tempTransitions.put("E", new HashSet<>(Arrays.asList()));
        tempTransitions.put("F", new HashSet<>(Arrays.asList()));
        tempTransitions.put("G", new HashSet<>(Arrays.asList()));
        tempTransitions.put("H", new HashSet<>(Arrays.asList()));
        tempTransitions.put("I", new HashSet<>(Arrays.asList()));
        tempTransitions.put("J", new HashSet<>(Arrays.asList()));
        tempTransitions.put("K", new HashSet<>(Arrays.asList()));
        tempTransitions.put("L", new HashSet<>(Arrays.asList()));
        tempTransitions.put("M", new HashSet<>(Arrays.asList()));
        tempTransitions.put("N", new HashSet<>(Arrays.asList()));
        tempTransitions.put("O", new HashSet<>(Arrays.asList()));
        tempTransitions.put("P", new HashSet<>(Arrays.asList()));
        tempTransitions.put("Q", new HashSet<>(Arrays.asList()));
        tempTransitions.put("R", new HashSet<>(Arrays.asList()));
        tempTransitions.put("S", new HashSet<>(Arrays.asList()));
        tempTransitions.put("T", new HashSet<>(Arrays.asList()));
        tempTransitions.put("U", new HashSet<>(Arrays.asList()));
        tempTransitions.put("V", new HashSet<>(Arrays.asList()));
        tempTransitions.put("W", new HashSet<>(Arrays.asList()));
        tempTransitions.put("X", new HashSet<>(Arrays.asList()));
        tempTransitions.put("Y", new HashSet<>(Arrays.asList()));
        tempTransitions.put("Z", new HashSet<>(Arrays.asList()));
        tempTransitions.put("a", new HashSet<>(Arrays.asList()));
        tempTransitions.put("b", new HashSet<>(Arrays.asList()));
        tempTransitions.put("c", new HashSet<>(Arrays.asList()));
        tempTransitions.put("d", new HashSet<>(Arrays.asList()));
        tempTransitions.put("e", new HashSet<>(Arrays.asList()));
        tempTransitions.put("f", new HashSet<>(Arrays.asList()));
        tempTransitions.put("g", new HashSet<>(Arrays.asList()));
        tempTransitions.put("h", new HashSet<>(Arrays.asList()));
        tempTransitions.put("i", new HashSet<>(Arrays.asList()));
        tempTransitions.put("j", new HashSet<>(Arrays.asList()));
        tempTransitions.put("k", new HashSet<>(Arrays.asList()));
        tempTransitions.put("l", new HashSet<>(Arrays.asList()));
        tempTransitions.put("m", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#0", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#1", new HashSet<>(Arrays.asList()));
        tempTransitions.put("n", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#2", new HashSet<>(Arrays.asList()));
        tempTransitions.put("o", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#3", new HashSet<>(Arrays.asList()));
        tempTransitions.put("p", new HashSet<>(Arrays.asList()));
        tempTransitions.put("q", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#4", new HashSet<>(Arrays.asList()));
        tempTransitions.put("r", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#5", new HashSet<>(Arrays.asList()));
        tempTransitions.put("s", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#6", new HashSet<>(Arrays.asList()));
        tempTransitions.put("t", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#7", new HashSet<>(Arrays.asList()));
        tempTransitions.put("u", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#8", new HashSet<>(Arrays.asList()));
        tempTransitions.put("v", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#9", new HashSet<>(Arrays.asList()));
        tempTransitions.put("w", new HashSet<>(Arrays.asList()));
        tempTransitions.put("x", new HashSet<>(Arrays.asList()));
        tempTransitions.put("y", new HashSet<>(Arrays.asList()));
        tempTransitions.put("z", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList()), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#6", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(194)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#4", new HashSet<>(Arrays.asList()));
        tempTransitions.put(")", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        transitionTable.put(new HashSet<>(Arrays.asList(187, 188, 189, 190)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#8", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(198)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#7", new HashSet<>(Arrays.asList()));
        tempTransitions.put("*", new HashSet<>(Arrays.asList(195, 196)));
        transitionTable.put(new HashSet<>(Arrays.asList(195, 196)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#9", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(200)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#11", new HashSet<>(Arrays.asList()));
        tempTransitions.put("#0", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(204, 206)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("#10", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(202)), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("\\u0000", new HashSet<>(Arrays.asList(204, 206)));
        tempTransitions.put("(", new HashSet<>(Arrays.asList(200)));
        tempTransitions.put(")", new HashSet<>(Arrays.asList(202)));
        tempTransitions.put("*", new HashSet<>(Arrays.asList(195, 196)));
        tempTransitions.put("+", new HashSet<>(Arrays.asList(192)));
        tempTransitions.put("-", new HashSet<>(Arrays.asList(194)));
        tempTransitions.put("/", new HashSet<>(Arrays.asList(198)));
        tempTransitions.put("0", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("1", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("2", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("3", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("4", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("5", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("6", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("7", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("8", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        tempTransitions.put("9", new HashSet<>(Arrays.asList(187, 188, 189, 190)));
        transitionTable.put(new HashSet<>(Arrays.asList(193, 195, 197, 199, 201, 203, 205, 207, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 191)), tempTransitions);
    }

    static {
        finalStates.put(new HashSet<>(Arrays.asList(192)), "PLUS");
        finalStates.put(new HashSet<>(Arrays.asList(194)), "MINUS");
        finalStates.put(new HashSet<>(Arrays.asList(187, 188, 189, 190)), "ID");
        finalStates.put(new HashSet<>(Arrays.asList(198)), "DIV");
        finalStates.put(new HashSet<>(Arrays.asList(195, 196)), "TIMES");
        finalStates.put(new HashSet<>(Arrays.asList(200)), "LPAREN");
        finalStates.put(new HashSet<>(Arrays.asList(204, 206)), "EOF");
        finalStates.put(new HashSet<>(Arrays.asList(202)), "RPAREN");
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
                tokens.add(token);
            } else {
                char invalidChar = input.charAt(position);
                if (invalidChar != '\u0000' && !Character.isWhitespace(invalidChar)) {
                    erroresLexicos.add(new parser.Yapar.ErrorDetalle(
                        "léxico",
                        position,
                        String.valueOf(invalidChar),
                        "Carácter no reconocido: '" + invalidChar + "'"
                    ));
                 }
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
            return new Token(lastAcceptingType, input.substring(start, lastAcceptingPos), start, lastAcceptingPos);
        }
        return null;
    }

    public static class Token {
        public String type;
        private String value;
        public int start;
        public int end;

        public Token(String type, String value, int start, int end) {
            this.type = type;
            this.value = value;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + type + ": \"" + value + "\"]";
        }
    }
}
