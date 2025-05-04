package parser.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GramaticaUtils {

    public static Map<String, Set<String>> calcularFirst(Map<String, List<List<String>>> gramatica, Set<String> terminales) {
        Map<String, Set<String>> first = new HashMap<>();

        for (String simbolo : gramatica.keySet()) {
            first.put(simbolo, new HashSet<>());
        }

        for (String terminal : terminales) {
            first.put(terminal, new HashSet<>(Collections.singleton(terminal)));
        }

        boolean cambiado;
        do {
            cambiado = false;
            for (String noTerminal : gramatica.keySet()) {
                for (List<String> produccion : gramatica.get(noTerminal)) {
                    for (String simbolo : produccion) {
                        Set<String> primeroSimbolo = first.get(simbolo);
                        int antes = first.get(noTerminal).size();
                        first.get(noTerminal).addAll(primeroSimbolo);
                        if (!first.get(noTerminal).contains("ε")) break;
                        if (first.get(noTerminal).size() > antes) cambiado = true;
                    }
                }
            }
        } while (cambiado);

        return first;
    }

    public static Set<String> firstDeCadenas(List<String> cadena, Map<String, Set<String>> first, Set<String> terminales) {
        Set<String> resultado = new HashSet<>();
        for (String simbolo : cadena) {
            resultado.addAll(first.getOrDefault(simbolo, Set.of()));
            if (!first.getOrDefault(simbolo, Set.of()).contains("ε")) break;
        }
        return resultado;
    }
}

