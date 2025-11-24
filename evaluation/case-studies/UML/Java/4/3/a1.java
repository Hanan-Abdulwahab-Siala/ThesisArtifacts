class BreakStatement extends Statement
{ public void display()
  { System.out.println("  break;"); }  
  public String getOperator() 
  { return "break"; } 
  public Object clone()
  { return new BreakStatement(); } 
  public String toString() 
  { return "break"; } 
  public String toAST() 
  { String res = "(OclStatement break)"; 
    return res;  
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { return Statement.BREAK; } 
  public boolean containsSubexpression(Expression expr) 
  { return false; } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    res.add(new ContinueStatement()); 
    return res; 
  } 
  public String bupdateForm()
  { return " "; } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("skip"); } 
  public void display(PrintWriter out)
  { out.println("  break;"); }  
  public void displayJava(String t)
  { display(); }  
  public void displayJava(String t, PrintWriter out)
  { display(out); }  
  public Statement substituteEq(String oldE, Expression newE)
  { return this; } 
  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { return this; } 
  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, Vector excludes)
  { return this; }  
  public String toStringJava()
  { return "  break;"; }
  public String toEtl()
  { return "  break;"; }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("breakstatement_"); 
    out.println(res + " : BreakStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { return true; }  
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { return true; } 
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
  public String updateForm(java.util.Map env, boolean local, Vector types, 
                           Vector entities, Vector vars)
  { return toStringJava(); }  
  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava(); }  
  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava(); }  
  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringJava(); }  
  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringJava(); }  
  public Statement dereference(BasicExpression var)
  { return new BreakStatement(); }  
  public Vector readFrame() 
  { Vector res = new Vector();
    return res; 
  } 
  public Vector writeFrame() 
  { Vector res = new Vector();
    return res;
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return new BreakStatement(); } 
  public Statement replaceModuleReferences(UseCase uc)
  { return new BreakStatement(); } 
  public int syntacticComplexity()
  { return 1; } 
  public int cyclomaticComplexity()
  { return 0; }  
  public int epl()
  { return 0; }  
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    return res; 
  }
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public Vector cgparameters()
  { Vector args = new Vector();
    return args; 
  } 
  public void findClones(java.util.Map clones, String op, String rule)
  { return; } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cloneDefs,
                         String op, String rule)
  { return; } 
  public Statement optimiseOCL()
  { return this; }  
  public Map energyUse(Map uses, 
                       Vector rUses, Vector oUses)
  { return uses; }  
  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { return operatorsAtLevel; }  
}