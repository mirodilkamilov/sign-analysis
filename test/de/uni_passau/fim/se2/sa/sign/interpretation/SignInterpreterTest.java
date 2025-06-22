package de.uni_passau.fim.se2.sa.sign.interpretation;

import de.uni_passau.fim.se2.sa.sign.ContextAwareSignAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.Frame;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SignInterpreterTest {
  private SignInterpreter si;
  private final String className = "MyTestClass";
  private Map<String, MethodNode> methods;

  @BeforeEach
  void setUp() {
    methods = mock(Map.class);
    si = spy(new SignInterpreter(className, methods));
  }

  @Test
  public void testNewValue_VoidType() {
    assertNull(si.newValue(Type.VOID_TYPE));
  }

  @Test
  public void testNewValue_OtherTypes() {
    assertEquals(SignValue.UNINITIALIZED_VALUE, si.newValue(Type.INT_TYPE));
    assertEquals(SignValue.UNINITIALIZED_VALUE, si.newValue(Type.DOUBLE_TYPE));
    assertEquals(SignValue.UNINITIALIZED_VALUE, si.newValue(Type.BOOLEAN_TYPE));
    assertEquals(SignValue.UNINITIALIZED_VALUE, si.newValue(Type.LONG_TYPE));
  }

  @Test
  public void testNewOperation_PushConst() throws AnalyzerException {
    assertEquals(SignValue.MINUS, si.newOperation(new InsnNode(Opcodes.ICONST_M1)));
    assertEquals(SignValue.ZERO, si.newOperation(new InsnNode(Opcodes.ICONST_0)));
    assertEquals(SignValue.PLUS, si.newOperation(new InsnNode(Opcodes.ICONST_1)));
    assertEquals(SignValue.PLUS, si.newOperation(new InsnNode(Opcodes.ICONST_2)));
    assertEquals(SignValue.PLUS, si.newOperation(new InsnNode(Opcodes.ICONST_3)));
    assertEquals(SignValue.PLUS, si.newOperation(new InsnNode(Opcodes.ICONST_4)));
    assertEquals(SignValue.PLUS, si.newOperation(new InsnNode(Opcodes.ICONST_5)));

    assertEquals(SignValue.TOP, si.newOperation(new InsnNode(Opcodes.LCONST_0)));
  }

  @Test
  public void testNewOperation_PushNonConst() throws AnalyzerException {
    assertEquals(SignValue.PLUS, si.newOperation(new IntInsnNode(Opcodes.BIPUSH, 10)));
    assertEquals(SignValue.MINUS, si.newOperation(new IntInsnNode(Opcodes.BIPUSH, -10)));
    assertEquals(SignValue.ZERO, si.newOperation(new IntInsnNode(Opcodes.BIPUSH, 0)));

    assertEquals(SignValue.PLUS, si.newOperation(new IntInsnNode(Opcodes.SIPUSH, 10)));
    assertEquals(SignValue.MINUS, si.newOperation(new IntInsnNode(Opcodes.SIPUSH, -10)));
    assertEquals(SignValue.ZERO, si.newOperation(new IntInsnNode(Opcodes.SIPUSH, 0)));

    assertEquals(SignValue.PLUS, si.newOperation(new LdcInsnNode(100000000)));
    assertEquals(SignValue.MINUS, si.newOperation(new LdcInsnNode(-100000000)));
    assertEquals(SignValue.ZERO, si.newOperation(new LdcInsnNode(0)));
    assertEquals(SignValue.TOP, si.newOperation(new LdcInsnNode(3.1415)));
  }

  @Test
  public void testCopyOperation() {
    assertEquals(SignValue.PLUS, si.copyOperation(new IntInsnNode(Opcodes.BIPUSH, 10), SignValue.PLUS));
    assertEquals(SignValue.MINUS, si.copyOperation(new InsnNode(Opcodes.ICONST_M1), SignValue.MINUS));
    assertEquals(SignValue.ZERO, si.copyOperation(new InsnNode(Opcodes.ICONST_0), SignValue.ZERO));
  }

  @Test
  public void testUnaryOperation_ReturnInstructions() throws AnalyzerException {
    assertEquals(SignValue.PLUS_MINUS, si.unaryOperation(new InsnNode(Opcodes.IRETURN), SignValue.PLUS_MINUS));
    assertEquals(SignValue.TOP, si.unaryOperation(new InsnNode(Opcodes.DRETURN), SignValue.TOP));
    assertEquals(SignValue.ZERO_MINUS, si.unaryOperation(new InsnNode(Opcodes.RETURN), SignValue.ZERO_MINUS));
  }

  @Test
  public void testUnaryOperation_INEG() throws AnalyzerException {
    assertEquals(SignValue.BOTTOM, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.BOTTOM));
    assertEquals(SignValue.PLUS, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.MINUS));
    assertEquals(SignValue.ZERO, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.ZERO));
    assertEquals(SignValue.ZERO_PLUS, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.ZERO_MINUS));
    assertEquals(SignValue.MINUS, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.PLUS));
    assertEquals(SignValue.PLUS_MINUS, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.PLUS_MINUS));
    assertEquals(SignValue.ZERO_MINUS, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.ZERO_PLUS));
    assertEquals(SignValue.TOP, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.TOP));
    assertEquals(SignValue.UNINITIALIZED_VALUE, si.unaryOperation(new InsnNode(Opcodes.INEG), SignValue.UNINITIALIZED_VALUE));
  }

  @Test
  public void testUnaryOperation_Others() throws AnalyzerException {
    assertEquals(SignValue.TOP, si.unaryOperation(new IincInsnNode(1, -1), SignValue.PLUS));
    assertEquals(SignValue.MINUS, si.unaryOperation(new IincInsnNode(1, -1), SignValue.MINUS));
    assertEquals(SignValue.PLUS, si.unaryOperation(new IincInsnNode(1, 0), SignValue.PLUS));
    assertEquals(SignValue.PLUS, si.unaryOperation(new IincInsnNode(1, 1), SignValue.PLUS));

    assertEquals(SignValue.BOTTOM, si.unaryOperation(new InsnNode(Opcodes.IXOR), SignValue.BOTTOM));
    assertEquals(SignValue.ZERO_MINUS, si.unaryOperation(new InsnNode(Opcodes.DNEG), SignValue.ZERO_MINUS));
  }

  @Test
  public void testBinaryOperation() {
    assertEquals(SignValue.TOP, si.binaryOperation(new InsnNode(Opcodes.IADD), SignValue.PLUS, SignValue.MINUS));
    assertEquals(SignValue.MINUS, si.binaryOperation(new InsnNode(Opcodes.IADD), SignValue.ZERO_MINUS, SignValue.MINUS));

    assertEquals(SignValue.MINUS, si.binaryOperation(new InsnNode(Opcodes.ISUB), SignValue.MINUS, SignValue.PLUS));
    assertEquals(SignValue.PLUS, si.binaryOperation(new InsnNode(Opcodes.ISUB), SignValue.ZERO_PLUS, SignValue.MINUS));

    assertEquals(SignValue.ZERO_MINUS, si.binaryOperation(new InsnNode(Opcodes.IMUL), SignValue.ZERO_PLUS, SignValue.MINUS));
    assertEquals(SignValue.BOTTOM, si.binaryOperation(new InsnNode(Opcodes.IMUL), SignValue.TOP, SignValue.BOTTOM));

    assertEquals(SignValue.BOTTOM, si.binaryOperation(new InsnNode(Opcodes.IDIV), SignValue.TOP, SignValue.ZERO));
    assertEquals(SignValue.BOTTOM, si.binaryOperation(new InsnNode(Opcodes.IDIV), SignValue.UNINITIALIZED_VALUE, SignValue.ZERO));

    assertEquals(SignValue.TOP, si.binaryOperation(new InsnNode(Opcodes.DADD), SignValue.ZERO_PLUS, SignValue.MINUS));
    assertEquals(SignValue.TOP, si.binaryOperation(new InsnNode(Opcodes.LDIV), SignValue.ZERO_PLUS, SignValue.ZERO));
  }

  @Test
  public void testTernaryOperation() {
    assertNull(si.ternaryOperation(new InsnNode(Opcodes.IASTORE), SignValue.PLUS, SignValue.MINUS, SignValue.ZERO));
    assertNull(si.ternaryOperation(new InsnNode(Opcodes.DASTORE), SignValue.PLUS_MINUS, SignValue.MINUS, SignValue.ZERO_MINUS));
  }

  @Test
  public void testTNaryOperation_WithNonMethodInstruction() {
    assertEquals(SignValue.TOP, si.naryOperation(new InsnNode(Opcodes.IASTORE), List.of(SignValue.PLUS, SignValue.ZERO)));
  }

  @Test
  void testNaryOperation_MethodNotFound() {
    MethodInsnNode insn = new MethodInsnNode(Opcodes.INVOKESTATIC, "SomeClass", "foo", "()I", false);
    when(methods.get("foo:()I")).thenReturn(null);

    SignValue result = si.naryOperation(insn, List.of());
    assertEquals(SignValue.TOP, result);
  }

  // Below tests are generated by LLM
  // Please refer to LLM/code_queries.txt
  @Test
  void testNaryOperation_ValidMethod_ReturnsMergedValue() throws Exception {
    MethodInsnNode insn = new MethodInsnNode(Opcodes.INVOKESTATIC, "MyTestClass", "foo", "()I", false);
    MethodNode methodNode = new MethodNode();
    methodNode.instructions = new InsnList();

    // Simulate: ICONST_1; IRETURN
    methodNode.instructions.add(new InsnNode(Opcodes.ICONST_1));
    methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));

    when(methods.get("foo:()I")).thenReturn(methodNode);

    // Spy analyzer to inject fake frames
    ContextAwareSignAnalyzer analyzerMock = mock(ContextAwareSignAnalyzer.class);
    Frame<SignValue>[] fakeFrames = new Frame[2];
    fakeFrames[1] = mock(Frame.class);
    when(fakeFrames[1].getStackSize()).thenReturn(1);
    when(fakeFrames[1].getStack(0)).thenReturn(SignValue.PLUS);

    doReturn(analyzerMock).when(si).createAnalyzer(any(), any(), any(), anyBoolean());
    when(analyzerMock.analyze(className, methodNode)).thenReturn(fakeFrames);

    // Provide custom implementation of merge
    doReturn(SignValue.PLUS).when(si).merge(SignValue.BOTTOM, SignValue.PLUS);
    SignValue result = si.naryOperation(insn, List.of());

    assertEquals(SignValue.PLUS, result);
  }

  @Test
  void testNaryOperation_AnalyzerThrows_ReturnsTOP() throws Exception {
    MethodInsnNode insn = new MethodInsnNode(Opcodes.INVOKESTATIC, "SomeClass", "bar", "()I", false);
    MethodNode methodNode = new MethodNode();
    methodNode.instructions = new InsnList();
    methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
    when(methods.get("bar:()I")).thenReturn(methodNode);

    ContextAwareSignAnalyzer analyzerMock = mock(ContextAwareSignAnalyzer.class);
    doReturn(analyzerMock).when(si).createAnalyzer(any(), any(), any(), anyBoolean());
    doThrow(new AnalyzerException(null, "")).when(analyzerMock).analyze(className, methodNode);

    SignValue result = si.naryOperation(insn, List.of());
    assertEquals(SignValue.TOP, result);
  }

  @Test
  void testMerge_sameValues_returnsSame() {
    SignValue value = SignValue.PLUS;
    assertSame(value, si.merge(value, value));
  }

  @Test
  void testMerge_firstIsBottom_returnsSecond() {
    SignValue result = si.merge(SignValue.BOTTOM, SignValue.MINUS);
    assertSame(SignValue.MINUS, result);
  }

  @Test
  void testMerge_secondIsBottom_returnsFirst() {
    SignValue result = si.merge(SignValue.ZERO, SignValue.BOTTOM);
    assertSame(SignValue.ZERO, result);
  }

  @Test
  void testMerge_firstIsUninitialized_returnsSecond() {
    SignValue result = si.merge(SignValue.UNINITIALIZED_VALUE, SignValue.PLUS);
    assertSame(SignValue.PLUS, result);
  }

  @Test
  void testMerge_secondIsUninitialized_returnsFirst() {
    SignValue result = si.merge(SignValue.MINUS, SignValue.UNINITIALIZED_VALUE);
    assertSame(SignValue.MINUS, result);
  }

  @Test
  void testMerge_differentValues_callsJoin() {
    SignValue val1 = mock(SignValue.class);
    SignValue val2 = mock(SignValue.class);
    SignValue joined = mock(SignValue.class);

    when(val1.join(val2)).thenReturn(joined);

    SignValue result = si.merge(val1, val2);

    verify(val1).join(val2);
    assertSame(joined, result);
  }
}