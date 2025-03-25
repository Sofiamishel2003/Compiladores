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
    private Map<Integer, String> acceptingTypes = new HashMap<>(); // Almacena el tipo de cada #num
    private Set<Integer> acceptingPositions = new HashSet<>(); // Ahora acepta múltiples posiciones
    
    public ASTBuilder(String postfix) {
        this.postfix = postfix;
    }

    public ASTNode buildAST() {
        Stack<ASTNode> stack = new Stack<>();
        boolean escaped = false;
        for (int i = 0; i < postfix.length(); i++) {
            char c = postfix.charAt(i);
        
            if (escaped) {
                if (c == 't') {
                    c = '\t';
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'u' && i + 4 < postfix.length()) {
                    // Procesar secuencias Unicode: \u0000
                    c= '\u0000';
                    i+=4;
                }
        
                // Crear una hoja con el carácter escapado o Unicode
                ASTNode leaf = new ASTNode(String.valueOf(c), positionCounter);
                symbolTable.put(positionCounter, String.valueOf(c));
                followpos.put(positionCounter, new HashSet<>());
                stack.push(leaf);
        
                positionCounter++;
                escaped = false;  // Resetear la bandera
            } else if (c == '/' || c == '\\') {
                escaped = true;  // Marcar para procesar el siguiente carácter como especial
            } else if (c == '?' || c == 'ε') {
                // Nodo epsilon
                ASTNode epsilonNode = new ASTNode("?", -1);
                epsilonNode.nullable = true;
                stack.push(epsilonNode);
            } else if (c == '|' || c == '^') {
                ASTNode right = stack.pop();
                ASTNode left = stack.pop();
                stack.push(new ASTNode(String.valueOf(c), left, right));
            } else if (c == '*') {
                ASTNode child = stack.pop();
                stack.push(new ASTNode("*", child, null));
            } else if (c == '#') {
                // Procesar #num (posición de aceptación con tipo)
                int start = i + 1;
                while (i + 1 < postfix.length() && Character.isDigit(postfix.charAt(i + 1))) {
                    i++;
                }
                int acceptNum = Integer.parseInt(postfix.substring(start, i + 1));
                acceptingPositions.add(positionCounter);
                acceptingTypes.put(positionCounter, "TYPE_" + acceptNum); // Almacena el tipo
                
                ASTNode leaf = new ASTNode("#" + acceptNum, positionCounter);
                symbolTable.put(positionCounter, "#" + acceptNum);
                followpos.put(positionCounter, new HashSet<>());
                stack.push(leaf);
                positionCounter++;
            } else {
                // Cualquier otro carácter normal
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

    public Map<Integer, String> getAcceptingTypes() {
        return acceptingTypes;
    }

    public Set<Integer> getAcceptingPositions() {
        return acceptingPositions;
    }
}