package de.uni_passau.fim.se2.sa.sign.interpretation;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SignTest {
  @Test
  public void testEvaluateAdd() {
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateAdd(Sign.MINUS, Sign.MINUS));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateAdd(Sign.MINUS, Sign.ZERO));
    assertEquals(Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS), Sign.evaluateAdd(Sign.MINUS, Sign.PLUS));

    assertEquals(Set.of(Sign.ZERO), Sign.evaluateAdd(Sign.ZERO, Sign.ZERO));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateAdd(Sign.ZERO, Sign.MINUS));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateAdd(Sign.ZERO, Sign.PLUS));

    assertEquals(Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS), Sign.evaluateAdd(Sign.PLUS, Sign.MINUS));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateAdd(Sign.PLUS, Sign.ZERO));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateAdd(Sign.PLUS, Sign.PLUS));
  }

  @Test
  public void testEvaluateSub() {
    assertEquals(Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS), Sign.evaluateSub(Sign.MINUS, Sign.MINUS));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateSub(Sign.MINUS, Sign.PLUS));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateSub(Sign.MINUS, Sign.ZERO));

    assertEquals(Set.of(Sign.ZERO), Sign.evaluateSub(Sign.ZERO, Sign.ZERO));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateSub(Sign.ZERO, Sign.MINUS));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateSub(Sign.ZERO, Sign.PLUS));

    assertEquals(Set.of(Sign.PLUS), Sign.evaluateSub(Sign.PLUS, Sign.ZERO));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateSub(Sign.PLUS, Sign.MINUS));
    assertEquals(Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS), Sign.evaluateSub(Sign.PLUS, Sign.PLUS));
  }

  @Test
  public void testEvaluateMul() {
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateMul(Sign.MINUS, Sign.MINUS));
    assertEquals(Set.of(Sign.MINUS), Sign.evaluateMul(Sign.MINUS, Sign.PLUS));
    assertEquals(Set.of(Sign.ZERO), Sign.evaluateMul(Sign.MINUS, Sign.ZERO));

    assertEquals(Set.of(Sign.ZERO), Sign.evaluateMul(Sign.ZERO, Sign.ZERO));
    assertEquals(Set.of(Sign.ZERO), Sign.evaluateMul(Sign.ZERO, Sign.MINUS));
    assertEquals(Set.of(Sign.ZERO), Sign.evaluateMul(Sign.ZERO, Sign.PLUS));

    assertEquals(Set.of(Sign.MINUS), Sign.evaluateMul(Sign.PLUS, Sign.MINUS));
    assertEquals(Set.of(Sign.ZERO), Sign.evaluateMul(Sign.PLUS, Sign.ZERO));
    assertEquals(Set.of(Sign.PLUS), Sign.evaluateMul(Sign.PLUS, Sign.PLUS));
  }

  @Test
  public void testEvaluateDiv() {
    assertEquals(Set.of(Sign.ZERO, Sign.PLUS), Sign.evaluateDiv(Sign.MINUS, Sign.MINUS));
    assertEquals(Set.of(), Sign.evaluateDiv(Sign.MINUS, Sign.ZERO));
    assertEquals(Set.of(Sign.ZERO, Sign.MINUS), Sign.evaluateDiv(Sign.MINUS, Sign.PLUS));

    assertEquals(Set.of(Sign.ZERO), Sign.evaluateDiv(Sign.ZERO, Sign.MINUS));
    assertEquals(Set.of(), Sign.evaluateDiv(Sign.ZERO, Sign.ZERO));
    assertEquals(Set.of(Sign.ZERO), Sign.evaluateDiv(Sign.ZERO, Sign.PLUS));

    assertEquals(Set.of(Sign.ZERO, Sign.MINUS), Sign.evaluateDiv(Sign.PLUS, Sign.MINUS));
    assertEquals(Set.of(), Sign.evaluateDiv(Sign.PLUS, Sign.ZERO));
    assertEquals(Set.of(Sign.ZERO, Sign.PLUS), Sign.evaluateDiv(Sign.PLUS, Sign.PLUS));
  }
}
