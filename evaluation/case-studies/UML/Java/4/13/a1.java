class CatchStatement extends Statement
{ 
  Expression caughtObject = null; 
  Statement action = null; 
  public CatchStatement(Expression expr, Statement stat) 
  { caughtObject = expr; 
    action = stat; 
  } 
  public CatchStatement(Expression var, Vector stats) 
  { Type t = new Type("OclAny", null);
    if (var.getType() != null) 
    { t = var.getType(); } 
    caughtObject = 
      new BinaryExpression(":", var, new BasicExpression(t)); 
    if (stats.size() == 0) 
    { action = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { action = (Statement) stats.get(0); } 
    else 
    { action = new SequenceStatement(stats); }  
  } 
  public void display()
  { System.out.println("  catch ( " + caughtObject + ") do " + action); 
  }
  public String toString()
  { return "  catch ( " + caughtObject + ") do " + action; }
  public String getOperator() 
  { return "catch"; } 
  public String toAST()
  { String res = "(OclStatement catch " + caughtObject.toAST() + " )"; 
    return res; 
  } 
  public boolean containsSubexpression(Expression expr) 
  { if (caughtObject.containsSubexpression(expr))
    { return true; }
    return action.containsSubexpression(expr);
  } 
  public Vector singleMutants()
  { return new Vector(); }
  public Object clone() 
  { return new CatchStatement(caughtObject,action); } 
  public Statement dereference(BasicExpression var) 
  { return 
      new CatchStatement(caughtObject.dereference(var), 
                         action.dereference(var)); 
  }
  public Statement optimiseOCL() 
  { Expression cobj = null; 
    if (caughtObject != null) 
    { cobj = caughtObject.simplifyOCL(); }
    Statement cact = null;  
    if (action != null) 
    { cact = action.optimiseOCL(); } 
    return new CatchStatement(cobj,cact); 
  }
  public void findClones(java.util.Map clones, String rule, String op)
  { if (action != null)
    { action.findClones(clones,rule,op); }
  } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (action != null)
    { action.findClones(clones,cdefs,rule,op); }
  } 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (action != null)
    { action.findMagicNumbers(mgns, this + "", op); }
  } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (action != null) 
    { action.energyUse(uses, rUses, aUses); } 
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (action != null) 
    { action.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excls) 
  { Vector newexcls = new Vector();
    newexcls.addAll(excls); 
    if (caughtObject instanceof BinaryExpression)
    { BinaryExpression ex = (BinaryExpression) caughtObject; 
      if (":".equals(ex.getOperator()))
      { newexcls.add(ex.getLeft() + ""); } 
    }  
    Statement newact = action.addContainerReference(ref,var,
                                                 newexcls); 
    return new CatchStatement(caughtObject, newact); 
  }
  public Statement substituteEq(String oldE, Expression newE)
  { Expression cobj = caughtObject.substituteEq(oldE,newE); 
    Statement astat = action.substituteEq(oldE,newE); 
    return new CatchStatement(cobj,astat); 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression cobj = 
       caughtObject.removeSlicedParameters(bf,fpars); 
    Statement astat = action.removeSlicedParameters(bf,fpars); 
    return new CatchStatement(cobj,astat); 
  } 
  public void display(PrintWriter out)
  { out.println("  catch (" + caughtObject + ") do " + action); }
  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }
  public void displayJava(String t)
  { java.util.Map env = new java.util.HashMap(); 
    String qf = caughtObject.declarationQueryForm(env,true); 
    System.out.println("  catch (" + qf + ") { ");
    action.displayJava(t); 
    System.out.println("  }");  
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("catchstatement_"); 
    out.println(res + " : CatchStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (caughtObject == null) 
    { out.println(res + ".caughtObject = null"); } 
    else 
    { String expId = caughtObject.saveModelData(out);
      out.println(res + ".caughtObject = " + expId);
    } 
    if (action == null) 
    { out.println(res + ".action = null"); } 
    else 
    { String sId = action.saveModelData(out);
      out.println(res + ".action = " + sId);
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("catchstatement_"); 
    out.println(res + " : CatchStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (caughtObject == null) 
    { out.println(res + ".caughtObject = null"); } 
    else 
    { String expId = caughtObject.saveModelData(out);
      out.println(res + ".caughtObject = " + expId);
    } 
    if (action == null) 
    { out.println(res + ".action = null"); } 
    else 
    { String sId = action.saveModelData(out, ent);
      out.println(res + ".action = " + sId);
    } 
    return res; 
  } 
  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.toStringJava() + "\n" + 
           "    }"; 
  }
  public String toEtl()
  { return ""; }
  public void displayJava(String t, PrintWriter out)
  { out.println(toStringJava()); } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { Vector localEnv = new Vector(); 
    localEnv.addAll(env); 
    caughtObject.typeCheck(types,entities,cs,localEnv);
    action.typeCheck(types,entities,cs,localEnv);  
    return true;
  } 
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { Vector localEnv = new Vector(); 
    localEnv.addAll(env); 
    caughtObject.typeInference(types,entities,
                               cs,localEnv,vartypes);
    action.typeInference(types,entities,cs,localEnv,vartypes);  
    return true;
  } 
  public Expression wpc(Expression post)
  { return post; }
  public Expression wpc(Expression inv, Expression post)
  { return inv; }
  public Vector dataDependents(Vector allvars, Vector vars)
  { if (action != null) 
    { return action.dataDependents(allvars,vars); } 
    return vars; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { if (action != null) 
    { return action.dataDependents(allvars,vars,mp,dlin); } 
    return vars; 
  }  
  public boolean updates(Vector v) 
  { return action.updates(v); } 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateForm(env,local,types,entities,vars) + "\n" + 
           "    }"; 
  }
  public String updateFormJava6(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormJava6(env,local) + "\n" + 
           "    }";
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryForm(env,true); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormJava7(env,local) + "\n" + 
           "    }";
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryFormCSharp(env,local); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormCSharp(env,local) + "\n" + 
           "    }"; 
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { String qf = caughtObject.declarationQueryFormCPP(env,local); 
    return "    catch (" + qf + ") {\n" + 
           "      " + action.updateFormCPP(env,local) + "\n" + 
           "    }";
  }
  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(action.readFrame());  
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    res.addAll(action.writeFrame());  
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity()
  { return 1 + action.syntacticComplexity(); } 
  public int cyclomaticComplexity()
  { return 1 + action.cyclomaticComplexity(); } 
  public int epl()
  { return 0; } 
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    res = action.allOperationsUsedIn();  
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    res = action.allAttributesUsedIn();  
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    res = action.getUses(var);  
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    res = action.getVariableUses();  
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    res = action.equivalentsUsedIn();  
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    res = caughtObject.metavariables(); 
    res.addAll(action.metavariables());  
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    args.add(caughtObject);  
    args.add(action); 
    return args; 
  } 
  public Vector cgterms()
  { Vector args = new Vector();
    args.add("catch"); 
    if (caughtObject != null) 
    { args.add("("); 
      args.add(caughtObject);
      args.add(")"); 
    } 
    if (action != null) 
    { args.add("do"); 
      args.add(action); 
    }
    return args; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector();
    args.add(caughtObject.cg(cgs));  
    args.add(action.cg(cgs));  
    eargs.add(caughtObject);  
    eargs.add(action);  
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext;
  }
}