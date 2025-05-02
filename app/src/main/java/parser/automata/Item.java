package parser.automata;

import java.util.Objects;

public class Item {
    public String izquierda;
    public java.util.List<String> derecha;
    public int punto;

    public Item(String izquierda, java.util.List<String> derecha, int punto) {
        this.izquierda = izquierda;
        this.derecha = derecha;
        this.punto = punto;
    }

    public boolean esReducido() {
        return punto >= derecha.size();
    }

    public String simboloDespuesDelPunto() {
        return esReducido() ? null : derecha.get(punto);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) return false;
        Item otro = (Item) obj;
        return izquierda.equals(otro.izquierda) &&
               derecha.equals(otro.derecha) &&
               punto == otro.punto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(izquierda, derecha, punto);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(izquierda + " -> ");
        for (int i = 0; i <= derecha.size(); i++) {
            if (i == punto) sb.append("• ");
            if (i < derecha.size()) sb.append(derecha.get(i)).append(" ");
        }
        return sb.toString().trim();
    }
}

