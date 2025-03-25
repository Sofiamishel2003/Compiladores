package clases;

import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.*;

public class AFDGeneratorIntegrationTest {

  @Test
  public void testAFD_verificaCadenas_validasEInvalidas() throws Exception {
    // Ruta al archivo lexer.yal (ajusta si es necesario)
    String path = new File("lexer.yal").getAbsolutePath();

    // Paso 1: Parsear el archivo YAL
    YalParser parser = new YalParser();
    List<YalParser.Rule> reglas = parser.parseYAL(path);
    assertFalse("No se encontraron reglas en el archivo YAL", reglas.isEmpty());

    System.out.println(RegexConverter.preprocessRegex("['0'-'9']+"));

    // Paso 2: Combinar regex + convertir a postfix
    String regex = parser.combineRegex();
    String postfix = RegexConverter.toPostfix(regex);

    // Paso 3: Construir AST y followpos
    ASTBuilder builder = new ASTBuilder(postfix);
    ASTNode root = builder.buildAST();
    builder.computeNullableFirstLast(root);
    builder.computeFollowpos(root);

    // Paso 4: Generar AFD
    Map<Integer, String> tokenMap = new HashMap<>();
    for (var entry : builder.getAcceptingPositions().entrySet()) {
      tokenMap.put(entry.getKey(), entry.getValue()); // Ej: NUM, PLUS, etc.
    }

    AFDGenerator afd = new AFDGenerator(
        builder.getFollowpos(),
        builder.getSymbolTable(),
        builder.getStartState(root),
        tokenMap);

    // Paso 5: Probar cadenas válidas
    String[] validas = {
        "123",
        "45+67",
        "9*8",
        "(5+3)*2",
        "123+456-789/10"
    };

    for (String cadena : validas) {
      assertTrue("La cadena debería ser válida: " + cadena, afd.verificarCadena(cadena));
    }

    // Paso 6: Probar cadenas inválidas
    String[] invalidas = {
        "++123",
        "abc",
        "42//3",
        "1+2-",
        ")(43"
    };

    for (String cadena : invalidas) {
      assertFalse("La cadena debería ser inválida: " + cadena, afd.verificarCadena(cadena));
    }
  }
}