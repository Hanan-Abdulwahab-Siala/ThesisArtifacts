class ImplicitInvocationStatement extends Statement
{ Expression callExp; 
  public ImplicitInvocationStatement(Expression ee)
  { callExp = ee; } 
  public ImplicitInvocationStatement(String ss)
  { callExp = new BasicExpression(ss); } 
  public void setEntity(Entity ent)
  { entity = ent; 
    callExp.setEntity(ent); 
  } 
  public String getOperator() 
  { return "execute"; } 
  public Expression getCallExp() 
  { return callExp; } 
  public boolean isSkip()
  { if ("true".equals(callExp + "")) 
    { return true; } 
    return false; 
  } 
  public Object clone()
  { ImplicitInvocationStatement res = 
      new ImplicitInvocationStatement(callExp);
    res.entity = entity; 
    return res; 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (callExp == null || 
        callExp.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    callExp.findClones(clones,rule,op); 
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String rule, String op)
  { if (callExp == null || 
        callExp.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    callExp.findClones(clones,cloneDefs,rule,op); 
  }
  public Vector allVariableNames()
  { return callExp.allVariableNames(); } 
  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { callExp.energyUse(uses, rUses, oUses); 
    int syncomp = callExp.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + callExp); 
      System.err.println(">>> Recommend OCL refactoring");
      System.err.println();  
    } 
    return uses; 
  }  
  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { callExp.collectionOperatorUses(nestingLevel, 
                                   operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { callExp.findMagicNumbers(mgns, this + "", op); } 
  public Statement dereference(BasicExpression var)
  { ImplicitInvocationStatement res = 
      new ImplicitInvocationStatement(callExp.dereference(var));
    res.entity = entity; 
    return res; 
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { callExp.execute(sigma, beta); 
    return Statement.NORMAL; 
  }
  public Statement substituteEq(String oldE, Expression newE)
  { Expression newExp = callExp.substituteEq(oldE,newE); 
    return new ImplicitInvocationStatement(newExp); 
  } 
  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { Expression newExp = 
      callExp.removeSlicedParameters(op,fpars); 
    return new ImplicitInvocationStatement(newExp); 
  } 
  public Statement addContainerReference(
                      BasicExpression ref, String var,
                      Vector excl)
  { Expression newExp = 
        callExp.addContainerReference(ref,var,excl); 
    return new ImplicitInvocationStatement(newExp); 
  } 
  public Statement optimiseOCL()
  { Expression cexp = callExp.simplifyOCL(); 
    return new ImplicitInvocationStatement(cexp); 
  }
  public String toString()    
  { String res = "execute ( " + callExp + " )"; 
    return res; 
  } 
  public String toAST()
  { String res = "(OclStatement execute " + callExp.toAST() + " )"; 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr) 
  { return callExp.containsSubexpression(expr); } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    Vector exprs = callExp.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression expr = (Expression) exprs.get(i); 
      res.add(new ImplicitInvocationStatement(expr)); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("implicitcallstatement_"); 
    out.println(res + " : ImplicitCallStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    if (callExp != null)
    { String callid = callExp.saveModelData(out); 
      out.println(res + ".callExp = " + callid);
    }
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }
  public String bupdateForm()
  { return " " + callExp; }   
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return callExp.bupdateForm(env,local); 
  } 
  public String toStringJava() 
  { String res = "execute ( " + callExp + " )"; 
    return res; 
  } 
  public String toEtl() 
  { String res = "  " + callExp + ";"; 
    return res; 
  } 
  public String toStringJava(String targ)
  { return toStringJava(); }
  public void display()
  { 
    System.out.print(toString()); 
  }
  public void display(PrintWriter out)
  { out.print(toString()); }
  public void displayJava(String targ)
  { if (targ != null) 
    { System.out.print(toStringJava(targ)); }
    else 
    { System.out.print(toStringJava()); } 
  }
  public void displayJava(String targ, PrintWriter out)
  { if (targ != null) 
    { out.print(toStringJava(targ)); }
    else 
    { out.print(toStringJava()); }  
  }
  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { if (callExp != null)
    { callExp.typeCheck(types,entities,ctxs,env); } 
    return true;
  }  
  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { if (callExp != null)
    { callExp.typeInference(types,entities,
                            ctxs,env,vartypes); 
    } 
    return true;
  }  
  public Expression wpc(Expression post)
  { return post; }
  public Expression wpc(Expression inv, Expression post)
  { return inv; }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  
  public boolean updates(Vector v) 
  { return false; } 
  public Statement generateDesign(java.util.Map env, boolean local)
  { return callExp.generateDesign(env,local); }  
  public Statement statLC(java.util.Map env, boolean local)
  { return callExp.statLC(env,local); }  
  public String updateForm(java.util.Map env, 
                      boolean local, 
                      Vector types, Vector entities, 
                      Vector vars)
  { if (callExp != null)
    { String uf = callExp.updateForm(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }
  public String updateFormJava6(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormJava6(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormJava7(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormCSharp(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { if (callExp != null)
    { String uf = callExp.updateFormCPP(env,local);
      return "   " + uf;   
    } 
    else 
    { return toStringJava(); }  
  }
  public Vector allPreTerms()
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(); 
  }  
  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.allPreTerms(var); 
  }  
  public Vector readFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.readFrame(); 
  } 
  public Vector writeFrame() 
  { Vector res = new Vector();
    if (callExp == null) 
    { return res; } 
    return callExp.writeFrame(); 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { if (callExp == null) { return this; } 
    Expression ce = callExp.replaceModuleReferences(uc);
    return new ImplicitInvocationStatement(ce); 
  } 
  public int syntacticComplexity()
  { if (callExp == null) 
    { return 1; } 
    int syncomp = callExp.syntacticComplexity(); 
    return syncomp + 1; 
  } 
  public int cyclomaticComplexity()
  { return 0; }  
  public int epl()
  { return 0; }  
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.allOperationsUsedIn(); 
  } 
  public Vector getUses(String var) 
  { if (callExp != null) 
    { return callExp.getUses(var); } 
    return new Vector();
  } 
  public Vector getVariableUses() 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector();
  } 
  public Vector getVariableUses(Vector unused) 
  { if (callExp != null) 
    { return callExp.getVariableUses(); } 
    return new Vector();
  } 
  public Vector allAttributesUsedIn() 
  { if (callExp != null) 
    { return callExp.allAttributesUsedIn(); } 
    return new Vector(); 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (callExp == null) 
    { return res; } 
    return callExp.equivalentsUsedIn(); 
  } 
  public Vector metavariables()
  { Vector res = new Vector();
    if (callExp != null) 
    { return callExp.metavariables(); }  
    return res; 
  } 
  public String cg(CGSpec cgs)
  { 
    String etext = this + "";
    Vector eargs = new Vector();
    Vector args = new Vector();
    if (callExp != null) 
    { callExp.setBrackets(false); 
      eargs.add(callExp); 
      args.add(callExp.cg(cgs)); 
    }
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    java.util.Map env = new java.util.HashMap(); 
    Statement stat = callExp.generateDesign(env,true); 
    if (stat != null) 
    { return stat.cg(cgs); } 
    return etext;
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    if (callExp != null) 
    { args.add(callExp); } 
    return args; 
  } 
}