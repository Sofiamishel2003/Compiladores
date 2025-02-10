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
        String regex1 = "a|b*";
        String regex2 = "(a|b)*c";
        String regex3 = "a(b|c)/*d";
        String regex4 = "[a-f]*";
        String regex5 = "a+";
        String regex6 = "a?";

        System.out.println("Expresión infija: " + regex1);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex1));

        System.out.println("\nExpresión infija: " + regex2);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex2));

        System.out.println("\nExpresión infija: " + regex3);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex3));

        System.out.println("\nExpresión infija: " + regex4);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex4));

        System.out.println("\nExpresión infija: " + regex5);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex5));

        System.out.println("\nExpresión infija: " + regex6);
        System.out.println("Expresión postfija: " + RegexConverter.toPostfix(regex6));

        // 2. Construir el AST a partir de la expresión postfija
        ASTBuilder astBuilder = new ASTBuilder("ab|*a^b^b^.^");
        //ASTBuilder astBuilder = new ASTBuilder("ab*|.^");
        ASTNode root = astBuilder.buildAST();
        astBuilder.computeNullableFirstLast(root);
        astBuilder.computeFollowpos(root);

        // 3. Obtener followpos y la tabla de símbolos
        var followpos = astBuilder.getFollowpos();
        var symbolTable = astBuilder.getSymbolTable();
        var startState = astBuilder.getStartState(root);
        var acceptingPosition = astBuilder.getAcceptingPosition();

        // 4. Generar el AFD
        AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, acceptingPosition);
        
        // 5. Imprimir los estados y transiciones del AFD
        afd.printAFD();

        //6. Generar el AFD minimizado
        afd.minimizeAFD();
        
        //7. Imprimir los estados y transiciones del AFD minimizado
        System.out.println("\n=== AFD Minimizado ===");
        afd.printAFD();

        // Leer cadenas desde JSON y verificarlas
        try (FileReader reader = new FileReader("cadenas.json")) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray cadenasArray = jsonObject.getAsJsonArray("cadenas");

            System.out.println("\nResultados de verificación:");
            for (JsonElement elemento : cadenasArray) {
                String cadena = elemento.getAsString();
                boolean aceptada = afd.verificarCadena(cadena);
                System.out.println("La cadena \"" + cadena + "\" es aceptada: " + aceptada);
            }
        } catch (IOException e) {
            System.err.println("Error al leer el archivo JSON: " + e.getMessage());
        }

        // Generar la representación visual del AFD
        afd.generarDot("afd.dot");
        afd.minimizeAFD();
        afd.generarDot("afd_min.dot");

        System.out.println("\nArchivos DOT generados: afd.dot y afd_min.dot");

    }
}
