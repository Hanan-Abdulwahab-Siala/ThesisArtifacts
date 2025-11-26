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
  public void displayImp(String var) 
  { System.out.print(lhs + "_STO_VAR(" + rhs + ")"); }
  public void displayImp(String var, PrintWriter out)
  { out.print(lhs + "_STO_VAR(" + rhs + ")"); }
  public void display(PrintWriter out)
  { out.print(lhs + " := " + rhs); }
  public void displayJava(String target)
  { if (type != null) 
    { System.out.print("  " + type.getJava() + " "); } 
    System.out.print(lhs + " = " + rhs + ";  " + 
                     "System.out.println(\"" + lhs + " set to " + rhs + "\");");
  }
  public void displayJava(String target, PrintWriter out)
  { if (type != null) 
    { out.print("  " + type.getJava() + " "); } 
    out.print(lhs + " = " + rhs + ";  " + 
              "System.out.println(\"" + lhs + " set to " + 
              rhs + "\");"); 
  }
  public String toStringJava()
  { java.util.Map env = new java.util.HashMap(); 
    if (entity != null) 
    { env.put(entity.getName(),"this"); } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateForm(env,true);  
    if (type != null) 
    { res = "  " + type.getJava() + " " + res; } 
    return res; 
  }
  public String toEtl()
  { String res = lhs + " = " + rhs + ";";  
    return res; 
  }
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { 
    boolean res = lhs.typeCheck(types,entities,cs,env); 
    res = rhs.typeCheck(types,entities,cs,env);
    if (lhs.type == null && rhs.type != null) 
    { lhs.type = rhs.type; } 
    if (rhs.elementType != null && lhs.elementType == null) 
    { lhs.elementType = rhs.elementType; } 
    else if (lhs.elementType != null && rhs.elementType == null) 
    { rhs.elementType = lhs.elementType; } 
    if (BasicExpression.hasVariable(lhs))
    { BasicExpression.updateVariableType(lhs,rhs); } 
    else if (BasicExpression.isMapAccess(lhs))
    { 
      BasicExpression.updateMapType(lhs,rhs); 
    } 
    if (type != null)  
    { Attribute att = new Attribute(lhs + "", rhs.type, ModelElement.INTERNAL); 
      att.setElementType(lhs.elementType);
      System.out.println(">>> " + lhs + " has type " + att.getType());  
      env.add(att); 
    } 
    return res; 
  }
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { 
    boolean res = rhs.typeInference(types,entities,cs,env,vartypes);
    Type rhsType = rhs.getType();  
    vartypes.put(lhs + "", rhsType); 
    if (Type.isVacuousType(lhs.type) && 
        !Type.isVacuousType(rhsType)) 
    { lhs.type = rhsType; } 
    if (rhs.elementType != null && lhs.elementType == null) 
    { lhs.elementType = rhs.elementType; } 
    else if (lhs.elementType != null && rhs.elementType == null) 
    { rhs.elementType = lhs.elementType; } 
    if (BasicExpression.hasVariable(lhs))
    { BasicExpression.updateVariableType(lhs,rhs); } 
    else if (BasicExpression.isMapAccess(lhs))
    { 
      BasicExpression.updateMapType(lhs,rhs); 
    } 
    if (type != null)  
    { Attribute att = new Attribute(lhs + "", rhs.type, ModelElement.INTERNAL); 
      att.setElementType(lhs.elementType);
      System.out.println(">>> local variable " + lhs + " has type " + att.getType());  
      env.add(att); 
    } 
    Type declaredType = (Type) vartypes.get(lhs + ""); 
    if (!Type.isVacuousType(declaredType) && 
        Type.isVacuousType(lhs.type))
    { lhs.type = declaredType;
      System.out.println(">>> " + lhs + " actual type is " + declaredType);  
    } 
    return res; 
  }
  public Expression wpc(Expression post)
  { return post.substituteEq(lhs.toString(),rhs); }
  public Expression wpc(Expression inv, Expression post)
  { return inv.substituteEq(lhs.toString(),rhs); }
  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    String updatedVar = lhs.updatedData(); 
    if (updatedVar != null && vars.contains(updatedVar))
    { 
      vbls.remove(updatedVar); 
      Vector es = rhs.allAttributesUsedIn(); 
      Vector vs = rhs.getVariableUses(); 
      es.addAll(vs); 
      for (int i = 0; i < es.size(); i++) 
      { String var = "" + es.get(i); 
        if (vbls.contains(var)) { } 
        else 
        { vbls.add(var); } 
      } 
    } 
    return vbls; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    String updatedVar = lhs + ""; 
    if (updatedVar != null) 
    { 
      vbls.remove(updatedVar); 
      Vector es = rhs.allAttributesUsedIn(); 
      Vector vs = rhs.getVariableUses(); 
      es.addAll(vs); 
      Vector rhsBEs = rhs.allReadBasicExpressionData(); 
      for (int i = 0; i < es.size(); i++) 
      { String var = "" + es.get(i); 
        mp.add_pair(var, updatedVar); 
        if (vbls.contains(var)) { } 
        else 
        { vbls.add(var); } 
      } 
      for (int i = 0; i < rhsBEs.size(); i++) 
      { String rv = "" + rhsBEs.get(i); 
        dlin.add_pair(rv, updatedVar);
      } 
    } 
    return vbls; 
  }  
  public boolean updates(Vector v) 
  { String updatedVar = lhs.updatedData(); 
    if (updatedVar != null && v.contains(updatedVar))
    { return true; }
    return false; 
  }  
  public Vector slice(Vector allvars, Vector vars)
  { Vector res = new Vector(); 
    if (vars.contains(lhs.toString()))  
    { res.add(this); } 
    else 
    { System.out.println(">> Deleting statement from slice: " + this); } 
    return res; 
  }  
  public Expression toExpression()
  { return new BinaryExpression("=",lhs,rhs); }
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { 
    if (copyValue && type != null && type.isMapType())
    { String res = "  " + type.getJava() + " " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isMapType())
    { String res = "  " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava() + " " + lhs + " = new Vector();\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = new Vector();\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryForm(env,local) + ");\n"; 
      return res; 
    } 
    Type letype = lhs.getElementType(); 
    Type retype = rhs.getElementType(); 
    if (letype != null && retype != null && letype.isEntity() && retype.isEntity())
    { Entity srcent = retype.getEntity(); 
      Entity trgent = letype.getEntity(); 
      if (srcent.isSourceEntity() && trgent.isTargetEntity())
      { BasicExpression fid = new BasicExpression("$id");
        fid.setType(new Type("String",null));
        fid.setUmlKind(Expression.ATTRIBUTE);
        fid.setEntity(srcent);
        fid.setObjectRef(rhs); 
        BasicExpression felem = new BasicExpression(trgent.getName());
        felem.setUmlKind(Expression.CLASSID);
        felem.setEntity(trgent);
        felem.setArrayIndex(fid);
        felem.setType(letype);
        felem.setElementType(letype);
        BinaryExpression feq = new BinaryExpression("=", lhs, felem);
        String fres = feq.updateForm(env,local);
        if (type != null) 
        { fres = "  " + type.getJava() + " " + fres; }
        return fres;  
      } 
    } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateForm(env,local);  
    if (type != null) 
    { res = "  " + type.getJava() + " " + res; } 
    return res; 
  } 
  public String updateFormJava6(java.util.Map env, boolean local)
  { 
    if (copyValue && type != null && type.isMapType())
    { String res = "  " + type.getJava6() + " " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isMapType())
    { String res = "  " + lhs + " = new HashMap();\n"; 
      res = res + "  " + lhs + ".putAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava6() + " " + lhs + " = " + type.initialValueJava6() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = " + lhs.getType().initialValueJava6() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava6(env,local) + ");\n"; 
      return res; 
    } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateFormJava6(env,local);  
    if (type != null) 
    { res = "  " + type.getJava6() + " " + res; } 
    return res; 
  } 
  public String updateFormJava7(java.util.Map env, boolean local)
  { 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "  " + type.getJava7(lhs.elementType) + " " + lhs + " = " + type.initialValueJava7() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava7(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "  " + lhs + " = " + lhs.getType().initialValueJava7() + ";\n"; 
      res = res + "  " + lhs + ".addAll(" + rhs.queryFormJava7(env,local) + ");\n"; 
      return res; 
    } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateFormJava7(env,local);  
    if (type != null) 
    { res = "  " + type.getJava7(lhs.elementType) + " " + res; } 
    return res; 
  } 
  public String updateFormCSharp(java.util.Map env, boolean local)
  { 
    if (copyValue && type != null && type.isCollectionType())
    { String res = "    " + type.getCSharp() + " " + lhs + " = new ArrayList();\n"; 
      res = res + "    " + lhs + ".AddRange(" + rhs.queryFormCSharp(env,local) + ");\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && lhs.getType().isCollectionType())
    { String res = "    " + lhs + " = new ArrayList();\n"; 
      res = res + "    " + lhs + ".AddRange(" + rhs.queryFormCSharp(env,local) + ");\n"; 
      return res; 
    } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateFormCSharp(env,local);
    if (type != null) 
    { res = "    " + type.getCSharp() + "   " + res; } 
    return res; 
  } 
  public String updateFormCPP(java.util.Map env, boolean local)
  { 
    if (copyValue && type != null && Type.isSequenceType(type))
    { String elemt = rhs.getElementType().getCPP(); 
      String res = "  vector<" + elemt + ">* " + lhs + " = new vector<" + elemt + ">();\n"; 
      String rqf = rhs.queryFormCPP(env,local); 
      res = res + "  " + lhs + "->insert(" + lhs + "->end(), " + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } 
    else if (copyValue && type != null && Type.isSetType(type))
    { String elemt = rhs.getElementType().getCPP(); 
      String res = "  std::set<" + elemt + ">* " + lhs + " = new std::set<" + elemt + ">();\n"; 
      String rqf = rhs.queryFormCPP(env,local); 
      res = res + "  " + lhs + "->insert(" + rqf + "->begin(), " + rqf + "->end());\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && Type.isSequenceType(lhs.getType()))
    { String elemt = rhs.getElementType().getCPP(); 
      String rqf = rhs.queryFormCPP(env,local); 
      String res = "  " + lhs + " = new vector<" + elemt + ">();\n"; 
      res = res + "  " + lhs + "->insert(" + lhs + "->end(), " + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } 
    else if (copyValue && lhs.getType() != null && Type.isSetType(lhs.getType()))
    { String elemt = rhs.getElementType().getCPP(); 
      String rqf = rhs.queryFormCPP(env,local); 
      String res = "  " + lhs + " = new std::set<" + elemt + ">();\n"; 
      res = res + "  " + lhs + "->insert(" + rqf + "->begin(), " + 
                                         rqf + "->end());\n"; 
      return res; 
    } 
    String res = (new BinaryExpression("=",lhs,rhs)).updateFormCPP(env,local);  
    if (type != null) 
    { res = "  " + type.getCPP(rhs.getElementType()) + " " + res; } 
    return res; 
  } 
  public Vector allPreTerms()
  { Vector res = rhs.allPreTerms();
    return res;  
  }  
  public Vector allPreTerms(String var)
  { Vector res = rhs.allPreTerms(var);
    return res;  
  }  
  public Vector readFrame()
  { Vector res = new Vector();
    res.addAll(rhs.allReadFrame());  
    return res;  
  }  
  public Vector writeFrame()
  { Vector res = new Vector();
    if (lhs instanceof BasicExpression) 
    { String frame = ((BasicExpression) lhs).data; 
      Entity e = lhs.getEntity(); 
      if (e != null) 
      { frame = e.getName() + "::" + frame; } 
      res.add(frame); 
    } 
    return res;  
  }  
  public Statement checkConversions(Entity e, Type _propType, Type _propElemType, java.util.Map interp)
  { if (lhs instanceof BasicExpression)
    { BasicExpression belhs = (BasicExpression) lhs; 
      String propertyName = belhs.getData(); 
      Type propType = e.getDefinedFeatureType(propertyName); 
      Type propElemType = e.getDefinedFeatureElementType(propertyName); 
      Expression newrhs = rhs.checkConversions(propType,propElemType,interp); 
      AssignStatement res = new AssignStatement(lhs,newrhs); 
      res.setType(type); 
      res.setCopyValue(copyValue); 
      return res; 
    } 
    else  
    { return this; }
  }  
  public Statement replaceModuleReferences(UseCase uc)
  { if (lhs instanceof BasicExpression)
    { BasicExpression belhs = (BasicExpression) lhs; 
      Expression newlhs = lhs.replaceModuleReferences(uc); 
      Expression newrhs = rhs.replaceModuleReferences(uc); 
      AssignStatement res = new AssignStatement(newlhs,newrhs); 
      res.setType(type); 
      res.setCopyValue(copyValue); 
      return res; 
    } 
    else  
    { return this; }
  }  
  public int syntacticComplexity()
  { int syncomp = rhs.syntacticComplexity(); 
    return lhs.syntacticComplexity() + syncomp + 1; 
  } 
  public int cyclomaticComplexity()
  { return 0; } 
  public int epl()
  { return 0; }  
  public Vector allOperationsUsedIn()
  { Vector res = new Vector();
    res.addAll(rhs.allOperationsUsedIn());  
    return res;  
  }  
  public Vector allAttributesUsedIn()
  { Vector res = new Vector();
    res.addAll(lhs.allAttributesUsedIn());  
    res.addAll(rhs.allAttributesUsedIn());  
    return res;  
  }  
  public Vector getUses(String var)
  { Vector res = new Vector();
    res.addAll(lhs.getUses(var));  
    res.addAll(rhs.getUses(var));  
    return res;  
  }  
  public Vector getVariableUses()
  { Vector res = new Vector();
    res.addAll(lhs.getVariableUses());  
    res.addAll(rhs.getVariableUses());  
    return res;  
  }  
  public Vector equivalentsUsedIn()
  { Vector res = new Vector();
    res.addAll(rhs.equivalentsUsedIn());  
    return res;  
  }  
  public Vector metavariables()
  { Vector res = lhs.metavariables();
    res.addAll(rhs.metavariables());  
    return res;  
  }  
}