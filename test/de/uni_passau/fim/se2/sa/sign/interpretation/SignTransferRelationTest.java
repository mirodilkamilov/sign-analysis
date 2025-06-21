package de.uni_passau.fim.se2.sa.sign.interpretation;

import de.uni_passau.fim.se2.sa.sign.interpretation.TransferRelation.Operation;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;

import static de.uni_passau.fim.se2.sa.sign.interpretation.SignTransferRelation.*;
import static org.junit.jupiter.api.Assertions.*;

public class SignTransferRelationTest {
  private final SignTransferRelation str = new SignTransferRelation();

  @Test
  public void testEvaluateOneParameter_IntParameterType() throws NoSuchMethodException {
    Method method = SignTransferRelation.class.getMethod("evaluate", int.class);
    assertEquals(SignValue.class, method.getReturnType());
  }

  @Test
  public void testEvaluateOneParameter_CorrectValues() {
    assertEquals(SignValue.ZERO, str.evaluate(0));
    assertEquals(SignValue.PLUS, str.evaluate(10));
    assertEquals(SignValue.MINUS, str.evaluate(-10));
  }

  @Test
  public void testEvaluateNegate_PreConditions() {
    assertThrowsExactly(
            IllegalStateException.class,
            () -> str.evaluate(Operation.ADD, SignValue.TOP)
    );
    assertThrowsExactly(
            NullPointerException.class,
            () -> str.evaluate(Operation.NEG, null)
    );
  }

  @Test
  public void testEvaluateNegate_CorrectValues() {
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.NEG, SignValue.BOTTOM));
    assertEquals(SignValue.PLUS, str.evaluate(Operation.NEG, SignValue.MINUS));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.NEG, SignValue.ZERO));
    assertEquals(SignValue.ZERO_PLUS, str.evaluate(Operation.NEG, SignValue.ZERO_MINUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.NEG, SignValue.PLUS));
    assertEquals(SignValue.PLUS_MINUS, str.evaluate(Operation.NEG, SignValue.PLUS_MINUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.NEG, SignValue.ZERO_PLUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.NEG, SignValue.TOP));
    assertEquals(SignValue.UNINITIALIZED_VALUE, str.evaluate(Operation.NEG, SignValue.UNINITIALIZED_VALUE));
  }

  @Test
  public void testEvaluateBinary_PreConditions() {
    assertThrowsExactly(
            IllegalStateException.class,
            () -> str.evaluate(Operation.NEG, SignValue.PLUS, SignValue.MINUS)
    );
    assertThrowsExactly(
            NullPointerException.class,
            () -> str.evaluate(Operation.ADD, null, SignValue.TOP)
    );
    assertThrowsExactly(
            NullPointerException.class,
            () -> str.evaluate(Operation.ADD, SignValue.TOP, null)
    );
  }

  @Test
  public void testEvaluateBinary_BottomUninitializedZeroValues() {
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.ADD, SignValue.BOTTOM, SignValue.TOP));
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.SUB, SignValue.TOP, SignValue.BOTTOM));

    assertEquals(SignValue.TOP, str.evaluate(Operation.ADD, SignValue.PLUS, SignValue.UNINITIALIZED_VALUE));
    assertEquals(SignValue.TOP, str.evaluate(Operation.SUB, SignValue.UNINITIALIZED_VALUE, SignValue.PLUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.ADD, SignValue.UNINITIALIZED_VALUE, SignValue.UNINITIALIZED_VALUE));

    assertEquals(SignValue.ZERO, str.evaluate(Operation.MUL, SignValue.UNINITIALIZED_VALUE, SignValue.ZERO));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.MUL, SignValue.ZERO, SignValue.UNINITIALIZED_VALUE));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.MUL, SignValue.UNINITIALIZED_VALUE, SignValue.ZERO));
    assertEquals(SignValue.TOP, str.evaluate(Operation.MUL, SignValue.UNINITIALIZED_VALUE, SignValue.PLUS_MINUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.MUL, SignValue.UNINITIALIZED_VALUE, SignValue.UNINITIALIZED_VALUE));

    assertEquals(SignValue.ZERO, str.evaluate(Operation.DIV, SignValue.ZERO, SignValue.UNINITIALIZED_VALUE));
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.DIV, SignValue.UNINITIALIZED_VALUE, SignValue.ZERO));
    assertEquals(SignValue.TOP, str.evaluate(Operation.DIV, SignValue.ZERO_MINUS, SignValue.UNINITIALIZED_VALUE));
    assertEquals(SignValue.TOP, str.evaluate(Operation.DIV, SignValue.UNINITIALIZED_VALUE, SignValue.UNINITIALIZED_VALUE));
  }

  @Test
  public void testEvaluateBinary_CorrectValuesAdd() {
    assertEquals(SignValue.PLUS_MINUS, str.evaluate(Operation.ADD, SignValue.ZERO, SignValue.PLUS_MINUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.ADD, SignValue.ZERO_MINUS, SignValue.PLUS_MINUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.ADD, SignValue.MINUS, SignValue.MINUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.ADD, SignValue.MINUS, SignValue.ZERO_PLUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.ADD, SignValue.MINUS, SignValue.ZERO_MINUS));
    assertEquals(SignValue.PLUS, str.evaluate(Operation.ADD, SignValue.PLUS, SignValue.ZERO_PLUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.ADD, SignValue.PLUS_MINUS, SignValue.PLUS_MINUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.ADD, SignValue.ZERO_MINUS, SignValue.ZERO_MINUS));
  }

  @Test
  public void testEvaluateBinary_CorrectValuesSub() {
    assertEquals(SignValue.ZERO, str.evaluate(Operation.SUB, SignValue.ZERO, SignValue.ZERO));
    assertEquals(SignValue.PLUS, str.evaluate(Operation.SUB, SignValue.PLUS, SignValue.MINUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.SUB, SignValue.MINUS, SignValue.PLUS));
    assertEquals(SignValue.PLUS_MINUS, str.evaluate(Operation.SUB, SignValue.PLUS_MINUS, SignValue.ZERO));
    assertEquals(SignValue.PLUS_MINUS, str.evaluate(Operation.SUB, SignValue.ZERO, SignValue.PLUS_MINUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.SUB, SignValue.ZERO_MINUS, SignValue.ZERO_PLUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.SUB, SignValue.ZERO_MINUS, SignValue.PLUS));
    assertEquals(SignValue.ZERO_PLUS, str.evaluate(Operation.SUB, SignValue.ZERO_PLUS, SignValue.ZERO_MINUS));
  }

  @Test
  public void testEvaluateBinary_CorrectValuesMul() {
    assertEquals(SignValue.ZERO, str.evaluate(Operation.MUL, SignValue.ZERO, SignValue.PLUS));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.MUL, SignValue.MINUS, SignValue.ZERO));
    assertEquals(SignValue.PLUS, str.evaluate(Operation.MUL, SignValue.PLUS, SignValue.PLUS));
    assertEquals(SignValue.PLUS, str.evaluate(Operation.MUL, SignValue.MINUS, SignValue.MINUS));
    assertEquals(SignValue.MINUS, str.evaluate(Operation.MUL, SignValue.PLUS, SignValue.MINUS));
    assertEquals(SignValue.PLUS_MINUS, str.evaluate(Operation.MUL, SignValue.PLUS_MINUS, SignValue.PLUS_MINUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.MUL, SignValue.ZERO_PLUS, SignValue.ZERO_MINUS));
  }

  @Test
  public void testEvaluateBinary_CorrectValuesDiv() {
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.DIV, SignValue.ZERO, SignValue.ZERO));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.DIV, SignValue.ZERO, SignValue.PLUS));
    assertEquals(SignValue.ZERO, str.evaluate(Operation.DIV, SignValue.ZERO, SignValue.MINUS));
    assertEquals(SignValue.ZERO_PLUS, str.evaluate(Operation.DIV, SignValue.PLUS, SignValue.PLUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.DIV, SignValue.PLUS, SignValue.MINUS));
    assertEquals(SignValue.ZERO_PLUS, str.evaluate(Operation.DIV, SignValue.MINUS, SignValue.MINUS));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.DIV, SignValue.MINUS, SignValue.PLUS));
    assertEquals(SignValue.TOP, str.evaluate(Operation.DIV, SignValue.PLUS_MINUS, SignValue.PLUS_MINUS));
    assertEquals(SignValue.BOTTOM, str.evaluate(Operation.DIV, SignValue.MINUS, SignValue.ZERO));
    assertEquals(SignValue.ZERO_MINUS, str.evaluate(Operation.DIV, SignValue.PLUS, SignValue.ZERO_MINUS));
  }

  @Test
  public void testGetOperationFromOpcode() {
    assertEquals(Operation.ADD, getOperationFromOpcode(Opcodes.IADD));
    assertEquals(Operation.SUB, getOperationFromOpcode(Opcodes.ISUB));
    assertEquals(Operation.MUL, getOperationFromOpcode(Opcodes.IMUL));
    assertEquals(Operation.DIV, getOperationFromOpcode(Opcodes.IDIV));
    assertEquals(Operation.NEG, getOperationFromOpcode(Opcodes.INEG));
  }

  @Test
  public void testGetOperationFromOpcode_UnsupportedOpcode() {
    int unsupportedOpcode = Opcodes.IRETURN;
    RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> getOperationFromOpcode(unsupportedOpcode)
    );
    assertTrue(exception.getMessage().contains("Not supported operation"));
  }
}
