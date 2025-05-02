package parser.automata;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class Estado {
    public Set<Item> items;
    public Map<String, Estado> transiciones;

    public Estado(Set<Item> items) {
        this.items = new LinkedHashSet<>(items);
        this.transiciones = new HashMap<>();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Estado)) return false;
        Estado otro = (Estado) obj;
        return this.items.equals(otro.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append("  ").append(item).append("\n");
        }
        return sb.toString();
    }

    public String itemsComoTextoEscapado() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.toString().replace("\"", "\\\"").replace("\n", "\\n")).append("\\n");
        }
        return sb.toString();
    }
    
}

