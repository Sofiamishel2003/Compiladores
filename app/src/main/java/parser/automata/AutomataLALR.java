package parser.automata;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import parser.utils.GramaticaUtils;

public class AutomataLALR {
    private final Map<String, List<List<String>>> gramatica;
    private final Set<String> terminales;

    public AutomataLALR(Map<String, List<List<String>>> gramatica, Set<String> terminales) {
        this.gramatica = gramatica;
        this.terminales = terminales;
    }

    public Set<ItemLALR> closure(Set<ItemLALR> conjunto, Map<String, Set<String>> first) {
        Set<ItemLALR> cerrado = new HashSet<>(conjunto);
        boolean cambiado;

        do {
            cambiado = false;
            Set<ItemLALR> nuevos = new HashSet<>();

            for (ItemLALR item : cerrado) {
                String simbolo = item.simboloDespuesDelPunto();
                if (simbolo != null && gramatica.containsKey(simbolo)) {
                    List<String> betaA = item.betaYSiguienteLookahead(null);
                    Set<String> lookaheads = GramaticaUtils.firstDeCadenas(betaA, first, terminales);
                    if (lookaheads.isEmpty()) lookaheads.add("ε");

                    for (List<String> produccion : gramatica.get(simbolo)) {
                        ItemLALR nuevoItem = new ItemLALR(simbolo, produccion, 0, lookaheads);

                        boolean existe = false;
                        for (ItemLALR ya : cerrado) {
                            if (ya.nucleo().equals(nuevoItem.nucleo())) {
                                if (ya.lookaheads.addAll(nuevoItem.lookaheads)) {
                                    cambiado = true;
                                }
                                existe = true;
                            }
                        }

                        if (!existe) {
                            nuevos.add(nuevoItem);
                            cambiado = true;
                        }
                    }
                }
            }

            cerrado.addAll(nuevos);
        } while (cambiado);

        return cerrado;
    }

    public Set<ItemLALR> gotoClosure(Set<ItemLALR> items, String simbolo, Map<String, Set<String>> first) {
        Set<ItemLALR> avanzados = new HashSet<>();
        for (ItemLALR item : items) {
            if (simbolo.equals(item.simboloDespuesDelPunto())) {
                avanzados.add(new ItemLALR(item.izquierda, item.derecha, item.punto + 1, item.lookaheads));
            }
        }
        return closure(avanzados, first);
    }

    public List<EstadoLALR> construirAutomata() {
        Map<String, Set<String>> first = GramaticaUtils.calcularFirst(gramatica, terminales);

        List<EstadoLALR> estados = new ArrayList<>();
        Set<ItemLALR> inicio = new HashSet<>();
        String inicioProd = "S'";
        inicio.add(new ItemLALR(inicioProd, gramatica.get(inicioProd).get(0), 0, Set.of("$")));

        EstadoLALR estadoInicial = new EstadoLALR(closure(inicio, first));
        estados.add(estadoInicial);

        Queue<EstadoLALR> porProcesar = new LinkedList<>();
        porProcesar.add(estadoInicial);

        while (!porProcesar.isEmpty()) {
            EstadoLALR actual = porProcesar.poll();

            Set<String> simbolos = new HashSet<>();
            for (ItemLALR item : actual.items) {
                String simbolo = item.simboloDespuesDelPunto();
                if (simbolo != null) simbolos.add(simbolo);
            }

            for (String simbolo : simbolos) {
                Set<ItemLALR> cierre = gotoClosure(actual.items, simbolo, first);
                EstadoLALR nuevoEstado = new EstadoLALR(cierre);

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

