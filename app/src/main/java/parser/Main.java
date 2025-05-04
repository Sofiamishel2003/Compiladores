package parser;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;

import parser.automata.AutomataLALR;
import parser.automata.AutomataLR0;
import parser.automata.Estado;
import parser.automata.EstadoLALR;
import parser.automata.ItemLALR;
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

        AutomataLALR automataLALR = new AutomataLALR(gramatica, terminales);
        List<EstadoLALR> estadosLALR = automataLALR.construirAutomata();
        automataLALR.exportarADotLALR(estadosLALR, "parser/automataLALR.dot");

        int j = 0;
        for (EstadoLALR estado : estadosLALR) {
            System.out.println("Estado LALR " + j + ":");
            for (ItemLALR item : estado.items) {
                System.out.println("  " + item);
            }
            for (Map.Entry<String, EstadoLALR> trans : estado.transiciones.entrySet()) {
                int destino = estadosLALR.indexOf(trans.getValue());
                System.out.println("  -- " + trans.getKey() + " --> Estado " + destino);
            }
            System.out.println("--------------------------------------------------");
            j++;
        }
    }
}
