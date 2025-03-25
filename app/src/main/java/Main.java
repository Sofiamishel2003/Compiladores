import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import clases.AFDGenerator;
import clases.ASTBuilder;
import clases.ASTNode;
import clases.RegexConverter;
import clases.YalParser;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Parsear el archivo .yal
            String path = Paths.get(".", "lexer.yal").toAbsolutePath().normalize().toString();
            YalParser parser = new YalParser();
            List<YalParser.Rule> rules = parser.parseYAL(path);

            System.out.println("Reglas extraídas:");
            for (YalParser.Rule rule : rules) {
                System.out.println(rule);
            }

            // 2, 3 y 4. Convertir a postfix con dummy token
            String combinedRegex = parser.combineRegex();
            //System.out.println("\nRegex combinada: " + combinedRegex);
            combinedRegex = "(( | |\\t))^#1|((\\n))^#2|((0|1|2|3|4|5|6|7|8|9)^(0|1|2|3|4|5|6|7|8|9)*)^#3|(\\+)^#4|(\\-)^#5|(\\*)^#6|(\\/)^#7|(\\()^#8|(\\))^#9";
            // Convertir la regex combinada a postfix
            String postfix = RegexConverter.toPostfix(combinedRegex);
            System.out.println("\nRegex en postfix: " + postfix);

            // 4. Construir AST
            ASTBuilder astBuilder = new ASTBuilder(postfix);
            ASTNode astRoot = astBuilder.buildAST();

            // 5. Calcular nullable, firstpos, lastpos, followpos
            astBuilder.computeNullableFirstLast(astRoot);
            astBuilder.computeFollowpos(astRoot);

            // 6. Mapear posiciones a nombres reales de tokens
            Map<Integer, String> positionToTokenMap = new HashMap<>();
            /*Map<Integer, String> accepting = (Map<Integer, String>) astBuilder.getAcceptingPosition();
            for (Map.Entry<Integer, String> entry : accepting.entrySet()) {
                String symbol = entry.getValue();
                int tokenNumber = Integer.parseInt(symbol.substring(6));
                if (tokenNumber == 0)
                    continue;
                String realAction = rules.get(tokenNumber - 1).action;
                positionToTokenMap.put(entry.getKey(), realAction);
            }*/
            // 7. Generar AFD
            var followpos = astBuilder.getFollowpos();
         var symbolTable = astBuilder.getSymbolTable();
         var startState = astBuilder.getStartState(astRoot);
         var acceptingPosition = astBuilder.getAcceptingPositions();
         // 4. Generar el AFD
         AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);

            // 8. Imprimir AFD
            System.out.println("\nAFD generado:");
            afd.printAFD();
            afd.generarDot("afd_" + combinedRegex.hashCode() + ".dot");

            // 10. Minimizar AFD (opcional pero recomendado)
            //afd.minimizeAFD();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
