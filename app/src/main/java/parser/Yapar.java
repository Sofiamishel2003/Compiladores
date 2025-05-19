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
import parser.automata.AutomataLR0;
import parser.automata.Estado;
import parser.automata.EstadoLALR;
import parser.automata.LALRTableGenerator;
import parser.automata.LR0TableGenerator;
import parser.automata.YalpParser;

public class Yapar {
    public static void main(String[] args) throws Exception {
        if (args.length != 5 || !args[1].equals("-l") || !args[3].equals("-o")) {
            System.err.println("Uso: yapar parser.yalp -l lexer.yal -o theparser");
            System.exit(1);
        }

        String rutaYalp = Paths.get(args[0]).toAbsolutePath().normalize().toString();
        String rutaYal = Paths.get(args[2]).toAbsolutePath().normalize().toString();
        String nombreParser = args[4];

        YalParser yalParser = new YalParser();
        List<YalParser.Rule> reglas = yalParser.parseYAL(rutaYal);
        String combinada = yalParser.combineRegex();
        String postfix = RegexConverter.toPostfix(combinada);
        System.out.println("POSTFIX" + postfix);
        ASTBuilder astBuilder = new ASTBuilder(postfix);
        ASTNode astRoot = astBuilder.buildAST();
        astBuilder.computeNullableFirstLast(astRoot);
        astBuilder.computeFollowpos(astRoot);

        var followpos = astBuilder.getFollowpos();
        var symbolTable = astBuilder.getSymbolTable();
        var startState = astBuilder.getStartState(astRoot);
        var acceptingPosition = astBuilder.getAcceptingPositions();

        AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);

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
        afd.generarCodigoLexer("src/main/java/clases/Lexer.java", acceptingTypesResolved);

        YalpParser.ResultadoYalp resultado = YalpParser.parsearArchivo(rutaYalp);
        Map<String, List<List<String>>> gramatica = resultado.gramatica;
        List<Map.Entry<String, List<String>>> listaProducciones = new ArrayList<>();
        for (Map.Entry<String, List<List<String>>> entry : gramatica.entrySet()) {
            for (List<String> rhs : entry.getValue()) {
                listaProducciones.add(Map.entry(entry.getKey(), rhs));
            }
        }

        Set<String> terminales = resultado.terminales;
        terminales.add("EOF");
        String simboloInicial = resultado.simboloInicial;
        gramatica.put("S'", List.of(List.of(simboloInicial)));
        listaProducciones.add(0, Map.entry("S'", List.of(simboloInicial)));

        Set<String> noTerminales = new HashSet<>(gramatica.keySet());
        noTerminales.removeAll(terminales);

        AutomataLR0 automataLR0 = new AutomataLR0(gramatica);
        List<Estado> estadosLR0 = automataLR0.construirAutomata();
        automataLR0.exportarADot(estadosLR0, "src/main/java/parser/automata.dot");

        LR0TableGenerator tableGen = new LR0TableGenerator(gramatica, listaProducciones);
        LR0TableGenerator.ParsingTable tablaLR0 = tableGen.generarTabla(estadosLR0, terminales, noTerminales);

        AutomataLALR lalr = new AutomataLALR(gramatica, terminales);
        List<EstadoLALR> estadosLR1 = lalr.construirAutomataLR1();

        AutomataLALR.exportarADotLALR(estadosLR1, "src/main/java/parser/automataLR1.dot");
        // Fusión LR(1) → LALR
        List<EstadoLALR> estadosLALR = lalr.fusionarLR1paraLALR(estadosLR1);
        AutomataLALR.exportarADotLALR(estadosLALR, "src/main/java/parser/automataLALR.dot");

        LALRTableGenerator generadorTablaLALR = new LALRTableGenerator(gramatica);
        LALRTableGenerator.ParsingTable tablaLALR = generadorTablaLALR.generarTabla(estadosLALR, terminales, noTerminales);
        
        System.out.println("Tabla LALR:");
        for (var entry : tablaLALR.action.entrySet()) {
            int state = entry.getKey();
            for (var sym : entry.getValue().keySet()) {
                String act = entry.getValue().get(sym);
                System.out.printf("  Estado %d, símbolo %s → %s%n", state, sym, act);
            }
        }

        List<String> cadenas = Files.readAllLines(Paths.get("cadenas.txt"));

        try (BufferedWriter out = new BufferedWriter(new FileWriter("resultado.txt"))) {
            for (String entrada : cadenas) {
                out.write("Cadena: " + entrada + "\n");
                String resultadoLR0 = analizarCadenaLR0(entrada, tablaLR0.action, tablaLR0.goTo, gramatica, listaProducciones);
                String resultadoLALR = analizarCadenaLALR(entrada, tablaLALR.action, tablaLALR.goTo, gramatica, listaProducciones);
                out.write("  Resultado LR(0): " + resultadoLR0 + "\n");
                out.write("  Resultado LALR : " + resultadoLALR + "\n");
                out.write("---------------------------\n");
            }
        }

        System.out.println("Parser generado: " + nombreParser + ".java");
        System.out.println("Resultados escritos en resultado.txt");
    }

    private static String analizarCadenaLR0(
        String entrada,
        Map<Integer, Map<String, String>> actionTable,
        Map<Integer, Map<String, Integer>> gotoTable,
        Map<String, List<List<String>>> gramatica,
        List<Map.Entry<String, List<String>>> listaProducciones) {

        Lexer lexer = new Lexer(entrada + "\u0000");
        List<String> tokens = new ArrayList<>();
        for (Lexer.Token token : lexer.tokenize()) {
            if (!token.type.equals("WHITESPACE") && !token.type.equals("EOL")) {
                tokens.add(token.type);
            }
        }
        tokens.add("EOF");

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        int i = 0;

        while (i < tokens.size()) {
            int state = stack.peek();
            String token = tokens.get(i);
            String action = actionTable.getOrDefault(state, new HashMap<>()).get(token);

            if (action == null) {
                if (token.equals("EOF")) return "RECHAZADA (no se pudo reducir a input antes del fin de cadena)";
                return "RECHAZADA (error en token: " + token + ")";
            }
            if (action.equals("accept")) return "ACEPTADA";

            if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(nextState);
                i++;
            } else if (action.startsWith("r")) {
                int prodNum = Integer.parseInt(action.substring(1));
                if (prodNum >= listaProducciones.size()) return "RECHAZADA (producción inválida)";

                Map.Entry<String, List<String>> prod = listaProducciones.get(prodNum);
                String lhs = prod.getKey();
                List<String> rhs = prod.getValue();

                if (rhs.size() > 0 && stack.size() < rhs.size()) return "RECHAZADA (pila insuficiente para reducción: " + lhs + ")";
                System.out.println("Reduciendo con: " + lhs + " -> " + rhs);
                System.out.println("Pila antes de reducir: " + stack);

                for (int j = 0; j < rhs.size(); j++) stack.pop();

                int topState = stack.peek();
                Integer next = gotoTable.getOrDefault(topState, new HashMap<>()).get(lhs);
                if (next == null) return "RECHAZADA (goto inválido desde estado " + topState + " con símbolo '" + lhs + "')";

                stack.push(next);
            } else {
                return "RECHAZADA (acción inválida: " + action + ")";
            }
        }

        return "ACEPTADA";
    }
    private static String analizarCadenaLALR(
        String entrada,
        Map<Integer, Map<String, String>> actionTable,
        Map<Integer, Map<String, Integer>> gotoTable,
        Map<String, List<List<String>>> gramatica,
        List<Map.Entry<String, List<String>>> listaProducciones) {

        Lexer lexer = new Lexer(entrada + "\u0000");
        List<String> tokens = new ArrayList<>();
        for (Lexer.Token token : lexer.tokenize()) {
            if (!token.type.equals("WHITESPACE") && !token.type.equals("EOL")) {
                tokens.add(token.type);
            }
        }
        tokens.add("EOF");

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        int i = 0;

        while (i < tokens.size()) {
            int state = stack.peek();
            String token = tokens.get(i);
            String action = actionTable.getOrDefault(state, Collections.emptyMap()).get(token);

            if (action == null) {
                if (token.equals("EOF")) {
                    return "RECHAZADA (no se pudo reducir a input antes del fin de cadena)";
                }
                return "RECHAZADA (error en token: " + token + ")";
            }

            if (action.equals("accept")) {
                return "ACEPTADA";
            }

            if (action.startsWith("s")) {
                int nextState = Integer.parseInt(action.substring(1));
                stack.push(nextState);
                i++;
            } else if (action.startsWith("r")) {
                int prodNum = Integer.parseInt(action.substring(1));
                if (prodNum >= listaProducciones.size()) {
                    return "RECHAZADA (producción inválida)";
                }

                Map.Entry<String, List<String>> prod = listaProducciones.get(prodNum);
                String lhs = prod.getKey();
                List<String> rhs = prod.getValue();

                System.out.println("⏬ Reducción: " + lhs + " -> " + rhs);
                System.out.println("📦 Pila antes de reducción: " + stack);

                // Verificación de pila
                if (stack.size() < rhs.size()) {
                    return "RECHAZADA (pila insuficiente para reducción: " + lhs + ")";
                }

                for (int j = 0; j < rhs.size(); j++) {
                    stack.pop();
                }

                if (stack.isEmpty()) {
                    return "RECHAZADA (pila vacía después de reducción)";
                }

                int topState = stack.peek();
                Integer next = gotoTable.getOrDefault(topState, Collections.emptyMap()).get(lhs);
                if (next == null) {
                    return "RECHAZADA (goto inválido desde estado " + topState + " con símbolo '" + lhs + "')";
                }

                System.out.println("➡️ Goto(" + topState + ", " + lhs + ") = " + next);
                stack.push(next);
                System.out.println("📦 Pila después de reducción: " + stack);
            } else {
                return "RECHAZADA (acción inválida: " + action + ")";
            }
        }

        return "ACEPTADA";
    }

}
