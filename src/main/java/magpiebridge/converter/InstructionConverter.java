package magpiebridge.converter;

import com.ibm.wala.cast.ir.ssa.AssignInstruction;
import com.ibm.wala.cast.ir.ssa.AstAssertInstruction;
import com.ibm.wala.cast.ir.ssa.AstLexicalAccess.Access;
import com.ibm.wala.cast.ir.ssa.AstLexicalRead;
import com.ibm.wala.cast.ir.ssa.AstLexicalWrite;
import com.ibm.wala.cast.ir.ssa.CAstBinaryOp;
import com.ibm.wala.cast.java.ssa.AstJavaInvokeInstruction;
import com.ibm.wala.cast.java.ssa.EnclosingObjectReference;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.shrikeBT.IBinaryOpInstruction;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.IOperator;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction.Operator;
import com.ibm.wala.shrikeBT.IShiftInstruction;
import com.ibm.wala.ssa.SSAArrayLengthInstruction;
import com.ibm.wala.ssa.SSAArrayLoadInstruction;
import com.ibm.wala.ssa.SSAArrayReferenceInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSABinaryOpInstruction;
import com.ibm.wala.ssa.SSACheckCastInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAConversionInstruction;
import com.ibm.wala.ssa.SSAFieldAccessInstruction;
import com.ibm.wala.ssa.SSAGetCaughtExceptionInstruction;
import com.ibm.wala.ssa.SSAGetInstruction;
import com.ibm.wala.ssa.SSAGotoInstruction;
import com.ibm.wala.ssa.SSAInstanceofInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSALoadMetadataInstruction;
import com.ibm.wala.ssa.SSAMonitorInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.ssa.SSAPutInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.ssa.SSAThrowInstruction;
import com.ibm.wala.ssa.SSAUnaryOpInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import magpiebridge.converter.sourceinfo.StmtPositionInfo;
import magpiebridge.converter.tags.StmtPositionInfoTag;
import soot.AbstractSootFieldRef;
import soot.ArrayType;
import soot.BooleanType;
import soot.ByteType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootFieldRef;
import soot.SootMethodRef;
import soot.SootMethodRefImpl;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.UnknownType;
import soot.Value;
import soot.VoidType;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.BinopExpr;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.ClassConstant;
import soot.jimple.ConditionExpr;
import soot.jimple.Constant;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceOfExpr;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NegExpr;
import soot.jimple.NewExpr;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.AbstractSwitchStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIfStmt;

/**
 * Convert WALA instruction to Jimple {@link Stmt}.
 *
 * @author Linghui Luo
 */
public class InstructionConverter {

  private ClassConverter converter;
  private final SymbolTable symbolTable;
  private final LocalGenerator localGenerator;
  // private final RefType convertingClassType;
  private final AstMethod walaMethod;
  // <ifStmt, iindex>
  private final Map<JIfStmt, Integer> targetsOfIfStmts;
  private final Map<JGotoStmt, Integer> targetsOfGotoStmts;
  private final Map<LookupSwitchStmt, List<Integer>> targetsOfLookUpSwitchStmts;
  private final Map<LookupSwitchStmt, Integer> defaultOfLookUpSwitchStmts;
  protected Map<AbstractSwitchStmt, List<Stmt>> targetStmtsOfLookUpSwitchStmts;
  private final Map<Integer, Local> locals;

  public InstructionConverter(
      ClassConverter converter, AstMethod walaMethod, LocalGenerator localGenerator) {
    this.converter = converter;
    this.walaMethod = walaMethod;
    this.symbolTable = walaMethod.symbolTable();
    this.localGenerator = localGenerator;
    this.targetsOfIfStmts = new HashMap<>();
    this.targetsOfGotoStmts = new HashMap<>();
    this.targetsOfLookUpSwitchStmts = new HashMap<>();
    this.defaultOfLookUpSwitchStmts = new HashMap<>();
    this.locals = new HashMap<>();
  }

  public List<Stmt> convertInstruction(DebuggingInformation debugInfo, SSAInstruction inst) {
    List<Stmt> stmts = new ArrayList<>();
    if (inst instanceof SSAConditionalBranchInstruction) {
      stmts.addAll(
          this.convertBranchInstruction(debugInfo, (SSAConditionalBranchInstruction) inst));
    } else if (inst instanceof SSAGotoInstruction) {
      stmts.add(this.convertGoToInstruction(debugInfo, (SSAGotoInstruction) inst));
    } else if (inst instanceof SSAReturnInstruction) {
      stmts.add(this.convertReturnInstruction(debugInfo, (SSAReturnInstruction) inst));
    } else if (inst instanceof AstJavaInvokeInstruction) {
      stmts.add(this.convertInvokeInstruction(debugInfo, (AstJavaInvokeInstruction) inst));
    } else if (inst instanceof SSAFieldAccessInstruction) {
      if (inst instanceof SSAGetInstruction) {
        stmts.add(this.convertGetInstruction(debugInfo, (SSAGetInstruction) inst)); // field read
      } else if (inst instanceof SSAPutInstruction) {
        stmts.add(this.convertPutInstruction(debugInfo, (SSAPutInstruction) inst)); // field write
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else if (inst instanceof SSANewInstruction) {
      stmts.add(convertNewInstruction(debugInfo, (SSANewInstruction) inst));
    } else if (inst instanceof SSAConversionInstruction) {
      stmts.add(convertConversionInstruction(debugInfo, (SSAConversionInstruction) inst));
    } else if (inst instanceof SSAInstanceofInstruction) {
      stmts.add(convertInstanceofInstruction(debugInfo, (SSAInstanceofInstruction) inst));
    } else if (inst instanceof SSABinaryOpInstruction) {
      stmts.addAll(this.convertBinaryOpInstruction(debugInfo, (SSABinaryOpInstruction) inst));
    } else if (inst instanceof SSAUnaryOpInstruction) {
      stmts.add(this.convertUnaryOpInstruction(debugInfo, (SSAUnaryOpInstruction) inst));
    } else if (inst instanceof SSAThrowInstruction) {
      stmts.add(this.convertThrowInstruction(debugInfo, (SSAThrowInstruction) inst));
    } else if (inst instanceof SSASwitchInstruction) {
      stmts.add(this.convertSwitchInstruction(debugInfo, (SSASwitchInstruction) inst));
    } else if (inst instanceof SSALoadMetadataInstruction) {
      stmts.add(this.convertLoadMetadataInstruction(debugInfo, (SSALoadMetadataInstruction) inst));
    } else if (inst instanceof EnclosingObjectReference) {
      stmts.add(this.convertEnclosingObjectReference(debugInfo, (EnclosingObjectReference) inst));
    } else if (inst instanceof AstLexicalRead) {
      stmts = (this.convertAstLexicalRead(debugInfo, (AstLexicalRead) inst));
    } else if (inst instanceof AstLexicalWrite) {
      stmts = (this.convertAstLexicalWrite(debugInfo, (AstLexicalWrite) inst));
    } else if (inst instanceof AstAssertInstruction) {
      stmts = this.convertAssertInstruction(debugInfo, (AstAssertInstruction) inst);
    } else if (inst instanceof SSACheckCastInstruction) {
      stmts.add(this.convertCheckCastInstruction(debugInfo, (SSACheckCastInstruction) inst));
    } else if (inst instanceof SSAMonitorInstruction) {
      stmts.add(
          this.convertMonitorInstruction(
              debugInfo, (SSAMonitorInstruction) inst)); // for synchronized
      // statement
    } else if (inst instanceof SSAGetCaughtExceptionInstruction) {
      stmts.add(
          this.convertGetCaughtExceptionInstruction(
              debugInfo, (SSAGetCaughtExceptionInstruction) inst));
    } else if (inst instanceof SSAArrayLengthInstruction) {
      stmts.add(this.convertArrayLengthInstruction(debugInfo, (SSAArrayLengthInstruction) inst));
    } else if (inst instanceof SSAArrayReferenceInstruction) {
      if (inst instanceof SSAArrayLoadInstruction) {
        stmts.add(this.convertArrayLoadInstruction(debugInfo, (SSAArrayLoadInstruction) inst));
      } else if (inst instanceof SSAArrayStoreInstruction) {
        stmts.add(this.convertArrayStoreInstruction(debugInfo, (SSAArrayStoreInstruction) inst));
      } else {
        throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
      }
    } else {
      throw new RuntimeException("Unsupported instruction type: " + inst.getClass().toString());
    }
    return stmts;
  }

  private Stmt convertArrayStoreInstruction(
      DebuggingInformation debugInfo, SSAArrayStoreInstruction inst) {
    Local base = getLocal(UnknownType.v(), inst.getArrayRef());
    int i = inst.getIndex();
    Value index = null;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i).snd;
    } else {
      index = getLocal(IntType.v(), i);
    }
    ArrayRef arrayRef = Jimple.v().newArrayRef(base, index);
    Value rvalue = null;
    int value = inst.getValue();
    if (symbolTable.isConstant(value)) {
      rvalue = getConstant(value).snd;
    } else {
      rvalue = getLocal(base.getType(), value);
    }

    Position[] operandPos = new Position[1];
    // FIXME: written arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    AssignStmt ret = Jimple.v().newAssignStmt(arrayRef, rvalue);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertArrayLoadInstruction(
      DebuggingInformation debugInfo, SSAArrayLoadInstruction inst) {
    Local base = getLocal(UnknownType.v(), inst.getArrayRef());
    int i = inst.getIndex();
    Value index;
    if (symbolTable.isConstant(i)) {
      index = getConstant(i).snd;
    } else {
      index = getLocal(IntType.v(), i);
    }
    ArrayRef arrayRef = Jimple.v().newArrayRef(base, index);
    Value left = null;
    int def = inst.getDef();
    left = getLocal(base.getType(), def);

    Position[] operandPos = new Position[1];
    // FIXME: loaded arrayindex position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    AssignStmt ret = Jimple.v().newAssignStmt(left, arrayRef);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertArrayLengthInstruction(
      DebuggingInformation debugInfo, SSAArrayLengthInstruction inst) {
    int result = inst.getDef();
    Local left = getLocal(IntType.v(), result);
    int arrayRef = inst.getArrayRef();
    Local arrayLocal = getLocal(UnknownType.v(), arrayRef);
    Value right = Jimple.v().newLengthExpr(arrayLocal);

    Position[] operandPos = new Position[1];
    Position p1 = debugInfo.getOperandPosition(inst.iIndex(), 0);
    operandPos[0] = p1;
    // FIXME: [ms] stmt position ends at variablename of the array
    AssignStmt ret = Jimple.v().newAssignStmt(left, right);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertGetCaughtExceptionInstruction(
      DebuggingInformation debugInfo, SSAGetCaughtExceptionInstruction inst) {
    int exceptionValue = inst.getException();
    Local local = getLocal(Scene.v().getType("java.lang.Throwable"), exceptionValue);
    CaughtExceptionRef caught = Jimple.v().newCaughtExceptionRef();

    Position[] operandPos = new Position[1];
    // FIXME: [ms] position info of parameter, target is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);

    IdentityStmt ret = Jimple.v().newIdentityStmt(local, caught);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertMonitorInstruction(
      DebuggingInformation debugInfo, SSAMonitorInstruction inst) {
    Value op = getLocal(UnknownType.v(), inst.getRef());

    Position[] operandPos = new Position[1];
    // FIXME: [ms] referenced object position info is missing
    // operandPos[0] = debugInfo.getOperandPosition(inst.iindex, 0);s
    if (inst.isMonitorEnter()) {
      EnterMonitorStmt ret = Jimple.v().newEnterMonitorStmt(op);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;
    } else {
      ExitMonitorStmt ret = Jimple.v().newExitMonitorStmt(op);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;
    }
  }

  private List<Stmt> convertAssertInstruction(
      DebuggingInformation debugInfo, AstAssertInstruction inst) {
    List<Stmt> stmts = new ArrayList<>();
    // create a static field for checking if assertion is disabled.
    Local testLocal = localGenerator.generateLocal(BooleanType.v());
    SootClass convertingClass = this.converter.getConvertingClass();
    SootFieldRef assertionsDisabled =
        new AbstractSootFieldRef(convertingClass, "$assertionsDisabled", BooleanType.v(), true);
    StaticFieldRef assertFieldRef = Jimple.v().newStaticFieldRef(assertionsDisabled);
    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    AssignStmt assignStmt = Jimple.v().newAssignStmt(testLocal, assertFieldRef);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    assignStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(assignStmt);

    // add ifStmt for testing assertion is disabled.
    EqExpr condition = Jimple.v().newEqExpr(testLocal, IntConstant.v(1));
    NopStmt nopStmt = Jimple.v().newNopStmt();
    nopStmt.addTag(new StmtPositionInfoTag(pos));

    IfStmt ifStmt = Jimple.v().newIfStmt(condition, nopStmt);
    ifStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(ifStmt);

    // create ifStmt for the actual assertion.
    Local assertLocal = getLocal(BooleanType.v(), inst.getUse(0));
    EqExpr assertionExpr = Jimple.v().newEqExpr(assertLocal, IntConstant.v(1));

    IfStmt assertIfStmt = Jimple.v().newIfStmt(assertionExpr, nopStmt);
    assertIfStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(assertIfStmt);
    // create failed assertion code.

    RefType assertionErrorType = Scene.v().getRefType("java.lang.AssertionError");
    Local failureLocal = localGenerator.generateLocal(assertionErrorType);
    NewExpr newExpr = Jimple.v().newNewExpr(assertionErrorType);
    AssignStmt newAssignStmt = Jimple.v().newAssignStmt(failureLocal, newExpr);
    newAssignStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(newAssignStmt);

    SootMethodRef methodRef =
        new SootMethodRefImpl(
            Scene.v().getSootClass("java.lang.AssertionError"),
            "<init>",
            Collections.emptyList(),
            VoidType.v(),
            false);
    SpecialInvokeExpr invoke = Jimple.v().newSpecialInvokeExpr(failureLocal, methodRef);
    InvokeStmt invokeStmt = Jimple.v().newInvokeStmt(invoke);
    invokeStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(invokeStmt);

    ThrowStmt throwStmt = Jimple.v().newThrowStmt(failureLocal);
    throwStmt.addTag(new StmtPositionInfoTag(pos));
    stmts.add(throwStmt);

    // add nop in the end
    stmts.add(nopStmt); // TODO. This should be removed later
    return stmts;
  }

  private List<Stmt> convertAstLexicalWrite(DebuggingInformation debugInfo, AstLexicalWrite inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Value right;
      if (symbolTable.isConstant(access.valueNumber)) {
        right = getConstant(access.valueNumber).snd;
      } else {
        right = getLocal(type, access.valueNumber);
      }
      // TODO check modifier
      Value left;

      if (!walaMethod.isStatic()) {
        SootFieldRef fieldRef =
            new AbstractSootFieldRef(
                converter.getConvertingClass(), "val$" + access.variableName, type, false);
        left = Jimple.v().newInstanceFieldRef(localGenerator.getThisLocal(), fieldRef);
        // TODO in old jimple this is not supported
      } else {
        left = localGenerator.generateLocal(type);
      }
      // TODO: [ms] no instruction example found to add positioninfo
      AssignStmt stmt = Jimple.v().newAssignStmt(left, right);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
      stmt.addTag(new StmtPositionInfoTag(pos));
      stmts.add(stmt);
    }
    return stmts;
  }

  private List<Stmt> convertAstLexicalRead(DebuggingInformation debugInfo, AstLexicalRead inst) {
    List<Stmt> stmts = new ArrayList<>();
    for (int i = 0; i < inst.getAccessCount(); i++) {
      Access access = inst.getAccess(i);
      Type type = converter.convertType(access.type);
      Local left = getLocal(type, access.valueNumber);
      // TODO check modifier
      Value rvalue = null;
      if (!walaMethod.isStatic()) {
        SootFieldRef fieldRef =
            new AbstractSootFieldRef(
                converter.getConvertingClass(), "val$" + access.variableName, type, false);
        rvalue = Jimple.v().newInstanceFieldRef(localGenerator.getThisLocal(), fieldRef);
      } else {
        rvalue = localGenerator.generateLocal(type);
      }

      // TODO: [ms] no instruction example found to add positioninfo
      AssignStmt stmt = Jimple.v().newAssignStmt(left, rvalue);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
      stmt.addTag(new StmtPositionInfoTag(pos));
      stmts.add(stmt);
    }
    return stmts;
  }

  private Stmt convertEnclosingObjectReference(
      DebuggingInformation debugInfo, EnclosingObjectReference inst) {
    Type enclosingType = converter.convertType(inst.getEnclosingType());
    Value variable = getLocal(enclosingType, inst.getDef());
    // TODO check modifier
    SootFieldRef fieldRef =
        new AbstractSootFieldRef(converter.getConvertingClass(), "this$0", enclosingType, false);
    InstanceFieldRef rvalue =
        Jimple.v().newInstanceFieldRef(localGenerator.getThisLocal(), fieldRef);
    // TODO: [ms] no instruction example found to add positioninfo
    AssignStmt ret = Jimple.v().newAssignStmt(variable, rvalue);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertCheckCastInstruction(
      DebuggingInformation debugInfo, SSACheckCastInstruction inst) {
    TypeReference[] types = inst.getDeclaredResultTypes();
    Local result = getLocal(converter.convertType(types[0]), inst.getResult());
    Value rvalue = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      rvalue = getConstant(val).snd;
    } else {
      rvalue = getLocal(converter.convertType(types[0]), val);
    }
    // TODO declaredResultType is wrong
    CastExpr castExpr = Jimple.v().newCastExpr(rvalue, converter.convertType(types[0]));

    // TODO: [ms] no instruction example found to add positioninfo
    AssignStmt ret = Jimple.v().newAssignStmt(result, castExpr);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertLoadMetadataInstruction(
      DebuggingInformation debugInfo, SSALoadMetadataInstruction inst) {
    Local lval = getLocal(converter.convertType(inst.getType()), inst.getDef());
    TypeReference token = (TypeReference) inst.getToken();
    ClassConstant c = ClassConstant.v(token.getName().toString());

    // TODO: [ms] no instruction example found to add positioninfo
    AssignStmt ret = Jimple.v().newAssignStmt(lval, c);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertSwitchInstruction(DebuggingInformation debugInfo, SSASwitchInstruction inst) {
    int val = inst.getUse(0);
    Local local = getLocal(UnknownType.v(), val);
    int[] cases = inst.getCasesAndLabels();
    int defaultCase = inst.getDefault();
    List<IntConstant> lookupValues = new ArrayList<>();
    List<Integer> targetsList = new ArrayList<>();
    List<? extends Stmt> targets = new ArrayList<>();
    for (int i = 0; i < cases.length; i++) {
      int c = cases[i];
      if (i % 2 == 0) {
        IntConstant cValue = IntConstant.v(c);
        lookupValues.add(cValue);
      } else {
        targetsList.add(c);
        targets.add(null); // add null as placeholder for targets
      }
    }
    Stmt defaultTarget = null;

    Position[] operandPos = new Position[2];
    // TODO: [ms] how to organize the operands
    // FIXME: has no operand positions yet for
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), ); // key
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), ); // default
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // lookups
    // operandPos[i] = debugInfo.getOperandPosition(inst.iIndex(), ); // targets

    LookupSwitchStmt stmt =
        Jimple.v().newLookupSwitchStmt(local, lookupValues, targets, defaultTarget);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    stmt.addTag(new StmtPositionInfoTag(pos));

    this.targetsOfLookUpSwitchStmts.put(stmt, targetsList);
    this.defaultOfLookUpSwitchStmts.put(stmt, defaultCase);
    return stmt;
  }

  private Stmt convertThrowInstruction(DebuggingInformation debugInfo, SSAThrowInstruction inst) {
    int exception = inst.getException();
    Local local = getLocal(UnknownType.v(), exception);

    Position[] operandPos = new Position[1];
    // FIXME: has no operand position yet for throwable
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    ThrowStmt ret = Jimple.v().newThrowStmt(local);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertUnaryOpInstruction(
      DebuggingInformation debugInfo, SSAUnaryOpInstruction inst) {
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value op;
    Type type = UnknownType.v();
    if (symbolTable.isConstant(use)) {
      Pair<Type, Constant> pair = getConstant(use);
      op = pair.snd;
      type = pair.fst;
    } else {
      op = getLocal(type, use);
      type = op.getType();
    }
    Local left = getLocal(type, def);

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for right side or assigned variable
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    AssignStmt ret;
    if (inst instanceof AssignInstruction) {
      ret = Jimple.v().newAssignStmt(left, op);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;

    } else {
      NegExpr expr = Jimple.v().newNegExpr(op);
      ret = Jimple.v().newAssignStmt(left, expr);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;
    }
  }

  private Stmt convertPutInstruction(DebuggingInformation debugInfo, SSAPutInstruction inst) {
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String declaringClassName =
        converter.convertClassNameFromWala(fieldRef.getDeclaringClass().getName().toString());
    SootClass declaringClass = converter.getSootClass(declaringClassName);
    Value fieldValue;
    if (inst.isStatic()) {
      SootFieldRef ref =
          new AbstractSootFieldRef(declaringClass, fieldRef.getName().toString(), fieldType, true);
      fieldValue = Jimple.v().newStaticFieldRef(ref);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(declaringClass.getType(), ref);
      SootFieldRef field =
          new AbstractSootFieldRef(declaringClass, fieldRef.getName().toString(), fieldType, false);
      fieldValue = Jimple.v().newInstanceFieldRef(base, field);
    }
    Value value = null;
    int val = inst.getVal();
    if (symbolTable.isConstant(val)) {
      value = getConstant(val).snd;
    } else {
      value = getLocal(fieldType, val);
    }

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for value, rvalue
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    AssignStmt ret = Jimple.v().newAssignStmt(fieldValue, value);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertNewInstruction(DebuggingInformation debugInfo, SSANewInstruction inst) {
    int result = inst.getDef();
    Type type = converter.convertType(inst.getNewSite().getDeclaredType());
    Value var = getLocal(type, result);
    Value rvalue = null;
    if (type instanceof ArrayType) {
      int use = inst.getUse(0);
      Value size = null;
      if (symbolTable.isConstant(use)) {
        size = getConstant(use).snd;
      } else {
        // TODO: size type unsure
        size = getLocal(IntType.v(), use);
      }
      Type baseType =
          converter.convertType(inst.getNewSite().getDeclaredType().getArrayElementType());
      rvalue = Jimple.v().newNewArrayExpr(baseType, size);
    } else {
      rvalue = Jimple.v().newNewExpr((RefType) type);
    }

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for type, size
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);

    AssignStmt ret = Jimple.v().newAssignStmt(var, rvalue);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertInstanceofInstruction(
      DebuggingInformation debugInfo, SSAInstanceofInstruction inst) {
    int result = inst.getDef();
    int ref = inst.getRef();
    Type checkedType = converter.convertType(inst.getCheckedType());
    // TODO. how to get type of ref?
    Local op = getLocal(UnknownType.v(), ref);
    InstanceOfExpr expr = Jimple.v().newInstanceOfExpr(op, checkedType);
    Value left = getLocal(BooleanType.v(), result);

    Position[] operandPos = new Position[2];
    // FIXME: has no operand positions yet for checked and expected side
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    AssignStmt ret = Jimple.v().newAssignStmt(left, expr);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertConversionInstruction(
      DebuggingInformation debugInfo, SSAConversionInstruction inst) {
    Type fromType = converter.convertType(inst.getFromType());
    Type toType = converter.convertType(inst.getToType());
    int def = inst.getDef();
    int use = inst.getUse(0);
    Value lvalue = getLocal(toType, def);
    Value rvalue = null;
    if (symbolTable.isConstant(use)) {
      rvalue = getConstant(use).snd;
    } else {
      rvalue = getLocal(fromType, use);
    }
    CastExpr cast = Jimple.v().newCastExpr(rvalue, toType);

    Position[] operandPos = new Position[2];
    // FIXME: has no positions for lvalue, rvalue yet
    // operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
    // operandPos[1] = debugInfo.getOperandPosition(inst.iIndex(), 1);
    AssignStmt ret = Jimple.v().newAssignStmt(lvalue, cast);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Stmt convertInvokeInstruction(
      DebuggingInformation debugInfo, AstJavaInvokeInstruction invokeInst) {
    Value invoke;
    CallSiteReference callee = invokeInst.getCallSite();
    MethodReference target = invokeInst.getDeclaredTarget();
    Type returnType = converter.convertType(target.getReturnType());
    List<String> parameters = new ArrayList<>();
    List<Type> paraTypes = new ArrayList<>();
    List<Value> args = new ArrayList<>();
    for (int i = 0; i < target.getNumberOfParameters(); i++) {
      Type paraType = converter.convertType(target.getParameterType(i)); // note
      // the
      // parameters
      // do
      // not
      // include
      // "this"
      paraTypes.add(paraType);
      parameters.add(paraType.toString());
    }
    Position[] operandPos = new Position[invokeInst.getNumberOfUses()];
    for (int j = 0; j < invokeInst.getNumberOfUses(); j++) {
      operandPos[j] = debugInfo.getOperandPosition(invokeInst.iIndex(), j);
    }
    int i = 0;
    if (!callee.isStatic()) {
      i = 1; // non-static invoke this first use is thisRef.
    }
    for (; i < invokeInst.getNumberOfUses(); i++) {
      int use = invokeInst.getUse(i);
      Value arg;
      if (symbolTable.isConstant(use)) {
        arg = getConstant(use).snd;
      } else {
        if (invokeInst.getNumberOfUses() > paraTypes.size()) {
          arg = getLocal(paraTypes.get(i - 1), use);
        } else {
          arg = getLocal(paraTypes.get(i), use);
        }
      }
      assert (arg != null);
      args.add(arg);
    }
    String declaringClassName =
        converter.convertClassNameFromWala(target.getDeclaringClass().getName().toString());
    SootClass declaringclass = this.converter.getSootClass(declaringClassName);
    if (!callee.isStatic()) {
      SootMethodRef methodRef =
          new SootMethodRefImpl(
              declaringclass, target.getName().toString(), paraTypes, returnType, false);
      int receiver = invokeInst.getReceiver();
      Type classType = converter.convertType(target.getDeclaringClass());
      Local base = getLocal(classType, receiver);
      if (callee.isSpecial()) {
        invoke = Jimple.v().newSpecialInvokeExpr(base, methodRef, args); // constructor
      } else if (callee.isVirtual()) {
        invoke = Jimple.v().newVirtualInvokeExpr(base, methodRef, args);
      } else if (callee.isInterface()) {
        invoke = Jimple.v().newInterfaceInvokeExpr(base, methodRef, args);
      } else {
        throw new RuntimeException("Unsupported invoke instruction: " + callee.toString());
      }
    } else {
      SootMethodRef methodRef =
          new SootMethodRefImpl(
              declaringclass, target.getName().toString(), paraTypes, returnType, true);
      invoke = Jimple.v().newStaticInvokeExpr(methodRef, args);
    }

    if (invokeInst.hasDef()) {
      Type type = converter.convertType(invokeInst.getDeclaredResultType());
      Local v = getLocal(type, invokeInst.getDef());
      AssignStmt ret = Jimple.v().newAssignStmt(v, invoke);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;

    } else {
      InvokeStmt ret = Jimple.v().newInvokeStmt(invoke);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(invokeInst.iIndex()), operandPos);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;
    }
  }

  private List<Stmt> convertBranchInstruction(
      DebuggingInformation debugInfo, SSAConditionalBranchInstruction condInst) {
    StmtPositionInfo posInfo =
        new StmtPositionInfo(debugInfo.getInstructionPosition(condInst.iIndex()), null);
    List<Stmt> stmts = new ArrayList<>();
    int val1 = condInst.getUse(0);
    int val2 = condInst.getUse(1);
    Value value1 = extractValueAndAddAssignStmt(posInfo, stmts, val1);
    Value value2 = extractValueAndAddAssignStmt(posInfo, stmts, val2);
    ConditionExpr condition;
    IOperator op = condInst.getOperator();
    if (op.equals(Operator.EQ)) {
      condition = Jimple.v().newEqExpr(value1, value2);
    } else if (op.equals(Operator.NE)) {
      condition = Jimple.v().newNeExpr(value1, value2);
    } else if (op.equals(Operator.LT)) {
      condition = Jimple.v().newLtExpr(value1, value2);
    } else if (op.equals(Operator.GE)) {
      condition = Jimple.v().newGeExpr(value1, value2);
    } else if (op.equals(Operator.GT)) {
      condition = Jimple.v().newGtExpr(value1, value2);
    } else if (op.equals(Operator.LE)) {
      condition = Jimple.v().newLtExpr(value1, value2);
    } else {
      throw new RuntimeException("Unsupported conditional operator: " + op);
    }
    UnitBox target = Jimple.v().newStmtBox(null);

    IfStmt ifStmt = Jimple.v().newIfStmt(condition, target);
    ifStmt.addTag(new StmtPositionInfoTag(posInfo));
    // target equals -1 refers to the end of the method
    this.targetsOfIfStmts.put((JIfStmt) ifStmt, condInst.getTarget());
    stmts.add(ifStmt);
    return stmts;
  }

  private Value extractValueAndAddAssignStmt(StmtPositionInfo posInfo, List<Stmt> addTo, int val) {
    Value value;
    Integer constant = null;
    if (symbolTable.isZero(val)) {
      value = IntConstant.v(0);
    } else {
      if (symbolTable.isConstant(val)) {
        Object c = symbolTable.getConstantValue(val);
        if (c instanceof Boolean) {
          constant = c.equals(true) ? 1 : 0;
        }
      }
      value = getLocal(IntType.v(), val);
    }
    if (constant != null) {
      AssignStmt assignStmt = Jimple.v().newAssignStmt(value, IntConstant.v(constant));
      assignStmt.addTag(new StmtPositionInfoTag(posInfo));
      addTo.add(assignStmt);
    }
    return value;
  }

  private Stmt convertReturnInstruction(DebuggingInformation debugInfo, SSAReturnInstruction inst) {
    int result = inst.getResult();
    if (inst.returnsVoid()) {
      // this is return void stmt
      ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), null);
      ret.addTag(new StmtPositionInfoTag(pos));
      return ret;

    } else {
      Value ret;
      if (symbolTable.isConstant(result)) {
        ret = getConstant(result).snd;
      } else {
        ret = this.getLocal(UnknownType.v(), result);
      }

      Position[] operandPos = new Position[1];
      operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);
      ReturnStmt r = Jimple.v().newReturnStmt(ret);
      StmtPositionInfo pos =
          new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
      r.addTag(new StmtPositionInfoTag(pos));
      return r;
    }
  }

  private List<Stmt> convertStringAddition(
      Value op1,
      Value op2,
      Value result,
      Type type,
      int iindex,
      AstMethod.DebuggingInformation debugInfo) {
    List<Stmt> ret = new ArrayList<>();
    CAstSourcePositionMap.Position p1 = debugInfo.getOperandPosition(iindex, 0);
    CAstSourcePositionMap.Position p2 = debugInfo.getOperandPosition(iindex, 1);
    CAstSourcePositionMap.Position stmtPosition = debugInfo.getInstructionPosition(iindex);

    RefType sbType = RefType.v("java.lang.StringBuilder");
    Local strBuilderLocal = this.localGenerator.generateLocal(sbType);

    Stmt newStmt = Jimple.v().newAssignStmt(strBuilderLocal, Jimple.v().newNewExpr(sbType));

    newStmt.addTag(new StmtPositionInfoTag(new StmtPositionInfo(stmtPosition, null)));
    ret.add(newStmt);

    SootMethodRef initMethod =
        new SootMethodRefImpl(
            sbType.getSootClass(), "<init>", Collections.singletonList(type), VoidType.v(), false);
    CAstSourcePositionMap.Position[] pos1 = new CAstSourcePositionMap.Position[2];
    pos1[0] = null;
    pos1[1] = p1;

    Stmt specStmt =
        Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(strBuilderLocal, initMethod, op1));

    specStmt.addTag(new StmtPositionInfoTag(new StmtPositionInfo(stmtPosition, pos1)));
    ret.add(specStmt);

    SootMethodRef appendMethod =
        new SootMethodRefImpl(
            sbType.getSootClass(), "append", Collections.singletonList(type), sbType, false);
    Local strBuilderLocal2 = this.localGenerator.generateLocal(sbType);
    CAstSourcePositionMap.Position[] pos2 = new CAstSourcePositionMap.Position[2];
    pos2[0] = null;
    pos2[1] = p2;

    Stmt virStmt =
        Jimple.v()
            .newAssignStmt(
                strBuilderLocal2,
                Jimple.v().newVirtualInvokeExpr(strBuilderLocal, appendMethod, op2));

    virStmt.addTag(new StmtPositionInfoTag(new StmtPositionInfo(stmtPosition, pos2)));

    ret.add(virStmt);

    SootMethodRef toStringMethod =
        new SootMethodRefImpl(
            sbType.getSootClass(), "toString", Collections.emptyList(), sbType, false);
    Stmt toStringStmt =
        Jimple.v()
            .newAssignStmt(
                result, Jimple.v().newVirtualInvokeExpr(strBuilderLocal2, toStringMethod));

    toStringStmt.addTag(new StmtPositionInfoTag(new StmtPositionInfo(stmtPosition, null)));

    ret.add(toStringStmt);
    return ret;
  }

  private List<Stmt> convertBinaryOpInstruction(
      DebuggingInformation debugInfo, SSABinaryOpInstruction binOpInst) {
    List<Stmt> stmts = new ArrayList<>();
    int def = binOpInst.getDef();
    int val1 = binOpInst.getUse(0);
    int val2 = binOpInst.getUse(1);
    Type type = UnknownType.v();
    Value op1;
    if (symbolTable.isConstant(val1)) {
      Pair<Type, Constant> pair = getConstant(val1);
      op1 = pair.snd;
      type = pair.fst;
    } else {
      op1 = getLocal(type, val1);
      type = op1.getType();
    }
    Value op2 = null;
    if (symbolTable.isConstant(val2)) {
      Pair<Type, Constant> pair = getConstant(val2);
      op2 = pair.snd;
      if (type.equals(UnknownType.v())) type = pair.fst;
    } else {
      op2 = getLocal(type, val2);
    }
    if (type.equals(UnknownType.v())) type = op2.getType();
    BinopExpr binExpr = null;
    IBinaryOpInstruction.IOperator operator = binOpInst.getOperator();

    if (operator.equals(IBinaryOpInstruction.Operator.ADD)) {
      if (type.toString().equals("java.lang.String")) {
        Value result = getLocal(type, def);
        return convertStringAddition(op1, op2, result, type, binOpInst.iIndex(), debugInfo);
      }
      binExpr = Jimple.v().newAddExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.SUB)) {
      binExpr = Jimple.v().newSubExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.MUL)) {
      binExpr = Jimple.v().newMulExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.DIV)) {
      binExpr = Jimple.v().newDivExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.REM)) {
      binExpr = Jimple.v().newRemExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.AND)) {
      binExpr = Jimple.v().newAndExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.OR)) {
      binExpr = Jimple.v().newOrExpr(op1, op2);
    } else if (operator.equals(IBinaryOpInstruction.Operator.XOR)) {
      binExpr = Jimple.v().newXorExpr(op1, op2);
    } else if (operator.equals(CAstBinaryOp.EQ)) {
      binExpr = Jimple.v().newEqExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(CAstBinaryOp.NE)) {
      binExpr = Jimple.v().newNeExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(CAstBinaryOp.LT)) {
      binExpr = Jimple.v().newLtExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(CAstBinaryOp.GE)) {
      binExpr = Jimple.v().newGeExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(CAstBinaryOp.GT)) {
      binExpr = Jimple.v().newGtExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(CAstBinaryOp.LE)) {
      binExpr = Jimple.v().newLeExpr(op1, op2);
      type = BooleanType.v();
    } else if (operator.equals(IShiftInstruction.Operator.SHL)) {
      binExpr = Jimple.v().newShlExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.SHR)) {
      binExpr = Jimple.v().newShrExpr(op1, op2);
    } else if (operator.equals(IShiftInstruction.Operator.USHR)) {
      binExpr = Jimple.v().newUshrExpr(op1, op2);
    } else {
      throw new RuntimeException("Unsupported binary operator: " + operator.getClass());
    }
    Position[] operandPos = new Position[2];
    Position p1 = debugInfo.getOperandPosition(binOpInst.iIndex(), 0);
    operandPos[0] = p1;
    Position p2 = debugInfo.getOperandPosition(binOpInst.iIndex(), 1);
    operandPos[1] = p2;
    Value result = getLocal(type, def);
    AssignStmt ret = Jimple.v().newAssignStmt(result, binExpr);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(binOpInst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    stmts.add(ret);
    return stmts;
  }

  private Stmt convertGoToInstruction(DebuggingInformation debugInfo, SSAGotoInstruction gotoInst) {
    UnitBox target = Jimple.v().newStmtBox(null);
    GotoStmt gotoStmt = Jimple.v().newGotoStmt(target);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(gotoInst.iIndex()), null);
    gotoStmt.addTag(new StmtPositionInfoTag(pos));
    this.targetsOfGotoStmts.put((JGotoStmt) gotoStmt, gotoInst.getTarget());
    return gotoStmt;
  }

  private Stmt convertGetInstruction(DebuggingInformation debugInfo, SSAGetInstruction inst) {
    int def = inst.getDef(0);
    FieldReference fieldRef = inst.getDeclaredField();
    Type fieldType = converter.convertType(inst.getDeclaredFieldType());
    String declaringClassName =
        converter.convertClassNameFromWala(fieldRef.getDeclaringClass().getName().toString());
    SootClass declaringClass = converter.getSootClass(declaringClassName);
    Value rvalue = null;
    if (inst.isStatic()) {
      AbstractSootFieldRef ref =
          new AbstractSootFieldRef(declaringClass, fieldRef.getName().toString(), fieldType, true);
      rvalue = Jimple.v().newStaticFieldRef(ref);
    } else {
      int ref = inst.getRef();
      Local base = getLocal(declaringClass.getType(), ref);
      AbstractSootFieldRef r =
          new AbstractSootFieldRef(declaringClass, fieldRef.getName().toString(), fieldType, false);
      rvalue = Jimple.v().newInstanceFieldRef(base, r);
    }

    Position[] operandPos = new Position[1];
    operandPos[0] = debugInfo.getOperandPosition(inst.iIndex(), 0);

    Value var = getLocal(fieldType, def);
    AssignStmt ret = Jimple.v().newAssignStmt(var, rvalue);
    StmtPositionInfo pos =
        new StmtPositionInfo(debugInfo.getInstructionPosition(inst.iIndex()), operandPos);
    ret.addTag(new StmtPositionInfoTag(pos));
    return ret;
  }

  private Pair<Type, Constant> getConstant(int valueNumber) {
    Object value = symbolTable.getConstantValue(valueNumber);
    if (value instanceof Boolean) {
      if ((Boolean) value) return Pair.make(BooleanType.v(), IntConstant.v(1));
      else return Pair.make(BooleanType.v(), IntConstant.v(0));
    } else if (value instanceof Byte) {
      return Pair.make(ByteType.v(), IntConstant.v((int) value));
    } else if (value instanceof Short) {
      return Pair.make(ShortType.v(), IntConstant.v((int) value));
    } else if (value instanceof Integer) {
      return Pair.make(IntType.v(), IntConstant.v((int) value));
    } else if (symbolTable.isLongConstant(valueNumber)) {
      return Pair.make(LongType.v(), LongConstant.v((long) value));
    } else if (symbolTable.isDoubleConstant(valueNumber)) {
      return Pair.make(DoubleType.v(), DoubleConstant.v((double) value));
    } else if (symbolTable.isFloatConstant(valueNumber)) {
      return Pair.make(FloatType.v(), FloatConstant.v((float) value));
    } else if (symbolTable.isStringConstant(valueNumber)) {
      return Pair.make(RefType.v("java.lang.String"), StringConstant.v((String) value));
    } else if (symbolTable.isNullConstant(valueNumber)) {
      return Pair.make(NullConstant.v().getType(), NullConstant.v());
    } else {
      throw new RuntimeException("Unsupported constant type: " + value.getClass().toString());
    }
  }

  private Local getLocal(Type type, int valueNumber) {
    if (locals.containsKey(valueNumber)) {
      return locals.get(valueNumber);
    }
    if (valueNumber == 1) {
      // in wala symbol numbers start at 1 ... the "this" parameter will be symbol
      // number 1 in a
      // non-static method.
      if (!walaMethod.isStatic()) {
        {
          return localGenerator.getThisLocal();
        }
      }
    }
    if (symbolTable.isParameter(valueNumber)) {
      Local para = localGenerator.getParameterLocal(valueNumber - 1);
      if (para != null) {
        return para;
      }
    }
    if (!locals.containsKey(valueNumber)) {
      Local local = localGenerator.generateLocal(type);
      locals.put(valueNumber, local);
    }
    Local ret = locals.get(valueNumber);

    if (!ret.getType().equals(type)) {
      // ret.setType(ret.getType().merge(type));
      // TODO. re-implement merge. Don't forget type can also be UnknownType.
      // throw new RuntimeException("Different types for same local
      // variable: "+ret.getType()+"<->"+type);
    }
    return ret;
  }

  /**
   * Test if the given stmt is the target stmt of {@link JIfStmt} or {@link JGotoStmt} and set it as
   * the target if it is the case.
   *
   * @param stmt the converted jimple stmt.
   * @param iindex the instruction index of the corresponding instruction in Wala.
   */
  protected void setTarget(Stmt stmt, int iindex) {
    if (this.targetsOfIfStmts.containsValue(iindex)) {
      for (JIfStmt ifStmt : this.targetsOfIfStmts.keySet()) {
        if (this.targetsOfIfStmts.get(ifStmt).equals(iindex)) {
          ifStmt.setTarget(stmt);
        }
      }
    }
    // FIXME: [ms] targetbox of JGotoStmt is null @PositionInfoTest.java
    // ->testSwitchInstruction()
    if (this.targetsOfGotoStmts.containsValue(iindex)) {
      for (JGotoStmt gotoStmt : this.targetsOfGotoStmts.keySet()) {
        if (this.targetsOfGotoStmts.get(gotoStmt).equals(iindex)) {
          gotoStmt.setTarget(stmt);
        }
      }
    }
    if (this.defaultOfLookUpSwitchStmts.containsValue(iindex)) {
      for (LookupSwitchStmt lookupSwitch : this.defaultOfLookUpSwitchStmts.keySet()) {
        if (this.defaultOfLookUpSwitchStmts.get(lookupSwitch).equals(iindex)) {
          lookupSwitch.setDefaultTarget(stmt);
        }
      }
    }
    for (LookupSwitchStmt lookupSwitch : this.targetsOfLookUpSwitchStmts.keySet()) {
      if (this.targetsOfLookUpSwitchStmts.get(lookupSwitch).contains(iindex)) {
        List<Unit> targets = lookupSwitch.getTargets();
        if (targets.contains(null)) { // targets only contains
          // placeholder
          targets = new ArrayList<>();
        }
        targets.add(stmt);
        Unit[] targetsArr = targets.toArray(new Unit[0]);
        lookupSwitch.setTargets(targetsArr);
      }
    }
  }
}
