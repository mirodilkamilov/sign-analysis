package de.uni_passau.fim.se2.sa.sign.interpretation;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;
import java.util.Map;

import static de.uni_passau.fim.se2.sa.sign.interpretation.SignTransferRelation.getOperationFromOpcode;
import static de.uni_passau.fim.se2.sa.sign.interpretation.TransferRelation.Operation;

public class SignInterpreter extends Interpreter<SignValue> implements Opcodes {

  private final String pClassName;
  private final Map<String, MethodNode> methods;

  public SignInterpreter(final String pClassName, final Map<String, MethodNode> methods) {
    this(ASM9, pClassName, methods);
  }

  /**
   * Constructs a new {@link Interpreter}.
   *
   * @param pAPI The ASM API version supported by this interpreter. Must be one of {@link #ASM4},
   *     {@link #ASM5}, {@link #ASM6}, {@link #ASM7}, {@link #ASM8}, or {@link #ASM9}
   * @param pClassName The name of the class that contains the method to be analyzed.
   * @param methods All methods of the class that contains the method to be analyzed.
   */
  protected SignInterpreter(final int pAPI, final String pClassName, final Map<String, MethodNode> methods) {
    super(pAPI);
    if (getClass() != SignInterpreter.class) {
      throw new IllegalStateException();
    }

    this.pClassName = pClassName;
    this.methods = methods;
  }

  /** {@inheritDoc} */
  @Override
  public SignValue newValue(final Type pType) {
    return SignValue.UNINITIALIZED_VALUE;
  }

  /** {@inheritDoc} */
  @Override
  public SignValue newOperation(final AbstractInsnNode pInstruction) throws AnalyzerException {
    switch (pInstruction.getOpcode()) {
      case Opcodes.ICONST_M1 -> {
        return SignValue.MINUS;
      }
      case Opcodes.ICONST_0 -> {
        return SignValue.ZERO;
      }
      case Opcodes.ICONST_1,
           Opcodes.ICONST_2,
           Opcodes.ICONST_3,
           Opcodes.ICONST_4,
           Opcodes.ICONST_5 -> {
        return SignValue.PLUS;
      }
      case Opcodes.BIPUSH,
           Opcodes.SIPUSH -> {
        int num = ((IntInsnNode) pInstruction).operand;
        return new SignTransferRelation().evaluate(num);
      }
      case Opcodes.LDC -> {
        Object cst = ((LdcInsnNode) pInstruction).cst;
        if (cst instanceof Integer intNum) {
          return new SignTransferRelation().evaluate(intNum);
        }
        return SignValue.TOP;
      }
    }
    return SignValue.TOP;
  }

  /** {@inheritDoc} */
  @Override
  public SignValue copyOperation(final AbstractInsnNode pInstruction, final SignValue pValue) {
    return pValue;
  }

  /** {@inheritDoc} */
  @Override
  public SignValue unaryOperation(final AbstractInsnNode pInstruction, final SignValue pValue)
      throws AnalyzerException {
    Operation operation = getOperationFromOpcode(pInstruction.getOpcode());
    return new SignTransferRelation().evaluate(operation, pValue);
  }

  /** {@inheritDoc} */
  @Override
  public SignValue binaryOperation(
      final AbstractInsnNode pInstruction, final SignValue pValue1, final SignValue pValue2) {
    Operation operation = getOperationFromOpcode(pInstruction.getOpcode());
    return new SignTransferRelation().evaluate(operation, pValue1, pValue2);
  }

  /** {@inheritDoc} */
  @Override
  public SignValue ternaryOperation(
      final AbstractInsnNode pInstruction,
      final SignValue pValue1,
      final SignValue pValue2,
      final SignValue pValue3) {
    return null; // Nothing to do.
  }

  /** {@inheritDoc} */
  @Override
  public SignValue naryOperation(
      final AbstractInsnNode pInstruction, final List<? extends SignValue> pValues) {
    return pValues.getFirst();
  }

  /** {@inheritDoc} */
  @Override
  public void returnOperation(
      final AbstractInsnNode pInstruction, final SignValue pValue, final SignValue pExpected) {
    // Nothing to do.
  }

  /** {@inheritDoc} */
  @Override
  public SignValue merge(final SignValue pValue1, final SignValue pValue2) {
    if (pValue1 == pValue2)
      return pValue1;
    return pValue1.join(pValue2);
  }
}
