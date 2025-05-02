package parser;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import parser.automata.AutomataLR0;
import parser.automata.Estado;
import parser.automata.YalpParser;

public class Main {
    public static void main(String[] args) throws Exception {
        String rutaYalp = Paths.get("../../../", "parser.yalp").toAbsolutePath().normalize().toString();

        Map<String, List<List<String>>> gramatica = YalpParser.parsearGramatica(rutaYalp);
        // Agregar producción aumentada S' → S
        String simboloInicial = "S";
        gramatica.put("S'", List.of(List.of(simboloInicial)));

        for (Map.Entry<String, List<List<String>>> entrada : gramatica.entrySet()) {
            String noTerminal = entrada.getKey();
            List<List<String>> producciones = entrada.getValue();
            System.out.println(noTerminal + " ->");
            for (List<String> produccion : producciones) {
                System.out.println("    " + String.join(" ", produccion));
            }
        }        

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
    }
}
