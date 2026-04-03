class AssignStatement extends Statement 
{ private Type type = null;  
  private Expression lhs;
  private Expression rhs;
  private boolean copyValue = false; 
  private String operator = ":=";  
  public AssignStatement(Expression left, Expression right)
  { lhs = left;
    rhs = right; 
  }
  public AssignStatement(Attribute left, Expression right)
  { lhs = new BasicExpression(left);
    rhs = right; 
  }
  public AssignStatement(Binding b) 
  { lhs = new BasicExpression(b.getPropertyName()); 
    rhs = b.expression; 
  } 
  public AssignStatement(String left, Expression right) 
  { lhs = new BasicExpression(left); 
    rhs = right; 
  } 
  public AssignStatement(String op, Expression left, Expression right)
  { if ("=".equals(op))
    { lhs = left; 
      rhs = right; 
    } 
    else if ("+=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("+", left, right); 
    } 
    else if ("-=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("-", left, right); 
    }  
    else if ("*=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("*", left, right); 
    } 
    else if ("/=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("/", left, right); 
    } 
    else if ("|=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("or", left, right); 
    } 
    else if ("&=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("&", left, right); 
    } 
    else if ("^=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("xor", left, right); 
    } 
    else if ("%=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("mod", left, right); 
    } 
    else if ("<<=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("*", left, 
              new BinaryExpression("->pow", 
                new BasicExpression(2), right)); 
    } 
    else if (">>=".equals(op) || ">>>=".equals(op))
    { lhs = left; 
      rhs = new BinaryExpression("/", left, 
              new BinaryExpression("->pow", 
                new BasicExpression(2), right)); 
    } 
    lhs = left; 
    rhs = right;
  } 
  public String getOperator() 
  { return ":="; } 
  public Expression getLeft()
  { return lhs; } 
  public Expression getRight()
  { return rhs; } 
  public Expression getLhs()
  { return lhs; } 
  public Expression getRhs()
  { return rhs; } 
  public void setType(Type t)
  { type = t; } 
  public void setElementType(Type t)
  { lhs.elementType = t; 
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { Expression rhsValue = rhs.evaluate(sigma, beta); 
    if (lhs instanceof BasicExpression)
    { BasicExpression lbe = (BasicExpression) lhs;
      Expression obj = lbe.getObjectRef(); 
      Expression indx = lbe.getArrayIndex(); 
      String var = lbe.getData(); 
      if (obj == null && 
          indx == null)
      { 
        if (lhs.isAttribute()) 
        { Expression oid = beta.getVariableValue("self"); 
          ObjectSpecification ref = 
                sigma.getObjectSpec("" + oid); 
          if (ref != null)
          { ref.setOCLValue(var, rhsValue); }
        }   
        else 
        { beta.setVariableValue(var, rhsValue); } 
      } 
      else if (obj == null)
      { 
        Expression indv = indx.evaluate(sigma, beta); 
        Expression arr = beta.getVariableValue(var); 
        if (arr instanceof SetExpression)
        { int indval = Integer.parseInt("" + indv); 
          ((SetExpression) arr).setExpression(indval, rhsValue); 
        } 
      }  
      else if (obj != null && 
          indx == null)
      { 
        Expression oid = obj.evaluate(sigma, beta); 
        ObjectSpecification ref = sigma.getObjectSpec("" + oid); 
        if (ref != null)
        { ref.setOCLValue(var, rhsValue); }   
      } 
    } 
    System.out.println(">> Updated state: " + beta);
    return Statement.NORMAL;  
  } 
  public Expression definedness()
  { Expression ldef = lhs.definedness(); 
    Expression rdef = rhs.definedness(); 
    Expression res = Expression.simplify("&", ldef, rdef, null); 
    return res; 
  } 
  public Vector cgparameters()
  { Vector args = new Vector();
    Expression rhsnopre = rhs.removePrestate(); 
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null;
        lhs0.type = lhsbe.arrayType;  
        args.add(lhs0); 
        args.add(lhsbe.arrayIndex); 
        args.add(rhsnopre);
        return args; 
      } 
    } 
    args.add(lhs);
    args.add(rhsnopre);
    return args; 
  } 
  public String basiccg(CGSpec cgs)
  { 
    String etext = this + "";
    Vector args = new Vector();
    Vector eargs = new Vector(); 
    Expression rhsnopre = rhs.removePrestate(); 
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null; 
        args.add(lhs0.cg(cgs)); 
        eargs.add(lhs0); 
        args.add(lhsbe.arrayIndex.cg(cgs)); 
        eargs.add(lhsbe.arrayIndex); 
        args.add(rhsnopre.cg(cgs));
        eargs.add(rhsnopre);
        CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
        if (r != null)
        { return r.applyRule(args,eargs,cgs); }
        return etext; 
      }
    }
    args.add(lhs.cg(cgs));
    eargs.add(lhs); 
    args.add(rhsnopre.cg(cgs));
    eargs.add(rhsnopre);
    CGRule r = cgs.matchedStatementRule(this,etext);
    System.out.println(">> Matched statement rule: " + r + " for " + this); 
    if (r != null)
    { return r.applyRule(args,eargs,cgs); }
    return etext; 
  }
  public Vector basiccgparameters()
  { 
    Vector eargs = new Vector(); 
    Expression rhsnopre = rhs.removePrestate(); 
    if (lhs instanceof BasicExpression)
    { BasicExpression lhsbe = (BasicExpression) lhs; 
      if (lhsbe.arrayIndex != null) 
      { BasicExpression lhs0 = (BasicExpression) lhsbe.clone(); 
        lhs0.arrayIndex = null; 
        eargs.add(lhs0); 
        eargs.add(lhsbe.arrayIndex); 
        eargs.add(rhsnopre);
        return eargs; 
      }
    }
    eargs.add(lhs); 
    eargs.add(rhsnopre);
    return eargs; 
  }
  public String cg(CGSpec cgs)
  { if (type == null) 
    { return basiccg(cgs); } 
    else 
    { 
      SequenceStatement stat = new SequenceStatement(); 
      CreationStatement cre = new CreationStatement(type + "", lhs + "");
      cre.setType(type); 
      cre.setElementType(lhs.elementType);  
      AssignStatement newas = new AssignStatement(lhs,rhs);
      newas.type = null;  
      stat.addStatement(cre); 
      stat.addStatement(newas); 
      return stat.cg(cgs); 
    } 
  }
  public void setCopyValue(boolean b)
  { copyValue = b; } 
  public void setOperator(String op)
  { operator = op; } 
  public Object clone()
  { Expression newlhs = (Expression) lhs.clone(); 
    Expression newrhs = (Expression) rhs.clone(); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public Statement optimiseOCL()
  { Expression newlhs = lhs.simplifyOCL(); 
    Expression newrhs = rhs.simplifyOCL(); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { if (rhs.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    rhs.findClones(clones,rule,op); 
  }
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs,
                         String rule, String op)
  { if (rhs.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
    { return; }
    rhs.findClones(clones,cdefs,rule,op); 
  }
  public Vector allVariableNames()
  { Vector res = lhs.allVariableNames();
    if (rhs != null)  
    { res = VectorUtil.union(res, rhs.allVariableNames()); } 
    return res; 
  } 
  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { lhs.energyUse(uses, rUses, oUses); 
    rhs.energyUse(uses, rUses, oUses);
    int syncomp = rhs.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { int acount = (int) uses.get("amber"); 
      uses.set("amber", acount + 1); 
      oUses.add("! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + rhs + "\n" + 
                ">>> Recommend OCL refactoring");  
    } 
    return uses; 
  }  
  public java.util.Map collectionOperatorUses(int lev, 
                          java.util.Map uses, 
                          Vector vars)
  { rhs.collectionOperatorUses(lev, uses, vars); 
    lhs.collectionOperatorUses(lev, uses, vars); 
    return uses; 
  } 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { lhs.findMagicNumbers(mgns, "" + this, op);
    rhs.findMagicNumbers(mgns, "" + this, op); 
  } 
  public Statement dereference(BasicExpression var)
  { Expression newlhs = (Expression) lhs.dereference(var); 
    Expression newrhs = (Expression) rhs.dereference(var); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public Statement addContainerReference(BasicExpression ref,
                                         String var,
                                         Vector excl)
  { Expression newlhs = 
        lhs.addContainerReference(ref,var,excl); 
    Expression newrhs = 
        rhs.addContainerReference(ref,var,excl); 
    AssignStatement res = new AssignStatement(newlhs,newrhs); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public Statement substituteEq(String oldE, Expression newE)
  { Expression lhs2 = 
         lhs.substituteEq(oldE,newE); 
    Expression rhs2 = rhs.substituteEq(oldE,newE); 
    AssignStatement res = new AssignStatement(lhs2,rhs2); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { Expression lhs2 = (Expression) lhs.clone(); 
    Expression rhs2 = rhs.removeSlicedParameters(bf,fpars); 
    AssignStatement res = new AssignStatement(lhs2,rhs2); 
    res.setType(type); 
    res.setCopyValue(copyValue); 
    return res; 
  } 
  public String toString() 
  { if (type == null) 
    { return lhs + " " + operator + " " + rhs + " "; }
    else 
    { return lhs + " : " + type + " := " + rhs + " "; } 
  }  
  public String toAST() 
  { String res = "(OclStatement " + lhs.toAST() + " := " + rhs.toAST() + " )";
    return res;  
  }  
  public boolean containsSubexpression(Expression expr)
  { return rhs.containsSubexpression(expr); } 
  public Vector singleMutants()
  { Vector exprs = rhs.singleMutants(); 
    Vector res = new Vector(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      res.add(new AssignStatement(lhs,mut)); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("assignstatement_"); 
    out.println(res + " : AssignStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    String lhsid = lhs.saveModelData(out); 
    String rhsid = rhs.saveModelData(out); 
    out.println(res + ".left = " + lhsid); 
    out.println(res + ".right = " + rhsid); 
    if (type != null) 
    { String typeid = type.getUMLModelName(out); 
      out.println(typeid + " : " + res + ".type"); 
    }
    return res; 
  }  
  public String saveModelData(PrintWriter out, Entity ent) 
  { java.util.Map env = new java.util.HashMap(); 
    Vector cons = ent.getInvariants(); 
    String ename = ent.getName(); 
    SequenceStatement ss = new SequenceStatement(); 
    ss.addStatement(this); 
    System.out.println(">>> " + ename + " invariants are: " + cons); 
    for (int i = 0; i < cons.size(); i++) 
    { Constraint cc = (Constraint) cons.get(i); 
      if (cc.dependsUpon(ename,lhs+""))
      { System.out.println(">>> Invariant " + cc + " affected by update to " + lhs); 
        Statement act = cc.generateDesign(env,true); 
        if (act != null) 
        { ss.addStatement(act); 
          System.out.println(">>> Additional action " + act + " for " + this + " due to invariant " + cc); 
        } 
      } 
    } 
    if (ss.size() == 1)  
    { return saveModelData(out); }
    else 
    { return ss.saveModelData(out); } 
  } 
  public void display()
  { if (type == null) 
    { System.out.println(lhs + " := " + rhs + " "); }
    else 
    { System.out.println(lhs + " : " + type + " := " + rhs + " "); } 
  } 
  public String bupdateForm()
  { return lhs + " := " + rhs; }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BExpression brhs = rhs.binvariantForm(env,local); 
    BStatement stat = ((BasicExpression) lhs).bEqupdateForm(env,brhs,local); 
    return stat; 
  } 
}