package magpiebridge.converter;

import com.ibm.wala.cast.java.loader.JavaSourceLoaderImpl.JavaClass;
import com.ibm.wala.cast.loader.AstClass;
import com.ibm.wala.cast.loader.AstField;
import com.ibm.wala.cast.loader.AstMethod;
import com.ibm.wala.cast.loader.AstMethod.DebuggingInformation;
import com.ibm.wala.cast.tree.CAstSourcePositionMap.Position;
import com.ibm.wala.cfg.AbstractCFG;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.intset.FixedSizeBitVector;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.NullType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.VoidType;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;
import soot.jimple.internal.JReturnVoidStmt;

public class ClassConverter {
  private final HashMap<String, String> walaToSootNameTable;
  private final HashMap<String, Integer> clsWithInnerCls;
  private SootClass convertingClass;

  public ClassConverter() {
    walaToSootNameTable = new HashMap<>();
    clsWithInnerCls = new HashMap<>();
  }

  protected void convertClass(JavaClass fromClass) {
    String className = convertClassNameFromWala(fromClass.getName().toString());
    SootClass toClass;
    if (!Scene.v().containsClass(className)) {
      toClass = new SootClass(className);
      Scene.v().addClass(toClass);
    } else toClass = Scene.v().getSootClass(className);
    convertingClass = toClass;
    toClass.setApplicationClass();

    // convert modifiers
    int modifiers = convertModifiers(fromClass);
    toClass.setModifiers(modifiers);

    // convert super class
    IClass sc = fromClass.getSuperclass();
    if (sc != null) {
      SootClass s = getSootClass(convertClassNameFromWala(sc.getName().toString()));
      toClass.setSuperclass(s);
    }

    // convert interfaces
    for (IClass i : fromClass.getDirectInterfaces()) {
      SootClass ic = getSootClass(convertClassNameFromWala(i.getName().toString()));
      toClass.addInterface(ic);
    }

    // convert outer class
    SootClass outerClass = null;
    IClass ec = (fromClass).getEnclosingClass();
    if (ec != null) {
      outerClass = getSootClass(convertClassNameFromWala(ec.getName().toString()));
      toClass.setOuterClass(outerClass);
    }

    // add source position
    Position position = fromClass.getSourcePosition();
    toClass.addTag(new PositionTag(position));

    // convert fields
    Set<IField> fields = HashSetFactory.make(fromClass.getDeclaredInstanceFields());
    fields.addAll(fromClass.getDeclaredStaticFields());
    for (IField walaField : fields) {
      SootField sootField = convertField(className, (AstField) walaField);
      toClass.addField(sootField);
      sootField.setDeclaringClass(toClass);
      sootField.setDeclared(true);
    }
    if (outerClass != null) {
      // create enclosing reference to outerClass
      SootField enclosingObject = new SootField("this$0", outerClass.getType(), Modifier.FINAL);
      toClass.addField(enclosingObject);
      enclosingObject.setDeclaringClass(toClass);
      enclosingObject.setDeclared(true);
    }

    // convert methods
    for (IMethod walaMethod : fromClass.getDeclaredMethods()) {
      SootMethod sootMethod = convertMethod(className, toClass.getType(), (AstMethod) walaMethod);
      toClass.addMethod(sootMethod);
      sootMethod.setDeclaringClass(toClass);
      sootMethod.setDeclared(true);
      if (sootMethod.hasActiveBody()) {
        toClass.setResolvingLevel(SootClass.BODIES);
      }
    }
  }

  protected SootClass getSootClass(String className) {
    if (!Scene.v().containsClass(className)) {
      // class not in Scene need to be forced to resolve
      return Scene.v().forceResolve(className, SootClass.SIGNATURES);
    } else return Scene.v().getSootClass(className);
  }

  protected SootClass getConvertingClass() {
    return convertingClass;
  }

  private SootMethod convertMethod(
      String className, RefType declaringClassType, AstMethod walaMethod) {
    // create SootMethod instance with parameter types, return type, modifiers and
    // declared exceptions.
    List<Type> paraTypes = new ArrayList<>();
    List<String> sigs = new ArrayList<>();
    if (walaMethod.symbolTable() != null) {
      for (int i = 0; i < walaMethod.getNumberOfParameters(); i++) {
        TypeReference type = walaMethod.getParameterType(i);
        if (i == 0) {
          if (!walaMethod.isStatic()) {
            // ignore this pointer
            continue;
          }
        }
        Type paraType = convertType(type);
        paraTypes.add(paraType);
        sigs.add(paraType.toString());
      }
    }

    Type returnType = convertType(walaMethod.getReturnType());

    int modifiers = convertModifiers(walaMethod);

    List<SootClass> thrownExceptions = new ArrayList<>();
    try {
      for (TypeReference exception : walaMethod.getDeclaredExceptions()) {
        String exceptionName = convertClassNameFromWala(exception.getName().toString());
        SootClass exceptionType = this.getSootClass(exceptionName);
        thrownExceptions.add(exceptionType);
      }
    } catch (UnsupportedOperationException | InvalidClassFileException e) {
      throw new RuntimeException(e);
    }

    SootMethod toMethod =
        new SootMethod(
            walaMethod.getName().toString(), paraTypes, returnType, modifiers, thrownExceptions);

    // add debug info
    DebuggingInformation debugInfo = walaMethod.debugInfo();
    toMethod.addTag(new DebuggingInformationTag(debugInfo));

    Body body = createBody(modifiers, declaringClassType, walaMethod, toMethod);
    if (body != null) {
      toMethod.setActiveBody(body);
      toMethod.setPhantom(false);
    }
    return toMethod;
  }

  private Body createBody(
      int modifiers, RefType declaringClassType, AstMethod walaMethod, SootMethod toMethod) {
    if (walaMethod.isAbstract() || walaMethod.isNative()) {
      return null;
    }
    JimpleBody toBody = Jimple.v().newBody(toMethod);
    AbstractCFG<?, ?> cfg = walaMethod.cfg();
    if (cfg != null) {
      List<Trap> traps = new ArrayList<>();
      List<Stmt> stmts = new ArrayList<>();
      LocalGenerator localGenerator = new LocalGenerator(new HashSet<>());
      // convert all wala instructions to jimple statements
      SSAInstruction[] insts = (SSAInstruction[]) cfg.getInstructions();
      if (insts.length > 0) {

        // set position for body
        DebuggingInformation debugInfo = walaMethod.debugInfo();
        Position bodyPos = debugInfo.getCodeBodyPosition();

        /*
         * Look AsmMethodSourceContent.getBody, see AsmMethodSourceContent.emitLocals();
         */

        if (!Modifier.isStatic(modifiers)) {

          Local thisLocal = localGenerator.generateThisLocal(declaringClassType);

          Stmt stmt =
              Jimple.v().newIdentityStmt(thisLocal, Jimple.v().newThisRef(declaringClassType));
          Position pos = debugInfo.getInstructionPosition(0);
          stmt.addTag(new PositionTag(pos));
          stmts.add(stmt);
        }

        int startPara = 0;
        if (!walaMethod.isStatic()) {
          // wala's first parameter is this reference for non-static methodRef
          startPara = 1;
        }
        for (; startPara < walaMethod.getNumberOfParameters(); startPara++) {
          TypeReference t = walaMethod.getParameterType(startPara);
          Type type = convertType(t);
          Local paraLocal = localGenerator.generateParameterLocal(type, startPara);
          int index = startPara;
          if (!walaMethod.isStatic()) {
            index = startPara - 1;
          }
          Stmt stmt =
              Jimple.v().newIdentityStmt(paraLocal, Jimple.v().newParameterRef(type, index));
          Position pos = debugInfo.getInstructionPosition(0);
          stmt.addTag(new PositionTag(pos));
          stmts.add(stmt);
        }

        // TODO 2. convert traps
        // get exceptions which are not caught
        FixedSizeBitVector blocks = cfg.getExceptionalToExit();
        InstructionConverter instConverter =
            new InstructionConverter(this, walaMethod, localGenerator);
        Map<Stmt, Integer> stmt2IIndex = new HashMap<>();
        for (SSAInstruction inst : insts) {
          List<Stmt> retStmts = instConverter.convertInstruction(debugInfo, inst);
          if (!retStmts.isEmpty()) {
            for (Stmt stmt : retStmts) {
              stmts.add(stmt);
              stmt2IIndex.put(stmt, inst.iIndex());
            }
          }
        }
        // set target for goto or conditional statements
        for (Stmt stmt : stmt2IIndex.keySet()) {
          instConverter.setTarget(stmt, stmt2IIndex.get(stmt));
        }

        // add return void stmt for methods with return type being void
        if (walaMethod.getReturnType().equals(TypeReference.Void)) {
          Stmt ret;
          if (stmts.isEmpty() || !(stmts.get(stmts.size() - 1) instanceof JReturnVoidStmt)) {
            // TODO? [ms] InstructionPosition of last line in the method seems strange to me
            // ->
            // maybe use lastLine with
            // startcol: -1 because it does not exist in the source explicitly?
            ret = Jimple.v().newReturnVoidStmt();
            Position pos = debugInfo.getInstructionPosition(insts.length - 1);
            ret.addTag(new PositionTag(pos));
            stmts.add(ret);
          } else {
            ret = stmts.get(stmts.size() - 1);
          }
          instConverter.setTarget(ret, -1); // -1 is the end of the method
        }
        localGenerator.getLocals().forEach(l -> toBody.getLocals().add(l));
        stmts.forEach(s -> toBody.getUnits().add(s));
        traps.forEach(t -> toBody.getTraps().add(t));
        return toBody;
      }
    }

    return null;
  }

  protected Type convertType(TypeReference type) {
    if (type.isPrimitiveType()) {
      if (type.equals(TypeReference.Boolean)) {
        return BooleanType.v();
      } else if (type.equals(TypeReference.Byte)) {
        return ByteType.v();
      } else if (type.equals(TypeReference.Char)) {
        return CharType.v();
      } else if (type.equals(TypeReference.Short)) {
        return ShortType.v();
      } else if (type.equals(TypeReference.Int)) {
        return IntType.v();
      } else if (type.equals(TypeReference.Long)) {
        return LongType.v();
      } else if (type.equals(TypeReference.Float)) {
        return FloatType.v();
      } else if (type.equals(TypeReference.Double)) {
        return DoubleType.v();
      } else if (type.equals(TypeReference.Void)) {
        return VoidType.v();
      }
    } else if (type.isReferenceType()) {
      if (type.isArrayType()) {
        TypeReference t = type.getInnermostElementType();
        Type baseType = convertType(t);
        int dim = type.getDimensionality();
        return ArrayType.v(baseType, dim);
      } else if (type.isClassType()) {
        if (type.equals(TypeReference.Null)) {
          return NullType.v();
        } else {
          String className = convertClassNameFromWala(type.getName().toString());
          RefType t = addRefTypeToScene(className);
          return t;
        }
      }
    }
    throw new RuntimeException("Unsupported tpye: " + type);
  }

  /**
   * It adds RefType with the given typeName to Scene and makes sure every RefType is resolved,
   * otherwise {@link soot.jimple.spark.internal.TypeManager#isUnresolved(soot.Type)} will case
   * problem in call graph construction.
   *
   * @param typeName
   * @return
   */
  private RefType addRefTypeToScene(String typeName) {
    RefType t = soot.RefType.v(typeName);
    if (!t.hasSootClass()) Scene.v().forceResolve(typeName, soot.SootClass.SIGNATURES);
    if (!Scene.v().containsType(typeName)) Scene.v().addRefType(t);
    return t;
  }

  private SootField convertField(String className, AstField field) {
    Type type = convertType(field.getFieldTypeReference());
    int modifiers = convertModifiers(field);
    return new SootField(field.getName().toString(), type, modifiers);
  }

  private int convertModifiers(AstField field) {
    Set<Integer> modifiers = new HashSet<Integer>();
    if (field.isFinal()) {
      modifiers.add(Modifier.FINAL);
    }
    if (field.isPrivate()) {
      modifiers.add(Modifier.PRIVATE);
    }
    if (field.isProtected()) {
      modifiers.add(Modifier.PROTECTED);
    }
    if (field.isPublic()) {
      modifiers.add(Modifier.PUBLIC);
    }
    if (field.isStatic()) {
      modifiers.add(Modifier.STATIC);
    }
    if (field.isVolatile()) {
      modifiers.add(Modifier.VOLATILE);
    }
    int bytecode = 0;
    for (Integer modifier : modifiers) {
      bytecode = bytecode | modifier;
    }
    return bytecode;
  }

  private int convertModifiers(AstMethod method) {
    Set<Integer> modifiers = new HashSet<Integer>();
    if (method.isPrivate()) {
      modifiers.add(Modifier.PRIVATE);
    }
    if (method.isProtected()) {
      modifiers.add(Modifier.PROTECTED);
    }
    if (method.isPublic()) {
      modifiers.add(Modifier.PUBLIC);
    }
    if (method.isStatic()) {
      modifiers.add(Modifier.STATIC);
    }
    if (method.isFinal()) {
      modifiers.add(Modifier.FINAL);
    }
    if (method.isAbstract()) {
      modifiers.add(Modifier.ABSTRACT);
    }
    if (method.isSynchronized()) {
      modifiers.add(Modifier.SYNCHRONIZED);
    }
    if (method.isNative()) {
      modifiers.add(Modifier.NATIVE);
    }
    if (method.isSynthetic()) {
      modifiers.add(Modifier.SYNTHETIC);
    }
    if (method.isBridge()) {
      modifiers.add(Modifier.VOLATILE);
    }
    int bytecode = 0;
    for (Integer modifier : modifiers) {
      bytecode = bytecode | modifier;
    }
    return bytecode;
  }

  private int convertModifiers(AstClass klass) {
    if (klass.getSuperclass().getName().toString().equals("Ljava/lang/Enum")) {
      return Modifier.ENUM;
    } else return klass.getModifiers();
  }

  /**
   * Convert className in wala-format to soot-format, e.g., wala-format: Ljava/lang/String ->
   * soot-format: java.lang.String.
   *
   * @param className in wala-format
   * @return className in soot.format
   */
  protected String convertClassNameFromWala(String className) {
    String cl = className.intern();
    if (walaToSootNameTable.containsKey(cl)) {
      return walaToSootNameTable.get(cl);
    }
    StringBuilder sb = new StringBuilder();
    if (className.startsWith("L")) {
      className = className.substring(1);
      String[] subNames = className.split("/");
      boolean isSpecial = false;
      for (int i = 0; i < subNames.length; i++) {
        String subName = subNames[i];
        if (subName.contains("(") || subName.contains("<")) {
          // handle anonymous or inner classes
          isSpecial = true;
          break;
        }
        if (i != 0) {
          sb.append(".");
        }
        sb.append(subName);
      }
      if (isSpecial) {
        String lastSubName = subNames[subNames.length - 1];
        String[] temp = lastSubName.split(">");
        if (temp.length > 0) {
          String name = temp[temp.length - 1];
          if (!name.contains("$")) {
            // This is an inner class
            String outClass = sb.toString();
            int count = 1;
            if (this.clsWithInnerCls.containsKey(outClass)) {
              count = this.clsWithInnerCls.get(outClass) + 1;
            }
            this.clsWithInnerCls.put(outClass, count);
            sb.append(count).append("$");
          }
          sb.append(name);
        }
      }
    } else {
      throw new RuntimeException("Can not convert WALA class name: " + className);
    }
    String ret = sb.toString();
    walaToSootNameTable.put(cl, ret);
    return ret;
  }
}
