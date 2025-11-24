class ConditionalStatement extends Statement
{ Expression test;
  Statement ifPart;
  Statement elsePart;
  ConditionalStatement(Expression e, Statement s)
  { test = e;
    ifPart = s;
    elsePart = null;
  }
  ConditionalStatement(Expression e, Statement s1, Statement s2)
  { test = e;
    ifPart = s1;
    elsePart = s2;
  }
  ConditionalStatement(Expression e, Vector ss1, Vector ss2)
  { test = e;
    if (ss1.size() == 0)
    { ifPart = new InvocationStatement("skip"); } 
    else if (ss1.size() == 1) 
    { ifPart = (Statement) ss1.get(0); } 
    else 
    { ifPart = new SequenceStatement(ss1); } 
    if (ss2.size() == 0)
    { elsePart = new InvocationStatement("skip"); } 
    else if (ss2.size() == 1) 
    { elsePart = (Statement) ss2.get(0); } 
    else 
    { elsePart = new SequenceStatement(ss2); } 
  }
  public void setElse(Statement stat) 
  { elsePart = stat; } 
  public String getOperator() 
  { return "if"; } 
  public Expression getTest()
  { return test; } 
  public void setTest(Expression tst)
  { test = tst; } 
  public Statement ifPart()
  { return ifPart; } 
  public Statement elsePart()
  { return elsePart; } 
  public Statement getIf()
  { return ifPart; } 
  public Statement getElse()
  { return elsePart; } 
  public boolean hasSkipElse()
  { if (elsePart == null) 
    { return true; } 
    return elsePart.isSkip(); 
  } 
  public void setIfPart(Statement st)
  { ifPart = st; } 
  public void setIf(Statement st)
  { ifPart = st; } 
  public void setElsePart(Statement st)
  { elsePart = st; } 
  public static void addToIfBranch(Statement st, Statement sx)
  { if (sx == null) 
    { return; } 
    if (st instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement ifp = cs.ifPart(); 
      if (ifp instanceof SequenceStatement)
      { ((SequenceStatement) ifp).addStatement(sx); } 
      else 
      { SequenceStatement ss = new SequenceStatement(); 
        ss.addStatement(ifp); 
        ss.addStatement(sx); 
        cs.ifPart = ss; 
      } 
    } 
  } 
  public static Statement mergeConditionals(Expression tst, 
                            Statement stat)
  { if (stat instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) stat; 
      Expression newexpr = 
        new BinaryExpression("or", tst, cs.test); 
      cs.test = newexpr; 
      return cs; 
    } 
    Statement els = new InvocationStatement("skip"); 
    return new ConditionalStatement(tst,stat,els); 
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { Expression tval = test.evaluate(sigma, beta); 
    if ("true".equals(tval + ""))
    { int res = ifPart.execute(sigma, beta); 
      return res; 
    } 
    else 
    { int res = elsePart.execute(sigma, beta); 
      return res; 
    } 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if ("true".equals(test + ""))
    { return ifPart.cg(cgs); }
    if ("false".equals(test + "") && 
        elsePart != null)
    { return elsePart.cg(cgs); }
    args.add(test.cg(cgs));
    args.add(ifPart.cg(cgs));
    if (elsePart == null) 
    { elsePart = new SequenceStatement(); } 
    args.add(elsePart.cg(cgs));
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { return r.applyRule(args); }
    return etext;
  }
  public Vector cgparameters()
  { Vector args = new Vector();
    if ("true".equals(test + ""))
    { args.add(ifPart);
      return args;
    }
    if ("false".equals(test + "") && 
        elsePart != null)
    { args.add(elsePart);
      return args;
    }
    args.add(test);
    args.add(ifPart);
    if (elsePart == null) 
    { elsePart = new SequenceStatement(); } 
    args.add(elsePart);
    return args;
  }
  public Expression definedness()
  { Expression testd = test.definedness(); 
    Expression ifdef = ifPart.definedness(); 
    Expression res = 
      Expression.simplify("&", testd, ifdef, null); 
    if (elsePart != null) 
    { res = 
        Expression.simplify("&", res, 
                            elsePart.definedness(), null); 
    }
    return res; 
  } 
  public Object clone()
  { Expression testc = (Expression) test.clone(); 
    Statement ifc = (Statement) ifPart.clone(); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = (Statement) elsePart.clone(); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public Statement optimiseOCL()
  { Expression testc = test.simplifyOCL(); 
    Statement ifc = ifPart.optimiseOCL(); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.optimiseOCL(); }
    testc.setBrackets(false); 
    if ("true".equals(testc + "")) 
    { return ifc; } 
    if ("false".equals(testc + "")) 
    { if (elsec == null) 
      { return new InvocationStatement("skip"); } 
      return elsec; 
    } 
    Statement elseStat = 
       Statement.getFirstStatement(elsePart); 
    if (testc instanceof BinaryExpression && 
        Statement.hasSingleStatement(elsePart) &&
        ifPart.isSkip())
    { BinaryExpression testbe = (BinaryExpression) testc;
      Expression testbeLeft = testbe.getLeft(); 
      Expression testbeRight = testbe.getRight(); 
      testbeLeft.setBrackets(false);  
      testbeRight.setBrackets(false);  
      if (Statement.isAdditionToCollection(elseStat, 
                        testbeRight, testbeLeft) && 
          "->includes".equals(testbe.getOperator()) && 
          testbeLeft.hasSetType())
      { System.out.println("! Removing redundant test for set addition: " + this); 
        return elseStat; 
      } 
    } 
    Statement ifStat = Statement.getFirstStatement(ifPart); 
    if (testc instanceof BinaryExpression && 
        Statement.hasSingleStatement(ifPart) &&
        (elsePart == null || elsePart.isSkip()))
    { BinaryExpression testbe = (BinaryExpression) testc;
      Expression testbeLeft = testbe.getLeft(); 
      Expression testbeRight = testbe.getRight(); 
      testbeLeft.setBrackets(false);  
      testbeRight.setBrackets(false);  
      if (Statement.isAdditionToCollection(ifStat, 
                        testbeRight, testbeLeft) &&
          "->excludes".equals(testbe.getOperator()) && 
          testbeLeft.hasSetType())
      { System.out.println("! Removing redundant test for set addition: " + this); 
        return ifStat; 
      } 
    } 
    if (elsec != null && elsec.isSkip()) { } 
    else if (Statement.endsWithControlFlowBreak(ifc))
    { Statement skipstat = new InvocationStatement("skip");
      SequenceStatement ss = new SequenceStatement(); 
      ss.addStatement(
           new ConditionalStatement(testc, ifc, skipstat)); 
      ss.addStatement(elsec);
      System.out.println(">> Promoting nested statements from else branch: " + elsec); 
      return ss; 
    } 
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)
  { test.collectionOperatorUses(lev, uses, vars); 
    ifPart.collectionOperatorUses(lev, uses, vars);
    elsePart.collectionOperatorUses(lev, uses, vars);
    return uses; 
  } 
  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { test.energyUse(uses, rUses, oUses); 
    ifPart.energyUse(uses, rUses, oUses);
    int res = test.syntacticComplexity();
    if (res > TestParameters.syntacticComplexityLimit)
    { int acount = (int) uses.get("amber"); 
      uses.set("amber", acount + 1); 
      oUses.add("! Code smell (MEL): too high expression complexity (" + res + ") for " + test + "\n" +  
                ">>> Recommend OCL refactoring");  
    } 
    if (elsePart != null) 
    { elsePart.energyUse(uses, rUses, oUses); } 
    Statement elseStat = 
                     Statement.getFirstStatement(elsePart); 
    Statement ifStat = Statement.getFirstStatement(ifPart); 
    Expression testSimplified = test; 
    if (test instanceof UnaryExpression && 
        "not".equals(((UnaryExpression) test).getOperator()))
    { testSimplified = 
          Expression.negate(
             ((UnaryExpression) test).getArgument()); 
    } 
    if (testSimplified instanceof BinaryExpression)
    { BinaryExpression testbe = 
          (BinaryExpression) testSimplified;
      Expression testbeLeft = testbe.getLeft(); 
      testbeLeft.setBrackets(false); 
      Expression testbeRight = testbe.getRight(); 
      testbeRight.setBrackets(false); 
      if ("->includes".equals(testbe.getOperator()) && 
          Statement.hasSingleStatement(elsePart) &&
          ifPart.isSkip() &&                  
          Statement.isAdditionToCollection(
                         elseStat, testbeRight, testbeLeft)) 
      { 
        if (testbeLeft.hasSequenceType())
        { rUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
               "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 
          int rscore = (int) uses.get("red"); 
          uses.set("red", rscore + 1); 
        } 
        else if (testbeLeft.hasSetType())
        { oUses.add("! Redundant test on set addition " + testbeLeft + " in: " + this); 
          int oscore = (int) uses.get("amber"); 
          uses.set("amber", oscore + 1); 
        } 
      } 
      else if ("->includes".equals(testbe.getOperator()) && 
          Statement.isControlFlowEnd(ifPart) &&                  
          (elsePart.isSkip() || 
           Statement.isAdditionToCollection(
                         elseStat, testbeRight, testbeLeft))) 
      { 
        if (testbeLeft.hasSequenceType())
        { oUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
               "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 
          int ascore = (int) uses.get("amber"); 
          uses.set("amber", ascore + 1); 
        } 
      } 
      else if ("->excludes".equals(testbe.getOperator()) &&
               Statement.hasSingleStatement(ifPart) &&
               (elsePart == null || elsePart.isSkip() ||
                Statement.isControlFlowEnd(elsePart)) &&                  
               Statement.isAdditionToCollection(
                         ifStat, testbeRight, testbeLeft)) 
      { 
        if (testbeLeft.hasSequenceType())
        { oUses.add("!! Possibly using sequence " + testbeLeft + " as set in: " + this + 
             "\n>> Recommend declaring " + testbeLeft + " as a Set or SortedSet"); 
          int ascore = (int) uses.get("amber"); 
          uses.set("amber", ascore + 1); 
        } 
        else if (testbeLeft.hasSetType())
        { oUses.add("! Redundant test on set addition " + testbeLeft + " in: " + this); 
          int oscore = (int) uses.get("amber"); 
          uses.set("amber", oscore + 1); 
        } 
      } 
    } 
    return uses; 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT)
    {  
      test.findClones(clones,rule,op); 
    } 
    ifPart.findClones(clones,rule,op); 
    if (elsePart != null) 
    { elsePart.findClones(clones,rule,op); } 
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String rule, String op)
  { if (test.syntacticComplexity() >= UCDArea.CLONE_LIMIT)
    { test.findClones(clones,cdefs,rule,op); } 
    ifPart.findClones(clones,cdefs,rule,op); 
    if (elsePart != null) 
    { elsePart.findClones(clones,cdefs,rule,op); } 
  }
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { test.findMagicNumbers(mgns,"" + this,op); 
    ifPart.findMagicNumbers(mgns,rule,op); 
    if (elsePart != null) 
    { elsePart.findMagicNumbers(mgns,rule,op); } 
  }
  public Vector allVariableNames()
  { Vector res = test.allVariableNames(); 
    res = VectorUtil.union(res, ifPart.allVariableNames()); 
    if (elsePart != null) 
    { res = VectorUtil.union(res, 
                             elsePart.allVariableNames()); 
    }
    return res; 
  } 
  public Statement generateDesign(java.util.Map env, boolean local)
  { Statement ifc = ifPart.generateDesign(env,local);
    if ("true".equals(test + ""))
    { return ifc; } 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.generateDesign(env,local); }
    return new ConditionalStatement(test, ifc, elsec); 
  }  
  public String toString()
  { String res = "if " + test + " then " + ifPart;
    if (elsePart == null || "skip".equals(elsePart + "")) 
    { res = res + " else skip "; } 
    else 
    { res = res + " else ( " + elsePart + " )"; }
    return res;
  }
  public String toAST()
  { String res = "(OclStatement if " + test.toAST() + " then " + ifPart.toAST() + " ";
    if (elsePart == null || "skip".equals(elsePart + "")) 
    { res = res + " else (OclStatement skip) )"; } 
    else 
    { res = res + " else " + elsePart.toAST() + " )"; }
    return res;
  }
  public boolean containsSubexpression(Expression expr)
  { if (test.containsSubexpression(expr))
    { return true; } 
    if (ifPart != null &&
        ifPart.containsSubexpression(expr))
    { return true; } 
    if (elsePart != null &&
        elsePart.containsSubexpression(expr))
    { return true; } 
    return false; 
  } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    Vector exprs = test.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setTest(mut); 
      res.add(ifclone); 
    } 
    if (ifPart == null) 
    { return res; } 
    Vector ifmuts = ifPart.singleMutants(); 
    for (int i = 0; i < ifmuts.size(); i++) 
    { Statement mut = (Statement) ifmuts.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setIfPart(mut); 
      res.add(ifclone); 
    } 
    if (elsePart == null) 
    { return res; } 
    Vector elsemuts = elsePart.singleMutants(); 
    for (int i = 0; i < elsemuts.size(); i++) 
    { Statement mut = (Statement) elsemuts.get(i); 
      ConditionalStatement ifclone = (ConditionalStatement) clone(); 
      ifclone.setElsePart(mut); 
      res.add(ifclone); 
    } 
    return res;
  } 
  public String toStringJava()
  { String res = "if (" + test + ") { " + ifPart + " } ";
    if (elsePart != null)
    { res = res + " else { " + elsePart + " }"; }
    return res;
  }
  public String toEtl()
  { String res = "  if (" + test + ") { " + ifPart.toEtl() + " }\n";
    if (elsePart != null)
    { res = res + "  else { " + elsePart.toEtl() + " }"; }
    return res;
  }
  public void display(java.io.PrintWriter out)
  { String res = "if " + test + " then " + ifPart;
    if (elsePart != null)
    { res = res + " else " + elsePart; }
    out.println(res);
  }
  public void display()
  { String res = "if " + test + " then " + ifPart;
    if (elsePart != null)
    { res = res + " else " + elsePart; }
    System.out.println(res);
  }
  public void displayJava(String v, java.io.PrintWriter out)
  { out.println("    if (" + test + ")"); 
    out.println("    { " + ifPart + " }");
    if (elsePart != null)
    { out.println("    else "); 
      out.println("    { " + elsePart + " }"); 
    }
  }
  public void displayJava(String v)
  { String res = "if (" + test + ") { " + ifPart + " }";
    if (elsePart != null)
    { res = res + " else { " + elsePart + " }"; }
    System.out.println(res);
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("conditionalstatement_");
    out.println(res + " : ConditionalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String testid = test.saveModelData(out);
    out.println(res + ".test = " + testid);
    String ifpartid = ifPart.saveModelData(out);
    out.println(res + ".ifPart = " + ifpartid);
    if (elsePart != null)
    { String elsepartid = elsePart.saveModelData(out);
      out.println(elsepartid + " : " + res + ".elsePart");
    }
    return res;
  }
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("conditionalstatement_");
    out.println(res + " : ConditionalStatement");
    out.println(res + ".statId = \"" + res + "\"");
    String testid = test.saveModelData(out);
    out.println(res + ".test = " + testid);
    String ifpartid = ifPart.saveModelData(out, ent);
    out.println(res + ".ifPart = " + ifpartid);
    if (elsePart != null)
    { String elsepartid = elsePart.saveModelData(out, ent);
      out.println(elsepartid + " : " + res + ".elsePart");
    }
    return res;
  }
  public Statement dereference(BasicExpression v)
  { Expression testc = test.dereference(v); 
    Statement ifc = ifPart.dereference(v); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.dereference(v); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Expression testc = test.addContainerReference(ref,var,excl); 
    Statement ifc = ifPart.addContainerReference(ref,var,excl); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.addContainerReference(ref,var,excl); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public Statement substituteEq(String oldE, Expression newE)
  { Expression testc = test.substituteEq(oldE, newE); 
    Statement ifc = ifPart.substituteEq(oldE, newE); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.substituteEq(oldE, newE); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression testc = test.removeSlicedParameters(bf,fpars); 
    Statement ifc = ifPart.removeSlicedParameters(bf,fpars); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.removeSlicedParameters(bf,fpars); }
    return new ConditionalStatement(testc, ifc, elsec); 
  }  
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = test.typeCheck(types,entities,cs,env); 
    res = ifPart.typeCheck(types,entities,cs,env);
    if (elsePart != null) 
    { res = elsePart.typeCheck(types, entities, cs, env); } 
    return res; 
  }
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = test.typeInference(types,entities,
                                     cs,env,vartypes); 
    res = ifPart.typeInference(types,entities,cs,env,vartypes);
    if (elsePart != null) 
    { res = elsePart.typeInference(types, entities, 
                               cs, env,vartypes); 
    } 
    return res; 
  }
  public Expression wpc(Expression post)
  { Expression ifwpc = ifPart.wpc(post); 
    ifwpc.setBrackets(true); 
    Expression ifimpl = 
      Expression.simplifyImp(test, ifwpc);
    if (elsePart != null) 
    { Expression ntest = 
        Expression.negate(test); 
      Expression elsewpc = elsePart.wpc(post); 
      elsewpc.setBrackets(true);  
      Expression elseimpl = 
        Expression.simplifyImp(ntest, elsewpc);
      return Expression.simplify("&", ifimpl, elseimpl, null); 
    }
    return ifimpl; 
  }  
  public Expression wpc(Expression inv, Expression post)
  { Expression ifwpc = ifPart.wpc(inv, post); 
    ifwpc.setBrackets(true); 
    Expression ifimpl = 
      Expression.simplifyImp(test, ifwpc);
    if (elsePart != null) 
    { Expression ntest = 
        Expression.negate(test); 
      Expression elsewpc = elsePart.wpc(inv, post); 
      elsewpc.setBrackets(true);  
      Expression elseimpl = 
        Expression.simplifyImp(ntest, elsewpc);
      return Expression.simplify("&", ifimpl, elseimpl, null); 
    }
    return ifimpl; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { if (ifPart.updates(vars))
    { } 
    else
    { if (elsePart == null) 
      { return vars; } 
      else if (elsePart.updates(vars)) { } 
      else 
      { return vars; } 
    } 
    Vector vars1 = ifPart.dataDependents(allvars, vars); 
    Vector testvars = new Vector(); 
    testvars.addAll(test.getVariableUses()); 
    testvars = VectorUtil.union(testvars,
                                test.allAttributesUsedIn()); 
    if (elsePart != null) 
    { vars1 = VectorUtil.union(vars1, 
                 elsePart.dataDependents(allvars,vars)); 
    }
    vars1 = VectorUtil.union(vars1, testvars); 
    return vars1; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { 
    Map mp1 = new Map();
    Vector vars1 = ifPart.dataDependents(allvars, vars, mp1, dlin); 
    Vector testvars = new Vector(); 
    testvars.addAll(test.getVariableUses()); 
    testvars = VectorUtil.union(testvars,
                                test.allAttributesUsedIn());
    Vector range1 = mp1.range(); 
    mp.unionWith(mp1); 
    if (elsePart != null) 
    { Map mp2 = new Map(); 
      Vector vars2 = elsePart.dataDependents(allvars,vars,mp2,dlin); 
      vars1 = VectorUtil.union(vars1,vars2);  
      Vector range2 = mp2.range(); 
      range1.addAll(range2); 
      mp.unionWith(mp2); 
    }
    vars1 = VectorUtil.union(vars1, testvars); 
    for (int i = 0; i < range1.size(); i++) 
    { String vv = "" + range1.get(i); 
      for (int j = 0; j < testvars.size(); j++) 
      { String rv = "" + testvars.get(j); 
        mp.add_pair(rv, vv); 
      } 
    } 
    return vars1; 
  }  
  public boolean updates(Vector v) 
  { if (ifPart.updates(v))
    { return true; }
    else if (elsePart != null && elsePart.updates(v))
    { return true; } 
    return false; 
  }  
  public String updateForm(java.util.Map env, boolean local, Vector types,
                           Vector entities, Vector vars)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateForm(env,local,types,entities,vars) + " }\n"; } 
	String res = "    if (" + test.queryForm(env,local) + ")\n";
    res = res +  "    { " + ifPart.updateForm(env,local,types,entities,vars) + " }\n";
    if (elsePart != null)
    { res = res + "    else { " + elsePart.updateForm(env,local,types,entities,vars) + " }\n"; }
    return res;
  } 
  public String updateFormJava6(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormJava6(env,local) + " }\n"; } 
	String res = "if (" + test.queryFormJava6(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormJava6(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormJava6(env,local) + " }\n"; }
    return res;
  } 
  public String updateFormJava7(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormJava7(env,local) + " }\n"; } 
	String res = "if (" + test.queryFormJava7(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormJava7(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormJava7(env,local) + " }\n"; }
    return res;
  } 
  public String updateFormCSharp(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormCSharp(env,local) + " }\n"; } 
	String res = "if (" + test.queryFormCSharp(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormCSharp(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormCSharp(env,local) + " }\n"; }
    return res;
  } 
  public String updateFormCPP(java.util.Map env, boolean local)
  { if ("true".equals(test + ""))
    { return "    { " + ifPart.updateFormCPP(env,local) + " }\n"; } 
	String res = "if (" + test.queryFormCPP(env,local) + ")\n";
    res = res + "{ " + ifPart.updateFormCPP(env,local) + " }\n";
    if (elsePart != null)
    { res = res + "else { " + elsePart.updateFormCPP(env,local) + " }\n"; }
    return res;
  } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression cond = test.binvariantForm(env,local); 
    BStatement ifstat = ifPart.bupdateForm(env,local);
    if (elsePart != null)
    { return new BIfStatement(cond,ifstat,
                     elsePart.bupdateForm(env,local)); 
    } 
    else 
    { return new BIfStatement(cond,ifstat); } 
  } 
  public String bupdateForm()
  { BExpression cond = test.bqueryForm(); 
    String ifstat = ifPart.bupdateForm();
    if (elsePart != null)
    { return "IF " + cond + " THEN " + ifstat + " ELSE " + elsePart.bupdateForm() + " END"; } 
    else 
    { return "IF " + cond + " THEN " + ifstat + " END"; } 
  } 
  public Vector allPreTerms()
  { Vector res = new Vector();
    res.addAll(test.allPreTerms()); 
    res.addAll(ifPart.allPreTerms()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allPreTerms()); }  
    return res;  
  }  
  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    res.addAll(test.allPreTerms(var)); 
    res.addAll(ifPart.allPreTerms(var)); 
    if (elsePart != null) 
    { res.addAll(elsePart.allPreTerms(var)); }  
    return res;  
  }  
  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(test.allReadFrame()); 
    res.addAll(ifPart.readFrame()); 
    if (elsePart != null) 
    { res.addAll(elsePart.readFrame()); } 
    return res;  
  }  
  public Vector writeFrame()
  { Vector res = new Vector();
    res.addAll(ifPart.writeFrame()); 
    if (elsePart != null) 
    { res.addAll(elsePart.writeFrame()); } 
    return res;  
  }  
  public Statement checkConversions(Entity e, Type propType, Type propElemType, 
                                    java.util.Map interp)
  { Statement ifc = ifPart.checkConversions(e,propType,propElemType,interp); 
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.checkConversions(e,propType,propElemType,interp); }
    return new ConditionalStatement(test, ifc, elsec); 
  }  
  public Statement replaceModuleReferences(UseCase uc)
  { Statement ifc = ifPart.replaceModuleReferences(uc);
    Expression tt = test.replaceModuleReferences(uc);  
    Statement elsec = null; 
    if (elsePart != null) 
    { elsec = elsePart.replaceModuleReferences(uc); }
    return new ConditionalStatement(tt, ifc, elsec); 
  }  
  public int syntacticComplexity()
  { int res = test.syntacticComplexity();
    res = res + ifPart.syntacticComplexity(); 
    if (elsePart != null)
    { res = res + elsePart.syntacticComplexity(); }
    return res + 1;
  }
  public int cyclomaticComplexity()
  { int res = test.cyclomaticComplexity(); 
    res = res + ifPart.cyclomaticComplexity();
    if (elsePart != null) 
    { res = res + elsePart.cyclomaticComplexity(); } 
    return res; 
  }
  public int epl()
  { int res = 0; 
    res = res + ifPart.epl();
    if (elsePart != null) 
    { res = res + elsePart.epl(); } 
    return res; 
  }
  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allOperationsUsedIn()); 
    res.addAll(ifPart.allOperationsUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allOperationsUsedIn()); } 
    return res;  
  }  
  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(test.allAttributesUsedIn()); 
    res.addAll(ifPart.allAttributesUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.allAttributesUsedIn()); } 
    return res;  
  }  
  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(test.getUses(var)); 
    res.addAll(ifPart.getUses(var)); 
    if (elsePart != null) 
    { res.addAll(elsePart.getUses(var)); } 
    return res;  
  }  
  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    res.addAll(ifPart.getVariableUses()); 
    if (elsePart != null) 
    { res.addAll(elsePart.getVariableUses()); } 
    return res;  
  }  
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector();
    res.addAll(test.getVariableUses()); 
    Vector ifuses = ifPart.getVariableUses(unused); 
    res.addAll(ifuses); 
    if (elsePart != null) 
    { Vector elseuses = elsePart.getVariableUses(unused); 
      res.addAll(elseuses); 
    } 
    return res;  
  }  
  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(test.equivalentsUsedIn()); 
    res.addAll(ifPart.equivalentsUsedIn()); 
    if (elsePart != null) 
    { res.addAll(elsePart.equivalentsUsedIn()); } 
    return res;  
  }  
  public Vector metavariables()
  { Vector res = test.metavariables();
    res.addAll(ifPart.metavariables());  
    if (elsePart != null) 
    { res.addAll(elsePart.metavariables()); }   
    return res;  
  }  
}