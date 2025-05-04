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

    public Set<String> nucleo() {
        Set<String> claves = new HashSet<>();
        for (ItemLALR item : items) {
            claves.add(item.nucleo());
        }
        return claves;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EstadoLALR)) return false;
        EstadoLALR otro = (EstadoLALR) o;
        return nucleo().equals(otro.nucleo());
    }

    @Override
    public int hashCode() {
        return nucleo().hashCode();
    }

    @Override
    public String toString() {
        return items.toString();
    }
}

