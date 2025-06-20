package de.uni_passau.fim.se2.sa.sign;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;
import de.uni_passau.fim.se2.sa.sign.interpretation.SignInterpreter;
import de.uni_passau.fim.se2.sa.sign.interpretation.SignValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignAnalysisImpl implements SignAnalysis {

  @Override
  public SortedSetMultimap<Integer, AnalysisResult> analyse(
          final String pClassName, final String pMethodName) throws AnalyzerException, IOException {
    String classPath = pClassName;
    if (classPath.endsWith(".class")) {
      classPath = classPath.substring(0, classPath.length() - ".class".length());
    }
    if (classPath.contains(".")) {
      classPath = classPath.replace(".", "/");
    }
    classPath += ".class";

    InputStream classStream = getClass().getClassLoader().getResourceAsStream(classPath);
    if (classStream == null) {
      throw new IllegalArgumentException("Invalid class name: Class file \"" + classPath + "\" not found on classpath.");
    }

    Pattern pattern = Pattern.compile("^(<\\w+>|\\w+):(\\([\\w/\\[\\];]*\\)[\\w/\\[\\];]+)$");
    Matcher matcher = pattern.matcher(pMethodName);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid method name: should be in name:descriptor format, e.g., addTwoIntegers:(II)I");
    }

    byte[] classBytes = classStream.readAllBytes();
    ClassReader reader = new ClassReader(classBytes);

    ClassNode classNode = new ClassNode();
    reader.accept(classNode, 0);

    Map<String, MethodNode> methods = new HashMap<>();
    for (MethodNode method : classNode.methods) {
      String key = method.name + ":" + method.desc;
      methods.put(key, method);
    }
    MethodNode method = methods.get(pMethodName);
    if (method == null) {
      throw new IllegalArgumentException("Method not found: Cannot find \"" + pMethodName + "in \"" + pClassName + "\" class");
    }

    SignInterpreter interpreter = new SignInterpreter(pClassName, methods);
    Analyzer<SignValue> analyzer = new Analyzer<>(interpreter);
    Frame<SignValue>[] frames = analyzer.analyze(classNode.name, method);

    List<Pair<AbstractInsnNode, Frame<SignValue>>> pairs = new ArrayList<>();
    for (int i = 0; i < method.instructions.size(); i++) {
      AbstractInsnNode insn = method.instructions.get(i);
      Frame<SignValue> frame = frames[i];

      if (frame != null) {
        pairs.add(new Pair<>(insn, frame));
      }
    }

    return extractAnalysisResults(pairs);
  }

  /**
   * Extracts the analysis results from the given pairs of instructions and frames.
   *
   * <p>The result is a {@link SortedSetMultimap} that maps line numbers to the analysis results.
   * For each line number, there can be multiple analysis results.  The method expects a list of
   * pairs of instructions and frames.  The instructions are expected to be in the same order as
   * they are in the method.  The frames are expected to be the frames that are computed for the
   * instructions.  The method will extract the analysis results from the frames and map them to
   * the line numbers of the instructions.
   *
   * @param pPairs The pairs of instructions and frames.
   * @return The analysis results.
   */
  private SortedSetMultimap<Integer, AnalysisResult> extractAnalysisResults(
      final List<Pair<AbstractInsnNode, Frame<SignValue>>> pPairs) {
    final SortedSetMultimap<Integer, AnalysisResult> result = TreeMultimap.create();
    int lineNumber = -1;

    for (final Pair<AbstractInsnNode, Frame<SignValue>> pair : pPairs) {
      final AbstractInsnNode instruction = pair.key();
      final Frame<SignValue> frame = pair.value();
      if (instruction instanceof LineNumberNode lineNumberNode) {
        lineNumber = lineNumberNode.line;
      }

      if (isDivByZero(instruction, frame)) {
        result.put(lineNumber, AnalysisResult.DIVISION_BY_ZERO);
      } else if (isMaybeDivByZero(instruction, frame)) {
        result.put(lineNumber, AnalysisResult.MAYBE_DIVISION_BY_ZERO);
      }

      if (isNegativeArrayIndex(instruction, frame)) {
        result.put(lineNumber, AnalysisResult.NEGATIVE_ARRAY_INDEX);
      } else if (isMaybeNegativeArrayIndex(instruction, frame)) {
        result.put(lineNumber, AnalysisResult.MAYBE_NEGATIVE_ARRAY_INDEX);
      }
    }

    return result;
  }

  private boolean isDivByZero(final AbstractInsnNode pInstruction, final Frame<SignValue> pFrame) {
    int opcode = pInstruction.getOpcode();
    if (opcode != Opcodes.IDIV) {
      return false;
    }

    int stackSize = pFrame.getStackSize();
    SignValue pRHS = pFrame.getStack(stackSize - 1);
    return SignValue.isZero(pRHS);
  }

  private boolean isMaybeDivByZero(
      final AbstractInsnNode pInstruction, final Frame<SignValue> pFrame) {
    int opcode = pInstruction.getOpcode();
    if (opcode != Opcodes.IDIV) {
      return false;
    }

    int stackSize = pFrame.getStackSize();
    SignValue pRHS = pFrame.getStack(stackSize - 1);
    return SignValue.isMaybeZero(pRHS);
  }

  private boolean isNegativeArrayIndex(
      final AbstractInsnNode pInstruction, final Frame<SignValue> pFrame) {
    int opcode = pInstruction.getOpcode();
    if (opcode != Opcodes.IALOAD) {
      return false;
    }

    int stackSize = pFrame.getStackSize();
    SignValue pRHS = pFrame.getStack(stackSize - 1);
    return SignValue.isNegative(pRHS);
  }

  private boolean isMaybeNegativeArrayIndex(
      final AbstractInsnNode pInstruction, final Frame<SignValue> pFrame) {
    int opcode = pInstruction.getOpcode();
    if (opcode != Opcodes.IALOAD) {
      return false;
    }

    int stackSize = pFrame.getStackSize();
    SignValue pRHS = pFrame.getStack(stackSize - 1);
    return SignValue.isMaybeNegative(pRHS);
  }

  private record Pair<K, V>(K key, V value) {

    @Override
    public String toString() {
      return "Pair{key=" + key + ", value=" + value + '}';
    }
  }
}
