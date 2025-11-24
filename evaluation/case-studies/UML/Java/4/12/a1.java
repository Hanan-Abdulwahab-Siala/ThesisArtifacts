class AssertStatement extends Statement
{ 
  Expression condition = null; 
  Expression message = null; 
  public AssertStatement(Expression expr) 
  { condition = expr; } 
  public AssertStatement(Expression expr, Expression msg) 
  { condition = expr; 
    message = msg; 
  } 
  public void display()
  { 
    if (message == null) 
    { System.out.println("  assert " + condition); } 
    else 
    { System.out.println("  assert " + condition + " do " + message); } 
  }
  public String getOperator() 
  { return "assert"; } 
  public Object clone() 
  { return new AssertStatement(condition,message); } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (condition != null) 
    { condition.energyUse(uses, rUses, aUses); 
      int res = condition.syntacticComplexity();
      if (res > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + res + ") for " + condition + "\n" +  
                  ">>> Recommend OCL refactoring");  
      } 
    }
    if (message != null)
    { message.energyUse(uses, rUses, aUses); } 
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (condition != null) 
    { condition.collectionOperatorUses(lev, uses, vars); } 
    if (message != null) 
    { message.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 
  public Statement dereference(BasicExpression var) 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.dereference(var); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.dereference(var); }
    return new AssertStatement(newcond,newmessage); 
  }  
  public Statement optimiseOCL() 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.simplifyOCL(); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.simplifyOCL(); }
    return new AssertStatement(newcond,newmessage); 
  }  
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl) 
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = 
         condition.addContainerReference(ref,var,excl);
    }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = 
         message.addContainerReference(ref,var,excl); 
    }
    return new AssertStatement(newcond,newmessage); 
  }  
  public Statement substituteEq(String oldE, Expression newE)
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.substituteEq(oldE,newE); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.substituteEq(oldE,newE); }
    return new AssertStatement(newcond,newmessage); 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression newcond = condition; 
    if (condition != null) 
    { newcond = condition.removeSlicedParameters(bf,fpars); }
    Expression newmessage = message; 
    if (message != null) 
    { newmessage = message.removeSlicedParameters(bf,fpars); }
    return new AssertStatement(newcond,newmessage); 
  } 
  public String toString()
  { if (message == null) 
    { return "  assert " + condition; } 
    else 
    { return "  assert " + condition + " do " + message; }
  }
  public String toAST()
  { String res = ""; 
    if (message == null)
    { res = "(OclStatement assert " + condition.toAST() + " )"; } 
    else
    { res = "(OclStatement assert " + condition.toAST() + " do " + message.toAST() + " )"; } 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr) 
  { return condition.containsSubexpression(expr); } 
  public Vector singleMutants()
  { if (condition == null) 
    { return new Vector(); } 
    Vector exprs = condition.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new AssertStatement(mvalue,message)); 
    } 
    return res; 
  } 
  public void display(PrintWriter out)
  { if (message == null) 
    { out.println("  assert " + condition); } 
    else 
    { out.println("  assert " + condition + " do " + message); }
  }
  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }
  public void displayJava(String t)
  { if (message == null) 
    { System.out.println("  assert " + condition); } 
    else 
    { System.out.println("  assert " + condition + " : " + message); } 
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("assertstatement_"); 
    out.println(res + " : AssertStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (condition == null) 
    {  } 
    else 
    { String expId = condition.saveModelData(out);
      out.println(res + ".condition = " + expId);
    } 
    if (message == null) 
    { } 
    else 
    { String expIdm = message.saveModelData(out);
      out.println(expIdm + " : " + res + ".message");
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); } 
  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    String qf = condition.queryForm(env,true); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryForm(env,true); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    } 
  }
  public String toEtl()
  { return ""; }
  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { condition.typeCheck(types,entities,cs,env); 
    if (message != null) 
    { message.typeCheck(types,entities,cs,env); } 
    return true;
  } 
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { condition.typeInference(types,entities,cs,env,vartypes); 
    if (message != null) 
    { message.typeInference(types,entities,cs,env,vartypes); } 
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
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String qf = condition.queryForm(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryForm(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }
  public String updateFormJava6(java.util.Map env, boolean local)
  { String qf = condition.queryFormJava6(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryFormJava6(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { String qf = condition.queryFormJava7(env,local); 
    if (message == null) 
    { return "    assert " + qf + ";\n"; }
    else 
    { String mqf = message.queryFormJava7(env,local); 
      return "    assert " + qf + " : " + mqf + ";\n"; 
    }
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String qf = condition.queryFormCSharp(env,local); 
    if (message == null) 
    { return "    Debug.Assert(" + qf + ");\n"; }
    else 
    { String mqf = message.queryFormCSharp(env,local); 
      return "    Debug.Assert(" + qf + ", " + mqf + ");\n"; 
    }
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { String qf = condition.queryFormCPP(env,local); 
    return "    assert(" + qf + ");";
  }
  public Vector readFrame()
  { Vector res = new Vector();
    res = condition.readFrame(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.readFrame()); }  
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    res = condition.writeFrame(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.writeFrame()); }
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity()
  { int res = condition.syntacticComplexity();
    res++; 
    if (message != null) 
    { return res + message.syntacticComplexity(); } 
    return res;
  } 
  public int cyclomaticComplexity()
  { return 1; } 
  public int epl()
  { return 0; } 
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    res = condition.allOperationsUsedIn(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.allOperationsUsedIn()); } 
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    res = condition.allAttributesUsedIn(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.allAttributesUsedIn()); } 
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    res = condition.getUses(var); 
    if (message != null) 
    { res = VectorUtil.union(res, message.getUses(var)); } 
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    res = condition.getVariableUses(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.getVariableUses()); } 
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    res = condition.equivalentsUsedIn(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.equivalentsUsedIn()); } 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    res = condition.metavariables(); 
    if (message != null) 
    { res = VectorUtil.union(res, message.metavariables()); } 
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    if (condition != null) 
    { args.add(condition); } 
    if (message != null) 
    { args.add(message); }
    return args; 
  } 
  public Vector cgterms()
  { Vector args = new Vector();
    args.add("assert"); 
    if (condition != null) 
    { args.add(condition); } 
    if (message != null) 
    { args.add("do"); 
      args.add(message); 
    }
    return args; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (condition != null) 
    { args.add(condition.cg(cgs)); } 
    if (message != null) 
    { args.add(message.cg(cgs)); } 
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public void findClones(java.util.Map clones, String op, String rule)
  { if (condition != null)
    { condition.findClones(clones,op,rule); } 
    if (message != null)
    { message.findClones(clones,op,rule); }
  } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { if (condition != null)
    { condition.findClones(clones,cdefs,op,rule); } 
    if (message != null)
    { message.findClones(clones,cdefs,op,rule); }
  } 
}