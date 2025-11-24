class CreationStatement extends Statement
{ String createsInstanceOf = null;
  String assignsTo = null;
  private Type instanceType = null; 
  private Type elementType = null; 
  boolean declarationOnly = false; 
  String initialValue = null;
  Expression initialExpression = null;  
  boolean isFrozen = false;  
  Attribute variable = null; 
  public Object clone()
  { CreationStatement cs = 
       new CreationStatement(createsInstanceOf,assignsTo); 
    cs.instanceType = instanceType; 
    cs.elementType = elementType; 
    cs.declarationOnly = declarationOnly; 
    cs.initialValue = initialValue; 
    if (initialExpression != null) 
    { cs.initialExpression = 
         (Expression) initialExpression.clone();
    }  
    cs.isFrozen = isFrozen; 
    cs.variable = variable; 
    return cs; 
  } 
  public Type getType()
  { return instanceType; } 
  public String getOclType()
  { if ("int".equals(createsInstanceOf) || 
        "long".equals(createsInstanceOf))
    { return "Integer"; }
    if ("double".equals(createsInstanceOf))
    { return "Real"; }
    if ("boolean".equals(createsInstanceOf))
    { return "Boolean"; }
    if ("String".equals(createsInstanceOf))
    { return "String"; }
    if (createsInstanceOf.startsWith("Sequence"))
    { return "Sequence"; } 
    if (createsInstanceOf.startsWith("Set"))
    { return "Set"; } 
    if (createsInstanceOf.startsWith("Map"))
    { return "Map"; } 
    if (createsInstanceOf.startsWith("Ref"))
    { return "Ref"; } 
    if (createsInstanceOf.startsWith("Function"))
    { return "Function"; } 
    return "Object"; 
  } 
  public Type getElementType()
  { return elementType; } 
  public String getVar()
  { return assignsTo; } 
  public String getDeclaredVariable()
  { return assignsTo; } 
  public CreationStatement(String cio, String ast)
  { createsInstanceOf = cio;
    assignsTo = ast; 
  }
  public CreationStatement(String vbl, Type typ)
  { createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = typ.getElementType();
    if (Type.isStringType(typ))
    { elementType = new Type("String", null); }  
    assignsTo = vbl; 
  }
  public CreationStatement(Expression vbl, Type typ)
  { createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = typ.getElementType();
    if (Type.isStringType(typ))
    { elementType = new Type("String", null); }  
    assignsTo = vbl + ""; 
  }
  public CreationStatement(BasicExpression var, Expression init)
  { instanceType = var.getType(); 
    elementType = var.getElementType(); 
    if (Type.isStringType(instanceType))
    { elementType = new Type("String", null); }
    initialExpression = init; 
    createsInstanceOf = instanceType.getName(); 
    assignsTo = var + ""; 
  }
  public CreationStatement defaultVersion()
  { CreationStatement res = (CreationStatement) clone(); 
    Expression defaultInit = 
      Type.defaultInitialValueExpression(instanceType);
    res.initialExpression = defaultInit; 
    res.initialValue = defaultInit + ""; 
    return res; 
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { 
    if (initialExpression != null) 
    { Expression val = initialExpression.evaluate(sigma, beta); 
      beta.addVariable(assignsTo, val);
    } 
    else if (instanceType != null)  
    { Expression defaultInit = 
        Type.defaultInitialValueExpression(instanceType);
      Expression val = defaultInit.evaluate(sigma, beta); 
      beta.addVariable(assignsTo, val);
    }
    return Statement.NORMAL; 
  } 
  public Expression definedness()
  { if (initialExpression != null) 
    { return initialExpression.definedness(); } 
    return new BasicExpression(true); 
  } 
  public Vector allVariableNames()
  { Vector res = new Vector(); 
    res.add(assignsTo); 
    if (initialExpression != null)
    { res.addAll(initialExpression.allVariableNames()); }
    return res; 
  }  
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (instanceType != null) 
    { int tcomp = instanceType.complexity(); 
      if (tcomp > TestParameters.nestedTypeLimit) 
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Warning (MNC) flaw: complex type with complexity " + tcomp + ": " + instanceType); 
      } 
    } 
    if (initialExpression != null) 
    { initialExpression.energyUse(uses, rUses, aUses); 
      int syncomp = initialExpression.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + initialExpression + "\n" +  
                  ">>> Recommend OCL refactoring");  
      } 
    } 
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                 java.util.Map uses,
                                 Vector vars)
  { 
    if (initialExpression != null) 
    { initialExpression.collectionOperatorUses(lev, 
                                        uses, vars); 
    } 
    return uses; 
  } 
  public CreationStatement(Attribute vbl)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getName();
    variable = vbl;  
  }
  public CreationStatement(Attribute vbl, Attribute val)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getName();
    variable = vbl;  
    initialExpression = new BasicExpression(val); 
    initialValue = initialExpression + ""; 
  }
  public CreationStatement(BasicExpression vbl)
  { Type typ = vbl.getType(); 
    createsInstanceOf = typ.getName();
    instanceType = typ; 
    elementType = vbl.getElementType(); 
    assignsTo = vbl.getData();
    variable = vbl.variable;  
  }
  public static CreationStatement newCreationStatement(
            String vbl, Type typ, Expression einit) 
  { CreationStatement cs = 
       new CreationStatement(typ.getName(), vbl); 
    cs.setType(typ);
    cs.setInitialisation(einit);   
    return cs; 
  } 
  public static CreationStatement newCreationStatement(String vbl, String typ) 
  { CreationStatement cs = new CreationStatement(typ, vbl); 
    Type t = Type.getTypeFor(typ);
    if (t == null)
    { t = new Type("OclAny", null); } 
    cs.setType(t);  
    return cs; 
  } 
  public static CreationStatement newCreationStatement(String vbl, String typ, Vector enumtypes, Vector ents) 
  { CreationStatement cs = new CreationStatement(typ, vbl); 
    Type t = Type.getTypeFor(typ, enumtypes, ents);
    if (t == null)
    { t = new Type("OclAny", null); } 
    cs.setType(t);  
    cs.setElementType(t.getElementType()); 
    return cs; 
  } 
  public String getDefinedVariable()
  { return assignsTo; } 
  public void setInitialValue(String init)
  { initialValue = init; } 
  public void setInitialExpression(Expression expr) 
  { initialExpression = expr; 
    initialValue = expr + ""; 
  } 
  public void setInitialisation(Expression expr) 
  { initialExpression = expr; 
    initialValue = expr + ""; 
  } 
  public Expression getInitialisation()
  { return initialExpression; } 
  public void setAssignsTo(Expression expr)
  { assignsTo = expr + ""; } 
  public void setFrozen(boolean froz)
  { isFrozen = froz; } 
  public String getOperator() 
  { return "var"; } 
  public boolean isResultDeclaration()
  { if (assignsTo.equals("result") && 
        (instanceType != null || createsInstanceOf != null))
    { return true; } 
    return false; 
  } 
  public void setInstanceType(Type t)
  { instanceType = t; 
    if (instanceType != null) 
    { createsInstanceOf = instanceType.getName(); }
  }  
  public void setType(Type t)
  { instanceType = t; 
    if (instanceType != null) 
    { createsInstanceOf = instanceType.getName(); 
      if ("String".equals(instanceType.getName()))
      { elementType = new Type("String", null); }
    } 
  }  
  public void setKeyType(Type t)
  { 
    if (instanceType != null) 
    { instanceType.setKeyType(t); }  
  } 
  public void setElementType(Type t)
  { elementType = t; 
    if (instanceType != null) 
    { instanceType.setElementType(t); }  
  } 
  public Statement dereference(BasicExpression var)
  { return this; } 
  public Statement substituteEq(String oldE, Expression newE)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 
    if (oldE.equals(createsInstanceOf))
    { cio = newE.toString(); }
    if (oldE.equals(assignsTo))
    { ast = newE.toString(); }
    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
    if (initialExpression != null) 
    { Expression newExpr = initialExpression.substituteEq(oldE,newE); 
      res.setInitialisation(newExpr); 
    }
    return res; 
  } 
  public Statement optimiseOCL()
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 
    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
    if (initialExpression != null) 
    { Expression newExpr = initialExpression.simplifyOCL(); 
      res.setInitialisation(newExpr); 
    }
    return res; 
  } 
  public Statement removeSlicedParameters(
                     BehaviouralFeature bf, Vector fpars)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 
    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
    if (initialExpression != null) 
    { Expression newExpr = 
        initialExpression.removeSlicedParameters(bf,fpars); 
      res.setInitialisation(newExpr); 
    }
    return res; 
 } 
  public Statement addContainerReference(
               BasicExpression ref, String var, Vector excl)
  { String cio = createsInstanceOf; 
    String ast = assignsTo; 
    CreationStatement res = new CreationStatement(cio,ast);
    res.setType(instanceType); 
    res.setElementType(elementType);  
    if (initialExpression != null) 
    { Expression newExpr = 
        initialExpression.addContainerReference(ref,var,excl); 
      res.setInitialisation(newExpr); 
    }
    excl.add(assignsTo); 
    return res; 
  } 
  public String toString()
  { String declType = createsInstanceOf; 
    if (instanceType != null && instanceType.isEntity()) 
    { declType = instanceType.getEntity().getCompleteName(); }
    else if (instanceType != null) 
    { declType = instanceType + ""; }  
    if (initialValue != null) 
    { return "  var " + assignsTo + " : " + declType + " := " + initialValue; } 
    else if (initialExpression != null) 
    { return "  var " + assignsTo + " : " + declType + " := " + initialExpression; } 
    else
    { return "  var " + assignsTo + " : " + declType; }
  } 
  public String toAST()
  { String res = "(OclStatement var " + assignsTo + " : " + instanceType.toAST() + " )"; 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr)
  { if (initialExpression != null) 
    { return initialExpression.containsSubexpression(expr); } 
    return false; 
  } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    return res; 
  } 
  public void findClones(java.util.Map clones, String op, String rule)
  { if (initialExpression != null) 
    { initialExpression.findClones(clones,op,rule); } 
  } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String op, String rule)
  { if (initialExpression != null) 
    { initialExpression.findClones(clones,cdefs,op,rule); } 
  } 
  public String saveModelData(PrintWriter out) 
  { String res = Identifier.nextIdentifier("creationstatement_"); 
    out.println(res + " : CreationStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".createsInstanceOf = \"" + createsInstanceOf + "\""); 
    out.println(res + ".assignsTo = \"" + assignsTo + "\""); 
    String tname = "OclAny"; 
    String etname = "OclAny"; 
    if (instanceType != null) 
    { tname = instanceType.getUMLModelName(out); } 
    out.println(res + ".type = " + tname); 
    if (elementType != null) 
    { etname = elementType.getUMLModelName(out); 
      out.println(res + ".elementType = " + etname); 
    } 
    else if (instanceType != null && 
             instanceType.getElementType() != null)
    { etname = 
        instanceType.getElementType().getUMLModelName(out); 
      out.println(res + ".elementType = " + etname);
    } 
    else if (instanceType != null && 
             Type.isBasicType(instanceType))
    { out.println(res + ".elementType = " + tname); } 
    else 
    { out.println(res + ".elementType = " + etname); } 
    if (initialExpression != null) 
    { String exprId = initialExpression.saveModelData(out); 
      out.println(exprId + " : " + res + ".initialExpression"); 
    } 
    return res; 
  } 
  public String bupdateForm()
  { return assignsTo + " :: " + createsInstanceOf; } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { Vector updates = new Vector(); 
    updates.add(assignsTo); 
    Expression assignsToE = new BasicExpression(assignsTo); 
    Expression createsInstanceOfE = new BasicExpression(createsInstanceOf); 
    Expression whereexp = new BinaryExpression(":", assignsToE, createsInstanceOfE); 
    BExpression bqual = whereexp.binvariantForm(env,local); 
    return new BAnyStatement(updates, bqual, new BBasicStatement("skip"));
  } 
  public String toEtl()
  { if (initialValue != null) 
    { return "  var " + assignsTo + " = " + initialValue + ";"; } 
    else 
    { return "  var " + assignsTo + ";"; } 
  }
  public String toStringJava()
  { String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 
    java.util.Map env = new java.util.HashMap(); 
    if (instanceType != null)
    { String jType = instanceType.getJava(); 
      System.out.println(">>> Creation instance type: " + instanceType); 
      System.out.println(">>> Creation Java type: " + jType); 
      System.out.println(); 
      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryForm(env,true) + ";\n"; }
    } 
    if (initialValue != null && instanceType != null) 
    { String jType = instanceType.getJava(); 
      return "  " + mode + jType + " " + assignsTo + " = " + initialValue + ";"; 
    } 
    else if (instanceType != null)
    { String jType = instanceType.getJava(); 
      if (initialExpression != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryForm(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava(); 
          return "  " + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (declarationOnly) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + "Map " + assignsTo + " = new HashMap();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isCollectionType(instanceType))
      { return "  " + mode + "List " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else  
        { return "  " + jType + " " + assignsTo + " = new " + jType + "();\n" + 
               "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        } 
      } 
    } 
    else if (createsInstanceOf.equals("boolean") || 
             createsInstanceOf.equals("int") ||
        createsInstanceOf.equals("long") || 
        createsInstanceOf.equals("String") || 
        createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Set") || 
        createsInstanceOf.startsWith("Sequence"))
    { return "  List " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Map " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Evaluation<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  Date " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }
    return "  " + mode + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
           "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }
  public String toStringJava6()
  { java.util.Map env = new java.util.HashMap(); 
    String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 
    if (instanceType != null)
    { String jType = instanceType.getJava6(); 
      if (initialExpression != null)
      { return "  " + mode + jType + " " + assignsTo + " = " + initialExpression.queryFormJava6(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava6(); 
          return "  " + mode + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
        }
        return "  " + mode + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + "Map " + assignsTo + " = new HashMap();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isSetType(instanceType))
      { return "  " + mode + "HashSet " + assignsTo + ";"; } 
      else if (Type.isSequenceType(instanceType))
      { return "  " + mode + "ArrayList " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n" + 
                 "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        }
      }  
    } 
    else if (createsInstanceOf.equals("boolean") || 
        createsInstanceOf.equals("int") ||
        createsInstanceOf.equals("long") || 
        createsInstanceOf.equals("String") || 
        createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Map " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Evaluation<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  Date " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }
    return "  " + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + 
           "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }
  public String toStringJava7()
  { 
    java.util.Map env = new java.util.HashMap(); 
    String mode = ""; 
    if (isFrozen) 
    { mode = "final "; } 
    if (instanceType != null)
    { String jType = instanceType.getJava7(elementType); 
      if (initialExpression != null)
      { return "  " + mode + jType + " " + assignsTo + " = " + initialExpression.queryFormJava7(env,true) + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = jType; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getJava7(); 
          return "  " + mode + rt + "[] " + assignsTo + " = new " + rt + "[1];"; 
        }
        return "  " + mode + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();"; } 
      else if (Type.isFunctionType(instanceType))
      { return "  " + mode + jType + " " + assignsTo + " = null;"; } 
      else if (Type.isBasicType(instanceType) || Type.isSetType(instanceType) || Type.isSequenceType(instanceType)) 
      { return "  " + mode + jType + " " + assignsTo + ";"; } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        if (ent.hasStereotype("external"))
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n"; } 
        else
        { return "  " + mode + jType + " " + assignsTo + " = new " + jType + "();\n" + "  Controller.inst().add" + jType + "(" + assignsTo + ");"; 
        }
      }  
    } 
    else if (createsInstanceOf.equals("boolean") || createsInstanceOf.equals("int") || createsInstanceOf.equals("long") || createsInstanceOf.equals("String") || createsInstanceOf.equals("double"))
    { return "  " + mode + createsInstanceOf + " " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  HashMap " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Function<String,Object> " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  Object[] " + assignsTo + " = new Object[1];"; }
    else if (createsInstanceOf.equals("OclAny"))
    { return "  Object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  Class " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  OclDate " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }
    return "  " + mode + createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");"; 
  }
  public String toStringCSharp()
  { String cstype = createsInstanceOf;
    if (instanceType != null)
    { String jType = instanceType.getCSharp(); 
      System.out.println(">>> Instance type: " + instanceType); 
      System.out.println(">>> C# type: " + jType); 
      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.toCSharp() + ";\n"; }
      else if (Type.isRefType(instanceType))
      { String rt = "object"; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getCSharp(); 
          if (Type.isBasicType(elemT) || elemT.isStructEntityType() || "Ref".equals(elemT.getName()))
          { return "  " + rt + "* " + assignsTo + ";"; }
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isBasicType(instanceType)) 
      { return "  " + jType + " " + assignsTo + ";"; } 
      else if (Type.isMapType(instanceType))
      { return "  Hashtable " + assignsTo + ";"; }
      else if (Type.isSetType(instanceType))
      { Type et = instanceType.getElementType(); 
        return "  HashSet<" + Type.getCSharptype(et) + "> " + assignsTo + ";"; 
      }
      else if (Type.isSequenceType(instanceType))
      { return "  ArrayList " + assignsTo + ";"; } 
      else if (Type.isFunctionType(instanceType))
      { String kt = "object"; 
        if (instanceType.getKeyType() != null) 
        { kt = instanceType.getKeyType().getCSharp(); } 
        String rt = "object"; 
        if (instanceType.getElementType() != null) 
        { rt = instanceType.getElementType().getCSharp(); } 
        return "  Func<" + kt + "," + rt + "> " + assignsTo + ";"; 
      }   
      else if (Type.isExceptionType(instanceType))
      { return "  " + jType + " " + assignsTo + ";"; }  
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity();
        String ename = ent.getName(); 
        if (ent.genericParameter)
        { return "  " + ename + " " + assignsTo + ";\n"; } 
        String gpars = ""; 
        if (ent.hasStereotype("external"))
        { return "  " + jType + gpars + " " + assignsTo + " = new " + jType + gpars + "();\n"; } 
        else
        { return "  " + jType + gpars + " " + assignsTo + " = new " + jType + gpars + "();\n" + "  Controller.inst().add" + ename + "(" + assignsTo + ");"; 
        } 
      } 
    } 
    else if (createsInstanceOf.startsWith("Set"))
    { return "  HashSet<object> " + assignsTo + ";"; }  
    else if (createsInstanceOf.startsWith("Sequence"))
    { return "  ArrayList " + assignsTo + ";"; } 
    else if (createsInstanceOf.startsWith("Map"))
    { return "  Hashtable "  + assignsTo + ";"; }
    else if (createsInstanceOf.startsWith("Function"))
    { return "  Func<object,object> " + assignsTo + ";"; }
    else if (createsInstanceOf.startsWith("Ref"))
    { return "  object* " + assignsTo + ";"; }
    if (createsInstanceOf.equals("boolean")) 
    { cstype = "bool"; 
      return "  " + cstype + " " + assignsTo + ";";   
    } 
    else if (createsInstanceOf.equals("String")) 
    { cstype = "string"; 
      return "  " + cstype + " " + assignsTo + ";"; 
    } 
    else if (createsInstanceOf.equals("int") || createsInstanceOf.equals("long") || createsInstanceOf.equals("double"))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclAny"))
    { return "  object " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  OclType " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclProcess"))
    { return "  OclProcess " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  DateTime " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclFile"))
    { return "  OclFile " + assignsTo + ";"; }
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; }
    return createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + "  Controller.inst().add" + createsInstanceOf + "(" + assignsTo + ");";  
  } 
  public String toStringCPP()  
  { String cstype = createsInstanceOf; 
    String cet = "void*"; 
    java.util.Map env = new java.util.HashMap(); 
    if (instanceType != null)
    { String jType = instanceType.getCPP(elementType); 
      if (initialExpression != null && assignsTo != null)
      { return "  " + jType + " " + assignsTo + " = " + initialExpression.queryFormCPP(env,true) + ";\n"; }
      if (Type.isBasicType(instanceType)) 
      { return "  " + jType + " " + assignsTo + ";"; } 
      else if (Type.isRefType(instanceType))
      { String rt = "void*"; 
        if (instanceType.getElementType() != null) 
        { Type elemT = instanceType.getElementType();
          rt = elemT.getCPP(); 
          if (Type.isBasicType(elemT) ||
              elemT.isStructEntityType() ||  
              "Ref".equals(elemT.getName()))
          { return "  " + rt + "* " + assignsTo + ";"; }
        }
        return "  " + rt + " " + assignsTo + ";";   
      }   
      else if (Type.isCollectionType(instanceType) || Type.isMapType(instanceType) || Type.isFunctionType(instanceType))
      { if (variable != null && elementType == null) 
        { elementType = variable.getElementType(); 
          jType = instanceType.getCPP(elementType);    
        }     
        return "  " + jType + " " + assignsTo + ";"; 
      } 
      else if (instanceType.isEntity())
      { Entity ent = instanceType.getEntity(); 
        String ename = ent.getName(); 
        if (ent.genericParameter)
        { return "  " + ename + " " + assignsTo + ";\n"; } 
        String gpars = ent.typeParameterTextCPP(); 
        if (initialExpression != null)
        { return "  " + jType + gpars + " " + assignsTo + " = " + initialExpression.toCPP() + ";\n"; }
        else if (ent.hasStereotype("external"))
        { return "  " + jType + gpars + " " + assignsTo + " = new " + ename + gpars + "();\n"; } 
        else
        { return "  " + jType + gpars + " " + assignsTo + " = new " + ename + gpars + "();\n" + 
                 "  Controller::inst->add" + ename + "(" + assignsTo + ");";
        }  
      } 
      else if (Type.isExceptionType(instanceType))
      { return "  " + jType + "* " + assignsTo + ";"; }  
      else if (instanceType.getName().equals("OclType"))
      { return "  OclType* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclDate"))
      { return "  OclDate* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclRandom"))
      { return "  OclRandom* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclFile"))
      { return "  OclFile* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclProcess"))
      { return "  OclProcess* " + assignsTo + ";"; }
      else if (instanceType.getName().equals("OclIterator"))
      { if (elementType != null) 
        { String celemt = elementType.getCPP(); 
          return "  OclIterator<" + celemt + ">* " + assignsTo + ";"; 
        } 
        if (variable != null && variable.getElementType() != null) 
        { String celemt = variable.getElementType().getCPP();
          return "  OclIterator<" + celemt + ">* " + assignsTo + ";"; 
        } 
        return "  OclIterator* " + assignsTo + ";"; 
      }
      else if (Type.isOclLibraryType(instanceType.getName()))
      { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }
    } 
    else if (elementType != null) 
    { cet = elementType.getCPP(); }
    else if (variable != null && variable.getElementType() != null) 
    { cet = variable.getElementType().getCPP(); }
    if (createsInstanceOf.startsWith("Set"))
    { return "  std::set<" + cet + ">* " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Sequence"))
    { return "  vector<" + cet + ">* " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Map"))
    { return "  map<string, " + cet + ">* " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Function"))
    { return "  function<" + cet + "(string)> " + assignsTo + ";"; } 
    if (createsInstanceOf.startsWith("Ref"))
    { return "  void* " + assignsTo + ";"; }
    if (createsInstanceOf.equals("boolean")) 
    { cstype = "bool"; 
      return "  " + cstype + " " + assignsTo + ";";   
    } 
    else if (createsInstanceOf.equals("String")) 
    { cstype = "string"; 
      return "  " + cstype + " " + assignsTo + ";"; 
    } 
    else if (createsInstanceOf.equals("int") || createsInstanceOf.equals("long") || createsInstanceOf.equals("double"))
    { return "  " + createsInstanceOf + " " + assignsTo + ";"; } 
    else if (createsInstanceOf.equals("OclAny"))
    { return "  void* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclType"))
    { return "  OclType* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclDate"))
    { return "  OclDate* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclRandom"))
    { return "  OclRandom* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclFile"))
    { return "  OclFile* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclIterator"))
    { return "  OclIterator<" + cet + ">* " + assignsTo + ";"; }
    else if (createsInstanceOf.equals("OclProcess"))
    { return "  OclProcess* " + assignsTo + ";"; }
    else if (Type.isExceptionType(createsInstanceOf))
    { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }  
    else if (Type.isOclLibraryType(createsInstanceOf))
    { return "  " + createsInstanceOf + "* " + assignsTo + ";"; }
    return createsInstanceOf + " " + assignsTo + " = new " + createsInstanceOf + "();\n" + "  Controller::inst->add" + createsInstanceOf + "(" + assignsTo + ");"; 
  } 
  public void display()
  { System.out.print(toString()); }
  public void display(PrintWriter out)
  { out.print(toString()); } 
  public void displayJava(String target)
  { System.out.println(toStringJava()); } 
  public void displayJava(String target, PrintWriter out)
  { out.println(toStringJava()); } 
  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { Attribute att = 
      new Attribute(assignsTo,instanceType, ModelElement.INTERNAL); 
    Type typ = Type.getTypeFor(createsInstanceOf, types, entities); 
    if (instanceType == null && typ != null) 
    { instanceType = typ; } 
    if (elementType != null) 
    { instanceType.setElementType(elementType); } 
    else if (typ != null) 
    { elementType = typ.elementType; 
      if ("String".equals(typ.getName()))
      { elementType = new Type("String", null); } 
    }
    if (instanceType == null) 
    { att.setType(typ); } 
    if (elementType != null) 
    { att.setElementType(elementType); } 
    variable = att; 
    env.add(att); 
    if (initialExpression != null) 
    { initialExpression.typeCheck(types,entities,ctxs,env); }
    return true; 
  }  
  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { Attribute att = new Attribute(assignsTo,instanceType, ModelElement.INTERNAL); 
    Type typ = Type.getTypeFor(createsInstanceOf, types, entities); 
    if (instanceType == null && typ != null) 
    { instanceType = typ; } 
    if (elementType != null) 
    { instanceType.setElementType(elementType); } 
    else if (typ != null) 
    { elementType = typ.elementType; 
      if ("String".equals(typ.getName()))
      { elementType = new Type("String", null); } 
    }
    if (instanceType == null) 
    { att.setType(typ); } 
    if (elementType != null) 
    { att.setElementType(elementType); } 
    if (initialExpression != null) 
    { initialExpression.typeInference(types,entities, ctxs,env,vartypes); 
      System.out.println(">>> Inferred type " + initialExpression.getType() + "(" + initialExpression.getElementType() + ") for variable " + att); 
      Type initType = initialExpression.getType(); 
      Type initElemType = initialExpression.getElementType(); 
      if (!Type.isVacuousType(initType))
      { instanceType = initType;  
        att.setType(instanceType);
      } 
      if (!Type.isVacuousType(initElemType))
      { elementType = initElemType;
        instanceType.setElementType(initElemType);  
        att.setElementType(initElemType); 
      } 
    } 
    variable = att; 
    env.add(att); 
    vartypes.put(assignsTo, att.getType()); 
    return true; 
  }  
  public Expression wpc(Expression post)
  { return post; }
  public Expression wpc(Expression inv, Expression post)
  { return inv; }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { 
    if (initialExpression != null && assignsTo != null)
    { if (vars.contains("" + assignsTo))
      { Vector vused = 
            initialExpression.allAttributesUsedIn(); 
        Vector result = new Vector(); 
        result.addAll(vused); 
        Vector vs = initialExpression.getVariableUses(); 
        result = VectorUtil.union(result,vs);
        result.remove("" + assignsTo); 
        result = VectorUtil.union(result,vars);
        result.remove("" + assignsTo); 
        return result; 
      } 
    }     
    return vars; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { 
    if (initialExpression != null && assignsTo != null)
    { String lv = "" + assignsTo; 
      if (vars.contains(lv))
      { Vector vused = 
            initialExpression.allAttributesUsedIn(); 
        Vector result = new Vector(); 
        result.addAll(vused); 
        Vector vs = initialExpression.getVariableUses(); 
        result = VectorUtil.union(result,vs);
        result.remove(lv); 
        Vector readBEs = initialExpression.allReadBasicExpressionData(); 
        for (int i = 0; i < readBEs.size(); i++) 
        { String rv = "" + readBEs.get(i); 
          dlin.add_pair(rv, lv);
        } 
        for (int i = 0; i < result.size(); i++) 
        { String rv = "" + result.get(i); 
          mp.add_pair(rv, lv); 
        } 
        result = VectorUtil.union(result,vars);
        result.remove("" + assignsTo); 
        return result; 
      } 
    }     
    return vars; 
  }  
  public boolean updates(Vector vars) 
  { if (vars.contains("" + assignsTo))
    { return true; }
    return false; 
  } 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, Vector vars)
  { return toStringJava(); }  
  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava6(); }  
  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava7(); }  
  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringCSharp(); }  
  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringCPP(); }  
  public Vector readFrame()
  { Vector res = new Vector(); 
    String declType = createsInstanceOf; 
    if (initialExpression != null) 
    { res.addAll(initialExpression.readFrame()); }  
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    String declType = createsInstanceOf; 
    Vector declTypes = new Vector(); 
    declTypes.add(declType); 
    if (assignsTo != null)
    { res.add(assignsTo); } 
    res.removeAll(declTypes); 
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity()
  { if (initialExpression == null)
    { return 3; } 
    int syncomp = initialExpression.syntacticComplexity(); 
    return 3 + syncomp; 
  } 
  public int cyclomaticComplexity()
  { return 0; } 
  public int epl()
  { return 1; }  
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.allOperationsUsedIn()); }  
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.allAttributesUsedIn()); }  
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getUses(var)); }  
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getVariableUses()); }  
    return res; 
  } 
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.getVariableUses()); }  
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (initialExpression != null) 
    { res.addAll(initialExpression.equivalentsUsedIn()); }  
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    if (assignsTo != null) 
    { if (assignsTo.startsWith("_") && assignsTo.length() == 2 && Character.isDigit(assignsTo.charAt(1)))
      { res.add(assignsTo); }
      else if (assignsTo.startsWith("_") && assignsTo.length() == 3 && Character.isDigit(assignsTo.charAt(1)) && Character.isDigit(assignsTo.charAt(2)))
      { res.add(assignsTo); } 
    } 
    if (instanceType != null) 
    { res.addAll(instanceType.metavariables()); }  
    if (initialExpression != null) 
    { res.addAll(initialExpression.metavariables()); }  
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    if (assignsTo != null) 
    { args.add(assignsTo); } 
    if (instanceType != null) 
    { args.add(instanceType); }
    if (initialExpression != null) 
    { args.add(initialExpression); }  
    return args; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector(); 
    if (assignsTo != null) 
    { args.add(assignsTo); 
      eargs.add(assignsTo); 
    } 
    if (instanceType != null) 
    { args.add(instanceType.cg(cgs)); 
      eargs.add(instanceType); 
    }
    if (initialExpression != null) 
    { args.add(initialExpression.cg(cgs)); 
      eargs.add(initialExpression); 
    }  
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext;
  } 
}
