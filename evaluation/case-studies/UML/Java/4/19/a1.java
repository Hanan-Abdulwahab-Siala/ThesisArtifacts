class FinalStatement extends Statement
{ Statement body;
  FinalStatement(Statement s)
  { body = s; }
  FinalStatement(Vector stats)
  { if (stats.size() == 0)
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  
  } 
  public String getOperator() 
  { return "finally"; } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    args.add(body.cg(cgs));
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public Vector cgparameters()
  { 
    Vector args = new Vector();
    args.add(body);
    return args;
  }
  public Vector cgterms()
  { 
    Vector args = new Vector();
    args.add("finally"); 
    args.add(body);
    return args;
  }
  public Object clone()
  { Statement ifc = (Statement) body.clone(); 
    return new FinalStatement(ifc); 
  }  
  public Statement optimiseOCL()
  { Statement ifc = body.optimiseOCL(); 
    return new FinalStatement(ifc); 
  }  
  public void findClones(java.util.Map clones, String rule, String op)
  { body.findClones(clones,rule,op); } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { body.findClones(clones,cdefs,rule,op); } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { body.energyUse(uses, rUses, aUses);  
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { body.collectionOperatorUses(lev, uses, vars); 
    return uses; 
  } 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { body.findMagicNumbers(mgns,this + "",op); } 
  public Statement generateDesign(java.util.Map env, boolean local)
  { return this; }  
  public String toString()
  { String res = "    finally ( " + body + " )";
    return res;
  }
  public String toAST()
  { String res = "(OclStatement finally " + body.toAST() + " )";
    return res;
  }
  public boolean containsSubexpression(Expression expr)
  { if (body != null) 
    { return body.containsSubexpression(expr); } 
    return false; 
  } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    if (body == null) 
    { return res; } 
    Vector jb = body.singleMutants();
    for (int i = 0; i < jb.size(); i++) 
    { Statement st = (Statement) jb.get(i); 
      res.add(new FinalStatement(st)); 
    } 
    return res; 
  }
  public String toStringJava()
  { String jb = body.toStringJava();
    return "    finally " + jb; 
  }
  public String toEtl()
  { return toStringJava(); }
  public void display(java.io.PrintWriter out)
  { out.println(toString()); }
  public void display()
  { System.out.println(toString()); }
  public void displayJava(String v, java.io.PrintWriter out)
  { System.out.println(toStringJava()); }
  public void displayJava(String v)
  { System.out.println(toStringJava()); }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("finalstatement_");
    out.println(res + " : FinalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String bodyid = body.saveModelData(out);
    out.println(res + ".body = " + bodyid);
    return res;
  }
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("finalstatement_");
    out.println(res + " : FinalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String bodyid = body.saveModelData(out, ent);
    out.println(res + ".body = " + bodyid);
    return res;
  }
  public Statement dereference(BasicExpression v)
  { Statement bodyc = body.dereference(v); 
    return new FinalStatement(bodyc); 
  }  
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Statement bodyc = 
       body.addContainerReference(ref,var,excl); 
    return new FinalStatement(bodyc); 
  }  
  public Statement substituteEq(String oldE, Expression newE)
  { Statement ifc = body.substituteEq(oldE, newE); 
    return new FinalStatement(ifc); 
  }  
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Statement ifc = body.removeSlicedParameters(bf,fpars); 
    return new FinalStatement(ifc);
  } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = body.typeCheck(types,entities,cs,env);
    return res; 
  }
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = body.typeInference(types,entities,
                                     cs,env,vartypes);
    return res; 
  }
  public Expression wpc(Expression post)
  { return body.wpc(post); }  
  public Expression wpc(Expression inv, Expression post)
  { return body.wpc(inv, post); }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  
  public boolean updates(Vector v) 
  { return body.updates(v); }  
  public String updateForm(java.util.Map env, boolean local, Vector types,
                           Vector entities, Vector vars)
  { String bup = body.updateForm(env,local,types,entities,vars);  
    return "    finally " + bup; 	
  } 
  public String updateFormJava6(java.util.Map env, boolean local)
  { String bup = body.updateFormJava6(env,local);  
    return "    finally " + bup; 	
  } 
  public String updateFormJava7(java.util.Map env, boolean local)
  { String bup = body.updateFormJava7(env,local);  
    return "    finally { " + bup + " }\n";
  } 
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String bup = body.updateFormCSharp(env,local);  
    return "    finally { " + bup + " }\n";
  } 
  public String updateFormCPP(java.util.Map env, boolean local)
  { String bup = body.updateFormCPP(env,local);  
    return "    catch(...) { " + bup + " }\n";
  } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BStatement ifstat = body.bupdateForm(env,local);
    return ifstat; 
  } 
  public String bupdateForm()
  { String ifstat = body.bupdateForm();
    return ifstat; 
  } 
  public Vector allPreTerms()
  { return body.allPreTerms(); }  
  public Vector allPreTerms(String var)
  { return body.allPreTerms(var); }  
  public Vector readFrame()
  { return body.readFrame(); }  
  public Vector writeFrame()
  { return body.writeFrame(); }  
  public Statement checkConversions(Entity e, Type propType, Type propElemType, 
                                    java.util.Map interp)
  { Statement ifc = body.checkConversions(e,propType,propElemType,interp); 
    return ifc; 
  }  
  public Statement replaceModuleReferences(UseCase uc)
  { Statement ifc = body.replaceModuleReferences(uc);
    return ifc; 
  }  
  public int syntacticComplexity()
  { int res = body.syntacticComplexity(); 
    return res + 1;
  }
  public int cyclomaticComplexity()
  { int res = body.cyclomaticComplexity();
    return res; 
  }
  public int epl()
  { int res = body.epl();
    return res; 
  }
  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(body.allOperationsUsedIn()); 
    return res;  
  }  
  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(body.allAttributesUsedIn()); 
    return res;  
  }  
  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(body.getUses(var)); 
    return res;  
  }  
  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(body.getVariableUses()); 
    return res;  
  }  
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    Vector bodyuses = body.getVariableUses(unused); 
    res.addAll(bodyuses); 
    return res;  
  }  
  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(body.equivalentsUsedIn()); 
    return res;  
  }  
  public Vector metavariables()
  { Vector res = new Vector();
    res.addAll(body.metavariables());  
    return res;  
  }  
}