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
}