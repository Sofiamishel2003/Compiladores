import java.io.FileReader;
import java.io.IOException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import clases.AFDGenerator;
import clases.ASTBuilder;
import clases.ASTNode;
import clases.RegexConverter;

public class Main {
    public static void main(String[] args) {
        try (FileReader reader = new FileReader("input.json")) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            
            if (!jsonObject.has("tokens")) {
                throw new RuntimeException("Error: La clave 'tokens' no existe en el JSON.");
            }
            if (!jsonObject.has("cadenas")) {
                throw new RuntimeException("Error: La clave 'cadenas' no existe en el JSON.");
            }

            // Obtener las expresiones regulares desde "tokens"
            JsonObject tokensObject = jsonObject.getAsJsonObject("tokens");
            JsonArray regexArray = new JsonArray();
            for (String key : tokensObject.keySet()) {
                regexArray.add(tokensObject.get(key).getAsString());
            }

            JsonArray cadenasArray = jsonObject.getAsJsonArray("cadenas");
            if (cadenasArray == null) {
                throw new RuntimeException("Error: El array 'cadenas' es null.");
            }

            for (JsonElement regexElement : regexArray) {
                String regex = regexElement.getAsString();
                System.out.println("\nProcesando regex: " + regex);
                System.out.println("\nPostfix: " + RegexConverter.toPostfix(regex));

                ASTBuilder astBuilder = new ASTBuilder(RegexConverter.toPostfix(regex));
                ASTNode root = astBuilder.buildAST();
                astBuilder.computeNullableFirstLast(root);
                astBuilder.computeFollowpos(root);

                var followpos = astBuilder.getFollowpos();
                var symbolTable = astBuilder.getSymbolTable();
                var startState = astBuilder.getStartState(root);
                var acceptingPosition = astBuilder.getAcceptingPosition();

                AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);
                afd.printAFD();

                afd.minimizeAFD();
                System.out.println("\n=== AFD Minimizado ===");
                afd.printAFD();

                System.out.println("\nResultados de verificación para regex: " + regex);
                for (JsonElement cadenaElement : cadenasArray) {
                    String cadena = cadenaElement.getAsString();
                    boolean aceptada = afd.verificarCadena(cadena);
                    System.out.println("La cadena \"" + cadena + "\" es aceptada: " + aceptada);
                }

                afd.generarDot("afd_" + regex.hashCode() + ".dot");
                afd.minimizeAFD();
                afd.generarDot("afd_min_" + regex.hashCode() + ".dot");
            }

        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
