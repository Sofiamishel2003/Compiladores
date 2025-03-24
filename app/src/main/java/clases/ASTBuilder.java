package clases;

import java.util.*;

public class ASTBuilder {
    private String postfix;
    private int positionCounter = 1;
    private Map<Integer, Set<Integer>> followpos = new HashMap<>();
    private Map<Integer, String> symbolTable = new HashMap<>();
    private Map<Integer, String> acceptingPositions = new HashMap<>();
    int acceptingPosition = -1;

    public ASTBuilder(String postfix) {
        this.postfix = postfix;
    }

    public ASTNode buildAST() {
        Stack<ASTNode> stack = new Stack<>();
        List<String> tokens = tokenizePostfix();
        System.out.println("Tokens:");
        for (String t : tokens) {
            System.out.print("[" + t + "] ");
        }
        System.out.println();
        for (String token : tokens) {
            switch (token) {
                case "|":
                case "^": {
                    if (stack.size() < 2) throw new IllegalStateException("Faltan operandos para " + token);
                    ASTNode right = stack.pop();
                    ASTNode left = stack.pop();
                    ASTNode op = new ASTNode(token, left, right);
                    stack.push(op);
                    break;
                }
                case "*": {
                    if (stack.isEmpty()) throw new IllegalStateException("Falta operando para *");
                    ASTNode child = stack.pop();
                    stack.push(new ASTNode("*", child, null));
                    break;
                }
                case "?": {
                    ASTNode epsilon = new ASTNode("?", -1);
                    epsilon.nullable = true;
                    stack.push(epsilon);
                    break;
                }
                default: {
                    if (token.startsWith("#")) {
                        String num = token.substring(1);
                        if (stack.isEmpty()) throw new IllegalStateException("No hay nodo al que asociar el token " + token);
                        ASTNode last = stack.pop();
                        acceptingPositions.put(last.position, "TOKEN_" + num);
                        symbolTable.put(last.position, "TOKEN_" + num);
                        stack.push(last);
                    } else {
                        ASTNode leaf = new ASTNode(token, positionCounter);
                        symbolTable.put(positionCounter, token);
                        followpos.put(positionCounter, new HashSet<>());
                        stack.push(leaf);
                        positionCounter++;
                    }
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("La postfix no se redujo a un solo árbol. Stack: " + stack);
        }

        return stack.pop();
    }

    private List<String> tokenizePostfix() {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
    
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);
        
            // Escaped sequence
            if (c == '\\') {
                current.setLength(0);
                current.append(c);
                i++;
                if (i < postfix.length()) {
                    current.append(postfix.charAt(i));
    
                    // Handle unicode \u0000
                    if (postfix.charAt(i) == 'u' && i + 4 < postfix.length()) {
                        for (int j = 0; j < 4; j++) {
                            i++;
                            current.append(postfix.charAt(i));
                        }
                    }
                }
                tokens.add(current.toString());
            }
    
            // Token number like #10
            else if (c == '#') {
                current.setLength(0);
                current.append('#');
                i++;
                while (i < postfix.length() && Character.isDigit(postfix.charAt(i))) {
                    current.append(postfix.charAt(i));
                    i++;
                }
                i--; // rollback one char after last digit
                tokens.add(current.toString());
            }
    
            // Operators or other characters
            else if ("|^*?.".indexOf(c) != -1) {
                tokens.add(String.valueOf(c));
            } else {
                tokens.add(String.valueOf(c));
            }
        }
    
        return tokens;
    }
    
    

    public void computeNullableFirstLast(ASTNode node) {
        if (node == null) return;
        computeNullableFirstLast(node.left);
        computeNullableFirstLast(node.right);

        if (node.position != -1) {
            node.nullable = false;
            node.firstpos.add(node.position);
            node.lastpos.add(node.position);
        } else if (node.value.equals("?")) {
            node.nullable = true;
        } else if (node.value.equals("|")) {
            node.nullable = node.left.nullable || node.right.nullable;
            node.firstpos.addAll(node.left.firstpos);
            node.firstpos.addAll(node.right.firstpos);
            node.lastpos.addAll(node.left.lastpos);
            node.lastpos.addAll(node.right.lastpos);
        } else if (node.value.equals("^")) {
            node.nullable = node.left.nullable && node.right.nullable;
            node.firstpos.addAll(node.left.firstpos);
            if (node.left.nullable) node.firstpos.addAll(node.right.firstpos);
            node.lastpos.addAll(node.right.lastpos);
            if (node.right.nullable) node.lastpos.addAll(node.left.lastpos);
        } else if (node.value.equals("*")) {
            node.nullable = true;
            node.firstpos.addAll(node.left.firstpos);
            node.lastpos.addAll(node.left.lastpos);
        }
    }

    public void computeFollowpos(ASTNode node) {
        if (node == null) return;
        computeFollowpos(node.left);
        computeFollowpos(node.right);

        if (node.value.equals("^")) {
            for (int i : node.left.lastpos) {
                followpos.get(i).addAll(node.right.firstpos);
            }
        } else if (node.value.equals("*")) {
            for (int i : node.lastpos) {
                followpos.get(i).addAll(node.firstpos);
            }
        }
    }

    public void printFollowpos() {
        for (Map.Entry<Integer, Set<Integer>> entry : followpos.entrySet()) {
            System.out.println("followpos(" + entry.getKey() + ") = " + entry.getValue());
        }
    }

    public Map<Integer, Set<Integer>> getFollowpos() {
        return followpos;
    }

    public Map<Integer, String> getSymbolTable() {
        return symbolTable;
    }

    public Set<Integer> getStartState(ASTNode root) {
        return root.firstpos; 
    }

    public int getAcceptingPosition() {
        return acceptingPosition;
    }

    public Map<Integer, String> getAcceptingPositions() {
        return acceptingPositions;
    }
}