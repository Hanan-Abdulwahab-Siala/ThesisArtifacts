class ErrorStatement extends Statement
{ 
  Expression thrownObject = null; 
  public ErrorStatement(Expression expr) 
  { thrownObject = expr; } 
  public void display()
  { 
    System.out.println("  error " + thrownObject); 
  }
  public String getOperator() 
  { return "error"; } 
  public Object clone() 
  { return new ErrorStatement(thrownObject); } 
  public Statement dereference(BasicExpression var) 
  { if (thrownObject != null) 
    { return new ErrorStatement(thrownObject.dereference(var)); }
    return new ErrorStatement(null); 
  }  
  public Statement substituteEq(String oldE, Expression newE)
  { if (thrownObject != null) 
    { Expression tobj = thrownObject.substituteEq(oldE,newE); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 
  public Statement optimiseOCL()
  { if (thrownObject != null) 
    { Expression tobj = thrownObject.simplifyOCL(); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (thrownObject != null) 
    { thrownObject.energyUse(uses, rUses, aUses);
      int syncomp = thrownObject.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + thrownObject + "\n" +  
                  ">>> Recommend OCL refactoring");  
      }
    }
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                 java.util.Map uses, 
                                 Vector vars)
  { if (thrownObject != null) 
    { thrownObject.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { if (thrownObject != null) 
    { Expression tobj = 
          thrownObject.removeSlicedParameters(bf,fpars); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 
  public Statement addContainerReference(
                     BasicExpression ref, 
                     String var, Vector excl)
  { if (thrownObject != null) 
    { Expression tobj = 
         thrownObject.addContainerReference(ref,var,excl); 
      return new ErrorStatement(tobj); 
    } 
    return new ErrorStatement(null); 
  } 
  public String toString()
  { return "  error " + thrownObject; }
  public String toAST()
  { String res = "(OclStatement error " + thrownObject.toAST() + " )"; 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr)
  { if (thrownObject == null) 
    { return thrownObject.containsSubexpression(expr); } 
    return false; 
  } 
  public Vector singleMutants()
  { if (thrownObject == null) 
    { return new Vector(); } 
    Vector exprs = thrownObject.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mvalue = (Expression) exprs.get(i); 
      res.add(new ErrorStatement(mvalue)); 
    } 
    return res; 
  } 
  public void display(PrintWriter out)
  { out.println("  error " + thrownObject); }
  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }
  public void displayJava(String t)
  { if (thrownObject == null) 
    { System.out.println("  throw null;"); } 
    else 
    { java.util.Map env = new java.util.HashMap(); 
      String qf = thrownObject.throwQueryForm(env,true); 
      System.out.println("  throw " + qf + ";"); 
    } 
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("errorstatement_"); 
    out.println(res + " : ErrorStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (thrownObject == null) 
    { out.println(res + ".thrownObject = null"); } 
    else 
    { String expId = thrownObject.saveModelData(out);
      out.println(res + ".thrownObject = " + expId);
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); } 
  public String toStringJava()
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { java.util.Map env = new java.util.HashMap(); 
      String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }
  public String toEtl()
  { return ""; }
  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { if (thrownObject != null) 
    { thrownObject.typeCheck(types,entities,cs,env); } 
    return true;
  } 
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (thrownObject != null) 
    { thrownObject.typeInference(types,entities,
                                 cs,env,vartypes); 
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
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
 }
  public String updateFormJava6(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryForm(env,true); 
      return "  throw " + qf + ";"; 
    }
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryFormCSharp(env,true); 
      return "  throw " + qf + ";"; 
    } 
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { if (thrownObject == null) 
    { return "  throw null;"; } 
    else 
    { String qf = thrownObject.throwQueryFormCPP(env,true); 
      return "  throw " + qf + ";"; 
    }
  }
  public Vector readFrame()
  { Vector res = new Vector();
    if (thrownObject != null) 
    { res.addAll(thrownObject.readFrame()); }  
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity()
  { if (thrownObject != null) 
    { int syncomp = thrownObject.syntacticComplexity(); 
      return 1 + syncomp; 
    }
    return 1;
  } 
  public int cyclomaticComplexity()
  { return 0; } 
  public int epl()
  { return 0; } 
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.allOperationsUsedIn(); } 
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.allAttributesUsedIn(); } 
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.getUses(var); } 
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.getVariableUses(); } 
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.equivalentsUsedIn(); } 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    if (thrownObject != null) 
    { res = thrownObject.metavariables(); } 
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    if (thrownObject != null) 
    { args.add(thrownObject); } 
    return args; 
  } 
  public Vector cgterms()
  { Vector args = new Vector();
    args.add("error"); 
    if (thrownObject != null) 
    { args.add(thrownObject); } 
    return args; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (thrownObject != null) 
    { args.add(thrownObject.cg(cgs)); } 
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public void findClones(java.util.Map clones, String op, String rule)
  { if (thrownObject != null) 
    { thrownObject.findClones(clones,op,rule); }
  }  
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { if (thrownObject != null) 
    { thrownObject.findClones(clones,cdefs,op,rule); }
  }  
}