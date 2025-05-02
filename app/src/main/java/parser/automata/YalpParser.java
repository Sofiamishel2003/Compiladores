package parser.automata;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class YalpParser {
    public static Map<String, List<List<String>>> parsearGramatica(String path) throws IOException {
        Map<String, List<List<String>>> gramatica = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String linea;
        boolean enProducciones = false;
        String produccionActual = null;
    
        while ((linea = reader.readLine()) != null) {
            linea = linea.trim();
    
            if (linea.equals("%%")) {
                enProducciones = true;
                continue;
            }
    
            if (!enProducciones || linea.isEmpty() || linea.startsWith("/*")) continue;
    
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
            
                List<List<String>> producciones = new ArrayList<>();
                StringBuilder rhsBuilder = new StringBuilder(partes[1].trim());
            
                // Seguir leyendo hasta encontrar el punto y coma
                while (!linea.endsWith(";")) {
                    linea = reader.readLine();
                    if (linea == null) break;
                    linea = linea.trim();
                    rhsBuilder.append(" ").append(linea);
                }
            
                String rhsCompleto = rhsBuilder.toString();
                rhsCompleto = rhsCompleto.replace(";", "").trim();
            
                for (String prod : rhsCompleto.split("\\|")) {
                    List<String> simbolos = List.of(prod.trim().split("\\s+"));
                    producciones.add(simbolos);
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
                }
            }
        }
    
        reader.close();
        return gramatica;
    }    
}

