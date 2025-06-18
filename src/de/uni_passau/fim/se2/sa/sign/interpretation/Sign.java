package de.uni_passau.fim.se2.sa.sign.interpretation;

import java.util.HashSet;
import java.util.Set;

public enum Sign {
  MINUS, ZERO, PLUS, TOP;

  public static SignValue evaluateAdd(SignValue lhs, SignValue rhs) {
    if (lhs == SignValue.UNINITIALIZED_VALUE || rhs == SignValue.UNINITIALIZED_VALUE) {
      return SignValue.UNINITIALIZED_VALUE;
    }
    if (lhs == SignValue.BOTTOM || rhs == SignValue.BOTTOM) {
      return SignValue.BOTTOM;
    }

    Set<Sign> lhsSigns = SignValue.toSignSet(lhs);
    Set<Sign> rhsSigns = SignValue.toSignSet(rhs);
    Set<Sign> resultSigns = new HashSet<>();

    for (Sign l : lhsSigns) {
      for (Sign r : rhsSigns) {
        Sign result = addPair(l, r);
        resultSigns.addAll(SignValue.toSignSet(SignValue.fromSignSet(Set.of(result))));
      }
    }

    return SignValue.fromSignSet(resultSigns);
  }

  private static Sign addPair(Sign a, Sign b) {
    return switch (a) {
      case MINUS -> switch (b) {
        case MINUS, ZERO -> Sign.MINUS;
        case PLUS, TOP -> Sign.TOP;
      };
      case ZERO -> b;
      case PLUS -> switch (b) {
        case MINUS, TOP -> Sign.TOP;
        case ZERO, PLUS -> Sign.PLUS;
      };
      case TOP -> Sign.TOP;
    };
  }
}
