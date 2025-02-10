package clases;

import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;

public class AFDGenerator {
    private Map<Integer, Set<Integer>> followpos;
    private Map<Integer, String> symbolTable;
    private Set<Integer> startState;
    private Set<Set<Integer>> states;
    private Map<Set<Integer>, Map<String, Set<Integer>>> transitions;
    private Set<Integer> deadState;
    private Set<Set<Integer>> acceptedStates;
    private Map<Set<Integer>, Integer> stateMapping; 
    private Map<Set<Integer>, String> stateLabels;

    public AFDGenerator(Map<Integer, Set<Integer>> followpos, Map<Integer, String> symbolTable, Set<Integer> startState, int acceptingPosition) {
        this.followpos = followpos;
        this.symbolTable = symbolTable;
        this.startState = startState;
        this.states = new HashSet<>();
        this.transitions = new HashMap<>();
        this.deadState = new HashSet<>();
        this.acceptedStates = new HashSet<>();
        this.stateLabels = new HashMap<>();

        generateAFD();  // Generate the AFD and populate states and transitions
        markAcceptedStates(acceptingPosition);
    }

    private void markAcceptedStates(int acceptingPosition) {
        for (Set<Integer> state : states) {
            if (state.contains(acceptingPosition)) {
                acceptedStates.add(state);
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
                if (symbol == null) {
                    // If the state has a null symbol, it should transition to the dead state for all symbols
                    for (String transitionSymbol : symbolTable.values()) {
                        transitionMap.putIfAbsent(transitionSymbol, new HashSet<>());
                        transitionMap.get(transitionSymbol).addAll(deadState);  // All transitions go to the dead state
                    }
                } else {
                    // Normal transition for valid symbols
                    transitionMap.putIfAbsent(symbol, new HashSet<>());
                    transitionMap.get(symbol).addAll(followpos.get(position));
                }
            }

            if (currentState.isEmpty()) {
                for (String symbol : symbolTable.values()) {
                    // Add transitions from the empty state to itself for all symbols
                    transitionMap.putIfAbsent(symbol, new HashSet<>());
                    transitionMap.get(symbol).addAll(currentState);  // Empty state transitions to itself
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

    /** MINIMIZATION USING HOPCROFT'S ALGORITHM **/
    public void minimizeAFD() {
        Set<Set<Integer>> acceptingGroup = new HashSet<>();
        Set<Set<Integer>> nonAcceptingGroup = new HashSet<>();
        
        for (Set<Integer> state : states) {
            if (acceptedStates.contains(state)) {
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
        
        // Update the transitions, states, and start state with minimized values
        this.states = new HashSet<>();
        for (Set<Set<Integer>> partition : partitions) {
            this.states.addAll(partition);
        }
        this.transitions = minimizedTransitions;
        this.startState = minimizedStartState;
    }
    
    // Helper method to check if a state contains the original start state
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
            if (acceptedStates.contains(state)) {
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

        return acceptedStates.contains(estadoActual); // Verificar si el estado final es de aceptación
    }

    public void generarDot(String nombreArchivo) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph AFD {\n");
        dot.append("    rankdir=LR;\n");
        dot.append("    node [shape=circle];\n");

        for (Set<Integer> estado : states) {
            String estadoLabel = estado.toString();
            if (acceptedStates.contains(estado)) {
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

        try (PrintWriter writer = new PrintWriter(nombreArchivo)) {
            writer.write(dot.toString());
            System.out.println("Archivo DOT generado: " + nombreArchivo);
        } catch (IOException e) {
            System.err.println("Error al escribir el archivo DOT: " + e.getMessage());
        }
    }
}

