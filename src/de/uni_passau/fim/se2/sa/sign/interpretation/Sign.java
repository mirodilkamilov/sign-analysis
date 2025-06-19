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
}
