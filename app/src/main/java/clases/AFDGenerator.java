package clases;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class AFDGenerator {
    private Map<Integer, Set<Integer>> followpos;
    private Map<Integer, String> symbolTable;
    private Set<Integer> startState;
    private Set<Set<Integer>> states;
    private Map<Set<Integer>, Map<String, Set<Integer>>> transitions;
    private Set<Integer> deadState;
    private Map<Set<Integer>, String> acceptedStates;

    private Map<Set<Integer>, Integer> stateMapping;
    private Map<Set<Integer>, String> stateLabels;
    private Map<Integer, String> positionToTokenMap;


    public AFDGenerator(Map<Integer, Set<Integer>> followpos, Map<Integer, String> symbolTable, Set<Integer> startState, Map<Integer, String> positionToTokenMap) {
        this.followpos = followpos;
        this.symbolTable = symbolTable;
        this.startState = startState;
        this.states = new HashSet<>();
        this.transitions = new HashMap<>();
        this.deadState = new HashSet<>();
        this.acceptedStates = new HashMap<>();
        this.stateLabels = new HashMap<>();
        this.positionToTokenMap = positionToTokenMap;

        generateAFD();
        markAcceptedStates(positionToTokenMap);

        generarCodigoLexer("Lexer.java");
    }

    private void markAcceptedStates(Map<Integer, String> positionToTokenMap) {
        for (Set<Integer> state : states) {
            for (int pos : state) {
                if (positionToTokenMap.containsKey(pos)) {
                    acceptedStates.put(state, positionToTokenMap.get(pos));
                    break;
                }
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
                if (symbol == null || symbol.equals("?") || symbol.equals("ε") || symbol.startsWith("TOKEN_")) {
                    continue; // ignora símbolos no válidos o etiquetas TOKEN_
                }

                transitionMap.putIfAbsent(symbol, new HashSet<>());
                transitionMap.get(symbol).addAll(followpos.get(position));
            }

            if (currentState.isEmpty()) {
                for (String symbol : symbolTable.values()) {
                    if (!symbol.startsWith("TOKEN_")) {
                        transitionMap.putIfAbsent(symbol, new HashSet<>());
                        transitionMap.get(symbol).addAll(currentState);
                    }
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
        Set<Integer> estadoActual = startState; // Estado inicial
        System.out.println("\n[🔍 Verificando cadena: \"" + cadena + "\"]");
        System.out.println("→ Estado inicial: " + estadoActual);

        for (int i = 0; i < cadena.length(); i++) {
            char simbolo = cadena.charAt(i);
            String simboloStr = String.valueOf(simbolo);

            System.out.printf("  [%d] '%s' ", i, simboloStr);

            if (!transitions.containsKey(estadoActual)) {
                System.out.println("No hay transiciones desde este estado.");
                return false;
            }

            Map<String, Set<Integer>> mapa = transitions.get(estadoActual);
            if (!mapa.containsKey(simboloStr)) {
                System.out.println("No hay transición con símbolo '" + simboloStr + "'");
                return false;
            }

            Set<Integer> siguienteEstado = mapa.get(simboloStr);
            System.out.println("→ " + siguienteEstado);

            estadoActual = siguienteEstado;
        }

        System.out.println("→ Estado final: " + estadoActual);

        for (Set<Integer> estadoAceptado : acceptedStates.keySet()) {
            if (estadoAceptado.equals(estadoActual)) {
                String token = acceptedStates.get(estadoAceptado);
                System.out.println("Estado de aceptación. Token reconocido: " + token);
                return true;
            }
        }

        System.out.println("Estado no aceptado.");
        return false;
    }

    public void generarDot(String nombreBase) {
    try {
        // Crear directorios si no existen
        Path dotDir = Paths.get("media", "other");
        Path imgDir = Paths.get("media", "img");
        Files.createDirectories(dotDir);
        Files.createDirectories(imgDir);

        // Archivos de salida
        Path dotFilePath = dotDir.resolve(nombreBase + ".dot");
        Path pngFilePath = imgDir.resolve(nombreBase + ".png");

        // Generar contenido DOT
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

        // Escribir archivo DOT
        Files.write(dotFilePath, dot.toString().getBytes(StandardCharsets.UTF_8));
        System.out.println("✅ Archivo DOT generado en: " + dotFilePath.toAbsolutePath());

        // Ejecutar Graphviz para generar PNG
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", dotFilePath.toString(), "-o", pngFilePath.toString());
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                System.out.println("[graphviz] " + linea);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode == 0) {
            System.out.println("🖼️ Imagen PNG generada en: " + pngFilePath.toAbsolutePath());
        } else {
            System.err.println("⚠️ Error al generar imagen PNG. Código de salida: " + exitCode);
        }

    } catch (IOException | InterruptedException e) {
        System.err.println("❌ Error al generar el grafo: " + e.getMessage());
        Thread.currentThread().interrupt(); // buena práctica al capturar InterruptedException
    }
}

    public void generarCodigoLexer(String nombreArchivo) {
        StringBuilder codigo = new StringBuilder();
        codigo.append("import java.util.*;\n\n");
        codigo.append("public class Lexer {\n");
        codigo.append("    private String input;\n");
        codigo.append("    private int position;\n");
        codigo.append("    private static final Map<Set<Integer>, Map<String, Set<Integer>>> transitionTable = new HashMap<>();\n");
        codigo.append("    private static final Map<Set<Integer>, String> finalStates = new HashMap<>();\n\n");
        codigo.append("    private static final Set<Integer> startState = new HashSet<>(Arrays.asList(")
            .append(startState.toString().replaceAll("[\\[\\]]", ""))
            .append("));\n\n");

        // Construcción de la tabla de transiciones
        codigo.append("    static {\n");
        codigo.append("        Map<String, Set<Integer>> tempTransitions;\n");
        for (Map.Entry<Set<Integer>, Map<String, Set<Integer>>> entry : transitions.entrySet()) {
            Set<Integer> state = entry.getKey();
            Map<String, Set<Integer>> transitionsMap = entry.getValue();
            
            codigo.append("        tempTransitions = new HashMap<>();\n");
            for (var t : transitionsMap.entrySet()) {
                codigo.append("        tempTransitions.put(\"").append(t.getKey()).append("\", new HashSet<>(Arrays.asList(")
                      .append(t.getValue().toString().replaceAll("[\\[\\]]", "")).append(")));\n");
            }
            codigo.append("        transitionTable.put(new HashSet<>(Arrays.asList(")
                  .append(state.toString().replaceAll("[\\[\\]]", ""))
                  .append(")), tempTransitions);\n");
        }
        codigo.append("    }\n\n");
    
        // Estados de aceptación
        codigo.append("    static {\n");
        for (var entry : acceptedStates.entrySet()) {
            codigo.append("        finalStates.put(new HashSet<>(Arrays.asList(")
                  .append(entry.getKey().toString().replaceAll("[\\[\\]]", ""))
                  .append(")), \"").append(entry.getValue()).append("\");\n");
        }
        codigo.append("    }\n\n");
    
        // Constructor
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
        codigo.append("                position++;\n"); // Evitar bucles infinitos
        codigo.append("            }\n");
        codigo.append("        }\n");
        codigo.append("        return tokens;\n");
        codigo.append("    }\n\n");
    
        // Método nextToken()
        codigo.append("    private Token nextToken() {\n");
        codigo.append("        Set<Integer> state = new HashSet<>(startState);\n");
        codigo.append("        int start = position;\n");
        codigo.append("        int lastAcceptingPos = -1;\n");
        codigo.append("        String lastAcceptingType = null;\n\n");
    
        codigo.append("        while (position < input.length()) {\n");
        codigo.append("            String current = String.valueOf(input.charAt(position));\n");
        codigo.append("            if (!transitionTable.containsKey(state) || !transitionTable.get(state).containsKey(current)) {\n");
        codigo.append("                break;\n");
        codigo.append("            }\n");
        codigo.append("            state = transitionTable.get(state).get(current);\n");
        codigo.append("            position++;\n\n");
    
        codigo.append("            if (finalStates.containsKey(state)) {\n");
        codigo.append("                lastAcceptingPos = position;\n");
        codigo.append("                lastAcceptingType = finalStates.get(state);\n");
        codigo.append("            }\n");
        codigo.append("        }\n");
        codigo.append("        if (lastAcceptingType != null) {\n");
        codigo.append("            position = lastAcceptingPos;\n");
        codigo.append("            return new Token(lastAcceptingType, input.substring(start, position));\n");
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
