package clases;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;
import java.util.List;

public class YalParserTest {

  @Test
  public void testParseYAL_basicRules() throws Exception {
    String path = new File("lexer.yal").getAbsolutePath();
    YalParser parser = new YalParser();
    List<YalParser.Rule> rules = parser.parseYAL(path);

    assertNotNull("Las reglas no deben ser nulas", rules);
    assertFalse("Debe haber al menos una regla", rules.isEmpty());

    for (YalParser.Rule rule : rules) {
      assertNotNull("La expresión regular no debe ser null", rule.regex);
      assertNotNull("La acción no debe ser null", rule.action);
    }
  }
}