package clases;

import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

public class AFDGeneratorTest {

  @Test
  public void testVerificarCadena_simpleMatch() {
    // Simular un AFD que acepta solo "a"
    Map<Integer, Set<Integer>> followpos = new HashMap<>();
    followpos.put(1, Set.of());

    Map<Integer, String> symbolTable = Map.of(1, "a");
    Set<Integer> startState = Set.of(1);
    Map<Integer, String> positionToTokenMap = Map.of(1, "A_TOKEN");

    AFDGenerator afd = new AFDGenerator(followpos, symbolTable, startState, positionToTokenMap);
    assertTrue(afd.verificarCadena("a"));
    assertFalse(afd.verificarCadena("b"));
  }
}