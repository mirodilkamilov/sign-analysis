package de.uni_passau.fim.se2.sa.sign.interpretation;

import com.google.common.base.Preconditions;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

public class SignTransferRelation implements TransferRelation {

  @Override
  public SignValue evaluate(final int pValue) {
    if (pValue == 0) {
      return SignValue.ZERO;
    } else if (pValue > 0) {
      return SignValue.PLUS;
    } else {
      return SignValue.MINUS;
    }
  }

  @Override
  public SignValue evaluate(final Operation pOperation, final SignValue pValue) {
    Preconditions.checkState(pOperation == Operation.NEG);
    Preconditions.checkNotNull(pValue);
    switch (pValue) {
      case BOTTOM -> {
        return SignValue.BOTTOM;
      }
      case MINUS -> {
        return SignValue.PLUS;
      }
      case ZERO -> {
        return SignValue.ZERO;
      }
      case ZERO_MINUS -> {
        return SignValue.ZERO_PLUS;
      }
      case PLUS -> {
        return SignValue.MINUS;
      }
      case PLUS_MINUS -> {
        return SignValue.PLUS_MINUS;
      }
      case ZERO_PLUS -> {
        return SignValue.ZERO_MINUS;
      }
      case TOP -> {
        return SignValue.TOP;
      }
      case UNINITIALIZED_VALUE -> {
        return SignValue.UNINITIALIZED_VALUE;
      }
      default -> throw new IllegalArgumentException("Unexpected sign value: " + pValue);
    }
  }

  @Override
  public SignValue evaluate(
      final Operation pOperation, final SignValue pLHS, final SignValue pRHS) {
    Preconditions.checkState(
        pOperation == Operation.ADD
            || pOperation == Operation.SUB
            || pOperation == Operation.MUL
            || pOperation == Operation.DIV);
    Preconditions.checkNotNull(pLHS);
    Preconditions.checkNotNull(pRHS);

    if (pLHS == SignValue.BOTTOM || pRHS == SignValue.BOTTOM) {
      return SignValue.BOTTOM;
    }
    if (pLHS == SignValue.UNINITIALIZED_VALUE || pRHS == SignValue.UNINITIALIZED_VALUE) {
      if (pOperation == Operation.ADD || pOperation == Operation.SUB) {
        return SignValue.TOP;
      }
      if (pOperation == Operation.MUL && (pLHS == SignValue.ZERO || pRHS == SignValue.ZERO)) {
        return SignValue.ZERO;
      }
      if (pOperation == Operation.DIV && pLHS == SignValue.ZERO) {
        return SignValue.ZERO;
      }
      if (pOperation == Operation.DIV && pRHS == SignValue.ZERO) {
        return SignValue.BOTTOM;
      }
      return SignValue.TOP;
    }

    Set<Sign> resultSet = new HashSet<>();
    for (Sign lSign : pLHS.getSigns()){
      for (Sign rSign : pRHS.getSigns()){
        Set<Sign> partialResult = switch (pOperation) {
          case ADD -> Sign.evaluateAdd(lSign, rSign);
          case SUB -> Sign.evaluateSub(lSign, rSign);
          case MUL -> Sign.evaluateMul(lSign, rSign);
          case DIV -> Sign.evaluateDiv(lSign, rSign);
          default -> throw new IllegalStateException("Unexpected operation " + pOperation.name());
        };
        resultSet.addAll(partialResult);
      }
    }

    return SignValue.fromSigns(resultSet);
  }

  public static Operation getOperationFromOpcode(int opcode) {
    switch (opcode) {
      case Opcodes.IADD -> {
        return Operation.ADD;
      }
      case Opcodes.ISUB -> {
        return Operation.SUB;
      }
      case Opcodes.IMUL -> {
        return Operation.MUL;
      }
      case Opcodes.IDIV -> {
        return Operation.DIV;
      }
      case Opcodes.INEG -> {
        return Operation.NEG;
      }
      default -> throw new RuntimeException("Not supported operation for " + opcode + " opcode");
    }
  }
}
