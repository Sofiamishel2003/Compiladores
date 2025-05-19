package parser.automata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class YalpParser {

    public static class ResultadoYalp {
        public final Map<String, List<List<String>>> gramatica;
        public final Set<String> terminales;
        public final String simboloInicial;
        public final List<Map.Entry<String, List<String>>> produccionesOrdenadas;

        public ResultadoYalp(Map<String, List<List<String>>> gramatica, Set<String> terminales, String simboloInicial, List<Map.Entry<String, List<String>>> produccionesOrdenadas) {
            this.gramatica = gramatica;
            this.terminales = terminales;
            this.simboloInicial = simboloInicial;
            this.produccionesOrdenadas = produccionesOrdenadas;
        }
    }

    public static ResultadoYalp parsearArchivo(String path) throws IOException {
        Map<String, List<List<String>>> gramatica = new LinkedHashMap<>();
        Set<String> terminales = new HashSet<>();
        Set<String> ignorados = new HashSet<>();
        String simboloInicial = null;
        List<Map.Entry<String, List<String>>> produccionesOrdenadas = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String linea;
        boolean enProducciones = false;
        String produccionActual = null;

        while ((linea = reader.readLine()) != null) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("/*")) continue;

            if (linea.startsWith("%token")) {
                String[] partes = linea.substring(6).trim().split("\\s+");
                terminales.addAll(Arrays.asList(partes));
                continue;
            }

            if (linea.startsWith("IGNORE")) {
                String[] partes = linea.substring(6).trim().split("\\s+");
                ignorados.addAll(Arrays.asList(partes));
                continue;
            }

            if (linea.equals("%%")) {
                enProducciones = true;
                continue;
            }

            if (!enProducciones) continue;

            if (linea.endsWith(";")) {
                linea = linea.substring(0, linea.length() - 1).trim();
            }

            if (linea.contains(":")) {
                String[] partes = linea.split(":", 2);
                if (partes.length < 2) {
                    throw new IllegalArgumentException("Producción mal formada: " + linea);
                }
                String lhs = partes[0].trim();
                produccionActual = lhs;
                
                // Guardar el primer no terminal como símbolo inicial
                if (simboloInicial == null) {
                    simboloInicial = lhs;
                }

                List<List<String>> producciones = new ArrayList<>();
                StringBuilder rhsBuilder = new StringBuilder(partes[1].trim());

                while (!linea.endsWith(";")) {
                    linea = reader.readLine();
                    if (linea == null) break;
                    linea = linea.trim();
                    rhsBuilder.append(" ").append(linea);
                }

                String rhsCompleto = rhsBuilder.toString().replace(";", "").trim();

                for (String prod : rhsCompleto.split("\\|")) {
                    List<String> simbolos = List.of(prod.trim().split("\\s+"));
                    producciones.add(simbolos);
                    produccionesOrdenadas.add(Map.entry(lhs, simbolos));
                }

                gramatica.put(lhs, producciones);
            } else if (linea.contains("|")) {
                if (produccionActual == null) {
                    throw new IllegalStateException("Producción sin lado izquierdo antes de '|'");
                }

                List<List<String>> lista = gramatica.get(produccionActual);

                for (String prod : linea.split("\\|")) {
                    List<String> simbolos = List.of(prod.trim().split("\\s+"));
                    lista.add(simbolos);
                    produccionesOrdenadas.add(Map.entry(produccionActual, simbolos));
                }
            }
        }

        reader.close();

        // Eliminar los tokens ignorados de los terminales
        terminales.removeAll(ignorados);

        return new ResultadoYalp(gramatica, terminales, simboloInicial, produccionesOrdenadas);
    }
}



