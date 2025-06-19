package de.uni_passau.fim.se2.sa.sign.interpretation;

import java.util.Set;

public enum Sign {
  MINUS, ZERO, PLUS;

  // Set implementation of addition is inspired by LLM
  // Please refer to code_queries.txt
  public static Set<Sign> evaluateAdd(Sign lhs, Sign rhs) {
    return switch (lhs) {
      case MINUS -> switch (rhs) {
        case MINUS, ZERO -> Set.of(Sign.MINUS);
        case PLUS -> Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS);
      };
      case ZERO -> Set.of(rhs);
      case PLUS -> switch (rhs) {
        case MINUS -> Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS);
        case ZERO, PLUS -> Set.of(Sign.PLUS);
      };
    };
  }

  public static Set<Sign> evaluateSub(Sign lhs, Sign rhs) {
    return switch (lhs) {
      case MINUS -> switch (rhs) {
        case MINUS -> Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS);
        case PLUS, ZERO -> Set.of(Sign.MINUS);
      };
      case ZERO -> Set.of(negateSign(rhs));
      case PLUS -> switch (rhs) {
        case ZERO, MINUS -> Set.of(Sign.PLUS);
        case PLUS -> Set.of(Sign.MINUS, Sign.ZERO, Sign.PLUS);
      };
    };
  }

  public static Set<Sign> evaluateMul(Sign lhs, Sign rhs) {
    return switch (lhs) {
      case MINUS -> Set.of(negateSign(rhs));
      case ZERO -> Set.of(Sign.ZERO);
      case PLUS -> switch (rhs) {
        case MINUS -> Set.of(Sign.MINUS);
        case ZERO -> Set.of(Sign.ZERO);
        case PLUS -> Set.of(Sign.PLUS);
      };
    };
  }

  public static Set<Sign> evaluateDiv(Sign lhs, Sign rhs) {
    return switch (lhs) {
      case MINUS -> switch (rhs) {
        case MINUS -> Set.of(Sign.PLUS);
        case ZERO -> Set.of();
        case PLUS -> Set.of(Sign.MINUS);
      };
      case ZERO -> switch (rhs) {
        case MINUS, PLUS -> Set.of(Sign.ZERO);
        case ZERO -> Set.of();
      };
      case PLUS -> switch (rhs) {
        case MINUS -> Set.of(Sign.MINUS);
        case ZERO -> Set.of();
        case PLUS -> Set.of(Sign.PLUS);
      };
    };
  }

  private static Sign negateSign(Sign sign) {
    return switch (sign) {
      case ZERO -> Sign.ZERO;
      case MINUS -> Sign.PLUS;
      case PLUS -> Sign.MINUS;
    };
  }
}
