package parser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import clases.AFDGenerator;
import clases.ASTBuilder;
import clases.ASTNode;
import clases.Lexer;
import clases.RegexConverter;
import clases.Stack;
import clases.YalParser;
import parser.automata.AutomataLALR;
import parser.automata.AutomataLALR.EntradaTabla;
import parser.automata.AutomataLR0;
import parser.automata.Estado;
import parser.automata.EstadoLALR;
import parser.automata.LR0TableGenerator;
import parser.automata.YalpParser;

public class Yapar {
    public static void main(String[] args) throws Exception {
        // ────────────────────────────────
        // FASE 1: Validar argumentos de entrada
        // Espera: yapar parser.yalp -l lexer.yal -o theparser
        // ────────────────────────────────
        if (args.length != 5 || !args[1].equals("-l") || !args[3].equals("-o")) {
            System.err.println("Uso: yapar parser.yalp -l lexer.yal -o theparser");
            System.exit(1);
        }
        // ────────────────────────────────
        //  FASE 2: Preparar rutas absolutas
        // ────────────────────────────────
        String rutaYalp = Paths.get(args[0]).toAbsolutePath().normalize().toString();
        String rutaYal = Paths.get(args[2]).toAbsolutePath().normalize().toString();
        String nombreParser = args[4];

        // ────────────────────────────────
        //  FASE 3: Procesar archivo .yal (Lexer)
        // ────────────────────────────────
        YalParser yalParser = new YalParser();
        // - Extraer las reglas
        List<YalParser.Rule> reglas = yalParser.parseYAL(rutaYal);
        // - Combinar una expresión regular única
        String combinada = yalParser.combineRegex();
        // - Convertir a postfix
        String postfix = RegexConverter.toPostfix(combinada);
        System.out.println("POSTFIX" + postfix);
        // - Construir el AST y calcular followpos
        ASTBuilder astBuilder = new ASTBuilder(postfix);
        ASTNode astRoot = astBuilder.buildAST();
        astBuilder.computeNullableFirstLast(astRoot);
        astBuilder.computeFollowpos(astRoot);

        // ────────────────────────────────
        // FASE 4: Generar AFD del lexer
        // ────────────────────────────────
        // - Obtener followpos, tabla de símbolos, estado inicial
        var followpos = astBuilder.getFollowpos();
        var symbolTable = astBuilder.getSymbolTable();
        var startState = astBuilder.getStartState(astRoot);
        var acceptingPosition = astBuilder.getAcceptingPositions();
        // - Crear el AFD
        AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);
        // - Mapear tipos aceptados a nombres reales
        Map<Integer, String> typeToName = new HashMap<>();
        for (int i = 0; i < reglas.size(); i++) {
            typeToName.put(i + 1, reglas.get(i).action);
        }

        Map<Integer, String> acceptingTypes = astBuilder.getAcceptingTypes();
        Map<Integer, String> acceptingTypesResolved = new HashMap<>();
        for (Map.Entry<Integer, String> entry : acceptingTypes.entrySet()) {
            String type = entry.getValue();
            if (type.startsWith("TYPE_")) {
                int typeNum = Integer.parseInt(type.substring(5));
                String realName = typeToName.getOrDefault(typeNum, type);
                acceptingTypesResolved.put(entry.getKey(), realName);
            }
        }
        // - Se genera el código del Lexer.java
        afd.generarCodigoLexer("src/main/java/clases/Lexer.java", acceptingTypesResolved);

        // ────────────────────────────────
        // FASE 5: Procesar archivo .yalp (Parser)
        // ────────────────────────────────
        YalpParser.ResultadoYalp resultado = YalpParser.parsearArchivo(rutaYalp);
        // - Extraer la gramática y producciones
        Map<String, List<List<String>>> gramatica = resultado.gramatica;
        List<Map.Entry<String, List<String>>> listaProducciones = new ArrayList<>();
        for (Map.Entry<String, List<List<String>>> entry : gramatica.entrySet()) {
            for (List<String> rhs : entry.getValue()) {
                listaProducciones.add(Map.entry(entry.getKey(), rhs));
            }
        }
        // - Añadir EOF -> Final de terminales
        Set<String> terminales = resultado.terminales;
        terminales.add("EOF");
        // - Añadir S' -> inicial
        String simboloInicial = resultado.simboloInicial;
        gramatica.put("S'", List.of(List.of(simboloInicial)));
        listaProducciones.add(0, Map.entry("S'", List.of(simboloInicial)));

        Set<String> noTerminales = new HashSet<>(gramatica.keySet());
        noTerminales.removeAll(terminales);

        // ────────────────────────────────
        //  FASE 6: Construcción del autómata LR(0)
        // ────────────────────────────────
        // - Construcción del autómata
        AutomataLR0 automataLR0 = new AutomataLR0(gramatica);
        List<Estado> estadosLR0 = automataLR0.construirAutomata();
        // - Exportar a .dot el automata LR(0)
        automataLR0.exportarADot(estadosLR0, "src/main/java/parser/automata.dot");
        // - Generar tabla LR(0)
        LR0TableGenerator tableGen = new LR0TableGenerator(gramatica, listaProducciones);
        LR0TableGenerator.ParsingTable tablaLR0 = tableGen.generarTabla(estadosLR0, terminales, noTerminales);

        // ────────────────────────────────
        //  FASE 7: Construcción del autómata LALR
        // ────────────────────────────────
        // - Construcción LR(1) completo
        AutomataLALR lalr = new AutomataLALR(gramatica, terminales);
        List<EstadoLALR> estadosLR1 = lalr.construirAutomataLR1();
        // - Exportar a .dot el automata LR(1)
        AutomataLALR.exportarADotLALR(estadosLR1, "src/main/java/parser/automataLR1.dot");
        // - Fusión LR(1) → LALR 
        List<EstadoLALR> estadosLALR = lalr.fusionarLR1paraLALR(estadosLR1);
        AutomataLALR.exportarADotLALR(estadosLALR, "src/main/java/parser/automataLALR.dot");
        // - Generación de tabla de análisis
        AutomataLALR.TablaAnalisis tablaLALR = lalr.generarTablaAnalisis(estadosLALR);
        tablaLALR.imprimir();
        
        // ────────────────────────────────
        //  FASE 8: Simulación con cadenas de prueba
        // ────────────────────────────────
        // - Lee cadenas.txt
        List<String> cadenas = Files.readAllLines(Paths.get("cadenas.txt"));
        // - Ejecuta análisis con LR(0) y LALR
        try (BufferedWriter out = new BufferedWriter(new FileWriter("resultado.txt"))) {
            for (String entrada : cadenas) {
                out.write("Cadena: " + entrada + "\n");
                errores = new ArrayList<>(); // Reiniciar errores por cadena
                erroresLr0 = new ArrayList<>(); // Reiniciar errores por cadena
                Lexer lexerTemporal = new Lexer(entrada + "\u0000");
                lexerTemporal.tokenize(); // Solo para capturar errores léxicos

                String resultadoLR0 = analizarCadenaLR0(entrada, tablaLR0.action, tablaLR0.goTo, gramatica, listaProducciones);
                String resultadoLALR = analizarCadenaLALR(entrada, tablaLALR.action, tablaLALR.goTo, gramatica, listaProducciones);
                // - Imprime resultados y errores
                out.write("--Resultado LR(0): " + resultadoLR0 + "--------\n");
                for (ErrorDetalle err : erroresLr0) {
                    out.write(err.formatoLinea(entrada) + "\n");
                }
                out.write("--Resultado LALR : " + resultadoLALR + "--------\n");
                for (ErrorDetalle err : errores) {
                    out.write(err.formatoLinea(entrada) + "\n");
                }
                if (!lexerTemporal.erroresLexicos.isEmpty()) {
                    out.write("--Errores léxicos-------------------\n");
                    for (ErrorDetalle err : lexerTemporal.erroresLexicos) {
                        out.write(err.formatoLinea(entrada) + "\n");
                    }
                }
                out.write("------------------------------------------------------\n");
            }
        }

        System.out.println("Resultados escritos en resultado.txt");
    }
    // ────────────────────────────────
    //  Manejo de errores
    // ────────────────────────────────
    public static class ErrorDetalle {
        String tipo; // "léxico", "sintáctico", "gramatical"
        int posicion;
        String token;
        String descripcion;

        public ErrorDetalle(String tipo, int posicion, String token, String descripcion) {
            this.tipo = tipo;
            this.posicion = posicion;
            this.token = token;
            this.descripcion = descripcion;
        }

        public String formatoLinea(String cadena) {
            int pos = Math.min(posicion, cadena.length());
            int offset = "Línea: ".length();
            StringBuilder caret = new StringBuilder();

            for (int i = 0; i < pos + offset; i++) {
                if (i < offset) {
                    caret.append(" "); // espacio por los caracteres de "Línea: "
                } else {
                    int charIndex = i - offset;
                    if (charIndex < cadena.length() && cadena.charAt(charIndex) == '\t') {
                        caret.append('\t');
                    } else {
                        caret.append(' ');
                    }
                }
            }
            caret.append("^");
            return "   Error " + tipo + ": " + descripcion +
                "\n  Posición: índice " + pos +
                "\n  Línea: " + cadena +
                "\n  " + caret.toString();
        }

    }
    
    private static List<ErrorDetalle> erroresLr0;
    private static List<ErrorDetalle> errores;
    // ────────────────────────────────
    //  Analiza Cadenas con LR(0)
    // ────────────────────────────────
    private static String analizarCadenaLR0(
        String entrada,
        Map<Integer, Map<String, String>> actionTable,
        Map<Integer, Map<String, Integer>> gotoTable,
        Map<String, List<List<String>>> gramatica,
        List<Map.Entry<String, List<String>>> listaProducciones) {

        erroresLr0 = new ArrayList<>();
        Lexer lexer = new Lexer(entrada + "\u0000");
        List<Lexer.Token> rawTokens = lexer.tokenize();
        List<String> tokens = new ArrayList<>();
        List<Integer> posiciones = new ArrayList<>();

        for (Lexer.Token token : rawTokens) {
            if (!token.type.equals("WHITESPACE") && !token.type.equals("EOL")) {
                tokens.add(token.type);
                posiciones.add(token.start);
            }
        }
        tokens.add("EOF");
        posiciones.add(entrada.length());

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        int i = 0;
        boolean huboError = false;

        while (i < tokens.size()) {
            int state = stack.peek();
            String token = tokens.get(i);
            String action = actionTable.getOrDefault(state, new HashMap<>()).get(token);

            if (action == null) {
                String tipo = token.equals("EOF") ? "sintáctico" : "gramatical";
                String desc;
                if (token.equals("EOF")) {
                    if (tokens.size() == 1) {
                        desc = "Cadena vacía no es aceptada por la gramática.";
                    } else {
                        desc = "Faltan elementos para completar la producción.";
                    }
                } else {
                    desc = "Token inesperado: '" + token + "'.";
                }

                int errorPos;
                if (token.equals("EOF")) {
                    errorPos = posiciones.isEmpty() ? 0 : posiciones.get(posiciones.size() - 2); // posición del último token antes de EOF
                } else {
                    errorPos = i < posiciones.size() ? posiciones.get(i) : entrada.length();
                }
                erroresLr0.add(new ErrorDetalle(tipo, errorPos, token, desc));
                huboError = true;
                i++;
                continue;
            }

            if (action.equals("accept")) return "ACEPTADA";

            if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(nextState);
                i++;
            } else if (action.startsWith("r")) {
                int prodNum = Integer.parseInt(action.substring(1));
                if (prodNum >= listaProducciones.size()) {
                    erroresLr0.add(new ErrorDetalle("sintáctico", posiciones.get(i), token, "Reducción inválida: producción fuera de rango."));
                    return "RECHAZADA (producción inválida)";
                }

                Map.Entry<String, List<String>> prod = listaProducciones.get(prodNum);
                String lhs = prod.getKey();
                List<String> rhs = prod.getValue();

                if (rhs.size() > 0 && stack.size() < rhs.size()) {
                    erroresLr0.add(new ErrorDetalle("sintáctico", posiciones.get(i), token,
                        "Reducción imposible: pila insuficiente para '" + lhs + "'."));
                    return "RECHAZADA (pila insuficiente para reducción: " + lhs + ")";
                }

                for (int j = 0; j < rhs.size(); j++) stack.pop();

                int topState = stack.peek();
                Integer next = gotoTable.getOrDefault(topState, new HashMap<>()).get(lhs);
                if (next == null) {
                    erroresLr0.add(new ErrorDetalle("gramatical", posiciones.get(i), token,
                        "Goto inválido tras reducción de '" + lhs + "'."));
                    return "RECHAZADA (goto inválido desde estado " + topState + " con símbolo '" + lhs + "')";
                }

                stack.push(next);
            } else {
                erroresLr0.add(new ErrorDetalle("gramatical", posiciones.get(i), token, "Acción inválida: " + action));
                return "RECHAZADA (acción inválida: " + action + ")";
            }
        }

        return huboError ? "RECHAZADA" : "ACEPTADA";
    }

    // ────────────────────────────────
    //  Analiza Cadenas con LALR
    // ────────────────────────────────

    private static String analizarCadenaLALR(
        String entrada,
        Map<Integer, Map<String, EntradaTabla>> actionTable,
        Map<Integer, Map<String, Integer>> gotoTable,
        Map<String, List<List<String>>> gramatica,
        List<Map.Entry<String, List<String>>> listaProducciones) {

        errores = new ArrayList<>();
        Lexer lexer = new Lexer(entrada + "\u0000");
        List<Lexer.Token> rawTokens = lexer.tokenize();
        List<String> tokens = new ArrayList<>();
        List<Integer> posiciones = new ArrayList<>();

        for (Lexer.Token token : rawTokens) {
            if (!token.type.equals("WHITESPACE") && !token.type.equals("EOL")) {
                tokens.add(token.type);
                posiciones.add(token.start);
            }
        }
        tokens.add("EOF");
        posiciones.add(entrada.length());

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        int i = 0;
        boolean huboError = false;
        while (i < tokens.size()) {
            int state = stack.peek();
            String token = tokens.get(i);
            EntradaTabla entradaTabla = actionTable.getOrDefault(state, Collections.emptyMap()).get(token);

            if (entradaTabla == null) {
                String tipo = token.equals("EOF") ? "sintáctico" : "gramatical";
                String desc;
                if (token.equals("EOF")) {
                    if (tokens.size() == 1) {
                        desc = "Cadena vacía no es aceptada por la gramática.";
                    } else {
                        desc = "Faltan elementos para completar la producción.";
                    }
                } else {
                    desc = "Token inesperado: '" + token + "'.";
                }
                int errorPos;
                if (token.equals("EOF")) {
                    errorPos = posiciones.isEmpty() ? 0 : posiciones.get(posiciones.size() - 2); // posición del último token válido
                } else {
                    errorPos = i < posiciones.size() ? posiciones.get(i) : entrada.length();
                }
                errores.add(new ErrorDetalle(tipo, errorPos, token, desc));
                huboError = true;
                i++; //  avanzar al siguiente token para continuar análisis
                continue; // intentar detectar más errores

                
            }
            switch (entradaTabla.getTipo()) {
                case ACCEPT:
                    return "ACEPTADA";
                case SHIFT:
                    stack.push(entradaTabla.getEstadoDestino());
                    i++;
                    break;
                case REDUCE:
                    String lhs = entradaTabla.getProduccionIzq();
                    List<String> rhs = entradaTabla.getProduccionDer();
                    if (stack.size() < rhs.size()) {
                        errores.add(new ErrorDetalle("sintáctico", posiciones.get(i), token,
                            "Reducción imposible: pila insuficiente para '" + lhs + "'."));
                        return "RECHAZADA (pila insuficiente para reducción: " + lhs + ")";
                    }
                    for (int j = 0; j < rhs.size(); j++) stack.pop();
                    int topState = stack.peek();
                    Integer next = gotoTable.getOrDefault(topState, Collections.emptyMap()).get(lhs);
                    if (next == null) {
                        errores.add(new ErrorDetalle("gramatical", posiciones.get(i), token,
                            "Goto inválido tras reducción de '" + lhs + "'."));
                        return "RECHAZADA (goto inválido desde estado " + topState + " con símbolo '" + lhs + "')";
                    }
                    stack.push(next);
                    break;
                default:
                    errores.add(new ErrorDetalle("gramatical", posiciones.get(i), token, "Acción inválida."));
                    return "RECHAZADA (acción inválida)";
            }
        }

        return huboError ? "RECHAZADA" : "ACEPTADA";

    }

}
