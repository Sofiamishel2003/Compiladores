package clases;

import org.junit.Test;
import java.util.*;

import static org.junit.Assert.*;

public class ASTBuilderTest {

  @Test
  public void testBuildAST_yFollowpos() {
    String postfix = "a#1b#2^"; // a concatenado con b
    ASTBuilder builder = new ASTBuilder(postfix);
    ASTNode root = builder.buildAST();
    assertNotNull(root);

    builder.computeNullableFirstLast(root);
    builder.computeFollowpos(root);

    Map<Integer, Set<Integer>> followpos = builder.getFollowpos();
    assertFalse("Followpos no debe estar vacío", followpos.isEmpty());
  }
}