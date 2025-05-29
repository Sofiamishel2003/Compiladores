package parser.automata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import parser.utils.GramaticaUtils;

public class AutomataLALR {
    private final Map<String, List<List<String>>> gramatica;
    private final Set<String> terminales;

    public AutomataLALR(Map<String, List<List<String>>> gramatica, Set<String> terminales) {
        this.gramatica = gramatica;
        this.terminales = terminales;
    }

    public Set<ItemLALR> closure(Set<ItemLALR> conjunto, Map<String, Set<String>> first) {
    Map<String, ItemLALR> mapa = new HashMap<>();

    Queue<ItemLALR> pendientes = new LinkedList<>(conjunto);
    while (!pendientes.isEmpty()) {
        ItemLALR actual = pendientes.poll();

        String clave = actual.nucleo(); // Izquierda + derecha + punto
        ItemLALR existente = mapa.get(clave);

        if (existente == null) {
            // Nuevo ítem
            mapa.put(clave, actual);
        } else {
            // Fusionar lookaheads
            Set<String> fusionados = new HashSet<>(existente.lookaheads);
            if (fusionados.addAll(actual.lookaheads)) {
                mapa.put(clave, new ItemLALR(existente.izquierda, existente.derecha, existente.punto, fusionados));
            }
            continue; // No procesamos expansiones duplicadas
        }

        String simbolo = actual.simboloDespuesDelPunto();
        if (simbolo != null && gramatica.containsKey(simbolo)) {
            List<String> beta = actual.derecha.subList(actual.punto + 1, actual.derecha.size());
            Set<String> lookaheads = new HashSet<>();
            for (String a : actual.lookaheads) {
                List<String> betaA = new ArrayList<>(beta);
                betaA.add(a);
                lookaheads.addAll(GramaticaUtils.firstDeCadenas(betaA, first, terminales));
            }

            if (lookaheads.isEmpty()) {
                lookaheads.addAll(actual.lookaheads); // fallback
            }

            for (List<String> produccion : gramatica.get(simbolo)) {
                ItemLALR nuevo = new ItemLALR(simbolo, produccion, 0, lookaheads);
                pendientes.add(nuevo);
            }
        }
    }

    return new HashSet<>(mapa.values());
}
   

     public Set<ItemLALR> gotoClosure(Set<ItemLALR> items, String simbolo, Map<String, Set<String>> first) {
        Set<ItemLALR> avanzados = new HashSet<>();

        for (ItemLALR item : items) {
            if (simbolo.equals(item.simboloDespuesDelPunto())) {
                ItemLALR nuevo = new ItemLALR(
                    item.izquierda,
                    item.derecha,
                    item.punto + 1,
                    new HashSet<>(item.lookaheads) // conservar los lookaheads
                );
                avanzados.add(nuevo);
            }
        }

        return closure(avanzados, first);
    }


    public List<EstadoLALR> construirAutomataLR1() {
        Map<String, Set<String>> firsts = GramaticaUtils.calcularFirst(gramatica, terminales);
    
        List<EstadoLALR> estados = new ArrayList<>();
    
        // Asumimos que el símbolo inicial está en la gramática como "S'"
        String simboloInicial = "S'";
        List<String> produccionInicial = gramatica.get(simboloInicial).get(0);
    
        // Crear estado inicial con lookahead EOF
        ItemLALR itemInicial = new ItemLALR(simboloInicial, produccionInicial, 0, Set.of("EOF"));
        Set<ItemLALR> conjuntoInicial = closure(Set.of(itemInicial), firsts);
        EstadoLALR estadoInicial = new EstadoLALR(conjuntoInicial);
        estados.add(estadoInicial);
    
        Queue<EstadoLALR> pendientes = new LinkedList<>();
        pendientes.add(estadoInicial);
    
        while (!pendientes.isEmpty()) {
            EstadoLALR actual = pendientes.poll();

            // Obtener todos los símbolos posibles después del punto en los ítems del estado
            Set<String> simbolos = new HashSet<>();
            for (ItemLALR item : actual.items) {
                String simbolo = item.simboloDespuesDelPunto();
                if (simbolo != null) {
                    simbolos.add(simbolo);
                }
            }
    
            for (String simbolo : simbolos) {
                Set<ItemLALR> irA = gotoClosure(actual.items, simbolo, firsts);
                if (irA.isEmpty()) continue;
    
                // Comparar ítems con lookaheads para detectar estados duplicados
                EstadoLALR destino = null;
                for (EstadoLALR existente : estados) {
                    if (existente.items.equals(irA)) {
                        // Verificar si también coinciden los lookaheads
                        boolean lookaheadsCoinciden = true;
                        for (ItemLALR item : irA) {
                            boolean encontrado = false;
                            for (ItemLALR itemExistente : existente.items) {
                                if (item.mismoNucleo(itemExistente)) {
                                    // Si el núcleo coincide, verificamos los lookaheads
                                    if (item.lookaheads.equals(itemExistente.lookaheads)) {
                                        encontrado = true;
                                        break;
                                    }
                                }
                            }
                            if (!encontrado) {
                                lookaheadsCoinciden = false;
                                break;
                            }
                        }
    
                        if (lookaheadsCoinciden) {
                            destino = existente;
                            break;
                        }
                    }
                }
    
                if (destino == null) {
                    destino = new EstadoLALR(irA);
                   // System.out.println("Transición por símbolo: " + simbolo + " genera nuevo estado con " + irA.size() + " items");
                    estados.add(destino);
                    pendientes.add(destino);
                }
    
                actual.transiciones.put(simbolo, destino);
            }
        }
    
        return estados;
    }    
    
    public List<EstadoLALR> fusionarLR1paraLALR(List<EstadoLALR> estadosLR1) {
        // Usamos LinkedHashMap para preservar el orden de inserción
        Map<Set<ItemLALR>, EstadoLALR> mapaNucleos = new LinkedHashMap<>();

        for (EstadoLALR estado : estadosLR1) {
            // Extraer núcleo (sin lookaheads)
            Set<ItemLALR> nucleo = estado.items.stream()
                .map(item -> new ItemLALR(item.izquierda, item.derecha, item.punto, Set.of()))
                .collect(Collectors.toCollection(LinkedHashSet::new)); // Orden estable

            EstadoLALR existente = mapaNucleos.get(nucleo);
            if (existente == null) {
                // Crear nuevo estado con ítems y lookaheads
                Set<ItemLALR> nuevosItems = new HashSet<>();
                for (ItemLALR item : estado.items) {
                    nuevosItems.add(new ItemLALR(item.izquierda, item.derecha, item.punto, new HashSet<>(item.lookaheads)));
                }
                EstadoLALR nuevoEstado = new EstadoLALR(nuevosItems);
                mapaNucleos.put(nucleo, nuevoEstado);
            } else {
                // Fusionar lookaheads
                for (ItemLALR item : estado.items) {
                    boolean encontrado = false;
                    for (ItemLALR itExistente : existente.items) {
                        if (itExistente.mismoNucleo(item)) {
                            itExistente.lookaheads.addAll(item.lookaheads);
                            encontrado = true;
                            break;
                        }
                    }
                    if (!encontrado) {
                        existente.items.add(new ItemLALR(item.izquierda, item.derecha, item.punto, new HashSet<>(item.lookaheads)));
                    }
                }
            }
        }

        // Construimos la lista de estados fusionados
        List<EstadoLALR> nuevosEstados = new ArrayList<>(mapaNucleos.values());

        // Reconstruir transiciones entre los nuevos estados
        for (EstadoLALR original : estadosLR1) {
            EstadoLALR fusionadoOrigen = encontrarEstadoFusionado(original, nuevosEstados);
            for (Map.Entry<String, EstadoLALR> trans : original.transiciones.entrySet()) {
                EstadoLALR fusionadoDestino = encontrarEstadoFusionado(trans.getValue(), nuevosEstados);
                if (fusionadoOrigen != null && fusionadoDestino != null) {
                    fusionadoOrigen.transiciones.put(trans.getKey(), fusionadoDestino);
                }
            }
        }

        return nuevosEstados;
    }

    private EstadoLALR encontrarEstadoFusionado(EstadoLALR original, List<EstadoLALR> fusionados) {
        Set<ItemLALR> nucleo = original.items.stream()
            .map(item -> new ItemLALR(item.izquierda, item.derecha, item.punto, Set.of()))
            .collect(Collectors.toSet());

        for (EstadoLALR est : fusionados) {
            Set<ItemLALR> nucleoEst = est.items.stream()
                .map(item -> new ItemLALR(item.izquierda, item.derecha, item.punto, Set.of()))
                .collect(Collectors.toSet());

            if (nucleoEst.equals(nucleo)) {
                return est;
            }
        }
        return null;
    }

    public static void exportarADotLALR(List<EstadoLALR> estados, String rutaArchivo) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(rutaArchivo));
        writer.write("digraph LALRAutomaton {\n");
        writer.write("    rankdir=LR;\n");
        writer.write("    node [shape=circle];\n\n");

        // Escribir nodos
        for (int i = 0; i < estados.size(); i++) {
            EstadoLALR estado = estados.get(i);
            StringBuilder label = new StringBuilder("I" + i + "\\n");
            for (ItemLALR item : estado.items) {
                label.append(item.toString().replace("\"", "\\\"")).append("\\l");
            }
            writer.write(String.format("    I%d [label=\"%s\"];\n", i, label));
        }

        // Escribir transiciones
        for (int i = 0; i < estados.size(); i++) {
            EstadoLALR estado = estados.get(i);
            for (Map.Entry<String, EstadoLALR> trans : estado.transiciones.entrySet()) {
                int j = estados.indexOf(trans.getValue());
                writer.write(String.format("    I%d -> I%d [label=\"%s\"];\n", i, j, trans.getKey()));
            }
        }

        writer.write("}\n");
        writer.close();
    }

    public static class EntradaTabla {
        public enum Tipo { SHIFT, REDUCE, ACCEPT }

        private Tipo tipo;
        private int estadoDestino; // para SHIFT
        private String produccionIzq; // para REDUCE
        private List<String> produccionDer; // para REDUCE


        // Constructor para SHIFT
        public EntradaTabla(Tipo tipo, int estadoDestino) {
            this.tipo = tipo;
            this.estadoDestino = estadoDestino;
        }

        // Constructor para REDUCE
        public EntradaTabla(Tipo tipo, String produccionIzq, List<String> produccionDer) {
            this.tipo = tipo;
            this.produccionIzq = produccionIzq;
            this.produccionDer = produccionDer;
        }

        // Constructor para ACCEPT
        public EntradaTabla(Tipo tipo) {
            this.tipo = tipo;
        }

        public Tipo getTipo() {
            return tipo;
        }

        public int getEstadoDestino() {
            return estadoDestino;
        }

        public String getProduccionIzq() {
            return produccionIzq;
        }

        public List<String> getProduccionDer() {
            return produccionDer;
        }

        @Override
        public String toString() {
            if (tipo == Tipo.SHIFT) {
                return "s" + estadoDestino;
            } else if (tipo == Tipo.REDUCE) {
                return "r(" + produccionIzq + " → " + String.join(" ", produccionDer) + ")";
            } else if (tipo == Tipo.ACCEPT) {
                return "acc";
            } else {
                return "";
            }
        }

    }

    public static class TablaAnalisis {
        public final Map<Integer, Map<String, EntradaTabla>> action = new HashMap<>();
        public final Map<Integer, Map<String, Integer>> goTo = new HashMap<>();

        public void imprimir() {
            System.out.println("ACTION:");
            for (var fila : action.entrySet()) {
                System.out.println("Estado " + fila.getKey() + ": " + fila.getValue());
            }
            System.out.println("GOTO:");
            for (var fila : goTo.entrySet()) {
                System.out.println("Estado " + fila.getKey() + ": " + fila.getValue());
            }
        }
    }

    public TablaAnalisis generarTablaAnalisis(List<EstadoLALR> estados) {
        TablaAnalisis tabla = new TablaAnalisis();

        for (int i = 0; i < estados.size(); i++) {
            EstadoLALR estado = estados.get(i);
            tabla.action.put(i, new HashMap<>());
            tabla.goTo.put(i, new HashMap<>());

            for (ItemLALR item : estado.items) {
                String simbolo = item.simboloDespuesDelPunto();

                if (simbolo != null) {
                    // SHIFT o GOTO
                    EstadoLALR destino = estado.transiciones.get(simbolo);
                    if (destino == null) continue;

                    int j = estados.indexOf(destino);

                    if (terminales.contains(simbolo)) {
                        EntradaTabla nuevaEntrada = new EntradaTabla(EntradaTabla.Tipo.SHIFT, j);
                        EntradaTabla anterior = tabla.action.get(i).putIfAbsent(simbolo, nuevaEntrada);

                        if (anterior != null && !anterior.equals(nuevaEntrada)) {
                            System.err.printf("Conflicto SHIFT/REDUCE en estado %d con símbolo '%s': %s vs %s%n", i, simbolo, anterior, nuevaEntrada);
                        }

                    } else {
                        // GOTO
                        tabla.goTo.get(i).put(simbolo, j);
                    }

                } else if (!item.izquierda.equals("S'")) {
                    // REDUCE
                    for (String lookahead : item.lookaheads) {
                        EntradaTabla nuevaEntrada = new EntradaTabla(EntradaTabla.Tipo.REDUCE, item.izquierda, item.derecha);
                        EntradaTabla anterior = tabla.action.get(i).putIfAbsent(lookahead, nuevaEntrada);

                        if (anterior != null && !anterior.equals(nuevaEntrada)) {
                            System.err.printf("Conflicto REDUCE/REDUCE o SHIFT/REDUCE en estado %d con lookahead '%s': %s vs %s%n", i, lookahead, anterior, nuevaEntrada);
                        }
                    }

                } else {
                    // ACCEPT
                    if (item.lookaheads.contains("EOF")) {
                        EntradaTabla nuevaEntrada = new EntradaTabla(EntradaTabla.Tipo.ACCEPT);
                        EntradaTabla anterior = tabla.action.get(i).putIfAbsent("EOF", nuevaEntrada);

                        if (anterior != null && !anterior.equals(nuevaEntrada)) {
                            System.err.printf("Conflicto con ACCEPT en estado %d: %s vs %s%n", i, anterior, nuevaEntrada);
                        }
                    }
                }
            }
        }

        return tabla;
    }


}


