class InvocationStatement extends Statement
{ String action; 
  String target; 
  String assignsTo = "";
  String assignsType = ""; 
  private Vector parameters = new Vector();  
  Expression callExp = new BasicExpression("skip"); 
  public InvocationStatement(Event ee)
  { 
    action = ee.label; 
    assignsTo = null; 
    target = null; 
  }
  public String getOperator() 
  { return "call"; } 
  InvocationStatement(String act, String targ, String assigns)
  { action = act; 
    target = targ; 
    assignsTo = assigns; 
  } 
  InvocationStatement(BehaviouralFeature bf)
  { action = bf.getName(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    parameters.addAll(bf.getParameters()); 
    BasicExpression calle = 
         new BasicExpression(bf + "", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    if (bf.isQuery())
    { callee.setUmlKind(Expression.QUERY); } 
    else 
    { callee.setUmlKind(Expression.UPDATEOP); } 
    callee.setType(bf.getResultType());
    callee.setElementType(bf.getElementType());
    callee.setEntity(bf.getEntity());
    callExp = callee; 
  } 
  InvocationStatement(String obj, BehaviouralFeature bf)
  { action = bf.getName(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    parameters.addAll(bf.getParameters()); 
    BasicExpression calle = 
         new BasicExpression(obj + "." + bf + "", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    if (bf.isQuery())
    { callee.setUmlKind(Expression.QUERY); } 
    else 
    { callee.setUmlKind(Expression.UPDATEOP); } 
    callee.setType(bf.getResultType());
    callee.setElementType(bf.getElementType());
    callee.setEntity(bf.getEntity());
    callExp = callee; 
  } 
  InvocationStatement(String obj, String bfname)
  { action = bfname; 
    target = null; 
    assignsTo = null; 
    parameters = new Vector(); 
    BasicExpression calle = 
         new BasicExpression(obj + "." + bfname + "()", 0);
    Expression callee = calle.checkIfSetExpression();
    if (callee == null) { return; }
    callee.setUmlKind(Expression.UPDATEOP); 
    callExp = callee; 
  } 
  InvocationStatement(BasicExpression be)
  { action = be.getData(); 
    target = null; 
    assignsTo = null; 
    parameters = new Vector();
    if (be.getParameters() != null)  
    { parameters.addAll(be.getParameters()); }  
    callExp = be; 
  } 
  InvocationStatement(Expression be)
  { action = be + ""; 
    target = null; 
    assignsTo = null; 
    parameters = new Vector();
    if (be instanceof BasicExpression) 
    { BasicExpression bexpr = (BasicExpression) be; 
      if (bexpr.getParameters() != null)  
      { parameters.addAll(bexpr.getParameters()); } 
    }  
    callExp = be; 
  } 
  InvocationStatement(String act)
  { action = act; 
    target = null; 
    assignsTo = null; 
    callExp = new BasicExpression(act); 
  } 
  public static InvocationStatement newInvocationStatement(
                                       Expression expr, 
                                       Vector pars) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.parameters.addAll(pars);   
    res.callExp = expr;
    return res;  
  } 
  public static InvocationStatement newInvocationStatement(
                                       Expression expr, 
                                       Expression par) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.parameters.add(par);   
    res.callExp = expr;
    return res;  
  } 
  public static InvocationStatement newInvocationStatement(
                                       Expression expr) 
  { InvocationStatement res = 
        new InvocationStatement(expr + ""); 
    res.target = null; 
    res.assignsTo = null; 
    res.parameters = new Vector();
    res.callExp = expr;
    return res;  
  } 
  public int execute(ModelSpecification sigma, 
                      ModelState beta)
  { int res = Statement.NORMAL; 
    if (callExp == null) 
    { return res; } 
    if ("skip".equals(callExp + "")) 
    { return res; } 
    if (callExp instanceof BasicExpression)
    { BasicExpression cexpr = (BasicExpression) callExp; 
      Expression obj = cexpr.getObjectRef(); 
      String op = cexpr.getData(); 
      Vector actualPars = cexpr.getParameters(); 
      int npars = actualPars.size(); 
      Expression selfobject; 
      if (obj != null) 
      { selfobject = obj.evaluate(sigma, beta); } 
      else 
      { selfobject = beta.getVariableValue("self"); } 
      if (selfobject == null) 
      { return res; } 
      ObjectSpecification ospec = 
                 sigma.getObjectSpec("" + selfobject);
      if (ospec == null) 
      { return res; }
      Entity ent = ospec.getEntity(); 
      if (ent == null) 
      { return res; } 
      BehaviouralFeature bf = ent.getOperation(op, npars);
      if (bf == null) 
      { return res; } 
      ModelState opstackframe = (ModelState) beta.clone(); 
      opstackframe.addNewEnvironment(); 
      opstackframe.addVariable("self", selfobject); 
      Vector parValues = new Vector(); 
      for (int i = 0; i < actualPars.size(); i++) 
      { Expression pval = (Expression) actualPars.get(i); 
        Expression parval = pval.evaluate(sigma, beta); 
        parValues.add(parval); 
      } 
      bf.execute(sigma, opstackframe, parValues);  
    }
    return res; 
  } 
  public Statement removeSlicedParameters(
             BehaviouralFeature op, Vector fpars)
  { 
    if (callExp == null) { return this; }
    Vector oldpars = new Vector(); 
    if (parameters == null || parameters.size() == 0) 
    { Vector callpars = callExp.getParameters(); 
      if (callpars != null)
      { oldpars.addAll(callpars); }
    }  
    else 
    { oldpars.addAll(parameters); } 
    if (action.equals(op.getName()) || 
        action.startsWith(op.getName() + "(") || 
        action.startsWith("self." + op.getName() + "("))
    { Vector newpars = new Vector(); 
      Vector oppars = op.getParameters(); 
      for (int i = 0; i < oppars.size(); i++) 
      { Attribute att = (Attribute) oppars.get(i); 
        if (fpars.contains(att.getName()))
        { System.out.println("++ Removing parameter " + att); } 
        else 
        { newpars.add(oldpars.get(i)); } 
      } 
      InvocationStatement res =
        new InvocationStatement(action); 
      res.callExp = 
        BasicExpression.newCallBasicExpression(
                    "self." + op.getName(),newpars); 
      return res;
    } 
    else 
    { return this; }  
  } 
  public boolean isSkip()
  { if ("skip".equals(action)) 
    { return true; } 
    if ("skip".equals(callExp + "")) 
    { return true; } 
    return false; 
  } 
  public Expression getCallExp()
  { return callExp; } 
  public void setCallExp(Expression e)
  { callExp = e; } 
  public String calledOperation()
  { return action; } 
  public void setAssignsTo(String atype, String avar)
  { assignsType = atype; 
    assignsTo = avar; 
  } 
  public void setEntity(Entity ent)
  { entity = ent; 
    if (callExp != null) 
    { callExp.setEntity(ent); }  
  } 
  public void setParameters(Vector pars)
  { parameters = pars; } 
  public Object clone()
  { InvocationStatement res = 
       new InvocationStatement(action,target,assignsTo);
    res.setCallExp(callExp); 
    res.setAssignsTo(assignsType,assignsTo); 
    res.entity = entity; 
    return res; 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  {  
  }
  public void findClones(java.util.Map clones, 
                       java.util.Map cloneDefs,
                       String rule, String op)
  { } 
  public Vector allVariableNames()
  { return callExp.allVariableNames(); } 
  public Statement optimiseOCL()
  { Expression cexp = callExp.simplifyOCL(); 
    return new InvocationStatement(cexp); 
  }  
  public Map energyUse(Map uses, 
                                Vector rUses, Vector oUses)
  { callExp.energyUse(uses, rUses, oUses);  
    int syncomp = callExp.syntacticComplexity(); 
    if (syncomp > TestParameters.syntacticComplexityLimit)
    { System.err.println("!!! Code smell (MEL): too high expression complexity (" + syncomp + ") for " + callExp); 
      System.err.println(">>> Recommend OCL refactoring"); 
    } 
    return uses; 
  }  
  public java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars)
  { callExp.collectionOperatorUses(nestingLevel, 
                                   operatorsAtLevel, vars); 
    return operatorsAtLevel; 
  }  
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { String val = callExp + ""; 
    callExp.findMagicNumbers(mgns, val, op); 
  }
  public Statement dereference(BasicExpression var)
  { InvocationStatement res = new InvocationStatement(action,target,assignsTo); 
    if (callExp != null) 
    { res.setCallExp(callExp.dereference(var)); }
    res.entity = entity; 
    return res; 
  }  
  public Statement substituteEq(String oldE, Expression newE)
  { String act = action; 
    String targ = target; 
    String ast = assignsTo; 
    if (target != null && target.equals(oldE))
    { targ = newE.toString(); } 
    if (assignsTo != null && assignsTo.equals(oldE))
    { ast = newE.toString(); }
    InvocationStatement res = 
        new InvocationStatement(act,targ,ast);
    res.entity = entity;
    if (parameters != null) 
    { Vector newpars = new Vector(); 
      for (int i = 0; i < parameters.size(); i++) 
      { Expression oldpar = (Expression) parameters.get(i); 
        Expression newpar = oldpar.substituteEq(oldE,newE);
        newpars.add(newpar); 
      } 
      res.setParameters(newpars); 
    } 
    if (callExp != null)
    { Expression newce = callExp.substituteEq(oldE,newE); 
      res.setCallExp(newce);
    }
    return res; 
  }  
  public Statement addContainerReference(
                                  BasicExpression ref,
                                  String var, Vector excludes)
  {  
    String act = action; 
    String targ = target; 
    String ast = assignsTo; 
    if (target != null && excludes.contains(target))
    { } 
    else 
    { targ = ref + "." + target; } 
    if (assignsTo != null && excludes.contains(assignsTo))
    { } 
    else 
    { ast = ref + "." + assignsTo; }
    InvocationStatement res = 
        new InvocationStatement(act,targ,ast);
    res.entity = entity;
    if (parameters != null) 
    { Vector newpars = new Vector(); 
      for (int i = 0; i < parameters.size(); i++) 
      { Expression oldpar = (Expression) parameters.get(i); 
        Expression newpar = oldpar.addContainerReference(
                                               ref,var,
                                               excludes);
        newpars.add(newpar); 
      } 
      res.setParameters(newpars); 
    } 
    if (callExp != null)
    { Expression newce = callExp.addContainerReference(
                                         ref,var,excludes); 
      res.setCallExp(newce);
    }
    return res; 
  }  
  public String toStringB()    
  { String res = ""; 
    if (assignsTo != null) 
    { res = assignsTo + " <-- "; } 
    res = res + action; 
    if (target != null)    
    { res = res + "(" + target + ")"; } 
    return res; 
  } 
  public String toString()   
  { String res = ""; 
    res = res + callExp; 
    return res; 
  } 
  public String toAST()
  { String res = "(OclStatement call " + callExp.toAST() + " )"; 
    return res;  
  } 
  public boolean containsSubexpression(Expression expr) 
  { return callExp.containsSubexpression(expr); } 
  public Vector singleMutants()
  { Vector res = new Vector(); 
    Vector exprs = callExp.singleMutants(); 
    for (int i = 0; i < exprs.size(); i++) 
    { Expression mut = (Expression) exprs.get(i); 
      res.add(new InvocationStatement(mut));
    }  
    return res; 
  } 
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("operationcallstatement_"); 
    out.println(res + " : OperationCallStatement"); 
    out.println(res + ".statId = \"" + res + "\""); 
    if (assignsTo != null) 
    { out.println(res + ".assignsTo = " + assignsTo); } 
    if (callExp != null)
    { String callid = callExp.saveModelData(out); 
      out.println(res + ".callExp = " + callid);
    }
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { return saveModelData(out); }
  public String bupdateForm()
  { return toString(); } 
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { if (callExp != null)
    { if (callExp instanceof BasicExpression)
      { BasicExpression cex = (BasicExpression) callExp; 
        String callString = cex.data; 
        BExpression uf = cex.objectRef.binvariantForm(env,local);
        Vector pars = new Vector(); 
        pars.add(uf); 
        return new BOperationCall(callString, pars);
      }
    } 
    return new BBasicStatement("skip"); 
  }  
  public String toStringJava() 
  { String res = ""; 
    if ("skip".equals(action)) { return res; } 
    if (assignsTo != null)  
    { res = assignsTo + " = "; }
    if (target != null) 
    { res = res + target + "."; }  
    res = res + action + ";";  
    return res; 
  } 
}