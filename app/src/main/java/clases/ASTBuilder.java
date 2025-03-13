package clases;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ASTBuilder {
    private String postfix;
    private int positionCounter = 1;
    private Map<Integer, Set<Integer>> followpos = new HashMap<>();
    private Map<Integer, String> symbolTable = new HashMap<>();
    int acceptingPosition = -1;
    
    public ASTBuilder(String postfix) {
        this.postfix = postfix;
    }

    public ASTNode buildAST() {
        Stack<ASTNode> stack = new Stack<>();
        boolean escaped = false;
        for (char c : postfix.toCharArray()) {
            if (escaped) {
                // El carácter actual se trata como símbolo porque antes hubo '/'
                ASTNode leaf = new ASTNode(String.valueOf(c), positionCounter);
                symbolTable.put(positionCounter, String.valueOf(c));
                followpos.put(positionCounter, new HashSet<>());
                stack.push(leaf);
                positionCounter++;
                escaped = false; // Resetear la bandera
            } else if (c == '/') {
                escaped = true; // La siguiente iteración debe tratar el carácter como símbolo
            } else if (c == '?' || c == 'ε') {
                // Epsilon: Es siempre nullable, sin firstpos ni lastpos
                ASTNode epsilonNode = new ASTNode("?", -1); 
                epsilonNode.nullable = true;
                stack.push(epsilonNode);
            }else if (c == '|' || c == '^') {
                ASTNode right = stack.pop();
                ASTNode left = stack.pop();
                stack.push(new ASTNode(String.valueOf(c), left, right));
            } else if (c == '*') {
                ASTNode child = stack.pop();
                ASTNode starNode = new ASTNode("*", child, null);
                stack.push(starNode);
            } else if (c == '.') {
                // Punto como un símbolo especial (no un operador)
                ASTNode leaf = new ASTNode(".", positionCounter); // El punto es tratado como símbolo
                //followpos.put(positionCounter, new HashSet<>());
                stack.push(leaf);
                acceptingPosition = positionCounter;
                positionCounter++;
            } else {
                ASTNode leaf = new ASTNode(String.valueOf(c), positionCounter);
                symbolTable.put(positionCounter, String.valueOf(c));
                followpos.put(positionCounter, new HashSet<>());
                stack.push(leaf);
                positionCounter++;
            }
        }
        return stack.pop();
    }

    public void computeNullableFirstLast(ASTNode node) {
        if (node == null) return;
        computeNullableFirstLast(node.left);
        computeNullableFirstLast(node.right);

        if (node.position != -1) {
            node.nullable = false;
        } else if (node.value.equals("?")) {
            node.nullable = true;  // Siempre nullable
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
}
