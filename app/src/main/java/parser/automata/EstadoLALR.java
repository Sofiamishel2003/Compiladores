package parser.automata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class EstadoLALR {
    public Set<ItemLALR> items;
    public Map<String, EstadoLALR> transiciones;

    public EstadoLALR(Set<ItemLALR> items) {
        this.items = items;
        this.transiciones = new HashMap<>();
    }

    public Set<ItemLALR> nucleo() {
        Set<ItemLALR> claves = new HashSet<>();
        for (ItemLALR item : items) {
            claves.add(item.nucleoSinLookahead());
        }
        return claves;
    }


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EstadoLALR)) return false;
        EstadoLALR otro = (EstadoLALR) o;
        return this.items.equals(otro.items);
    }

    @Override
    public int hashCode() {
        return items.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (ItemLALR item : items) {
            sb.append(item).append("\n");
        }
        return sb.toString();
    }

}

