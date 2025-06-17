package de.uni_passau.fim.se2.sa.sign.interpretation;

import com.google.common.base.Preconditions;
import de.uni_passau.fim.se2.sa.sign.lattice.SignLattice;
import org.objectweb.asm.Opcodes;

public class SignTransferRelation implements TransferRelation {

  @Override
  public SignValue evaluate(final int pValue) {
    // TODO Implement me
    throw new UnsupportedOperationException("Implement me");
  }

  @Override
  public SignValue evaluate(final Operation pOperation, final SignValue pValue) {
    Preconditions.checkState(pOperation == Operation.NEG);
    Preconditions.checkNotNull(pValue);
    // TODO Implement me
    throw new UnsupportedOperationException("Implement me");
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

    switch (pOperation) {
      case ADD -> {
        if (pLHS == pRHS) {
          return pLHS;
        } else {
          return new SignLattice().join(pLHS, pRHS);
        }
      }
      default -> throw new RuntimeException("Rest not implemented yet");
    }
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
