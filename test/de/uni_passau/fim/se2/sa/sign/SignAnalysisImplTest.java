package de.uni_passau.fim.se2.sa.sign;

import com.google.common.collect.SortedSetMultimap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SignAnalysisImplTest {
  private final SignAnalysisImpl analysis = new SignAnalysisImpl();
  private final String pClassName = "de.uni_passau.fim.se2.sa.examples.PublicFunctional";

  @Test
  void testAnalyse_validInput_returnsAnalysis() throws Exception {
    SortedSetMultimap<Integer, AnalysisResult> result =
            analysis.analyse(pClassName, "loop0:()V");

    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test
  void testAnalyse_classNotFound_throwsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () ->
            analysis.analyse("non.existing.Class", "someMethod:(I)V"));

    assertTrue(e.getMessage().contains("not found"));
  }

  @Test
  void testAnalyse_invalidMethodFormat_throwsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () ->
            analysis.analyse(pClassName, "badMethodFormat"));

    assertTrue(e.getMessage().contains("Invalid method name"));
  }

  @Test
  void testAnalyse_methodNotFound_throwsIllegalArgumentException() {
    Exception e = assertThrows(IllegalArgumentException.class, () ->
            analysis.analyse(pClassName, "nonexistentMethod:(I)V"));

    assertTrue(e.getMessage().contains("Method not found"));
  }
}
