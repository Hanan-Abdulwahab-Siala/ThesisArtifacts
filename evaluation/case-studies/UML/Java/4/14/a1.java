class TryStatement extends Statement
{ 
  Statement body = null; 
  Vector catchClauses = new Vector(); 
  Statement endStatement = null; 
  public TryStatement(Statement stat) 
  { body = stat; } 
  public TryStatement(Statement stat, Vector cclauses, Statement es) 
  { body = stat; 
    catchClauses = cclauses; 
    endStatement = es; 
  } 
  public TryStatement(Vector stats, Vector cclauses, Vector ends)
  { if (stats.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  
    catchClauses = cclauses; 
    if (ends.size() == 0)
    { endStatement = null; } 
    else if (ends.size() == 1)
    { endStatement = (Statement) ends.get(0); } 
    else 
    { endStatement = new SequenceStatement(ends); }  
  }
  public TryStatement(Vector stats, Vector ends)
  { if (stats.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (stats.size() == 1)
    { body = (Statement) stats.get(0); } 
    else 
    { body = new SequenceStatement(stats); }  
    if (ends.size() == 0)
    { endStatement = null; } 
    else if (ends.size() == 1)
    { Statement stat = (Statement) ends.get(0); 
      if (stat instanceof FinalStatement)
      { endStatement = stat; }
      else 
      { catchClauses = ends; 
        endStatement = null; 
      } 
    }  
    else 
    { catchClauses = ends; 
      endStatement = null; 
    }  
  }
  public void addBody(Statement stat)
  { if (stat == null) 
    { return; } 
    if (body == null) 
    { body = stat; } 
    else if (body instanceof SequenceStatement)
    { ((SequenceStatement) body).addStatement(stat); } 
    else 
    { SequenceStatement ss = new SequenceStatement(); 
      ss.addStatement(body);  
      ss.addStatement(stat); 
      body = ss; 
    } 
  } 
  public void setClauses(Vector stats)
  { catchClauses = stats; } 
  public void addClause(Statement stat)
  { if (stat instanceof CatchStatement) 
    { catchClauses.add(stat); } 
    else if (stat instanceof FinalStatement)
    { endStatement = stat; } 
    else 
    { System.err.println("!! Warning: can only have catch and finally statements in a try statement: " + stat); } 
  } 
  public void setEndStatement(Statement stat)
  { endStatement = stat; } 
  public void display()
  { if (body == null) 
    { return; } 
    System.out.println("  try "); 
    body.display(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.display(); 
    }
    if (endStatement != null) 
    { endStatement.display(); }  
  }
  public String toString()
  { String res = ""; 
    if (body == null) 
    { return res; } 
    res = "    try\n" + 
          "  " + body;
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs; 
    }
    if (endStatement != null) 
    { res = res + "  " + endStatement + "\n"; }
    return res; 
  }
  public String toAST()
  { String res = "(OclStatement try ";
    if (body != null) 
    { res = res + body.toAST() + " "; } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.toAST() + " "; 
    }
    if (endStatement != null) 
    { res = res + endStatement.toAST() + " "; }
    res = res + ")";
    return res; 
  }
  public void findClones(java.util.Map clones, String rule, String op)
  { if (body != null) 
    { body.findClones(clones,rule,op); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findClones(clones,rule,op); 
    }
    if (endStatement != null) 
    { endStatement.findClones(clones,rule,op); } 
  } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (body != null) 
    { body.findClones(clones,cdefs,rule,op); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findClones(clones,cdefs,rule,op); 
    }
    if (endStatement != null) 
    { endStatement.findClones(clones,cdefs,rule,op); } 
  } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (body != null) 
    { body.energyUse(uses, rUses, aUses); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.energyUse(uses, rUses, aUses); 
    }
    if (endStatement != null)
    { endStatement.energyUse(uses, rUses, aUses); } 
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                                    java.util.Map uses, 
                                    Vector vars)
  { if (body != null) 
    { body.collectionOperatorUses(lev, uses, vars); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.collectionOperatorUses(lev, uses, vars); 
    }
    if (endStatement != null)
    { endStatement.collectionOperatorUses(lev, uses, vars); } 
    return uses; 
  } 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (body != null) 
    { body.findMagicNumbers(mgns,rule,op); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      stat.findMagicNumbers(mgns,rule,op); 
    }
    if (endStatement != null) 
    { endStatement.findMagicNumbers(mgns,rule,op); } 
  } 
  public boolean containsSubexpression(Expression expr) 
  { if (body != null) 
    { if (body.containsSubexpression(expr))
      { return true; }
    } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement stat = (Statement) catchClauses.get(i); 
      if (stat.containsSubexpression(expr))
      { return true; }
    } 
    if (endStatement != null) 
    { return endStatement.containsSubexpression(expr); }
    return false;  
  } 
  public Vector singleMutants()
  { if (body == null) 
    { return new Vector(); } 
    Vector stats = body.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < stats.size(); i++) 
    { Statement mvalue = (Statement) stats.get(i); 
      res.add(new TryStatement(mvalue, catchClauses, endStatement)); 
    } 
    return res; 
  } 
  public String getOperator() 
  { return "try"; } 
  public Statement getBody() 
  { return body; } 
  public Vector getClauses() 
  { return catchClauses; }
  public Statement getEndStatement()
  { return endStatement; }  
  public Object clone() 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = (Statement) body.clone(); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = (Statement) cc.clone(); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement((Statement) endStatement.clone()); } 
    return res; 
  } 
  public Statement optimiseOCL() 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.optimiseOCL(); }
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = (Statement) cc.optimiseOCL(); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(endStatement.optimiseOCL()); } 
    return res; 
  } 
  public Statement dereference(BasicExpression var) 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.dereference(var); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.dereference(var); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(endStatement.dereference(var)); } 
    return res;
  } 
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl) 
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.addContainerReference(ref,var,excl); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone =
        cc.addContainerReference(ref,var,excl); 
      catchClones.add(ccClone); 
    }
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(
         endStatement.addContainerReference(ref,var,excl)); 
    } 
    return res;
  } 
  public Statement substituteEq(String oldE, Expression newE)
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.substituteEq(oldE,newE); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.substituteEq(oldE,newE); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(
             endStatement.substituteEq(oldE,newE)); 
    } 
    return res; 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Statement s1 = null; 
    if (body != null) 
    { s1 = body.removeSlicedParameters(bf,fpars); }  
    TryStatement res = new TryStatement(s1);
    Vector catchClones = new Vector(); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Statement ccClone = cc.removeSlicedParameters(bf,fpars); 
      catchClones.add(ccClone); 
    } 
    res.setClauses(catchClones); 
    if (endStatement != null) 
    { res.setEndStatement(
        endStatement.removeSlicedParameters(bf,fpars)); 
    } 
    return res; 
  } 
  public void display(PrintWriter out)
  { if (body == null) 
    { return; } 
    out.println("  try "); 
    body.display(out); 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.display(out); 
    }
    if (endStatement != null) 
    { endStatement.display(out); }  
  }
  public String bupdateForm()
  { return "SELECT false THEN skip END\n"; }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { return new BBasicStatement("SELECT false THEN skip END"); }
  public void displayJava(String t)
  { if (body == null) 
    { return; } 
    System.out.println("  try { "); 
    body.displayJava(t); 
    System.out.println(" }");
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.displayJava(t); 
    }
    if (endStatement != null) 
    { endStatement.displayJava(t); }  
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("trystatement_"); 
    out.println(res + " : TryStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (body != null) 
    { String s1 = body.saveModelData(out); 
      out.println(res + ".body = " + s1); 
    }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      String ccid = cc.saveModelData(out); 
      out.println(ccid + " : " + res + ".catchClauses");  
    } 
    if (endStatement != null) 
    { String endId = endStatement.saveModelData(out); 
      out.println(endId + " : " + res + ".endStatement");  
    } 
    return res;
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("trystatement_"); 
    out.println(res + " : TryStatement"); 
    out.println(res + ".statId = \"" + res + "\"");  
    if (body != null) 
    { String s1 = body.saveModelData(out, ent); 
      out.println(res + ".body = " + s1); 
    }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      String ccid = cc.saveModelData(out, ent); 
      out.println(ccid + " : " + res + ".catchClauses");  
    } 
    if (endStatement != null) 
    { String endId = endStatement.saveModelData(out, ent); 
      out.println(endId + " : " + res + ".endStatement");  
    } 
    return res;
  } 
  public String toStringJava()
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.toStringJava() + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.toStringJava(); 
    }
    if (endStatement != null) 
    { res = res + endStatement.toStringJava(); }  
    return res; 
  }
  public String toEtl()
  { return ""; }
  public void displayJava(String t, PrintWriter out)
  { out.println("  try"); 
    if (body == null) 
    { out.println(" { }"); }
    else  
    { out.println("  { "); 
      body.displayJava(t,out); 
      out.println("  }"); 
    }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      cs.displayJava(t,out); 
    }
    if (endStatement != null) 
    { endStatement.displayJava(t,out); }  
 } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { if (body != null) 
    { body.typeCheck(types,entities,cs,env); }  
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      cc.typeCheck(types,entities,cs,env);
    } 
    if (endStatement != null) 
    { endStatement.typeCheck(types,entities,cs,env); } 
    return true; 
  }  
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { if (body != null) 
    { body.typeInference(types,entities,cs,env,vartypes); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      cc.typeInference(types,entities,cs,env,vartypes);
    } 
    if (endStatement != null) 
    { endStatement.typeInference(types,entities,
                                 cs,env,vartypes); 
    }
    return true; 
  }  
  public Expression wpc(Expression post)
  { return post; }
  public Expression wpc(Expression inv, Expression post)
  { return inv; }
  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    if (endStatement != null) 
    { vbls = endStatement.dataDependents(allvars,vars); }
    else 
    { vbls.addAll(vars); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      vbls = cc.dataDependents(allvars, vbls);
    } 
    if (body != null) 
    { return body.dataDependents(allvars, vbls); }
    return vbls; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    if (endStatement != null) 
    { vbls = endStatement.dataDependents(allvars,vars,mp,dlin); }
    else 
    { vbls.addAll(vars); } 
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      vbls = cc.dataDependents(allvars, vbls, mp, dlin);
    } 
    if (body != null) 
    { return body.dataDependents(allvars, vbls, mp, dlin); }
    return vbls; 
  }  
  public boolean updates(Vector v) 
  { boolean res = false; 
    if (body != null) 
    { if (body.updates(v))
      { return true; }
    }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      if (cc.updates(v))
      { return true; } 
    } 
    if (endStatement != null) 
    { if (endStatement.updates(v))
      { return true; }
    }  
    return res; 
  } 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateForm(env,local,entities,types,vars) + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateForm(env,local,entities,types,vars); 
    }
    if (endStatement != null) 
    { res = res + endStatement.updateForm(env,local,entities,types,vars); }  
    return res; 
 }
  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormJava6(env,local) + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormJava6(env,local); 
    }
    if (endStatement != null) 
    { res = res + endStatement.updateFormJava6(env,local); }  
    return res; 
 }
  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormJava7(env,local) + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormJava7(env,local); 
    }
    if (endStatement != null) 
    { res = res + endStatement.updateFormJava7(env,local); }  
    return res; 
 }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormCSharp(env,local) + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormCSharp(env,local) + "\n"; 
    }
    if (endStatement != null) 
    { res = res + endStatement.updateFormCSharp(env,local); }  
    return res; 
 }
  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = "  try"; 
    if (body == null) 
    { res = res + " { }\n"; }
    else  
    { res = res + "  { " + body.updateFormCPP(env,local) + " }\n"; }
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cs = (Statement) catchClauses.get(i); 
      res = res + cs.updateFormCPP(env,local) + "\n"; 
    }
    if (endStatement != null) 
    { res = res + endStatement.updateFormCPP(env,local); }  
    return res; 
  }
  public Vector readFrame()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.readFrame()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.readFrame(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.readFrame(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.writeFrame()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.writeFrame(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.writeFrame(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity()
  { int res = 1; 
    if (body != null) 
    { res = res + body.syntacticComplexity(); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      int vv = cc.syntacticComplexity(); 
      res = res + vv; 
    } 
    if (endStatement != null) 
    { int endsc = endStatement.syntacticComplexity(); 
      res = res + endsc; 
    }  
    return res; 
  } 
  public int cyclomaticComplexity()
  { int res = 1; 
    if (body != null) 
    { res = res + body.cyclomaticComplexity(); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      int vv = cc.cyclomaticComplexity(); 
      res = res + vv; 
    } 
    if (endStatement != null) 
    { int endsc = endStatement.cyclomaticComplexity(); 
      res = res + endsc; 
    }  
    return res; 
  }
  public int epl()
  { return 0; } 
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.allOperationsUsedIn()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.allOperationsUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.allOperationsUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.allAttributesUsedIn()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.allAttributesUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.allAttributesUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.getUses(var)); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getUses(var); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.getUses(var); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.getVariableUses()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getVariableUses(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.getVariableUses(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (body != null) 
    { Vector bodyuses = body.getVariableUses(unused); 
      res = VectorUtil.union(res,bodyuses); 
    }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.getVariableUses(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.getVariableUses(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.equivalentsUsedIn()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.equivalentsUsedIn(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.equivalentsUsedIn(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    if (body != null) 
    { res = VectorUtil.union(res,body.metavariables()); }   
    for (int i = 0; i < catchClauses.size(); i++) 
    { Statement cc = (Statement) catchClauses.get(i); 
      Vector vv = cc.metavariables(); 
      res = VectorUtil.union(res,vv); 
    } 
    if (endStatement != null) 
    { Vector endrd = endStatement.metavariables(); 
      res = VectorUtil.union(res,endrd); 
    }  
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    if (body != null) 
    { args.add(body); } 
    if (catchClauses != null) 
    { args.add(catchClauses); }
    if (endStatement != null) 
    { args.add(endStatement); }
    return args; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector();
    if (body != null) 
    { args.add(body.cg(cgs)); 
      eargs.add(body); 
    } 
    if (catchClauses != null) 
    { String ccres = ""; 
      for (int i = 0; i < catchClauses.size(); i++) 
      { Statement cc = (Statement) catchClauses.get(i); 
        ccres = ccres + cc.cg(cgs); 
      }   
      args.add(ccres); 
      eargs.add(catchClauses); 
    }
    if (endStatement != null) 
    { args.add(endStatement.cg(cgs)); 
      eargs.add(endStatement); 
    } 
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext;
  }
}