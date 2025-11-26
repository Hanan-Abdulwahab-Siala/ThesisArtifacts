class ReturnStatement extends Statement
{ Expression value = null; 
  public ReturnStatement()
  { value = null; } 
  public ReturnStatement(Expression e)
  { value = e; } 
  public ReturnStatement(Vector exprs)
  { if (exprs == null || exprs.size() == 0) 
    { value = null; } 
    value = (Expression) exprs.get(0); 
  } 
  public String getOperator() 
  { return "return"; } 
  public Expression getExpression() 
  { return value; } 
  public Expression getReturnValue() 
  { return value; } 
  public Expression getValue() 
  { return value; } 
  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { if (value != null)
    { Expression expr = value.evaluate(sigma, beta); 
      beta.setVariableValue("result", expr); 
    } 
    return Statement.RETURN; 
  } 
  public Expression definedness()
  { if (value != null) 
    { return value.definedness(); } 
    return new BasicExpression(true); 
  } 
  public Object clone()
  { return new ReturnStatement(value); } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (value == null || 
        value.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    value.findClones(clones,rule,op); 
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String rule, String op)
  { if (value == null || 
        value.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    value.findClones(clones,cloneDefs,rule,op); 
  }
  public Map energyUse(Map uses, 
                       Vector rUses, Vector oUses)
  { if (value == null) 
    { return uses; } 
    value.energyUse(uses, rUses, oUses); 
    int syncomp = value.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + value); 
      System.err.println(">>> Recommend OCL refactoring"); 
    } 
    return uses; 
  }  
  public Statement optimiseOCL()
  { if (value == null) 
    { return this; } 
    Expression newval = value.simplifyOCL(); 
    return new ReturnStatement(newval); 
  }  
  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel,
                             Vector vars)
  { if (value == null) 
    { return operatorsAtLevel; } 
    value.collectionOperatorUses(nestingLevel, 
                                 operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (value == null) 
    { return; }
    String val = this + ""; 
    value.findMagicNumbers(mgns,val,op); 
  }
  public boolean hasValue()
  { return value != null; } 
  public void display()
  { System.out.print("  return"); 
    if (value != null)
    { System.out.print(" " + value); } 
    System.out.println(";"); 
  }  
  public void display(PrintWriter out)
  { out.print("  return"); 
    if (value != null)
    { out.print(" " + value); } 
    out.println(";"); 
  }  
  public void displayJava(String t)
  { display(); }  
  public void displayJava(String t, PrintWriter out)
  { display(out); }  
  public Statement substituteEq(String oldE, Expression newE)
  { if (value != null)
    { Expression newval = value.substituteEq(oldE,newE); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  } 
  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { if (value != null)
    { Expression newval = 
                 value.removeSlicedParameters(op,fpars); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  } 
  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var,
                                  Vector excludes)
  { if (value != null)
    { Expression newval = value.addContainerReference(
                                    ref,var,excludes); 
      ReturnStatement res = new ReturnStatement(newval);
      res.setEntity(entity); 
      return res;  
    } 
    return this; 
  }  
  public String toString()
  { if (value == null)
    { return "return "; } 
    return "return " + value;
  } 
  public String toAST()
  { String res = ""; 
    if (value == null)
    { res = "(OclStatement return)"; } 
    else 
    { res = "(OclStatement return " + value.toAST() + ")"; } 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr) 
  { if (value == null) 
    { return false; } 
    return value.containsSubexpression(expr); 
  } 
  public Vector singleMutants()
  { if (value == null) 
    { return new Vector(); } 
    Vector exprs = value.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new ReturnStatement(mvalue)); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("returnstatement_"); 
    out.println(res + " : ReturnStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    if (value != null) 
    { String valueid = value.saveModelData(out); 
      out.println(valueid + " : " + res + ".returnValue"); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }
  public String bupdateForm()
  { return " "; } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("skip"); } 
  public String toStringJava()
  { String res = "  return"; 
    if (value != null)
    { java.util.Map env = new java.util.HashMap(); 
      if (entity != null) 
      { env.put(entity.getName(),"this"); 
        res = res + " " + value.queryForm(env,true);
      }
      else 
      { res = res + " " + value.queryForm(env,true); } 
    } 
    res = res + ";"; 
    return res; 
  }  
  public String toEtl()
  { String res = "  return"; 
    if (value != null)
    { res = res + " " + value; } 
    res = res + ";"; 
    return res; 
  }  
  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { if (value == null) { return true; } 
    return value.typeCheck(types,entities,ctxs,env); 
  }  
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (value == null) { return true; } 
    value.typeInference(types,entities,cs,env,vartypes);
    vartypes.put("result", value.getType()); 
    return true;  
  } 
  public void displayImp(String var, PrintWriter out) 
  { } 
  public Expression wpc(Expression post)
  { return post; }  
  public Expression wpc(Expression inv, Expression post)
  { return post; }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  
  public boolean updates(Vector v) 
  { return false; } 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryForm(env,local); } 
    res = res + ";"; 
    return res; 
  }  
  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormJava6(env,local); } 
    res = res + ";"; 
    return res; 
  }  
  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormJava7(env,local); } 
    res = res + ";"; 
    return res; 
  }  
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormCSharp(env,local); } 
    res = res + ";"; 
    return res; 
  }  
  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = "    return"; 
    if (value != null)
    { res = res + " " + value.queryFormCPP(env,local); } 
    res = res + ";"; 
    return res; 
  }  
  public Vector allPreTerms()
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allPreTerms(); 
  }  
  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allPreTerms(var); 
  }  
  public Statement dereference(BasicExpression var)
  { if (value == null) 
    { return new ReturnStatement(value); }
    Expression val = value.dereference(var); 
    return new ReturnStatement(val); 
  }  
  public Vector metavariables()
  { Vector res = new Vector(); 
    if (value != null) 
    { return value.metavariables(); }  
    return res; 
  } 
  public Vector readFrame() 
  { Vector res = new Vector();
    if (value == null) 
    { return res; } 
    return value.allReadFrame(); 
  } 
  public Vector writeFrame() 
  { Vector res = new Vector();
    return res;
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { if (value == null) 
    { return this; } 
    Expression val = value.checkConversions(propType,propElemType,interp); 
    return new ReturnStatement(val); 
  }   
  public Statement replaceModuleReferences(UseCase uc)
  { if (value == null) 
    { return this; } 
    Expression val = value.replaceModuleReferences(uc); 
    return new ReturnStatement(val); 
  }   
  public int syntacticComplexity()
  { if (value == null) 
    { return 1; } 
    int syncomp = value.syntacticComplexity(); 
    return syncomp + 1; 
  } 
  public int cyclomaticComplexity()
  { return 0; }  
  public int epl()
  { return 0; }  
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allOperationsUsedIn(); 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getUses(var); 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getVariableUses(); 
  } 
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.getVariableUses(); 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allAttributesUsedIn(); 
  } 
  public Vector allFeaturesUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allFeaturesUsedIn(); 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.equivalentsUsedIn(); 
  } 
  public Vector allVariableNames()
  { Vector res = new Vector(); 
    if (value == null) 
    { return res; } 
    return value.allVariableNames(); 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (value != null) 
    { args.add(value.cg(cgs)); } 
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public Vector cgparameters()
  { Vector args = new Vector();
    if (value != null) 
    { args.add(value); } 
    return args; 
  } 
}
