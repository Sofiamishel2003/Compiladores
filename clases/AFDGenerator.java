package clases;

import java.util.*;

public class AFDGenerator {
    private Map<Integer, Set<Integer>> followpos;
    private Map<Integer, String> symbolTable;
    private Set<Integer> startState;
    private Set<Set<Integer>> states;
    private Map<Set<Integer>, Map<String, Set<Integer>>> transitions;
    private Set<Integer> deadState;
    private Set<Set<Integer>> acceptedStates;

    public AFDGenerator(Map<Integer, Set<Integer>> followpos, Map<Integer, String> symbolTable, Set<Integer> startState, int acceptingPosition) {
        this.followpos = followpos;
        this.symbolTable = symbolTable;
        this.startState = startState;
        this.states = new HashSet<>();
        this.transitions = new HashMap<>();
        this.deadState = new HashSet<>();
        this.acceptedStates = new HashSet<>();

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

            transitions.put(currentState, transitionMap);

            for (Set<Integer> newState : transitionMap.values()) {
                if (!states.contains(newState)) {
                    states.add(newState);
                    queue.add(newState);
                }
            }
        }
    }

    public void printAFD() {
        System.out.println("Estados:");
        for (Set<Integer> state : states) {
            System.out.println(state + (acceptedStates.contains(state) ? " (Accepted)" : ""));
        }
        System.out.println("\nTransiciones:");
        for (Map.Entry<Set<Integer>, Map<String, Set<Integer>>> entry : transitions.entrySet()) {
            Set<Integer> fromState = entry.getKey();
            for (Map.Entry<String, Set<Integer>> transition : entry.getValue().entrySet()) {
                System.out.println(fromState + " -- " + transition.getKey() + " --> " + transition.getValue());
            }
        }
    }
}

