package de.uni_passau.fim.se2.sa.sign;

import de.uni_passau.fim.se2.sa.sign.interpretation.SignValue;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.Interpreter;

import java.util.List;

public class ContextAwareSignAnalyzer extends Analyzer<SignValue> {
  private final List<? extends SignValue> argumentSigns;
  private final String methodDesc;
  private final boolean isStatic;

  public ContextAwareSignAnalyzer(
          Interpreter<SignValue> interpreter,
          List<? extends SignValue> argumentSigns,
          String methodDesc,
          boolean isStatic) {
    super(interpreter);
    this.argumentSigns = argumentSigns;
    this.methodDesc = methodDesc;
    this.isStatic = isStatic;
  }

  @Override
  protected Frame<SignValue> newFrame(int nLocals, int nStack) {
    return new Frame<>(nLocals, nStack);
  }

  @Override
  protected void init(final String owner, final MethodNode method) throws AnalyzerException {
    super.init(owner, method);
    Frame<SignValue> frame = getFrames()[0];

    Type[] args = Type.getArgumentTypes(methodDesc);
    int localIndex = 0;

    if (!isStatic) {
      localIndex++;
    }

    for (Type ignored : args) {
      frame.setLocal(localIndex, argumentSigns.get(localIndex));
      localIndex++;
    }
  }
}