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
}
