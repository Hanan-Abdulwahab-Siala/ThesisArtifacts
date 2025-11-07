class IfCase
{ private Expression test; 
  private Statement ifPart;
  private Entity entity;
  IfCase(Expression t, Statement i)
  { test = t; 
    ifPart = i; 
  }
  public Object clone()
  { Expression newtest = (Expression) test.clone(); 
    Statement newif = (Statement) ifPart.clone(); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  
  public IfCase optimiseOCL()
  { Expression newtest = test.simplifyOCL(); 
    Statement newif = ifPart.optimiseOCL(); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  
  public IfCase dereference(BasicExpression var)
  { Expression newtest = (Expression) test.dereference(var); 
    Statement newif = (Statement) ifPart.dereference(var); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { test.findClones(clones,rule,op); }
    ifPart.findClones(clones,rule,op);
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { test.findClones(clones,cdefs,rule,op); } 
    ifPart.findClones(clones,cdefs,rule,op);
  }
  public Map energyUse(Map uses, Vector ruses, Vector ouses)
  { test.energyUse(uses, ruses, ouses); 
    ifPart.energyUse(uses, ruses, ouses);
    return uses; 
  }
  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)
  { test.collectionOperatorUses(lev, uses, vars); 
    ifPart.collectionOperatorUses(lev, uses, vars);
    return uses; 
  }
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { test.findMagicNumbers(mgns,this + "",op); 
    ifPart.findMagicNumbers(mgns,rule,op);
  }
  public IfCase addContainerReference(BasicExpression ref,
                                      String var,
                                      Vector excl)
  { Expression newtest = test.addContainerReference(ref,var,excl); 
    Statement newif = ifPart.addContainerReference(ref,var,excl); 
    IfCase res = new IfCase(newtest,newif); 
    res.setEntity(entity); 
    return res; 
  }  
  public IfCase generateDesign(java.util.Map env, boolean local)
  { Statement newif = ifPart.generateDesign(env,local); 
    IfCase res = new IfCase(test,newif); 
    res.setEntity(entity); 
    return res; 
  }  
  public boolean isNull()
  { return "true".equals(test + "") && "skip".equals(ifPart + ""); } 
  public Expression getTest() 
  { return test; } 
  public Statement getIf()
  { return ifPart; } 
  public void setIf(Statement s)
  { ifPart = s; } 
  public void setEntity(Entity e)
  { entity = e; 
    ifPart.setEntity(e);   
  }
  public IfCase substituteEq(String oldE, Expression newE) 
  { Expression e = test.substituteEq(oldE,newE); 
    Statement stat = ifPart.substituteEq(oldE,newE);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res; 
  } 
  public IfCase removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression e = test.removeSlicedParameters(bf,fpars); 
    Statement stat = ifPart.removeSlicedParameters(bf,fpars);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res;
  } 
  public void display()
  { System.out.print("IF " + test + " THEN "); 
    ifPart.display();
    System.out.println(""); 
  }
  public String bupdateForm()
  { String res = "IF " + test + " THEN "; 
    res = res + ifPart.bupdateForm();
    return res + "\n"; 
  }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression btest = test.binvariantForm(env,local); 
    BStatement bif = ifPart.bupdateForm(env,local); 
    return new BIfStatement(btest,bif); 
  } 
  public void displayImp(String var) 
  { System.out.print("IF " + test + " THEN "); 
    ifPart.displayImp(var); 
    System.out.println(""); 
  }
  public void displayImp(String var, PrintWriter out)
  { out.print("IF " + test + " THEN ");
    ifPart.displayImp(var,out);
    out.println(""); 
  }
  public void display(PrintWriter out)
  { out.print("IF " + test + " THEN ");
    ifPart.display(out);
    out.println(""); 
  }
  public void displayJava(String t)
  { System.out.print("if (" + test.toJava() + 
                       ") { "); 
    ifPart.displayJava(t); 
    System.out.println(" }"); 
  }
  public String toStringJava()
  { java.util.Map env = new java.util.HashMap();
    if (entity != null)
    { env.put(entity.getName(),"this"); } 
    String res = "if (" + test.queryForm(env,false) + 
                       ") { "; 
    res = res + ifPart.toStringJava(); 
    return res + " }\n";
  }
  public String toEtl()
  { String res = "if (" + test + ") { "; 
    res = res + ifPart.toEtl(); 
    return res + " }\n";
  }
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res1 = test.typeCheck(types,entities,cs,env);
    boolean res2 = ifPart.typeCheck(types,entities,cs,env);
    return res1 && res2; 
  }
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res1 = test.typeInference(types,entities,
                                      cs,env,vartypes);
    boolean res2 = ifPart.typeInference(types,entities,
                                        cs,env,vartypes);
    return res1 && res2; 
  }
  public void displayJava(String t, PrintWriter out)
  { out.print("if (" + test.toJava() + 
                       ") { "); 
    ifPart.displayJava(t, out); 
    out.println(" }"); 
  }
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { 
    if ("true".equals("" + test))
    { return ifPart.updateForm(env,local,types,entities,vars); } 
    String res = "if (" + test.queryForm(env,false) + 
                       ") { "; 
    res = res + ifPart.updateForm(env,local,types,entities,vars); 
    return res + " }\n";
  }  
  public String updateFormJava6(java.util.Map env, boolean local)
  { 
    if ("true".equals("" + test))
    { return ifPart.updateFormJava6(env,local); } 
    String res = "if (" + test.queryFormJava6(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormJava6(env,local); 
    return res + " }\n";
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { 
    if ("true".equals("" + test))
    { return ifPart.updateFormJava7(env,local); } 
    String res = "if (" + test.queryFormJava7(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormJava7(env,local); 
    return res + " }\n";
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { 
    if ("true".equals("" + test))
    { return ifPart.updateFormCSharp(env,local); } 
    String res = "if (" + test.queryFormCSharp(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormCSharp(env,local); 
    return res + " }\n";
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { 
    if ("true".equals("" + test))
    { return ifPart.updateFormCPP(env,local); } 
    String res = "if (" + test.queryFormCPP(env,false) + 
                       ") { "; 
    res = res + ifPart.updateFormCPP(env,local); 
    return res + " }\n";
  }
  public Vector allPreTerms()
  { Vector res1 = test.allPreTerms(); 
    return VectorUtil.union(res1,ifPart.allPreTerms()); 
  }  
  public Vector allPreTerms(String var)
  { Vector res1 = test.allPreTerms(var); 
    return VectorUtil.union(res1,ifPart.allPreTerms(var)); 
  }  
  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(test.allReadFrame()); 
    res.addAll(ifPart.readFrame()); 
    return res;  
  }  
  public Vector writeFrame()
  { Vector res = new Vector();
    res.addAll(ifPart.writeFrame()); 
    return res;  
  }  
  public IfCase replaceModuleReferences(UseCase uc) 
  { Expression e = test.replaceModuleReferences(uc); 
    Statement stat = ifPart.replaceModuleReferences(uc);
    IfCase res = new IfCase(e,stat);
    res.setEntity(entity); 
    return res; 
  } 
  public int syntacticComplexity()
  { int res = test.syntacticComplexity();
    if (res > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + res + ") for " + test); 
      System.err.println(">>> Recommend OCL refactoring");  
    } 
    res = res + ifPart.syntacticComplexity();
    return res + 1; 
  }
  public int cyclomaticComplexity()
  { int res = test.cyclomaticComplexity(); 
    res = res + ifPart.cyclomaticComplexity();
    return res; 
  }
  public int epl()
  { int res = 0; 
    res = res + ifPart.epl();
    return res; 
  }
  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allOperationsUsedIn()); 
    res.addAll(ifPart.allOperationsUsedIn()); 
    return res;  
  }  
  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allAttributesUsedIn()); 
    res.addAll(ifPart.allAttributesUsedIn()); 
    return res;  
  }  
  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(test.getUses(var)); 
    res.addAll(ifPart.getUses(var)); 
    return res;  
  }  
  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    res.addAll(ifPart.getVariableUses()); 
    return res;  
  }  
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    Vector ifuses = ifPart.getVariableUses(unused); 
    res.addAll(ifuses); 
    return res;  
  }  
  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.equivalentsUsedIn()); 
    res.addAll(ifPart.equivalentsUsedIn()); 
    return res;  
  }  
  public Vector metavariables()
  { Vector res = test.metavariables();
    res.addAll(ifPart.metavariables());  
    return res;  
  }  
} 