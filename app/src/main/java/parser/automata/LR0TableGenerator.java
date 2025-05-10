package parser.automata;

import java.util.*;

public class LR0TableGenerator {

    private final Map<String, List<List<String>>> gramatica;

    public LR0TableGenerator(Map<String, List<List<String>>> gramatica) {
        this.gramatica = gramatica;
    }

    public static class ParsingTable {
        public Map<Integer, Map<String, String>> action = new HashMap<>();
        public Map<Integer, Map<String, Integer>> goTo = new HashMap<>();
    }

    public ParsingTable generarTabla(List<Estado> estados, Set<String> terminales, Set<String> noTerminales) {
        ParsingTable tabla = new ParsingTable();

        for (int i = 0; i < estados.size(); i++) {
            Estado estado = estados.get(i);

            tabla.action.putIfAbsent(i, new HashMap<>());
            tabla.goTo.putIfAbsent(i, new HashMap<>());

            for (Item item : estado.items) {
                if (item.izquierda.equals("S'") && item.esReducido()) {
                    tabla.action.get(i).put("$", "accept");
                } else if (!item.esReducido()) {
                    String simbolo = item.simboloDespuesDelPunto();
                    Estado siguiente = estado.transiciones.get(simbolo);
                    if (siguiente == null) continue;
                    int j = estados.indexOf(siguiente);

                    if (terminales.contains(simbolo)) {
                        tabla.action.get(i).put(simbolo, "s" + j); // shift
                    } else if (noTerminales.contains(simbolo)) {
                        tabla.goTo.get(i).put(simbolo, j); // goto
                    }
                } else {
                    int prodIndex = buscarProduccionIndex(item.izquierda, item.derecha);
                    for (String terminal : terminales) {
                        tabla.action.get(i).putIfAbsent(terminal, "r" + prodIndex);
                    }
                    tabla.action.get(i).putIfAbsent("$", "r" + prodIndex);
                }
            }
        }

        return tabla;
    }

    private int buscarProduccionIndex(String izquierda, List<String> derecha) {
        int index = 0;
        for (Map.Entry<String, List<List<String>>> entry : gramatica.entrySet()) {
            String lhs = entry.getKey();
            for (List<String> rhs : entry.getValue()) {
                if (lhs.equals(izquierda) && rhs.equals(derecha)) {
                    return index;
                }
                index++;
            }
        }
        return -1; // no encontrado
    }
}