import java.io.IOException;
import java.util.List;

import clases.RegexConverter;
import clases.YalParser;

public class Main {
    public static void main(String[] args) {
        try {
            // 1. Parsear el archivo .yal
            YalParser parser = new YalParser();
            List<YalParser.Rule> rules = parser.parseYAL("Lexer.yal");

            System.out.println("Reglas extraídas:");
            for (YalParser.Rule rule : rules) {
                System.out.println(rule);
            }

            // 2, 3 y 4. Convertir a postfix con dummy token
            String combinedRegex = parser.combineRegex();
            System.out.println("\nRegex combinada: " + combinedRegex);

            // Convertir la regex combinada a postfix
            String postfix = RegexConverter.toPostfix(combinedRegex);
            System.out.println("\nRegex en postfix: " + postfix);

            // Aquí puedes continuar con tu AST y AFD
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



