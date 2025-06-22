package de.uni_passau.fim.se2.sa.sign.lattice;

import de.uni_passau.fim.se2.sa.sign.interpretation.SignValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class SignLatticeTest {
  private final SignLattice lattice = new SignLattice();

  @Test
  void testTop_returnsTop() {
    assertSame(SignValue.TOP, lattice.top());
  }

  @Test
  void testBottom_returnsBottom() {
    assertSame(SignValue.BOTTOM, lattice.bottom());
  }

  @Test
  void testJoin_delegatesToSignValueJoin() {
    SignValue a = mock(SignValue.class);
    SignValue b = mock(SignValue.class);
    SignValue joined = mock(SignValue.class);

    when(a.join(b)).thenReturn(joined);

    SignValue result = lattice.join(a, b);

    verify(a).join(b);
    assertSame(joined, result);
  }

  @Test
  void testIsLessOrEqual_delegatesToSignValue() {
    SignValue a = mock(SignValue.class);
    SignValue b = mock(SignValue.class);

    when(a.isLessOrEqual(b)).thenReturn(true);

    boolean result = lattice.isLessOrEqual(a, b);

    verify(a).isLessOrEqual(b);
    assertTrue(result);
  }
}
