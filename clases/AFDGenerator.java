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
    private Map<Set<Integer>, Integer> stateMapping; 

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
        Set<Set<Integer>> partitions = new HashSet<>();
        Set<Set<Integer>> newPartitions = new HashSet<>();

        Set<Integer> nonAccepting = new HashSet<>();
        for (Set<Integer> state : states) {
            if (!acceptedStates.contains(state)) {
                nonAccepting.addAll(state);
            }
        }

        partitions.add(nonAccepting);
        partitions.addAll(acceptedStates);

        boolean changed;
        do {
            newPartitions.clear();
            changed = false;

            for (Set<Integer> group : partitions) {
                Map<Map<String, Set<Integer>>, Set<Integer>> classified = new HashMap<>();

                for (Integer state : group) {
                    Map<String, Set<Integer>> transitionMap = new HashMap<>();
                    for (String symbol : symbolTable.values()) {
                        for (Set<Integer> targetGroup : partitions) {
                            if (targetGroup.contains(getTransitionState(state, symbol))) {
                                transitionMap.put(symbol, targetGroup);
                                break;
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

    private Integer getTransitionState(Integer state, String symbol) {
        for (Set<Integer> key : transitions.keySet()) {
            if (key.contains(state) && transitions.get(key).containsKey(symbol)) {
                Set<Integer> targetSet = transitions.get(key).get(symbol);
                if (!targetSet.isEmpty()) {  
                    return targetSet.iterator().next();
                }
            }
        }
        return -1;
    }

    private void reconstructMinimizedAFD(Set<Set<Integer>> partitions) {
        Map<Set<Integer>, Map<String, Set<Integer>>> minimizedTransitions = new HashMap<>();
        this.stateMapping = new HashMap<>();
        stateMapping.clear();

        Set<Integer> minimizedStartState = null;
        Set<Integer> deadState = null;

        int stateCounter = 0;
        for (Set<Integer> partition : partitions) {
            stateMapping.put(partition, stateCounter++);
            if (partition.containsAll(startState)) {
                minimizedStartState = partition; 
            }
            if (partition.isEmpty()) {  
                deadState = partition;
            }
        }

        for (Set<Integer> partition : partitions) {
            Set<Integer> representative = partition;
            Map<String, Set<Integer>> transitionMap = new HashMap<>();

            for (String symbol : symbolTable.values()) {
                boolean foundTransition = false;
                for (Set<Integer> targetPartition : partitions) {
                    if (targetPartition.contains(getTransitionState(representative.iterator().next(), symbol))) {
                        transitionMap.put(symbol, targetPartition);
                        foundTransition = true;
                        break;
                    }
                }
                if (!foundTransition && deadState != null) {
                    transitionMap.put(symbol, deadState);  
                }
            }

            minimizedTransitions.put(partition, transitionMap);
        }

        this.states = partitions;
        this.transitions = minimizedTransitions;
        this.startState = minimizedStartState; 
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
}

