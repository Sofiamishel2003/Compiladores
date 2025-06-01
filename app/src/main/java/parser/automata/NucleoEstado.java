package parser.automata;

import java.util.Objects;
import java.util.Set;

public class NucleoEstado {
    private final Set<ItemLALR> itemsSinLookaheads;

    public NucleoEstado(Set<ItemLALR> itemsSinLookaheads) {
        this.itemsSinLookaheads = itemsSinLookaheads;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NucleoEstado)) return false;
        NucleoEstado that = (NucleoEstado) o;
        return Objects.equals(itemsSinLookaheads, that.itemsSinLookaheads);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemsSinLookaheads);
    }
}

