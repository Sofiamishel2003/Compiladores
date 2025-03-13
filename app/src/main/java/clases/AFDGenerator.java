package clases;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AFDGenerator {
    private Map<Integer, Set<Integer>> followpos;
    private Map<Integer, String> symbolTable;
    private Set<Integer> startState;
    private Set<Set<Integer>> states;
    private Map<Set<Integer>, Map<String, Set<Integer>>> transitions;
    private Set<Integer> deadState;
    //private Set<Set<Integer>> acceptedStates;
    private Map<Set<Integer>, String> acceptedStates;

    private Map<Set<Integer>, Integer> stateMapping;
    private Map<Set<Integer>, String> stateLabels;

    public AFDGenerator(Map<Integer, Set<Integer>> followpos, Map<Integer, String> symbolTable, Set<Integer> startState,
            int acceptingPosition) {
        this.followpos = followpos;
        this.symbolTable = symbolTable;
        this.startState = startState;
        this.states = new HashSet<>();
        this.transitions = new HashMap<>();
        this.deadState = new HashSet<>();
        //this.acceptedStates = new HashSet<>();
        this.acceptedStates = new HashMap<>();
        this.stateLabels = new HashMap<>();

        generateAFD(); // Generate the AFD and populate states and transitions
        markAcceptedStates(acceptingPosition);

        generarCodigoLexer("Lexer.java");
    }

    //private void markAcceptedStates(int acceptingPosition) {
    //    for (Set<Integer> state : states) {
    //        if (state.contains(acceptingPosition)) {
    //            acceptedStates.add(state);
    //        }
    //    }
    //}
    private void markAcceptedStates(int acceptingPosition) {
        for (Set<Integer> state : states) {
            if (state.contains(acceptingPosition)) {
                String tokenName = symbolTable.get(acceptingPosition);
                acceptedStates.put(state, tokenName); // Asocia el estado con el token
            }
        }
    }
    

    public void generateAFD() {
        Queue<Set<Integer>> queue = new LinkedList<>();
        queue.add(startState);
        states.add(startState);

        while (!queue.isEmpty()) {
            Set<Integer> currentState = queue.poll();
            Map<String, Set<Integer>> transitionMap = new HashMap<>();

            for (int position : currentState) {
                String symbol = symbolTable.get(position);

                if (symbol == null || symbol.equals("?") || symbol.equals("ε")) {
                    // si el estado tiene un simbolo null deberia ir al deadstate
                    for (String transitionSymbol : symbolTable.values()) {
                        transitionMap.putIfAbsent(transitionSymbol, new HashSet<>());
                        transitionMap.get(transitionSymbol).addAll(deadState); // todas las transiciones van al
                                                                               // deadstate
                    }
                } else {
                    // transiciones para simbolos validos
                    transitionMap.putIfAbsent(symbol, new HashSet<>());
                    transitionMap.get(symbol).addAll(followpos.get(position));
                }

            }

            if (currentState.isEmpty()) {
                for (String symbol : symbolTable.values()) {
                    // deadstate a si mismo para todas las transiciones
                    transitionMap.putIfAbsent(symbol, new HashSet<>());
                    transitionMap.get(symbol).addAll(currentState);
                }
            }

            transitions.put(currentState, transitionMap);

            for (Set<Integer> newState : transitionMap.values()) {
                if (!states.contains(newState)) {
                    states.add(newState);
                    queue.add(newState);
                }
            }
        }
    }

    /** MINIMIZACION USANDO HOPCROFT'S ALGORITHM **/
    public void minimizeAFD() {
        Set<Set<Integer>> acceptingGroup = new HashSet<>();
        Set<Set<Integer>> nonAcceptingGroup = new HashSet<>();

        for (Set<Integer> state : states) {
            if (acceptedStates.containsKey(state)) {
                acceptingGroup.add(state);
            } else {
                nonAcceptingGroup.add(state);
            }
        }

        Set<Set<Set<Integer>>> partitions = new HashSet<>();
        partitions.add(acceptingGroup);
        partitions.add(nonAcceptingGroup);

        boolean changed;
        do {
            Set<Set<Set<Integer>>> newPartitions = new HashSet<>();
            changed = false;

            for (Set<Set<Integer>> group : partitions) {
                Map<Map<String, Set<Integer>>, Set<Set<Integer>>> classified = new HashMap<>();
                for (Set<Integer> state : group) {
                    Map<String, Set<Integer>> transitionMap = new HashMap<>();

                    for (String symbol : symbolTable.values()) {
                        Set<Integer> targetState = getTransitionState(state, symbol);

                        for (Set<Set<Integer>> partition : partitions) {
                            for (Set<Integer> partitionState : partition) {
                                if (partitionState.equals(targetState)) {

                                    transitionMap.put(symbol, partitionState);
                                    break;
                                }
                            }
                        }
                    }
                    classified.computeIfAbsent(transitionMap, k -> new HashSet<>()).add(state);
                }

                newPartitions.addAll(classified.values());
                if (classified.size() > 1) {
                    changed = true;
                }
            }

            partitions = new HashSet<>(newPartitions);
        } while (changed);
        reconstructMinimizedAFD(partitions);
    }

    private Set<Integer> getTransitionState(Set<Integer> state, String symbol) {
        for (Set<Integer> key : transitions.keySet()) {
            if (key.containsAll(state) && transitions.get(key).containsKey(symbol)) {
                Set<Integer> targetSet = transitions.get(key).get(symbol);
                if (!targetSet.isEmpty()) {
                    return targetSet;
                }
            }
        }
        return new HashSet<>();
    }

    private void reconstructMinimizedAFD(Set<Set<Set<Integer>>> partitions) {
        Map<Set<Integer>, Map<String, Set<Integer>>> minimizedTransitions = new HashMap<>();
        this.stateMapping = new HashMap<>();
        stateMapping.clear();

        Set<Integer> minimizedStartState = null;
        Set<Integer> minimizedDeadState = null;

        int stateCounter = 0;
        for (Set<Set<Integer>> partition : partitions) {
            for (Set<Integer> state : partition) {
                stateMapping.put(state, stateCounter++);

                if (minimizedStartState == null && containsStartState(state)) {
                    minimizedStartState = state;
                }

                if (state.isEmpty()) {
                    minimizedDeadState = state;
                }
            }
        }

        if (minimizedDeadState != null) {
            for (Set<Set<Integer>> partition : partitions) {
                partition.remove(minimizedDeadState);
            }
        }

        for (Set<Set<Integer>> partition : partitions) {
            for (Set<Integer> state : partition) {
                Map<String, Set<Integer>> transitionMap = new HashMap<>();

                for (String symbol : symbolTable.values()) {
                    Set<Integer> targetState = getTransitionState(state, symbol);

                    if (targetState.isEmpty() && minimizedDeadState != null) {
                        transitionMap.put(symbol, minimizedDeadState);
                    } else {
                        for (Set<Set<Integer>> targetPartition : partitions) {
                            for (Set<Integer> targetStateSet : targetPartition) {
                                if (targetStateSet.equals(targetState)) {
                                    transitionMap.put(symbol, targetStateSet);
                                    break;
                                }
                            }
                        }
                    }
                }

                minimizedTransitions.put(state, transitionMap);
            }
        }

        this.states = new HashSet<>();
        for (Set<Set<Integer>> partition : partitions) {
            this.states.addAll(partition);
        }
        this.transitions = minimizedTransitions;
        this.startState = minimizedStartState;
    }

    private boolean containsStartState(Set<Integer> state) {
        return state.containsAll(startState);
    }

    public void printAFD() {
        System.out.println("Estados:");
        for (Set<Integer> state : states) {
            String stateLabel = "";
            if (state.equals(startState)) {
                stateLabel += " (Start)";
            }
            if (acceptedStates.containsKey(state)) {
                stateLabel += " (Accepted)";
            }
            System.out.println(state + stateLabel);
        }
        System.out.println("\nTransiciones:");
        for (Map.Entry<Set<Integer>, Map<String, Set<Integer>>> entry : transitions.entrySet()) {
            Set<Integer> fromState = entry.getKey();
            for (Map.Entry<String, Set<Integer>> transition : entry.getValue().entrySet()) {
                System.out.println(fromState + " -- " + transition.getKey() + " --> " + transition.getValue());
            }
        }
    }

    public boolean verificarCadena(String cadena) {
        Set<Integer> estadoActual = startState; // Iniciar en el estado inicial

        for (char simbolo : cadena.toCharArray()) {
            String simboloStr = String.valueOf(simbolo);
            if (!transitions.containsKey(estadoActual) || !transitions.get(estadoActual).containsKey(simboloStr)) {
                return false; // No hay transición para el símbolo, la cadena no es aceptada
            }
            estadoActual = transitions.get(estadoActual).get(simboloStr); // Mover al siguiente estado
        }

        return acceptedStates.containsKey(estadoActual); // Verificar si el estado final es de aceptación
    }

    public void generarDot(String nombreBase) {
        new File("media/other").mkdirs(); // Carpeta para los archivos DOT
        new File("media/img").mkdirs(); // Carpeta para los archivos PNG
        String dotFilePath = "media/other" + nombreBase + ".dot";
        String pngFilePath = "media/img" + nombreBase + ".png";
        System.out.println(pngFilePath);

        StringBuilder dot = new StringBuilder();
        dot.append("digraph AFD {\n");
        dot.append("    rankdir=LR;\n");
        dot.append("    node [shape=circle];\n");

        for (Set<Integer> estado : states) {
            String estadoLabel = estado.toString();
            if (acceptedStates.containsKey(estado)) {
                dot.append("    \"" + estadoLabel + "\" [shape=doublecircle];\n");
            }
            if (estado.equals(startState)) {
                dot.append("    inicio [shape=point];\n");
                dot.append("    inicio -> \"" + estadoLabel + "\";\n");
            }
        }

        for (Map.Entry<Set<Integer>, Map<String, Set<Integer>>> transicion : transitions.entrySet()) {
            Set<Integer> desde = transicion.getKey();
            for (Map.Entry<String, Set<Integer>> mov : transicion.getValue().entrySet()) {
                String simbolo = mov.getKey();
                Set<Integer> hacia = mov.getValue();
                dot.append("    \"" + desde + "\" -> \"" + hacia + "\" [label=\"" + simbolo + "\"];\n");
            }
        }

        dot.append("}\n");

        // Guardar el archivo DOT en media/other/
        try (PrintWriter writer = new PrintWriter(dotFilePath)) {
            writer.write(dot.toString());
            System.out.println("Archivo DOT generado: " + dotFilePath);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo DOT: " + e.getMessage());
            return;
        }

        // Generar PNG automáticamente en media/img/
        try {
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFilePath, "-o", pngFilePath);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            process.waitFor();
            System.out.println("Imagen PNG generada: " + pngFilePath);
        } catch (IOException | InterruptedException e) {
            System.err.println("Error al generar el archivo PNG: " + e.getMessage());
        }
    }

    public void generarCodigoLexer(String nombreArchivo) {
        StringBuilder codigo = new StringBuilder();
        codigo.append("import java.util.*;\n\n");
        codigo.append("public class Lexer {\n");
        codigo.append("    private String input;\n");
        codigo.append("    private int position;\n");
        codigo.append("    private static final Map<Integer, Map<Character, Integer>> transitionTable = new HashMap<>();\n");
        codigo.append("    private static final Map<Integer, String> finalStates = new HashMap<>();\n\n");

        // Construir la tabla de transiciones en el código generado
        codigo.append("    static {\n");
        for (Map.Entry<Set<Integer>, Map<String, Set<Integer>>> entry : transitions.entrySet()) {
            Set<Integer> state = entry.getKey();  // El estado es un conjunto de enteros
            Map<String, Set<Integer>> transitionsMap = entry.getValue();
            codigo.append("        transitionTable.put(").append(state).append(", Map.of(");
            List<String> transList = new ArrayList<>();
            for (var t : transitions.entrySet()) {
                transList.add("'" + t.getKey() + "', " + t.getValue());
            }
            codigo.append(String.join(", ", transList));
            codigo.append("));\n");
        }
        codigo.append("    }\n\n");

        // Estados de aceptación
        codigo.append("    static {\n");
        for (var entry : acceptedStates.entrySet()) {
            codigo.append("        finalStates.put(").append(entry.getKey()).append(", \"").append(entry.getValue()).append("\");\n");
        }
        codigo.append("    }\n\n");

        // Constructor del lexer
        codigo.append("    public Lexer(String input) {\n");
        codigo.append("        this.input = input;\n");
        codigo.append("        this.position = 0;\n");
        codigo.append("    }\n\n");

        // Método tokenize()
        codigo.append("    public List<Token> tokenize() {\n");
        codigo.append("        List<Token> tokens = new ArrayList<>();\n");
        codigo.append("        while (position < input.length()) {\n");
        codigo.append("            Token token = nextToken();\n");
        codigo.append("            if (token != null) {\n");
        codigo.append("                tokens.add(token);\n");
        codigo.append("            } else {\n");
        codigo.append("                System.err.println(\"Error léxico en posición \" + position);\n");
        codigo.append("                break;\n");
        codigo.append("            }\n");
        codigo.append("        }\n");
        codigo.append("        return tokens;\n");
        codigo.append("    }\n\n");

        // Método nextToken()
        codigo.append("    private Token nextToken() {\n");
        codigo.append("        int state = 0;\n");
        codigo.append("        int start = position;\n");
        codigo.append("        while (position < input.length()) {\n");
        codigo.append("            char current = input.charAt(position);\n");
        codigo.append("            if (!transitionTable.containsKey(state) || !transitionTable.get(state).containsKey(current)) {\n");
        codigo.append("                break;\n");
        codigo.append("            }\n");
        codigo.append("            state = transitionTable.get(state).get(current);\n");
        codigo.append("            position++;\n");
        codigo.append("        }\n");
        codigo.append("        if (finalStates.containsKey(state)) {\n");
        codigo.append("            return new Token(finalStates.get(state), input.substring(start, position));\n");
        codigo.append("        }\n");
        codigo.append("        return null;\n");
        codigo.append("    }\n\n");

        // Método main para prueba
        codigo.append("    public static void main(String[] args) {\n");
        codigo.append("        Lexer lexer = new Lexer(\"aabbcc\");\n");
        codigo.append("        List<Token> tokens = lexer.tokenize();\n");
        codigo.append("        tokens.forEach(System.out::println);\n");
        codigo.append("    }\n");
        codigo.append("}\n\n");

        // Clase Token
        codigo.append("class Token {\n");
        codigo.append("    private String type;\n");
        codigo.append("    private String value;\n\n");
        codigo.append("    public Token(String type, String value) {\n");
        codigo.append("        this.type = type;\n");
        codigo.append("        this.value = value;\n");
        codigo.append("    }\n\n");
        codigo.append("    @Override\n");
        codigo.append("    public String toString() {\n");
        codigo.append("        return \"[\" + type + \": \\\"\" + value + \"\\\"]\";\n");
        codigo.append("    }\n");
        codigo.append("}\n");

        // Escribir el archivo
        try (FileWriter writer = new FileWriter(nombreArchivo)) {
            writer.write(codigo.toString());
            System.out.println("Archivo Lexer.java generado con éxito.");
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo Lexer.java: " + e.getMessage());
        }
    }

    
}
