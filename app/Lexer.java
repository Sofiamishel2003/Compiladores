import java.util.*;

public class Lexer {
    private String input;
    private int position;
    private static final Map<Set<Integer>, Map<String, Set<Integer>>> transitionTable = new HashMap<>();
    private static final Map<Set<Integer>, String> finalStates = new HashMap<>();

    private static final Set<Integer> startState = new HashSet<>(Arrays.asList(1));

    static {
        Map<String, Set<Integer>> tempTransitions;
        tempTransitions = new HashMap<>();
        tempTransitions.put("a", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList()), tempTransitions);
        tempTransitions = new HashMap<>();
        tempTransitions.put("a", new HashSet<>(Arrays.asList()));
        transitionTable.put(new HashSet<>(Arrays.asList(1)), tempTransitions);
    }

    static {
        finalStates.put(new HashSet<>(Arrays.asList(1)), "A_TOKEN");
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

    public static void main(String[] args) {
        Lexer lexer = new Lexer("aabbcc");
        List<Token> tokens = lexer.tokenize();
        tokens.forEach(System.out::println);
    }
}

class Token {
    private String type;
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
