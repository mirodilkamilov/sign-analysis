package de.uni_passau.fim.se2.sa.sign.interpretation;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class SignValueTest {
  @Test
  public void testGetSize_ReturnsOne() {
    assertEquals(1, SignValue.ZERO.getSize());
  }

  @Test
  public void testToString() {
    assertEquals("⊥", SignValue.BOTTOM.toString());
    assertEquals("{0}", SignValue.ZERO.toString());
    assertEquals("{+,–}", SignValue.PLUS_MINUS.toString());
    assertEquals("⊤", SignValue.TOP.toString());
    assertEquals("∅", SignValue.UNINITIALIZED_VALUE.toString());
  }

  @Test
  public void testGetSigns_ReturnsCorrectType() throws NoSuchMethodException {
    Method method = SignValue.class.getMethod("getSigns");
    assertEquals(Set.class, method.getReturnType());
    String genericType = "<" + Sign.PLUS.getClass().getName() + ">";
    assertEquals(Set.class.getName() + genericType, method.getGenericReturnType().getTypeName());

    assertEquals(Set.of(), SignValue.BOTTOM.getSigns());
    assertEquals(Set.of(Sign.ZERO, Sign.MINUS), SignValue.ZERO_MINUS.getSigns());
    assertEquals(Set.of(Sign.PLUS, Sign.ZERO, Sign.MINUS), SignValue.TOP.getSigns());
    assertNull(SignValue.UNINITIALIZED_VALUE.getSigns());
  }

  @Test
  public void testJoin_PreConditions() {
    assertThrowsExactly(
            IllegalStateException.class,
            () -> SignValue.UNINITIALIZED_VALUE.join(SignValue.TOP)
    );
    assertThrowsExactly(
            IllegalStateException.class,
            () -> SignValue.ZERO_PLUS.join(SignValue.UNINITIALIZED_VALUE)
    );
    assertThrowsExactly(
            IllegalStateException.class,
            () -> SignValue.MINUS.join(SignValue.UNINITIALIZED_VALUE)
    );
  }

  @Test
  public void testJoin_BottomAndTop_EarlyReturns() {
    assertEquals(SignValue.MINUS, SignValue.BOTTOM.join(SignValue.MINUS));
    assertEquals(SignValue.ZERO_MINUS, SignValue.ZERO_MINUS.join(SignValue.BOTTOM));
    assertEquals(SignValue.BOTTOM, SignValue.BOTTOM.join(SignValue.BOTTOM));

    assertEquals(SignValue.TOP, SignValue.TOP.join(SignValue.ZERO_MINUS));
    assertEquals(SignValue.TOP, SignValue.TOP.join(SignValue.ZERO_PLUS));
    assertEquals(SignValue.TOP, SignValue.TOP.join(SignValue.TOP));
  }

  @Test
  public void testJoin_ComplexJoinsWithMinus() {
    assertEquals(SignValue.MINUS, SignValue.MINUS.join(SignValue.MINUS));
    assertEquals(SignValue.ZERO_MINUS, SignValue.MINUS.join(SignValue.ZERO));
    assertEquals(SignValue.ZERO_MINUS, SignValue.ZERO_MINUS.join(SignValue.MINUS));
    assertEquals(SignValue.ZERO_MINUS, SignValue.MINUS.join(SignValue.ZERO_MINUS));
    assertEquals(SignValue.ZERO_MINUS, SignValue.ZERO_MINUS.join(SignValue.MINUS));
    assertEquals(SignValue.PLUS_MINUS, SignValue.MINUS.join(SignValue.PLUS));
    assertEquals(SignValue.PLUS_MINUS, SignValue.PLUS.join(SignValue.MINUS));
    assertEquals(SignValue.TOP, SignValue.MINUS.join(SignValue.ZERO_PLUS));
    assertEquals(SignValue.TOP, SignValue.ZERO_PLUS.join(SignValue.MINUS));
    assertEquals(SignValue.TOP, SignValue.MINUS.join(SignValue.TOP));
  }

  @Test
  public void testIsLessOrEqual_PreConditions() {
    assertThrowsExactly(
            IllegalStateException.class,
            () -> SignValue.UNINITIALIZED_VALUE.isLessOrEqual(SignValue.TOP)
    );
    assertThrowsExactly(
            IllegalStateException.class,
            () -> SignValue.TOP.isLessOrEqual(SignValue.UNINITIALIZED_VALUE)
    );
  }

  @Test
  public void testIsLessOrEqual_EqualConditions() {
    assertTrue(SignValue.BOTTOM.isLessOrEqual(SignValue.BOTTOM));
    assertTrue(SignValue.MINUS.isLessOrEqual(SignValue.MINUS));
    assertTrue(SignValue.ZERO.isLessOrEqual(SignValue.ZERO));
    assertTrue(SignValue.ZERO_MINUS.isLessOrEqual(SignValue.ZERO_MINUS));
    assertTrue(SignValue.PLUS.isLessOrEqual(SignValue.PLUS));
    assertTrue(SignValue.PLUS_MINUS.isLessOrEqual(SignValue.PLUS_MINUS));
    assertTrue(SignValue.ZERO_PLUS.isLessOrEqual(SignValue.ZERO_PLUS));
    assertTrue(SignValue.TOP.isLessOrEqual(SignValue.TOP));
  }

  @Test
  public void testIsLessOrEqual_LessOrGreaterConditions() {
    assertTrue(SignValue.BOTTOM.isLessOrEqual(SignValue.MINUS));
    assertFalse(SignValue.MINUS.isLessOrEqual(SignValue.ZERO));
    assertTrue(SignValue.ZERO.isLessOrEqual(SignValue.ZERO_MINUS));
    assertFalse(SignValue.ZERO_MINUS.isLessOrEqual(SignValue.PLUS));
    assertTrue(SignValue.PLUS.isLessOrEqual(SignValue.PLUS_MINUS));
    assertFalse(SignValue.PLUS_MINUS.isLessOrEqual(SignValue.ZERO_PLUS));
    assertTrue(SignValue.ZERO_PLUS.isLessOrEqual(SignValue.TOP));
  }

  @Test
  public void testIsZero() {
    assertTrue(SignValue.isZero(SignValue.ZERO));
    assertFalse(SignValue.isZero(SignValue.BOTTOM));
    assertFalse(SignValue.isZero(SignValue.UNINITIALIZED_VALUE));
    assertFalse(SignValue.isZero(SignValue.TOP));
    assertFalse(SignValue.isZero(SignValue.MINUS));
    assertFalse(SignValue.isZero(SignValue.ZERO_MINUS));
    assertFalse(SignValue.isZero(SignValue.ZERO_PLUS));
  }

  @Test
  public void testIsMaybeZero() {
    assertTrue(SignValue.isMaybeZero(SignValue.ZERO));
    assertTrue(SignValue.isMaybeZero(SignValue.ZERO_MINUS));
    assertTrue(SignValue.isMaybeZero(SignValue.ZERO_PLUS));
    assertTrue(SignValue.isMaybeZero(SignValue.TOP));

    assertFalse(SignValue.isMaybeZero(SignValue.BOTTOM));
    assertFalse(SignValue.isMaybeZero(SignValue.UNINITIALIZED_VALUE));
    assertFalse(SignValue.isMaybeZero(SignValue.PLUS_MINUS));
  }

  @Test
  public void testIsNegative() {
    assertTrue(SignValue.isNegative(SignValue.MINUS));
    assertFalse(SignValue.isNegative(SignValue.ZERO));
    assertFalse(SignValue.isNegative(SignValue.ZERO_MINUS));
    assertFalse(SignValue.isNegative(SignValue.PLUS_MINUS));
    assertFalse(SignValue.isNegative(SignValue.TOP));
    assertFalse(SignValue.isNegative(SignValue.UNINITIALIZED_VALUE));
  }

  @Test
  public void testIsMaybeNegative() {
    assertTrue(SignValue.isMaybeNegative(SignValue.MINUS));
    assertTrue(SignValue.isMaybeNegative(SignValue.ZERO_MINUS));
    assertTrue(SignValue.isMaybeNegative(SignValue.PLUS_MINUS));
    assertTrue(SignValue.isMaybeNegative(SignValue.TOP));

    assertFalse(SignValue.isMaybeNegative(SignValue.UNINITIALIZED_VALUE));
    assertFalse(SignValue.isMaybeNegative(SignValue.BOTTOM));
    assertFalse(SignValue.isMaybeNegative(SignValue.ZERO));
  }

  @Test
  public void testFromSigns() {
    assertEquals(SignValue.BOTTOM, SignValue.fromSigns(Set.of()));
    assertEquals(SignValue.MINUS, SignValue.fromSigns(Set.of(Sign.MINUS)));
    assertEquals(SignValue.ZERO, SignValue.fromSigns(Set.of(Sign.ZERO)));
    assertEquals(SignValue.ZERO_MINUS, SignValue.fromSigns(Set.of(Sign.ZERO, Sign.MINUS)));
    assertEquals(SignValue.PLUS, SignValue.fromSigns(Set.of(Sign.PLUS)));
    assertEquals(SignValue.PLUS_MINUS, SignValue.fromSigns(Set.of(Sign.PLUS, Sign.MINUS)));
    assertEquals(SignValue.ZERO_PLUS, SignValue.fromSigns(Set.of(Sign.PLUS, Sign.ZERO)));
    assertEquals(SignValue.TOP, SignValue.fromSigns(Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS)));
  }
}
