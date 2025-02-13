import clases.AFDGenerator;
import clases.ASTBuilder;
import clases.ASTNode;
import clases.RegexConverter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try (FileReader reader = new FileReader("input.json")) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray regexArray = jsonObject.getAsJsonArray("regex");
            JsonArray cadenasArray = jsonObject.getAsJsonArray("cadenas");

            for (JsonElement regexElement : regexArray) {
                String regex = regexElement.getAsString();
                System.out.println("\nProcesando regex: " + regex);

                // Construcción del AST a partir de la expresión postfija
                ASTBuilder astBuilder = new ASTBuilder(RegexConverter.toPostfix(regex));
                ASTNode root = astBuilder.buildAST();
                astBuilder.computeNullableFirstLast(root);
                astBuilder.computeFollowpos(root);

                // Obtener followpos y la tabla de símbolos
                var followpos = astBuilder.getFollowpos();
                var symbolTable = astBuilder.getSymbolTable();
                var startState = astBuilder.getStartState(root);
                var acceptingPosition = astBuilder.getAcceptingPosition();

                // Generar el AFD
                AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);

                // Imprimir los estados y transiciones del AFD
                afd.printAFD();

                // Minimizar el AFD
                afd.minimizeAFD();
                System.out.println("\n=== AFD Minimizado ===");
                afd.printAFD();

                // Verificar cadenas con el AFD generado
                System.out.println("\nResultados de verificación para regex: " + regex);
                for (JsonElement cadenaElement : cadenasArray) {
                    String cadena = cadenaElement.getAsString();
                    boolean aceptada = afd.verificarCadena(cadena);
                    System.out.println("La cadena \"" + cadena + "\" es aceptada: " + aceptada);
                }

                // Generar representaciones visuales del AFD
                afd.generarDot("afd_" + regex.hashCode() + ".dot");
                afd.minimizeAFD();
                afd.generarDot("afd_min_" + regex.hashCode() + ".dot");
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }
    }
}