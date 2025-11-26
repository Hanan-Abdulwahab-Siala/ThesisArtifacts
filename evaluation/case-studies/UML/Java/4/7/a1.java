class WhileStatement extends Statement
{ private Expression loopTest; 
  private Statement body; 
  private Expression invariant; 
  private Expression variant; 
  int loopKind = WHILE; 
  Expression loopVar; 
  Expression loopRange; 
  public WhileStatement()
  { loopTest = new BasicExpression(true); 
    body = new InvocationStatement("skip");
  } 
  public WhileStatement(Expression e, Statement b)
  { loopTest = e; 
    if (b == null) 
    { body = new InvocationStatement("skip"); } 
    else 
    { body = b;
      body.setBrackets(true);
    } 
  } 
  public WhileStatement(Expression e, Vector b)
  { loopTest = e; 
    if (b == null || b.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (b.size() == 1)
    { body = (Statement) b.get(0);
      body.setBrackets(true);
    }
    else 
    { body = new SequenceStatement(b);
      body.setBrackets(true);
    }  
  } 
  public WhileStatement(Expression lv, Expression lr, 
                        Vector b)
  { 
    if (lv instanceof SetExpression &&
        lr.isMap()) 
    { SetExpression sv = (SetExpression) lv; 
      if (sv.size() == 2)
      { lv = (Expression) sv.getElement(0); 
        Expression mv = (Expression) sv.getElement(1); 
        UnaryExpression kys = 
          new UnaryExpression("->keys",lr); 
        kys.setType(new Type("Set", null)); 
        loopTest = 
          new BinaryExpression(":", lv, kys); 
        loopTest.setType(new Type("boolean", null)); 
        CreationStatement cs = 
          new CreationStatement(mv,lr.getElementType());
        Expression lrAtlv = 
          new BinaryExpression("->at", lr, lv);  
        AssignStatement asgn = 
          new AssignStatement(mv,lrAtlv); 
        b.add(0,asgn); 
        b.add(0,cs); 
      }  
      else 
      { loopTest = new BinaryExpression(":", lv, lr);
        loopTest.setType(new Type("boolean", null)); 
      } 
    } 
    else 
    { loopTest = new BinaryExpression(":", lv, lr);
      loopTest.setType(new Type("boolean", null)); 
    } 
    loopKind = FOR;  
    if (b == null || b.size() == 0) 
    { body = new InvocationStatement("skip"); } 
    else if (b.size() == 1)
    { body = (Statement) b.get(0); 
      body.setBrackets(true);
    }
    else 
    { body = new SequenceStatement(b);
      body.setBrackets(true);
    }  
    loopVar = lv;
    loopRange = lr;
  } 
  public WhileStatement(Expression lv, Expression lr, 
                        Statement stat)
  { loopTest = new BinaryExpression(":", lv, lr);
    loopTest.setType(new Type("boolean", null)); 
    loopKind = FOR;  
    body = stat;
    body.setBrackets(true);
    loopVar = lv;
    loopRange = lr;
  } 
  public String getOperator() 
  { if (loopKind == WHILE) 
    { return "while"; }
    else if (loopKind == REPEAT)
    { return "repeat"; } 
    return "for"; 
  } 
  public Expression getLoopTest()
  { return loopTest; } 
  public void setTest(Expression tst)
  { loopTest = tst;
    if (loopRange == null &&
        loopTest != null && 
        loopTest instanceof BinaryExpression)
    { BinaryExpression bexpr = (BinaryExpression) loopTest; 
      if (bexpr.getOperator().equals(":") && 
          (loopVar + "").equals(bexpr.getLeft() + ""))
      { loopRange = bexpr.getRight(); } 
    } 
  } 
  public void setBody(Statement stat)
  { body = stat; } 
  public void setLoopKind(int lk)
  { loopKind = lk; } 
  public void setLoopRange(Expression lv, Expression lr)
  { loopVar = lv;
    loopRange = lr;
  }
  public void setLoopVar(Expression lv)
  { loopVar = lv; }
  public void setLoopRangeVarFromTest(Expression expr)
  { if (expr != null && 
        expr instanceof BinaryExpression)
    { BinaryExpression binexpr = (BinaryExpression) expr; 
      loopVar = binexpr.getLeft(); 
      loopRange = binexpr.getRight(); 
    } 
  } 
  public void setIterationRange(Expression expr)
  { loopRange = expr; } 
  public void setEntity(Entity e)
  { entity = e; 
    if (body != null) 
    { body.setEntity(e); } 
  }
  public Statement getBody()
  { return body; } 
  public Statement getLoopBody()
  { return body; } 
  public Expression getLoopVar()
  { return loopVar; } 
  public Expression getTest()
  { return loopTest; } 
  public Object clone()
  { Expression lv = null; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = (Expression) loopRange.clone(); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = (Expression) loopTest.clone(); }  
    Statement newbody = (Statement) body.clone(); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.clone(); }  
    res.setInvariant(inv); 
    Expression var = null; 
    if (variant != null) 
    { var = (Expression) variant.clone(); }  
    res.setVariant(var); 
    return res; 
  } 
  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { int res = Statement.NORMAL; 
    if (loopKind == Statement.WHILE)
    { Expression testvalue = 
         loopTest.evaluate(sigma, beta); 
      while ("true".equals(testvalue + ""))
      { res = body.execute(sigma, beta);
        System.out.println("---> iteration of while loop: " + sigma + ", " + beta + " " + res);
        if (res == Statement.BREAK)
        { return Statement.NORMAL; } 
        if (res == Statement.RETURN)
        { return res; }   
        testvalue = loopTest.evaluate(sigma, beta); 
      } 
      return Statement.NORMAL; 
    } 
    else if (loopKind == Statement.REPEAT)
    { res = body.execute(sigma, beta); 
      if (res == Statement.BREAK)
      { return Statement.NORMAL; } 
      if (res == Statement.RETURN)
      { return res; }   
      Expression testvalue = 
         loopTest.evaluate(sigma, beta); 
      while ("false".equals(testvalue + ""))
      { res = body.execute(sigma, beta);
        System.out.println("---> iteration of repeat loop: " + sigma + ", " + beta + " " + res);
        if (res == Statement.BREAK)
        { return Statement.NORMAL; } 
        if (res == Statement.RETURN)
        { return res; }   
        testvalue = loopTest.evaluate(sigma, beta); 
      }
      return Statement.NORMAL;  
    } 
    else if (loopKind == Statement.FOR)
    { Expression rng = loopRange.evaluate(sigma, beta); 
      if (rng instanceof SetExpression)
      { SetExpression serange = (SetExpression) rng;   
        int n = serange.size();     
        String lv = "" + loopVar; 
        beta.addNewEnvironment(); 
        beta.addVariable(lv, new BasicExpression("null")); 
        for (int i = 0; i < n; i++) 
        { Expression val = serange.getElement(i); 
          beta.setVariableValue(lv, val); 
          res = body.execute(sigma, beta); 
          System.out.println("---> iteration of for loop: " + sigma + ", " + beta + " " + res);
          if (res == Statement.BREAK)
          { return Statement.NORMAL; } 
          if (res == Statement.RETURN)
          { return res; }   
        } 
        beta.removeLastEnvironment();
        return Statement.NORMAL;  
      } 
    } 
    return Statement.NORMAL; 
  } 
  public Statement loopContinuation()
  { 
    if (loopKind == Statement.FOR)
    { Expression lv = null; 
      if (loopVar != null) 
      { lv = (Expression) loopVar.clone(); } 
      BasicExpression lr = null; 
      if (loopRange != null && 
          (loopRange + "").startsWith("Integer.subrange(") &&
          loopRange.getParameters() != null) 
      { lr = (BasicExpression) loopRange.clone(); 
        Expression par2 = lr.getParameter(2); 
        Expression newtest = new BinaryExpression("<", lv, par2); 
        Statement newassign = 
           new AssignStatement(lv, 
             new BinaryExpression("+", lv, 
                                  new BasicExpression(1)));  
        SequenceStatement newbody = new SequenceStatement(); 
        newbody.addStatement(newassign); 
        newbody.addStatement(body); 
        WhileStatement ws = 
           new WhileStatement(newtest, newbody); 
        ws.setLoopKind(Statement.WHILE); 
        return ws; 
      } 
    } 
    return (Statement) this.clone(); 
  } 
  public Statement dereference(BasicExpression var)
  { Expression lv = null; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = (Expression) loopRange.dereference(var); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = (Expression) loopTest.dereference(var); }
    if ((var + "").equals(loopVar + ""))
    { WhileStatement res1 = new WhileStatement(lt,body); 
      res1.setEntity(entity); 
      res1.setLoopKind(loopKind); 
      res1.setLoopRange(lv,lr); 
      res1.setBrackets(brackets); 
      res1.setInvariant(invariant); 
      res1.setVariant(variant); 
      return res1; 
    } 
    Statement newbody = (Statement) body.dereference(var); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.dereference(var); }  
    res.setInvariant(inv); 
    Expression vv = null; 
    if (variant != null) 
    { vv = (Expression) variant.dereference(var); }  
    res.setVariant(vv); 
    return res; 
  } 
  public Expression definedness()
  { Expression rtest = new BasicExpression(true); 
    if (loopRange != null) 
    { rtest = loopRange.definedness(); } 
    else if (loopTest != null) 
    { rtest = loopTest.definedness(); } 
    Expression bdef = body.definedness(); 
    return Expression.simplify("&", rtest, bdef, null); 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (loopRange != null && 
        loopRange.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    {  
      loopRange.findClones(clones,rule,op); 
    }  
    else if (loopTest != null && 
        loopTest.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    {  
      loopTest.findClones(clones,rule,op); 
    } 
    body.findClones(clones,rule,op); 
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (loopRange != null && 
        loopRange.syntacticComplexity() >= 
                                UCDArea.CLONE_LIMIT) 
    { loopRange.findClones(clones,cdefs,rule,op); }  
    else if (loopTest != null && 
        loopTest.syntacticComplexity() >= UCDArea.CLONE_LIMIT) 
    { 
      loopTest.findClones(clones,cdefs,rule,op); 
    } 
    body.findClones(clones,cdefs,rule,op); 
  }
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { if (loopRange != null) 
    { loopRange.energyUse(uses,rUses,aUses); }
    else if (loopTest != null)
    { loopTest.energyUse(uses,rUses,aUses); }
    body.energyUse(uses,rUses,aUses);
    if (loopKind == FOR) 
    { if (loopRange != null) 
      { int rcomp = loopRange.syntacticComplexity();
        if (rcomp > TestParameters.syntacticComplexityLimit)
        { int acount = (int) uses.get("amber"); 
          uses.set("amber", acount + 1); 
          aUses.add("! Code smell (MEL): too high expression complexity (" + rcomp + ") for " + loopRange + "\n" +  
                    ">>> Recommend OCL refactoring"); 
        } 
      } 
    }
    else if (loopTest != null)
    { int syncomp = loopTest.syntacticComplexity(); 
      if (syncomp > TestParameters.syntacticComplexityLimit)
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + loopTest + "\n" +  
                  ">>> Recommend OCL refactoring"); 
      }
    }  
    if (loopKind == WHILE || loopKind == REPEAT)
    { if (loopTest != null && loopKind == WHILE &&
          "true".equals("" + loopTest)) 
      { int rcount = (int) uses.get("red"); 
        uses.set("red", rcount + 1); 
        rUses.add("!!! Unbounded while loop with true condition: may not terminate!: " + this); 
      }
      else if (loopTest != null && loopKind == REPEAT &&
               "false".equals("" + loopTest)) 
      { int rcount = (int) uses.get("red"); 
        uses.set("red", rcount + 1); 
        rUses.add("!!! Unbounded repeat loop with false condition: may not terminate!: " + this); 
      }
      else 
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount + 1); 
        aUses.add("! Unbounded loops can be inefficient: " + 
                  this + 
                  "\n>> Recommend replacing by a bounded loop");
      }  
    } 
    if (Statement.hasLoopStatement(body))
    { int rcount = (int) uses.get("amber"); 
      uses.set("amber", rcount + 1); 
      aUses.add("! Nested loops can be very inefficient: " + this); 
    } 
    else if (loopKind == FOR && 
             Statement.isCumulativeBody(loopVar,body))
    { int rcount = (int) uses.get("amber"); 
      uses.set("amber", rcount + 1); 
      aUses.add("! Possible code reduction of loop to assignment(s): " + this);
    }
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { if (loopRange != null) 
    { loopRange.collectionOperatorUses(nestingLevel, 
                                       operatorsAtLevel, 
                                       vars); 
    }
    else if (loopTest != null)
    { loopTest.collectionOperatorUses(nestingLevel, 
                                      operatorsAtLevel, 
                                      vars); 
    }
    Vector newvars = new Vector(); 
    newvars.addAll(vars); 
    if (loopVar != null) 
    { newvars.add("" + loopVar); }
    else if (loopTest != null)
    { Vector evuses = loopTest.getVariableUses(); 
      Vector vuses = 
                VectorUtil.getStrings(evuses); 
      newvars.addAll(vuses); 
    }  
    Vector wrfr = body.writeFrame();
    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { newvars.add(wrv.substring(k+2)); } 
      else 
      { newvars.add(wrv); } 
    }  
    body.collectionOperatorUses(nestingLevel + 1,
                                operatorsAtLevel, newvars);
    return operatorsAtLevel; 
  }  
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { if (loopRange != null) 
    { String val = loopRange + ""; 
      loopRange.findMagicNumbers(mgns,val,op); 
    }  
    else if (loopTest != null) 
    { loopTest.findMagicNumbers(mgns,"" + loopTest,op); } 
    body.findMagicNumbers(mgns,rule,op); 
  }
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Vector newexcls = new Vector(); 
    newexcls.addAll(excl); 
    Expression lv = null; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone();
      newexcls.add(lv + ""); 
    }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.addContainerReference(
                               ref,var,newexcls); 
    }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.addContainerReference(ref,var,newexcls); }
    Statement newbody = 
         body.addContainerReference(ref,var,newexcls); 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = invariant.addContainerReference(
                                 ref,var,newexcls); 
    }  
    res.setInvariant(inv); 
    Expression vv = null; 
    if (variant != null) 
    { vv = variant.addContainerReference(ref,var,newexcls); }  
    res.setVariant(vv); 
    return res; 
  } 
  public Statement optimiseOCL()
  { Expression lv = loopVar; 
    if (loopVar != null) 
    { lv = (Expression) loopVar.clone(); }
    Expression lr = loopRange; 
    if (loopRange != null) 
    { lr = loopRange.simplifyOCL(); }
    else if (loopTest != null && 
             loopTest instanceof BinaryExpression)
    { BinaryExpression bexpr = (BinaryExpression) loopTest; 
      if (bexpr.getOperator().equals(":") && 
          (lv + "").equals(bexpr.getLeft() + ""))
      { lr = bexpr.getRight(); } 
    } 
    Expression lt = loopTest; 
    if (loopTest != null) 
    { lt = loopTest.simplifyOCL(); }
    Statement newbody = body.optimiseOCL();
    if (loopKind == FOR)
    { 
      if (Statement.isCumulativeBody(lv,newbody))
      { 
        Statement newcode = 
           Statement.cumulativeCode(lv,lr,newbody);
        if (newcode != null) 
        { return newcode; }  
      } 
    } 
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets); 
    Expression inv = null; 
    if (invariant != null) 
    { inv = invariant.simplifyOCL(); }  
    res.setInvariant(inv); 
    Expression vv = null; 
    if (variant != null) 
    { vv = variant.simplifyOCL(); }  
    res.setVariant(vv); 
    return res; 
  } 
  public void setInvariant(Expression inv) 
  { invariant = inv; } 
  public void setVariant(Expression inv) 
  { variant = inv; } 
  public static WhileStatement createInvocationLoop(BasicExpression call, Expression range)
  { String v = Identifier.nextIdentifier("loopvar$"); 
    BasicExpression ve = new BasicExpression(v); 
    Type elemt = range.getElementType(); 
    ve.setType(elemt);
    if (elemt != null) 
    { ve.setElementType(elemt.getElementType()); } 
    ve.umlkind = Expression.VARIABLE; 
    BinaryExpression test = new BinaryExpression(":", ve, range); 
    test.setType(new Type("boolean", null)); 
    test.setElementType(new Type("boolean", null)); 
    BasicExpression invokee = (BasicExpression) call.clone(); 
    invokee.setObjectRef(ve); 
    InvocationStatement invoke = new InvocationStatement(invokee); 
    WhileStatement lp = new WhileStatement(test, invoke); 
    lp.setLoopKind(Statement.FOR);
    lp.setLoopRange(ve,range);  
    return lp;
  } 
  public String bupdateForm()
  { String res = "  WHILE (" + loopTest + ")"; 
    res = res + "  DO \n "; 
    res = res + body.bupdateForm(); 
    if (invariant != null) 
    { res = res + "  INVARIANT " + invariant; } 
    if (variant != null) 
    { res = res + "  VARIANT " + variant; } 
    res = res + "  END";
    return res;  
  } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression btest = new BBasicExpression("true"); 
    if (loopRange != null && loopVar != null)
    { 
      if (loopRange.isOrdered())
      { String ind = Identifier.nextIdentifier(loopVar + "_ind");
        BasicExpression indbe = new BasicExpression(ind); 
        BBasicExpression indbeb = new BBasicExpression(ind);   
        Expression loopRangeSize0 = new UnaryExpression("->size",loopRange); 
        Expression tst0 = new BinaryExpression("<=", indbe, loopRangeSize0); 
        btest = tst0.binvariantForm(env,local); 
        BParallelStatement ss = new BParallelStatement(false); 
        BAssignStatement bast = new BAssignStatement(indbeb,new BBasicExpression("1"));
        BApplyExpression seqAtInd = new BApplyExpression(loopRange.binvariantForm(env,local),indbeb); 
        BinaryExpression loopRangeSize = new BinaryExpression("+",
                                           new UnaryExpression("->size",loopRange),
                                           new BasicExpression("1")); 
        Expression tst = new BinaryExpression("<=", indbe, loopRangeSize); 
        BExpression invb = tst.binvariantForm(env,local);
        BStatement bbody = body.bupdateForm(env,local); 
        BAssignStatement bast0 = new BAssignStatement(
            new BBasicExpression(loopVar + ""), seqAtInd); 
        ss.addStatement(bast0); 
        ss.addStatement(bbody); 
        BAssignStatement bast1 = new BAssignStatement(indbeb,
                                   new BBinaryExpression("+", indbeb, new BBasicExpression("1")));
        ss.addStatement(bast1); 
        BinaryExpression var1 = 
          new BinaryExpression("+",
            new BinaryExpression("-",new UnaryExpression("->size",loopRange),indbe), 
                                        new BasicExpression(1)); 
        BExpression bvar1 = var1.binvariantForm(env,local); 
        BStatement loop1 = new BLoopStatement(btest,invb,bvar1,
                                  new BVarStatement(loopVar + "", ss) );
        BParallelStatement ss0 = new BParallelStatement(false); 
        ss0.addStatement(bast); 
        ss0.addStatement(loop1); 
        BStatement res = new BVarStatement(ind,ss0);
        return res;  
      }
      else 
      { String ind = Identifier.nextIdentifier(loopVar + "_unprocessed");
        BasicExpression indbe = new BasicExpression(ind); 
        BBasicExpression indbeb = new BBasicExpression(ind);  
        BExpression loopvarb = loopVar.binvariantForm(env,local);  
        BExpression brange = loopRange.binvariantForm(env,local); 
        BExpression emptysetb = new BSetExpression(); 
        btest = new BBinaryExpression("/=", indbeb, emptysetb); 
        BParallelStatement ss = new BParallelStatement(false); 
        BAssignStatement bast = new BAssignStatement(indbeb,brange);
        BExpression indInRange = new BBinaryExpression(":",loopvarb,indbeb); 
        Expression tst = new BinaryExpression("<:", indbe, loopRange); 
        BExpression invb = tst.binvariantForm(env,local);
        BStatement bbody = body.bupdateForm(env,local); 
        Vector indsetelems = new Vector(); 
        indsetelems.add(loopVar); 
        SetExpression indset = new SetExpression(indsetelems); 
        BExpression indsetb = indset.binvariantForm(env,local); 
        BAssignStatement bast0 = new BAssignStatement(indbeb, 
                                       new BBinaryExpression("-", indbeb, indsetb)); 
        ss.addStatement(bast0); 
        ss.addStatement(bbody); 
        Expression var1 = new UnaryExpression("->size",indbe); 
        BExpression bvar1 = var1.binvariantForm(env,local); 
        Vector loopanyvars = new Vector(); 
        loopanyvars.add(loopVar + ""); 
        BStatement loop1 = new BLoopStatement(btest,invb,bvar1,
                                   new BAnyStatement(loopanyvars, indInRange, ss) );
        BParallelStatement ss0 = new BParallelStatement(false); 
        ss0.addStatement(bast); 
        ss0.addStatement(loop1); 
        BStatement res = new BVarStatement(ind,ss0);
        return res;  
      } 
    } 
    if (loopTest != null) 
    { btest = loopTest.binvariantForm(env,local); }  
    BExpression binv = new BBasicExpression("true"); 
    if (invariant != null) 
    { binv = invariant.binvariantForm(env,local); } 
    BExpression bvar = new BBasicExpression("true"); 
    if (variant != null) 
    { bvar = variant.binvariantForm(env,local); }  
    BStatement bbody = body.bupdateForm(env,local); 
    return new BLoopStatement(btest,binv,bvar,bbody); 
  } 
  public void display()
  { System.out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { System.out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { System.out.println("  VARIANT " + variant); } 
    System.out.println("  DO \n "); 
    body.display(); 
    System.out.println("  END"); 
  } 
  public void display(PrintWriter out)
  { out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { out.println("  VARIANT " + variant); } 
    out.println("  DO\n "); 
    body.display(out); 
    out.println("  END"); 
  } 
  public void displayImp(String var, PrintWriter out) 
  { out.println("  WHILE (" + loopTest + ")"); 
    if (invariant != null) 
    { out.println("  INVARIANT " + invariant); } 
    if (variant != null) 
    { out.println("  VARIANT " + variant); } 
    out.println("  DO\n "); 
    body.displayImp(var,out); 
    out.println("  END"); 
  } 
  public void displayJava(String t) 
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { System.out.println("  do {"); 
      body.displayJava(t); 
      System.out.println("  } while (!(" + loopTest.toJava() + "));"); 
      return;
    } 
    if (brackets)
    { System.out.println(" ( " + loop + " ( " + loopTest.toJava() + " )"); 
      System.out.println("   {\n "); 
      body.displayJava(t); 
      System.out.println("   } )"); 
    } 
    else 
    { System.out.println("  " + loop + " ( " + loopTest.toJava() + " )"); 
      System.out.println("  {\n "); 
      body.displayJava(t); 
      System.out.println("  }");
    }  
  } 
  public void displayJava(String t, PrintWriter out)
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { out.println("  do {"); 
      body.displayJava(t,out); 
      out.println("  } while (!(" + loopTest.toJava() + "));"); 
      return;
    } 
    if (brackets)
    { out.println(" ( " + loop + " ( " + loopTest.toJava() + " )"); 
      out.println("   {\n "); 
      body.displayJava(t,out); 
      out.println("  } )");
    } 
    else  
    { out.println("  " + loop + " ( " + loopTest.toJava() + " )"); 
      out.println("  {\n "); 
      body.displayJava(t,out); 
      out.println("  }");
    }  
  }   
  public String saveModelData(PrintWriter out)
  { String res = ""; 
    if (loopKind == FOR)
    { res = Identifier.nextIdentifier("boundedloopstatement_"); 
      out.println(res + " : BoundedLoopStatement");
    } 
    else 
    { res = Identifier.nextIdentifier("unboundedloopstatement_"); 
      out.println(res + " : UnboundedLoopStatement");
    } 
    out.println(res + ".statId = \"" + res + "\""); 
    Statement actualBody = body; 
    Expression actualTest = loopTest; 
    if (loopKind == REPEAT)
    { 
      actualTest = new BasicExpression(true); 
      Statement breakOut = new BreakStatement(); 
      Statement skipStatement = new InvocationStatement("skip"); 
      ConditionalStatement newif = 
        new ConditionalStatement(loopTest, 
                                 breakOut, skipStatement); 
      actualBody = new SequenceStatement(); 
      ((SequenceStatement) actualBody).addStatement(body); 
      ((SequenceStatement) actualBody).addStatement(newif); 
      actualBody.setBrackets(true); 
    } 
    String testid = actualTest.saveModelData(out); 
    String bodyid = actualBody.saveModelData(out); 
    out.println(res + ".test = " + testid); 
    out.println(res + ".body = " + bodyid);  
    if (loopVar != null) 
    { String lvid = loopVar.saveModelData(out); 
      out.println(res + ".loopVar = " + lvid);
    } 
    if (loopRange != null) 
    { String lrid = loopRange.saveModelData(out); 
      out.println(res + ".loopRange = " + lrid);
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = ""; 
    if (loopKind == FOR)
    { res = Identifier.nextIdentifier("boundedloopstatement_"); 
      out.println(res + " : BoundedLoopStatement");
    } 
    else 
    { res = Identifier.nextIdentifier("unboundedloopstatement_"); 
      out.println(res + " : UnboundedLoopStatement");
    } 
    out.println(res + ".statId = \"" + res + "\""); 
    Statement actualBody = body; 
    Expression actualTest = loopTest; 
    if (loopKind == REPEAT)
    { 
      actualTest = new BasicExpression(true); 
      Statement breakOut = new BreakStatement(); 
      Statement skipStatement = new InvocationStatement("skip"); 
      ConditionalStatement newif = 
        new ConditionalStatement(loopTest, 
                                 breakOut, skipStatement); 
      actualBody = new SequenceStatement(); 
      ((SequenceStatement) actualBody).addStatement(body); 
      ((SequenceStatement) actualBody).addStatement(newif); 
      actualBody.setBrackets(true); 
    } 
    String testid = actualTest.saveModelData(out); 
    String bodyid = actualBody.saveModelData(out, ent); 
    out.println(res + ".test = " + testid); 
    out.println(res + ".body = " + bodyid);  
    if (loopVar != null) 
    { String lvid = loopVar.saveModelData(out); 
      out.println(res + ".loopVar = " + lvid);
    } 
    if (loopRange != null) 
    { String lrid = loopRange.saveModelData(out); 
      out.println(res + ".loopRange = " + lrid);
    } 
    return res; 
  } 
  public Statement substituteEq(String oldE, Expression newE)
  { Statement newbody; 
    if (oldE.equals(body + ""))
    { newbody = new InvocationStatement(newE); } 
    else 
    { newbody = body.substituteEq(oldE,newE); }
    Expression lv = null; 
    if (loopVar != null) 
    { lv = loopVar.substituteEq(oldE,newE); }  
    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.substituteEq(oldE,newE); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.substituteEq(oldE,newE); }  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(lv,lr); 
    res.setBrackets(brackets);
    Expression inv = null; 
    if (invariant != null) 
    { inv = (Expression) invariant.substituteEq(oldE,newE); }  
    res.setInvariant(inv); 
    Expression var = null; 
    if (variant != null) 
    { var = (Expression) variant.substituteEq(oldE,newE); }  
    res.setVariant(var); 
    return res; 
  }  
  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { Statement newbody = body.removeSlicedParameters(op,fpars); 
    Expression lr = null; 
    if (loopRange != null) 
    { lr = loopRange.removeSlicedParameters(op,fpars); }  
    Expression lt = null; 
    if (loopTest != null) 
    { lt = loopTest.removeSlicedParameters(op,fpars); }  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(loopVar,lr); 
    res.setBrackets(brackets);
    res.setInvariant(invariant); 
    res.setVariant(variant); 
    return res; 
  }  
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { Statement newbody = body.checkConversions(e,propType, propElemType, interp); 
    WhileStatement res = new WhileStatement(loopTest,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind); 
    res.setLoopRange(loopVar,loopRange); 
    res.setBrackets(brackets);
    res.setInvariant(invariant); 
    res.setVariant(variant); 
    return res; 
  } 
  public Statement replaceModuleReferences(UseCase uc)
  { Statement newbody = body.replaceModuleReferences(uc);
    Expression lt = loopTest.replaceModuleReferences(uc);  
    WhileStatement res = new WhileStatement(lt,newbody); 
    res.setEntity(entity); 
    res.setLoopKind(loopKind);
    Expression lr = loopRange; 
    if (loopRange != null) 
    { lr = loopRange.replaceModuleReferences(uc); } 
    res.setLoopRange(loopVar,lr); 
    res.setBrackets(brackets);
    res.setInvariant(invariant); 
    res.setVariant(variant); 
    return res; 
  } 
  public String toStringJava() 
  { java.util.Map env = new java.util.HashMap(); 
    if (entity != null)
    { String ename = entity.getName(); 
      env.put(ename,"this");
    } 
    String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    else if (loopKind == REPEAT)
    { String rres = "  do {\n"; 
      rres = rres + "  " + body.toStringJava(); 
      rres = rres + "  } while (!(" + loopTest.queryForm(env,false) + "));\n"; 
      return rres;
    } 
    String res = "  " + loop + " (" + loopTest.queryForm(env,false) + ")"; 
    res = res + "  {\n "; 
    res = res + body.toStringJava(); 
    return res + "  }"; 
  } 
  public String toEtl() 
  { String loop = "while"; 
    if (loopKind == FOR)
    { loop = "for"; } 
    String res = "  " + loop + " (" + loopTest + ")"; 
    res = res + "  {\n "; 
    res = res + body.toEtl(); 
    return res + "  }"; 
  } 
  public String toString()
  { String res = " while "; 
    if (loopKind == FOR)
    { res = " for "; }
    else if (loopKind == REPEAT)
    { res = "  repeat "; 
      res = res + body.toString(); 
      res = res + "  until " + loopTest + " "; 
      return res;
    } 
    res = res + loopTest + " do " + body + " "; 
    if (brackets)
    { res = "( " + res + " )"; } 
    return res; 
  } 
  public String toAST()
  { String res = "(OclStatement "; 
    if (loopKind == FOR)
    { res = res + "for " + loopVar + " : " + loopRange.toAST() + " do " + body.toAST() + " )"; 
    }
    else if (loopKind == REPEAT)
    { res = res + " repeat " + body.toAST() + " until " + 
            loopTest.toAST() + " )"; 
    } 
    else 
    { res = res + "while " + loopTest.toAST() + " do " + 
            body.toAST() + " )"; 
    }
    return res;  
  }  
  public boolean containsSubexpression(Expression expr) 
  { if (loopKind == WHILE || loopKind == REPEAT)
    { if (loopTest.containsSubexpression(expr))
      { return true; }  
      return body.containsSubexpression(expr); 
    }
    if (loopRange.containsSubexpression(expr))
    { return true; } 
    return body.containsSubexpression(expr); 
  } 
  public Vector singleMutants()
  { Vector res = new Vector();
    if (loopKind == WHILE || loopKind == REPEAT)
    { Vector exprs = loopTest.singleMutants(); 
      for (int i = 0; i < exprs.size(); i++) 
      { Expression mut = (Expression) exprs.get(i); 
        WhileStatement clne = (WhileStatement) clone(); 
        clne.loopTest = mut; 
        res.add(clne);
      } 
    }  
    Vector bodymutants = body.singleMutants(); 
    for (int i = 0; i < bodymutants.size(); i++) 
    { Statement mut = (Statement) bodymutants.get(i); 
      WhileStatement clne = (WhileStatement) clone(); 
      clne.body = mut; 
      res.add(clne);
    } 
    return res; 
  } 
  public boolean typeCheck(Vector types, Vector entities, Vector ctxs, Vector env)
  { Vector env1 = new Vector(); 
    env1.addAll(env);
    System.out.println(">>> Type-checking " + this + " " + loopRange); 
    boolean res = loopTest.typeCheck(types,entities,ctxs,env1);
    if (loopRange != null) 
    { res = loopRange.typeCheck(types,entities,ctxs,env1);
      Type lrt = loopRange.getType(); 
      Type lret = loopRange.getElementType(); 
      System.out.println(">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")");
      System.out.println(); 
      if (lret == null)
      { if (loopVar.type != null) 
        { lret = loopVar.type; } 
        else 
        { lret = new Type("OclAny", null); } 
      } 
      Attribute lv = new Attribute(loopVar + "", lret, ModelElement.INTERNAL); 
      if (lret != null) 
      { lv.setElementType(lret.getElementType()); 
        if (lret.isEntity())
        { lv.setEntity(lret.getEntity()); } 
      } 
      System.out.println(">>> Type of loop variable " + lv + " is " + lv.getType() + " entity: " + lv.getEntity());   
      env1.add(lv); 
      Vector wrf = body.writeFrame(); 
      Vector actuses = loopRange.getVariableUses();
      actuses = ModelElement.removeExpressionByName("skip", actuses); 
      actuses.add(loopVar + ""); 
      Vector attrs = loopRange.allAttributesUsedIn(); 
      for (int i = 0; i < wrf.size(); i++) 
      { String wv = (String) wrf.get(i); 
        int indx = wv.indexOf("::"); 
        if (indx > 0) 
        { wv = wv.substring(indx + 2); }
        if (VectorUtil.containsEqualString(wv,actuses))
        { System.err.println("!! ERROR: writing loop var/range variable " + wv + " in loop body\n"); } 
        if (VectorUtil.containsEqualString(wv,attrs))
        { System.err.println("!! ERROR: writing loop range attribute " + wv + " in loop body\n"); } 
      } 
    } 
    return body.typeCheck(types,entities,ctxs,env1); 
  }  
  public boolean typeInference(Vector types, Vector entities, Vector ctxs, Vector env, java.util.Map vartypes)
  { Vector env1 = new Vector(); 
    env1.addAll(env);
    System.out.println(">>> Type-checking " + this + " " + loopRange); 
    boolean res = loopTest.typeInference(
                     types,entities,ctxs,env1,vartypes);
    if (loopRange != null) 
    { res = loopRange.typeInference(types,entities,
                                    ctxs,env1,vartypes);
      Type lrt = loopRange.getType(); 
      Type lret = loopRange.getElementType(); 
      System.out.println(">>> Type of loop range " + loopRange + " is " + lrt + "(" + lret + ")");
      System.out.println(); 
      if (lret == null)
      { if (loopVar.type != null) 
        { lret = loopVar.type; } 
        else 
        { lret = new Type("OclAny", null); } 
      } 
      Attribute lv = new Attribute(loopVar + "", lret, ModelElement.INTERNAL); 
      if (lret != null) 
      { lv.setElementType(lret.getElementType()); 
        if (lret.isEntity())
        { lv.setEntity(lret.getEntity()); } 
      } 
      System.out.println(">>> Type of loop variable " + lv + " is " + lv.getType() + " entity: " + lv.getEntity());   
      env1.add(lv); 
    } 
    return body.typeInference(types,entities,ctxs,env1,vartypes); 
  }  
  public Expression wpc(Expression post)
  { if (loopKind == WHILE)
    { 
      Expression bodywpc = body.wpc(post); 
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(loopTest,post),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(post, nextIter); 
    } 
    if (loopKind == REPEAT)
    { 
      Expression bodywpc = body.wpc(post);
      Expression ntest = Expression.negate(loopTest);  
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(ntest,post),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(bodywpc, nextIter); 
    } 
    return loopTest; 
  } 
  public Expression wpc(Expression inv, Expression post)
  { if (loopKind == WHILE)
    { 
      Expression bodywpc = body.wpc(inv, post); 
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(loopTest,inv),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(inv, nextIter); 
    } 
    if (loopKind == REPEAT)
    { 
      Expression bodywpc = body.wpc(inv,post);
      Expression ntest = Expression.negate(loopTest);  
      Expression nextIter = 
        Expression.simplifyImp(
          Expression.simplifyAnd(ntest,inv),bodywpc); 
      nextIter.setBrackets(true); 
      return Expression.simplifyAnd(bodywpc, nextIter); 
    } 
    return inv; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector bodydeps = body.dataDependents(allvars,vars);
    Vector result = new Vector(); 
    result.addAll(bodydeps); 
    if (loopKind == FOR && loopVar != null && 
        loopRange != null)
    { Vector rangevars = loopRange.allReadData(); 
      String lv = loopVar + ""; 
      if (vars.contains(lv)) 
      { 
        result = VectorUtil.union(result,rangevars); 
        result.remove(lv); 
      } 
      return result;  
    } 
    if ((loopKind == WHILE || loopKind == REPEAT) && 
        loopTest != null && body.updates(vars)) 
    { Vector testvars = loopTest.allReadData(); 
      result = VectorUtil.union(result,testvars); 
    } 
    return result; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Map bodymap = new Map(); 
    Map bodydlin = new Map(); 
    Vector bodydeps = body.dataDependents(allvars,vars, bodymap, bodydlin);
    Vector result = new Vector(); 
    result.addAll(bodydeps); 
    Vector updatedvariables = bodymap.range(); 
    mp.unionWith(bodymap); 
    Vector modifiedvariables = bodydlin.range(); 
    dlin.unionWith(bodydlin); 
    if (loopKind == FOR && loopVar != null && 
        loopRange != null)
    { Vector rangevars = loopRange.allReadData(); 
      Vector rangeBEs = loopRange.allReadBasicExpressionData(); 
      String lv = loopVar + ""; 
      for (int i = 0; i < rangevars.size(); i++) 
      { String rv = "" + rangevars.get(i); 
        mp.add_pair(rv, lv); 
        for (int j = 0; j < updatedvariables.size(); j++) 
        { String vv = "" + updatedvariables.get(j); 
          mp.add_pair(rv, vv); 
        } 
      }
      for (int i = 0; i < rangeBEs.size(); i++) 
      { String rv = "" + rangeBEs.get(i); 
        dlin.add_pair(rv, lv);
        for (int k = 0; k < modifiedvariables.size(); k++) 
        { String vv = "" + modifiedvariables.get(k); 
          dlin.add_pair(rv, vv); 
        } 
      } 
      if (vars.contains(lv)) 
      { 
        result = VectorUtil.union(result,rangevars); 
        result.remove(lv); 
      } 
      return result;  
    } 
    if ((loopKind == WHILE || loopKind == REPEAT) && 
        loopTest != null && body.updates(vars)) 
    { Vector testvars = loopTest.allReadData(); 
      result = VectorUtil.union(result,testvars); 
      for (int i = 0; i < updatedvariables.size(); i++) 
      { String vv = "" + updatedvariables.get(i); 
        for (int j = 0; j < testvars.size(); j++) 
        { String rv = "" + testvars.get(j); 
          mp.add_pair(rv, vv); 
        } 
      } 
    } 
    return result; 
  }  
  public boolean updates(Vector v) 
  { return body.updates(v); } 
  public Statement generateDesign(java.util.Map env, boolean local)
  { Statement bdy = body.generateDesign(env,local); 
    WhileStatement result = (WhileStatement) clone(); 
    if (loopRange != null && loopRange instanceof BasicExpression)
    { if (loopRange.umlkind == Expression.CLASSID) 
      { BasicExpression lr = new BasicExpression("allInstances"); 
        lr.umlkind = Expression.FUNCTION;
        lr.setIsEvent(); 
        lr.setParameters(null);  
        lr.type = loopRange.type; 
        lr.elementType = loopRange.elementType;
        BasicExpression lrang = (BasicExpression) loopRange.clone(); 
        lrang.setObjectRef(null);  
        lr.setObjectRef(lrang); 
        result.loopRange = lr; 
      } 
    } 
    result.body = bdy; 
    return result; 
  }  
  public Statement statLC(java.util.Map env, boolean local)
  { Statement bdy = body.statLC(env,local); 
    WhileStatement result = (WhileStatement) clone(); 
    result.body = bdy; 
    return result; 
  }  
  public String updateForm(java.util.Map env, 
                           boolean local, Vector types, 
                           Vector entities, Vector vars)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryForm(new java.util.HashMap(), local);  
        String lr; 
        if (loopRange instanceof BasicExpression &&  loopRange.umlkind == Expression.CLASSID)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryForm(env, local); 
        } 
        else 
        { lr = loopRange.queryForm(env, local); } 
        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for loop range: " + loopRange);
          JOptionPane.showMessageDialog(null, "ERROR: No element type for: " + loopRange,
                                        "Type error", JOptionPane.ERROR_MESSAGE); 
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava(); }
        }  
        else
        { etr = et.getJava(); }
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTerms(body, preterms, env1, local, types, entities, vars); 
        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 
        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 
        return "  List " + rang + " = new Vector();\n" + 
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = " + extract + ";\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      else if (loopTest != null && 
               (loopTest instanceof BinaryExpression))
      { 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryForm(env, local); 
        String lr; 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryForm(env, local); 
        } 
        else 
        { lr = lt.right.queryForm(env, local); } 
        Type et = lt.right.getElementType();
        if (et == null) 
        { et = new Type("OclAny", null); }  
        String etr = et.getJava(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTerms(body, preterms, env1, local, types, entities, vars); 
        return "  List " + rang + " = new Vector();\n" + 
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryForm(env,local) + ") \n" + 
             "  { " + body.updateForm(env,local,types,entities,vars) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
          body.updateForm(env,local,types,entities,vars) + 
          "\n  } while (!(" + loopTest.queryForm(env,local) + "));\n"; 
    } 
    else
    { return "  while (" + loopTest.queryForm(env,local) + ") \n" + 
        "  { " + body.updateForm(env,local,types,entities,vars) + 
        " }"; 
    } 
 }  
  public String updateFormJava6(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormJava6(new java.util.HashMap(), local);  
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormJava6(env, local); 
        } 
        else 
        { lr = loopRange.queryFormJava6(env, local); } 
        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava6(); }
          else 
          { etr = "Object"; }  
        }  
        else
        { etr = et.getJava6(); }
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava6(body, preterms, env1, local); 
        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 
        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 
        return "  ArrayList " + rang + " = new ArrayList();\n" +
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = " + extract + ";\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormJava6(env, local); 
        String lr; 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormJava6(env, local); 
        } 
        else 
        { lr = lt.right.queryFormJava6(env, local); } 
        Type et = lt.right.getElementType();   
        if (et == null) 
        { System.err.println("!! Warning!: no element type for loop iteration " + this); 
          et = new Type("OclAny", null); 
        } 
        String etr = et.typeWrapperJava6();  
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava6(body, preterms, env1, local); 
        return "  ArrayList " + rang + " = new ArrayList();\n" +
               "  " + rang + ".addAll(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryFormJava6(env,local) + ") \n" + 
             "  { " + body.updateFormJava6(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormJava6(env,local) + 
        "\n  } while (!(" + loopTest.queryFormJava6(env,local) + "));\n"; 
    } 
    else 
    { return "  while (" + loopTest.queryFormJava6(env,local) + ") \n" + 
             "  { " + body.updateFormJava6(env,local) + " }"; 
    } 
 }  
  public String updateFormJava7(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { 
      if (loopVar != null && loopRange != null)
      { String lv = 
          loopVar.queryFormJava7(new java.util.HashMap(), local);  
        if (env.values().contains(loopVar))
        { lv = loopVar + ""; } 
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormJava7(env, local); 
        } 
        else 
        { lr = loopRange.queryFormJava7(env, local); } 
        Type et = loopRange.getElementType(); 
        String etr = "Object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getJava7(loopVar.getElementType()); }
        }  
        else
        { etr = et.getJava7(et.getElementType()); }
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava7(body, preterms, env1, local); 
        String extract = "(" + etr + ") " + rang + ".get(" + ind + ")"; 
        if ("int".equals(etr))
        { extract = "((Integer) " + rang + ".get(" + ind + ")).intValue()"; } 
        else if ("double".equals(etr))
        { extract = "((Double) " + rang + ".get(" + ind + ")).doubleValue()"; } 
        else if ("long".equals(etr))
        { extract = "((Long) " + rang + ".get(" + ind + ")).longValue()"; } 
        else if ("boolean".equals(etr))
        { extract = "((Boolean) " + rang + ".get(" + ind + ")).booleanValue()"; } 
        String wrappedElemType = Type.typeWrapperJava(et); 
        String res = 
          "  ArrayList<" + wrappedElemType + "> " + rang + " = new ArrayList<" + wrappedElemType + ">();\n" +
          "    " + rang + ".addAll(" + lr + ");\n" + 
          "    for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n"; 
        if (env.values().contains(loopVar))
        { res = res +  
               "    { " + lv + " = " + extract + ";\n"; 
        } 
        else 
        { res = res + 
               "    { " + etr + " " + lv + " = " + extract + ";\n"; 
        } 
        return res + "    " + newbody + "\n" + 
               "  }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormJava7(env, local); 
        String lr; 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormJava7(env, local); 
        } 
        else 
        { lr = lt.right.queryFormJava7(env, local); } 
        Type et = lt.right.getElementType(); 
        if (et == null) 
        { System.err.println("! Warning!: no element type for loop iteration " + this); 
          et = new Type("OclAny", null); 
        } 
        String etr = et.typeWrapperJava7();  
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsJava7(body, preterms, env1, local); 
        return "  ArrayList<" + etr + "> " + rang + " = new ArrayList<" + etr + ">();\n" +
               "    " + rang + ".addAll(" + lr + ");\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + ".size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (" + etr + ") " + rang + ".get(" + ind + ");\n" +
               "    " + newbody + "\n" + 
               "  }"; 
      } 
      return "  for (" + loopTest.queryFormJava7(env,local) + ") \n" + 
             "  { " + body.updateFormJava7(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormJava7(env,local) + 
        "\n  } while (!(" + loopTest.queryFormJava7(env,local) + "));\n"; 
    } 
    else 
    { return "  while (" + loopTest.queryFormJava7(env,local) + ") \n" + 
             "  { " + body.updateFormJava7(env,local) + " }"; 
    } 
 }  
  public String updateFormCSharp(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormCSharp(new java.util.HashMap(), local);  
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormCSharp(env, local); 
        } 
        else 
        { lr = loopRange.queryFormCSharp(env, local); } 
        Type et = loopRange.getElementType(); 
        String etr = "object"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getCSharp(); }
        }  
        else
        { etr = et.getCSharp(); }
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCSharp(body, preterms, env1, local); 
        return "  ArrayList " + rang + 
                 " = SystemTypes.asSequence(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".Count; " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + "[" + ind + "];\n" +
               "    " + newbody + "\n  }"; 
      } 
      else if (loopTest != null && 
               (loopTest instanceof BinaryExpression))
      { 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormCSharp(env, local); 
        String lr; 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormCSharp(env, local); 
        } 
        else 
        { lr = lt.right.queryFormCSharp(env, local); } 
        Type et = lt.right.getElementType();
        if (et == null) 
        { et = new Type("OclAny", null); } 
        String etr = et.getCSharp(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCSharp(body, preterms, env1, local); 
        return "  ArrayList " + rang + " = SystemTypes.asSequence(" + lr + ");\n" + 
               "  for (int " + ind + " = 0; " + ind + " < " + rang + ".Count; " + ind + "++)\n" + 
               "  { " + etr + " " + lv + " = (" + etr + ") " + rang + "[" + ind + "];\n" +
               "    " + newbody + "\n  }"; 
      } 
      return "  for (" + loopTest.queryFormCSharp(env,local) + ") \n" + 
             "  { " + body.updateFormCSharp(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormCSharp(env,local) + 
        "\n  } while (!(" + loopTest.queryFormCSharp(env,local) + "));\n"; 
    } 
    else 
    { return "  while (" + loopTest.queryFormCSharp(env,local) + ") \n" + 
             "  { " + body.updateFormCSharp(env,local) + " }"; 
    } 
 }  
  public String updateFormCPP(java.util.Map env, boolean local)
  { if (loopKind == FOR)
    { if (loopVar != null && loopRange != null)
      { String lv = loopVar.queryFormCPP(new java.util.HashMap(), local);  
        String lr; 
        if (loopRange instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) loopRange; 
          lr = lran.classqueryFormCPP(env, local); 
        } 
        else 
        { lr = loopRange.queryFormCPP(env, local); } 
        Type et = loopRange.getElementType(); 
        String etr = "void*"; 
        if (et == null) 
        { System.err.println("!! Error: null element type for " + loopRange);
          if (loopVar.getType() != null)
          { etr = loopVar.getType().getCPP(); }
        }  
        else
        { etr = et.getCPP(); }
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range"); 
        String rangesource = Identifier.nextIdentifier("_range_source"); 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCPP(body, preterms, env1, local); 
        return "    vector<" + etr + ">* " + rangesource + " = " + lr + ";\n" + 
               "    vector<" + etr + ">* " + rang + " = new vector<" + etr + ">();\n" + 
               "    " + rang + "->insert(" + rang + "->end(), " + rangesource + "->begin(), " + 
                                       rangesource + "->end());\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + "->size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (*" + rang + ")[" + ind + "];\n" +
               "      " + newbody + "\n" + 
               "    }"; 
      } 
      else if (loopTest != null && (loopTest instanceof BinaryExpression))
      { 
        BinaryExpression lt = (BinaryExpression) loopTest; 
        String lv = lt.left.queryFormCPP(env, local); 
        String lr; 
        java.util.Map env1 = (java.util.HashMap) ((java.util.HashMap) env).clone();
        if (lt.right instanceof BasicExpression)
        { BasicExpression lran = (BasicExpression) lt.right; 
          lr = lran.classqueryFormCPP(env, local); 
        } 
        else 
        { lr = lt.right.queryFormCPP(env, local); } 
        Type et = lt.right.getElementType(); 
        if (et == null) 
        { et = new Type("OclAny", null); } 
        String etr = et.getCPP(); 
        String ind = Identifier.nextIdentifier("_i");
        String rang = Identifier.nextIdentifier("_range");
        String rangesource = Identifier.nextIdentifier("_range_source"); 
        env1.put(etr,lv); 
        Vector preterms = body.allPreTerms(lv); 
        String newbody = processPreTermsCPP(body, preterms, env1, local); 
        return "    vector<" + etr + ">* " + rangesource + " = " + lr + ";\n" + 
               "    vector<" + etr + ">* " + rang + " = new vector<" + etr + ">();\n" + 
               "    " + rang + "->insert(" + rang + "->end(), " + rangesource + "->begin(), " + 
                                       rangesource + "->end());\n" + 
               "    for (int " + ind + " = 0; " + ind + " < " + rang + "->size(); " + ind + "++)\n" + 
               "    { " + etr + " " + lv + " = (*" + rang + ")[" + ind + "];\n" +
               "      " + newbody + "\n" + 
               "    }"; 
      } 
      return "  for (" + loopTest.queryFormCPP(env,local) + ") \n" + 
             "  { " + body.updateFormCPP(env,local) + " }"; 
    } 
    else if (loopKind == REPEAT)
    { return "  do {\n  " +  
        body.updateFormCPP(env,local) + 
        "\n  } while (!(" + loopTest.queryFormCPP(env,local) + "));\n"; 
    } 
    else 
    { return "  while (" + loopTest.queryFormCPP(env,local) + ") \n" + 
             "  { " + body.updateFormCPP(env,local) + " }"; 
    } 
  }   
  public Vector allPreTerms()
  { Vector res = body.allPreTerms();
    Vector res1 = new Vector(); 
    if (loopVar != null) 
    { res1 = body.allPreTerms(loopVar + ""); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allPreTerms();
    res.addAll(res2); 
    res.removeAll(res1); 
    return res;  
  }  
  public Vector allPreTerms(String var)
  { Vector res = body.allPreTerms(var);
    if (loopTest == null) 
    { return res; } 
    Vector res1 = loopTest.allPreTerms(var);
    res.addAll(res1); 
    return res;  
  }  
  public Vector readFrame()
  { Vector res = body.readFrame();
    if (loopRange != null) 
    { res.addAll(loopRange.allReadFrame()); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allReadFrame();
    res.addAll(res2);  
    if (loopVar != null) 
    { Vector res1 = new Vector(); 
      res1.add(loopVar); 
      res.removeAll(res1); 
    } 
    return res;  
  }  
  public Vector writeFrame() 
  { Vector res = body.writeFrame();
    if (loopVar != null) 
    { Vector res1 = new Vector(); 
      if (res.contains("" + loopVar))
      { System.err.println("!! Error: iteration variable " + 
           loopVar + " cannot be written in loop body " + body); 
      } 
    }
    return res; 
  } 
  public int syntacticComplexity()
  { int res = body.syntacticComplexity(); 
    if (loopKind == FOR) 
    { if (loopRange != null) 
      { int rcomp = loopRange.syntacticComplexity();
        return res + rcomp + 1; 
      } 
    } 
    if (loopTest == null) 
    { return res + 1; }
    int syncomp = loopTest.syntacticComplexity(); 
    return syncomp + res + 1; 
  } 
  public int cyclomaticComplexity()
  { int res = body.cyclomaticComplexity(); 
    if (loopKind == FOR && loopRange != null) 
    { return res; } 
    if (loopTest == null) 
    { return res; } 
    return loopTest.cyclomaticComplexity() + res; 
  } 
  public int epl()
  { return body.epl(); }  
  public Vector allOperationsUsedIn()
  { Vector res = body.allOperationsUsedIn();
    if (loopRange != null) 
    { res.addAll(loopRange.allOperationsUsedIn()); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allOperationsUsedIn();
    res.addAll(res2);  
    return res;  
  }  
  public Vector allAttributesUsedIn()
  { Vector res = body.allAttributesUsedIn();
    if (loopRange != null) 
    { res.addAll(loopRange.allAttributesUsedIn()); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.allAttributesUsedIn();
    res.addAll(res2);  
    return res;  
  }  
  public Vector getUses(String var)
  { Vector res = body.getUses(var);
    if (loopRange != null) 
    { res.addAll(loopRange.getUses(var)); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.getUses(var);
    res.addAll(res2);  
    return res;  
  }  
  public Vector getVariableUses()
  { Vector res = body.getVariableUses();
    String lv = ""; 
    if (loopVar != null) 
    { lv = loopVar + ""; 
      Expression expr = 
        ModelElement.lookupExpressionByName(lv, res); 
      if (expr == null) 
      { System.err.println("! Warning: no use of loop variable " +
                 loopVar + " in loop body: " + body); 
      } 
      res = ModelElement.removeExpressionByName(lv,res); 
    } 
    if (loopRange != null) 
    { Vector lrvars = loopRange.getVariableUses(); 
      res.addAll(lrvars); 
      Expression rexpr = 
        ModelElement.lookupExpressionByName(lv, lrvars); 
      if (loopVar != null && rexpr != null) 
      { System.err.println("!! Error: loop variable " +
               loopVar + " used in loop range: " + loopRange); 
      } 
    } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.getVariableUses();
    res.addAll(res2);  
    return res;  
  }  
  public Vector getVariableUses(Vector unused)
  { Vector res = body.getVariableUses(unused);
    String lv = ""; 
    if (loopVar != null) 
    { lv = loopVar + ""; 
      Expression expr = 
        ModelElement.lookupExpressionByName(lv, res); 
      if (expr == null) 
      { System.err.println("! Warning: no use of loop variable " +
                 loopVar + " in loop body: " + body); 
      } 
      res = ModelElement.removeExpressionByName(lv,res); 
    } 
    if (loopRange != null) 
    { Vector lrvars = loopRange.getVariableUses(); 
      res.addAll(lrvars); 
      Expression rexpr = 
        ModelElement.lookupExpressionByName(lv, lrvars); 
      if (loopVar != null && rexpr != null) 
      { System.err.println("!! Semantic error: loop variable " +
               loopVar + " used in loop range: " + loopRange); 
      } 
    } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.getVariableUses();
    res.addAll(res2);  
    return res;  
  }  
  public Vector equivalentsUsedIn()
  { Vector res = body.equivalentsUsedIn();
    if (loopRange != null) 
    { res.addAll(loopRange.equivalentsUsedIn()); } 
    if (loopTest == null) 
    { return res; } 
    Vector res2 = loopTest.equivalentsUsedIn();
    res.addAll(res2);  
    return res;  
  }  
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector eargs = new Vector(); 
    Vector args = new Vector();
    String bodyText = ""; 
    if (body != null) 
    { bodyText = body.cg(cgs); } 
    else 
    { bodyText = (new SequenceStatement()).cg(cgs); } 
    Expression ltest = loopTest; 
    if (loopTest == null) 
    { ltest = new BasicExpression(true); } 
    if (loopKind == WHILE) 
    { args.add(ltest.cg(cgs));  
      args.add(bodyText);
      eargs.add(ltest); 
      eargs.add(body); 
    } 
    else if (loopKind == REPEAT) 
    { args.add(bodyText);
      args.add(ltest.cg(cgs));  
      eargs.add(body); 
      eargs.add(ltest); 
    } 
    else 
    { args.add(loopVar + ""); 
      if (loopRange != null) 
      { args.add(loopRange.cg(cgs)); } 
      else 
      { args.add(""); }  
      args.add(bodyText); 
      eargs.add(loopVar); 
      eargs.add(loopRange); 
      eargs.add(body); 
    } 
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext;
  } 
  public Vector allVariableNames()
  { Vector res = new Vector(); 
    if (loopKind == WHILE && loopTest != null) 
    { res.addAll(loopTest.allVariableNames()); } 
    else if (loopKind == FOR && 
             loopVar != null && loopRange != null) 
    { res.addAll(loopVar.allVariableNames()); 
      res.addAll(loopRange.allVariableNames()); 
    }  
    res = VectorUtil.union(res,body.allVariableNames()); 
    if (loopKind == REPEAT && loopTest != null) 
    { res.addAll(loopTest.allVariableNames()); } 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    if (loopKind == WHILE && loopTest != null) 
    { res.addAll(loopTest.metavariables()); } 
    else if (loopKind == FOR && 
             loopVar != null && loopRange != null) 
    { res.addAll(loopVar.metavariables()); 
      res.addAll(loopRange.metavariables()); 
    }  
    res = VectorUtil.union(res,body.metavariables()); 
    if (loopKind == REPEAT && loopTest != null) 
    { res.addAll(loopTest.metavariables()); } 
    return res; 
  } 
  public Vector cgparameters()
  { Vector eargs = new Vector(); 
    if (loopKind == WHILE) 
    { eargs.add(loopTest); 
      eargs.add(body); 
    } 
    else if (loopKind == REPEAT) 
    { eargs.add(body); 
      eargs.add(loopTest); 
    } 
    else 
    { eargs.add(loopVar); 
      eargs.add(loopRange); 
      eargs.add(body); 
    } 
    return eargs;
  } 
} 
