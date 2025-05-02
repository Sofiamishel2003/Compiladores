package parser.automata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
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

    public void exportarADot(List<Estado> estados, String rutaArchivo) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo));
        writer.write("digraph LR0Automaton {\n");
        writer.write("    rankdir=LR;\n");
        writer.write("    node [shape=circle];\n\n");
    
        // Escribir nodos
        for (int i = 0; i < estados.size(); i++) {
            Estado estado = estados.get(i);
            String label = "I" + i + "\\n";
            for (Item item : estado.items) {
                label += item.toString().replace("\"", "\\\"") + "\\l"; // \\l = left align
            }
            writer.write(String.format("    I%d [label=\"%s\"];\n", i, label));
        }
    
        // Escribir transiciones
        for (int i = 0; i < estados.size(); i++) {
            Estado estado = estados.get(i);
            for (Map.Entry<String, Estado> trans : estado.transiciones.entrySet()) {
                int j = estados.indexOf(trans.getValue());
                writer.write(String.format("    I%d -> I%d [label=\"%s\"];\n", i, j, trans.getKey()));
            }
        }
    
        writer.write("}\n");
        writer.close();
    }

}
