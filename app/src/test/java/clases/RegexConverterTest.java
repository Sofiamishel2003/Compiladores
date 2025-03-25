package clases;

import org.junit.Test;
import static org.junit.Assert.*;

public class RegexConverterTest {

  @Test
  public void testToPostfix_fuzzedExpressions() {
    String[][] cases = {
        { "a|b", "ab|" },
        { "a*b", "a*b^" },
        { "(a|b)*", "ab|*" },
        { "a^b|c", "ab^c|" },
        { "(a)", "a" },
        { "a+b", "aab*^" },
        { "[0-2]", "01|2|" }
    };

    for (int i = 0; i < cases.length; i++) {
      String infix = cases[i][0];
      String expectedPostfix = cases[i][1];
      String actualPostfix = RegexConverter.toPostfix(infix);

      System.out.println("\n--- Caso #" + (i + 1) + " ---");
      System.out.println("Infix:     " + infix);
      System.out.println("Esperado:  " + expectedPostfix);
      System.out.println("Resultado: " + actualPostfix);

      try {
        compareTokens(expectedPostfix, actualPostfix);
      } catch (AssertionError e) {
        System.err.println(" Fallo en expresión: " + infix);
        throw e;
      }
    }
  }

  private void compareTokens(String expected, String actual) {
    String[] exp = expected.split("");
    String[] act = actual.split("");

    int maxLength = Math.max(exp.length, act.length);
    boolean mismatch = false;

    System.out.printf("%-10s %-10s %-5s\n", "ESPERADO", "OBTENIDO", "IDX");
    for (int i = 0; i < maxLength; i++) {
      String e = i < exp.length ? exp[i] : "[null]";
      String a = i < act.length ? act[i] : "[null]";

      if (!e.equals(a)) {
        mismatch = true;
        System.out.printf("%-10s %-10s %-5d  <-- \n", e, a, i);
      } else {
        System.out.printf("%-10s %-10s %-5d\n", e, a, i);
      }
    }

    if (mismatch) {
      fail("Postfix generado no coincide con el esperado.");
    }
  }
}