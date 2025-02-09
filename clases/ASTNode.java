package clases;

import java.util.*;

public class ASTNode {
    String value;
    ASTNode left, right;
    int position;
    boolean nullable;
    Set<Integer> firstpos, lastpos;

    public ASTNode(String value, int position) {
        this.value = value;
        this.position = position;
        this.firstpos = new HashSet<>();
        this.lastpos = new HashSet<>();
        if (position != -1) {
            this.firstpos.add(position);
            this.lastpos.add(position);
        }
    }

    public ASTNode(String value, ASTNode left, ASTNode right) {
        this.value = value;
        this.left = left;
        this.right = right;
        this.position = -1;
        this.firstpos = new HashSet<>();
        this.lastpos = new HashSet<>();
    }

    public void print(String prefix, boolean isLeft) {
        System.out.println(prefix + (isLeft ? "├── " : "└── ") + value);
        if (left != null) left.print(prefix + (isLeft ? "│   " : "    "), true);
        if (right != null) right.print(prefix + (isLeft ? "│   " : "    "), false);
    }
    
}
