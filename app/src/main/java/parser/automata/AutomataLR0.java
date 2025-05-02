package parser.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AutomataLR0 {
    private final Map<String, List<List<String>>> gramatica;

    public AutomataLR0(Map<String, List<List<String>>> gramatica) {
        this.gramatica = gramatica;
    }

    public Set<Item> closure(Set<Item> conjunto) {
        Set<Item> cerrado = new HashSet<>(conjunto);
        boolean cambiado;

        do {
            cambiado = false;
            Set<Item> nuevos = new HashSet<>();

            for (Item item : cerrado) {
                String simbolo = item.simboloDespuesDelPunto();
                if (simbolo != null && gramatica.containsKey(simbolo)) {
                    for (List<String> produccion : gramatica.get(simbolo)) {
                        Item nuevo = new Item(simbolo, produccion, 0);
                        if (!cerrado.contains(nuevo)) {
                            nuevos.add(nuevo);
                            cambiado = true;
                        }
                    }
                }
            }

            cerrado.addAll(nuevos);
        } while (cambiado);

        return cerrado;
    }

    public Set<Item> gotoClosure(Set<Item> items, String simbolo) {
        Set<Item> avanzados = new HashSet<>();
        for (Item item : items) {
            if (simbolo.equals(item.simboloDespuesDelPunto())) {
                avanzados.add(new Item(item.izquierda, item.derecha, item.punto + 1));
            }
        }
        return closure(avanzados);
    }

    public List<Estado> construirAutomata() {
        List<Estado> estados = new ArrayList<>();
        Set<Item> inicio = new HashSet<>();
        String inicioProd = "S'";
        inicio.add(new Item(inicioProd, gramatica.get(inicioProd).get(0), 0));

        Estado estadoInicial = new Estado(closure(inicio));
        estados.add(estadoInicial);

        Queue<Estado> porProcesar = new LinkedList<>();
        porProcesar.add(estadoInicial);

        while (!porProcesar.isEmpty()) {
            Estado actual = porProcesar.poll();

            Set<String> simbolos = new HashSet<>();
            for (Item item : actual.items) {
                String simbolo = item.simboloDespuesDelPunto();
                if (simbolo != null) simbolos.add(simbolo);
            }

            for (String simbolo : simbolos) {
                Set<Item> cierre = gotoClosure(actual.items, simbolo);
                Estado nuevoEstado = new Estado(cierre);

                int idx = estados.indexOf(nuevoEstado);
                if (idx == -1) {
                    estados.add(nuevoEstado);
                    porProcesar.add(nuevoEstado);
                } else {
                    nuevoEstado = estados.get(idx);
                }

                actual.transiciones.put(simbolo, nuevoEstado);
            }
        }

        return estados;
    }

}
