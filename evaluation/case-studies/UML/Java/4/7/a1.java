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
} 
