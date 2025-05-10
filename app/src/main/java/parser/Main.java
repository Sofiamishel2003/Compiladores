package parser;

import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.automata.AutomataLALR;
import parser.automata.AutomataLR0;
import parser.automata.Estado;
import parser.automata.EstadoLALR;
import parser.automata.ItemLALR;
import parser.automata.LR0TableGenerator;
import parser.automata.YalpParser;
import parser.automata.YalpParser.ResultadoYalp;

public class Main {
    public static void main(String[] args) throws Exception {
        String rutaYalp = Paths.get("../../../", "parser.yalp").toAbsolutePath().normalize().toString();

        ResultadoYalp resultado = YalpParser.parsearArchivo(rutaYalp);
        Map<String, List<List<String>>> gramatica = resultado.gramatica;
        Set<String> terminales = resultado.terminales;

        // Agregar producción aumentada
        gramatica.put("S'", List.of(List.of("S")));

        // Mostrar la gramatica
        for (Map.Entry<String, List<List<String>>> entrada : gramatica.entrySet()) {
            System.out.println(entrada.getKey() + " ->");
            for (List<String> prod : entrada.getValue()) {
                System.out.println("    " + String.join(" ", prod));
            }
        }
        System.out.println("Terminales: " + terminales);       

        AutomataLR0 automata = new AutomataLR0(gramatica);
        List<Estado> estados = automata.construirAutomata();
        automata.exportarADot(estados, "parser/automata.dot");

        int i = 0;
        for (Estado estado : estados) {
            System.out.println("Estado " + i + ":");
            System.out.println(estado);
            for (Map.Entry<String, Estado> trans : estado.transiciones.entrySet()) {
                int idx = estados.indexOf(trans.getValue());
                System.out.println("  -- " + trans.getKey() + " --> Estado " + idx);
            }
            System.out.println("--------------------------------------------------");
            i++;
        }

        // Construcción de la tabla LR(0)
        System.out.println("\n=== TABLA LR(0) ===");
        Set<String> noTerminales = new HashSet<>(gramatica.keySet());
        noTerminales.removeAll(terminales);

        LR0TableGenerator generadorTabla = new LR0TableGenerator(gramatica);
        LR0TableGenerator.ParsingTable tabla = generadorTabla.generarTabla(estados, terminales, noTerminales);

        // Mostrar tabla ACTION
        System.out.println("\nTabla ACTION:");
        for (Map.Entry<Integer, Map<String, String>> fila : tabla.action.entrySet()) {
            System.out.println("Estado " + fila.getKey() + ": " + fila.getValue());
        }

        // Mostrar tabla GOTO
        System.out.println("\nTabla GOTO:");
        for (Map.Entry<Integer, Map<String, Integer>> fila : tabla.goTo.entrySet()) {
            System.out.println("Estado " + fila.getKey() + ": " + fila.getValue());
        }

        
        // Construcción del autómata LR(1)
        System.out.println("\n=== AUTÓMATA LR(1) ===");
        AutomataLALR automataLALR = new AutomataLALR(gramatica, terminales);
        List<EstadoLALR> estadosLR1 = automataLALR.construirAutomataLR1();
        automataLALR.exportarADotLALR(estadosLR1, "parser/automataLR1.dot");

        int j = 0;
        for (EstadoLALR estado : estadosLR1) {
            System.out.println("Estado LR(1) " + j + ":");
            for (ItemLALR item : estado.items) {
                System.out.println("  " + item);
            }
            for (Map.Entry<String, EstadoLALR> trans : estado.transiciones.entrySet()) {
                int destino = estadosLR1.indexOf(trans.getValue());
                System.out.println("  -- " + trans.getKey() + " --> Estado " + destino);
            }
            System.out.println("--------------------------------------------------");
            j++;
        }

        // Fusión LR(1) → LALR
        System.out.println("\n=== AUTÓMATA LALR (FUSIÓN) ===");
        List<EstadoLALR> estadosLALR = automataLALR.fusionarLR1paraLALR(estadosLR1);
        automataLALR.exportarADotLALR(estadosLALR, "parser/automataLALR.dot");

        int k = 0;
        for (EstadoLALR estado : estadosLALR) {
            System.out.println("Estado LALR " + k + ":");
            for (ItemLALR item : estado.items) {
                System.out.println("  " + item);
            }
            for (Map.Entry<String, EstadoLALR> trans : estado.transiciones.entrySet()) {
                int destino = estadosLALR.indexOf(trans.getValue());
                System.out.println("  -- " + trans.getKey() + " --> Estado " + destino);
            }
            System.out.println("--------------------------------------------------");
            k++;
        }
    }
}
