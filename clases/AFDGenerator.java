package clases;

import java.util.*;

public class AFDGenerator {
    private Map<Integer, Set<Integer>> followpos;
    private Map<Integer, String> symbolTable;
    private Set<Integer> startState;
    private Set<Set<Integer>> states;
    private Map<Set<Integer>, Map<String, Set<Integer>>> transitions;

    public AFDGenerator(Map<Integer, Set<Integer>> followpos, Map<Integer, String> symbolTable, Set<Integer> startState) {
        this.followpos = followpos;
        this.symbolTable = symbolTable;
        this.startState = startState;
        this.states = new HashSet<>();
        this.transitions = new HashMap<>();
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
                if (symbol == null) continue;

                transitionMap.putIfAbsent(symbol, new HashSet<>());
                transitionMap.get(symbol).addAll(followpos.get(position));
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
            System.out.println(state);
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

