package parser.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ItemLALR {
    public final String izquierda;
    public final List<String> derecha;
    public final int punto;
    public final Set<String> lookaheads;

    public ItemLALR(String izquierda, List<String> derecha, int punto, Set<String> lookaheads) {
        this.izquierda = izquierda;
        this.derecha = new ArrayList<>(derecha);
        this.punto = punto;
        this.lookaheads = new HashSet<>(lookaheads);
    }

    public String simboloDespuesDelPunto() {
        return punto < derecha.size() ? derecha.get(punto) : null;
    }

    public boolean esReducido() {
        return punto >= derecha.size();
    }

    public List<String> betaYLookahead() {
        List<String> betaA = new ArrayList<>();
        if (punto + 1 < derecha.size()) {
            betaA.addAll(derecha.subList(punto + 1, derecha.size()));  // β
        }
        betaA.addAll(lookaheads); // a
        return betaA;
    }

    public ItemLALR nucleoSinLookahead() {
        return new ItemLALR(izquierda, derecha, punto, Set.of());
    }

    public boolean mismoNucleo(ItemLALR otro) {
        return this.izquierda.equals(otro.izquierda) &&
            this.derecha.equals(otro.derecha) &&
            this.punto == otro.punto;
    }

    public boolean mismoLookAhead(ItemLALR otro) {
        return this.lookaheads.equals(otro.lookaheads);
    }
    

    public List<String> betaYSiguienteLookahead(String siguiente) {
        List<String> beta = new ArrayList<>();
        for (int i = punto + 1; i < derecha.size(); i++) {
            beta.add(derecha.get(i));
        }
        if (siguiente != null) beta.add(siguiente);
        return beta;
    }

    // Para agrupar núcleos iguales
    public String nucleo() {
        return izquierda + " → " + derecha + " • " + punto;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ItemLALR)) return false;
        ItemLALR other = (ItemLALR) obj;
        return izquierda.equals(other.izquierda) &&
            derecha.equals(other.derecha) &&
            punto == other.punto; // ⚠️ No incluir lookaheads
    }

    @Override
    public int hashCode() {
        return Objects.hash(izquierda, derecha, punto); // ⚠️ No incluir lookaheads
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(izquierda + " -> ");
        for (int i = 0; i < derecha.size(); i++) {
            if (i == punto) sb.append("• ");
            sb.append(derecha.get(i)).append(" ");
        }
        if (punto == derecha.size()) sb.append("• ");
        sb.append(", ").append(lookaheads);
        return sb.toString();
    }

    public List<String> simbolosDespuesDelPuntoYLookahead() {
        List<String> resultado = new ArrayList<>();
        if (punto + 1 < derecha.size()) {
            resultado.addAll(derecha.subList(punto + 1, derecha.size()));
        }
        resultado.addAll(lookaheads); // aquí va el lookahead del item padre
        return resultado;
    }
    
}

