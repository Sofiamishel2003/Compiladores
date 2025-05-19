package parser.automata;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
        Set<ItemLALR> nuevos = new HashSet<>();
    
        for (ItemLALR item : items) {
            if (simbolo.equals(item.simboloDespuesDelPunto())) {
                // Avanza el punto
                List<String> beta = item.derecha.subList(item.punto + 1, item.derecha.size());
                List<String> betaA = new ArrayList<>(beta);
                betaA.addAll(item.lookaheads); // append lookahead symbols for FIRST
    
                Set<String> nuevosLookaheads = GramaticaUtils.firstDeCadenas(betaA, first, terminales);
                if (nuevosLookaheads.isEmpty()) {
                    nuevosLookaheads = new HashSet<>(item.lookaheads); // fallback si β ⇒ ε
                }
    
                nuevos.add(new ItemLALR(item.izquierda, item.derecha, item.punto + 1, item.lookaheads)); // mantenemos lookaheads aquí
            }
        }
    
        return closure(nuevos, first);
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
  
    private boolean sePuedeFusionar(Set<ItemLALR> items1, Set<ItemLALR> items2) {
        Map<String, Set<String>> mapa1 = mapearNucleoALookaheads(items1);
        Map<String, Set<String>> mapa2 = mapearNucleoALookaheads(items2);

        if (!mapa1.keySet().equals(mapa2.keySet())) {
            return false;
        }

        for (String nucleo : mapa1.keySet()) {
            Set<String> lookaheads1 = mapa1.get(nucleo);
            Set<String> lookaheads2 = mapa2.get(nucleo);

            // No deben solaparse con símbolos diferentes
            Set<String> union = new HashSet<>(lookaheads1);
            union.addAll(lookaheads2);
            if (union.size() != lookaheads1.size() + lookaheads2.size()) {
                return false; // hay solapamiento distinto
            }
        }

        return true;
    }

    private Map<String, Set<String>> mapearNucleoALookaheads(Set<ItemLALR> items) {
        Map<String, Set<String>> mapa = new HashMap<>();
        for (ItemLALR item : items) {
            String clave = item.izquierda + " -> " + String.join(" ", item.derecha) + " • " + item.punto;
            mapa.computeIfAbsent(clave, k -> new HashSet<>()).addAll(item.lookaheads);
        }
        return mapa;
    }


    private Map<String, Integer> contarItemsBase(Set<ItemLALR> items) {
        Map<String, Set<Set<String>>> grupos = new HashMap<>();
        for (ItemLALR item : items) {
            String clave = item.izquierda + " -> " + String.join(" ", item.derecha) + " • " + item.punto;
            grupos.computeIfAbsent(clave, k -> new HashSet<>()).add(item.lookaheads);
        }

        Map<String, Integer> conteo = new HashMap<>();
        for (Map.Entry<String, Set<Set<String>>> entry : grupos.entrySet()) {
            conteo.put(entry.getKey(), entry.getValue().size());
        }

        return conteo;
    }


    
    public List<EstadoLALR> fusionarLR1paraLALR(List<EstadoLALR> estadosLR1) {
        Map<Set<ItemLALR>, EstadoLALR> mapaNucleos = new HashMap<>();

        for (EstadoLALR estado : estadosLR1) {
            // Extraer núcleo (sin lookaheads)
            Set<ItemLALR> nucleo = estado.items.stream()
                .map(item -> new ItemLALR(item.izquierda, item.derecha, item.punto, Set.of()))
                .collect(Collectors.toSet());

            EstadoLALR existente = mapaNucleos.get(nucleo);
            if (existente == null) {
                // Clonar items con lookaheads
                Set<ItemLALR> nuevosItems = new HashSet<>();
                for (ItemLALR item : estado.items) {
                    nuevosItems.add(new ItemLALR(item.izquierda, item.derecha, item.punto, new HashSet<>(item.lookaheads)));
                }
                EstadoLALR nuevoEstado = new EstadoLALR(nuevosItems);
                mapaNucleos.put(nucleo, nuevoEstado);
            } else {
                
                // Eliminar lookaheads
                if (sePuedeFusionar(estado.items, existente.items)) {
                    Map<String, Integer> conteo1 = contarItemsBase(estado.items);
                    Map<String, Integer> conteo2 = contarItemsBase(existente.items);
                    if (conteo1.equals(conteo2)) {
                        for (ItemLALR item : estado.items) {
                            for (ItemLALR itExistente : existente.items) {
                                if (itExistente.mismoNucleo(item)) {
                                    itExistente.lookaheads.clear();
                                }
                            }
                        }
                    }
                }
                else {
                    Set<ItemLALR> nuevosItems = new HashSet<>();
                    for (ItemLALR item : estado.items) {
                        nuevosItems.add(new ItemLALR(item.izquierda, item.derecha, item.punto, new HashSet<>(item.lookaheads)));
                    }
                    EstadoLALR nuevoEstado = new EstadoLALR(nuevosItems);
                    mapaNucleos.put(nucleo, nuevoEstado);
                }
            }
        }

        // Reconstruir transiciones
        List<EstadoLALR> nuevosEstados = new ArrayList<>(mapaNucleos.values());
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

}


