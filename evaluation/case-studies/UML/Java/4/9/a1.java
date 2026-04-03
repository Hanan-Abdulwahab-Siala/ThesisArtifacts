class SequenceStatement extends Statement
{ private Vector statements = new Vector();
  public SequenceStatement(Vector stats)
  { statements = stats; } 
  public void addStatements(SequenceStatement ss)
  { statements.addAll(ss.getStatements()); } 
  public void addStatements(Statement st)
  { if (st == null) 
    { return; } 
    if (st instanceof SequenceStatement)
    { statements.addAll(
          ((SequenceStatement) st).getStatements());
    } 
  }  
  public SequenceStatement(Statement s1, Statement s2)
  { statements = new Vector(); 
    statements.add(s1); 
    statements.add(s2); 
  } 
  public static Statement composedStatement(
                   Statement s1, Statement s2)
  { if (s1 == null) 
    { return s2; } 
    if (s2 == null) 
    { return s1; }
    return new SequenceStatement(s1,s2);
  } 
  public static Statement composedStatement(
                   Statement s1, Statement s2, Statement s3)
  { if (s1 == null && s2 == null) 
    { return s3; } 
    if (s1 == null && s3 == null) 
    { return s2; } 
    if (s2 == null && s3 == null) 
    { return s1; }
    if (s1 == null) 
    { return new SequenceStatement(s2,s3); } 
    if (s2 == null) 
    { return new SequenceStatement(s1,s3); } 
    if (s3 == null)
    { return new SequenceStatement(s1,s2); }
    SequenceStatement res = new SequenceStatement(); 
    res.addStatement(s1); 
    res.addStatement(s2);
    res.addStatement(s3); 
    return res; 
  } 
  public static Statement combineSequenceStatements(Statement s1, Statement s2) 
  { if (s1 == null) 
    { return s2; } 
    if (s2 == null) 
    { return s1; }
    if (s1 instanceof SequenceStatement)
    { SequenceStatement sqstat1 = (SequenceStatement) s1; 
      if (s2 instanceof SequenceStatement)
      { SequenceStatement sqstat2 = (SequenceStatement) s2; 
        sqstat1.addStatements(sqstat2.getStatements()); 
        return sqstat1; 
      } 
      else  
      { sqstat1.addStatement(s2); 
        return sqstat1; 
      } 
    } 
    else if (s2 instanceof SequenceStatement) 
    { SequenceStatement res = (SequenceStatement) s2; 
      res.addStatement(0,s1); 
      return res; 
    } 
    return new SequenceStatement(s1,s2); 
  }          
  public String getOperator() 
  { return ";"; } 
  public Object clone()
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = (Statement) stat.clone(); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public int execute(ModelSpecification sigma, ModelState beta)
  { 
    int res = Statement.NORMAL; 
    beta.addNewEnvironment(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = stat.execute(sigma, beta);
      if (res == Statement.BREAK || res == Statement.RETURN ||
          res == Statement.CONTINUE)
      { beta.removeLastEnvironment();
        return res; 
      }   
    } 
    beta.removeLastEnvironment();
    return Statement.NORMAL;  
  } 
  public Expression definedness()
  { Expression res = new BasicExpression(true); 
    Expression post = new BasicExpression(true); 
    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Expression def = stat.definedness();
      def.setBrackets(true);  
      Expression inv = stat.wpc(res, post); 
      inv.setBrackets(true); 
      res = Expression.simplify("&", inv, def, null); 
    } 
    return res;  
  } 
  public int size()
  { return statements.size(); } 
  public boolean isEmpty()
  { return statements.size() == 0; } 
  public boolean notEmpty()
  { return statements.size() > 0; } 
  public boolean isSkip()
  { for (int i = 0; i < statements.size(); i++) 
    { Statement st = (Statement) statements.get(i); 
      if (st.isSkip()) { } 
      else 
      { return false; } 
    } 
    return true; 
  } 
  public String cg(CGSpec cgs)
  { String etext = this + "";
    Vector args = new Vector();
    if (statements.size() == 0)
    { etext = "skip"; 
      return ""; 
    }
    else if (statements.size() == 1)
    { Statement st = (Statement) statements.get(0);
      return st.cg(cgs);
    }
    else
    { SequenceStatement tailst = new SequenceStatement();
      Statement st0 = (Statement) statements.get(0);
      Vector newsts = new Vector();
      newsts.addAll(statements);
      newsts.remove(0);
      tailst.statements = newsts;
      args.add(st0.cg(cgs));
      args.add(tailst.cg(cgs));
    }
    CGRule r = cgs.matchedStatementRule(this,etext);
    if (r != null)
    { 
      String res = r.applyRule(args);
      return res; 
    }
    return etext;
  }
  public Vector cgparameters()
  {
    Vector args = new Vector();
    if (statements.size() == 0)
    { return args; }
    else if (statements.size() == 1)
    { Statement st = (Statement) statements.get(0);
      args.add(st);
      return args; 
    }
    else
    { SequenceStatement tailst = new SequenceStatement();
      Statement st0 = (Statement) statements.get(0);
      Vector newsts = new Vector();
      newsts.addAll(statements);
      newsts.remove(0);
      tailst.statements = newsts;
      args.add(st0);
      args.add(tailst);
    }
    return args;
  }
  public void findClones(java.util.Map clones, String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findClones(clones,rule,op); 
    }
    Vector fstats = flattenSequenceStatement(); 
    Vector substats = VectorUtil.allSubsegments(fstats,2);
    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (sq.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
      { continue; } 
      String val = sq + ""; 
      Vector used = (Vector) clones.get(val); 
      if (used == null)
      { used = new Vector(); }
      if (rule != null && !used.contains(rule))
      { used.add(rule); }
      else if (op != null && !used.contains(op))
      { used.add(op); }
      clones.put(val,used);
    } 
  } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findClones(clones,cdefs,rule,op); 
    }
    Vector fstats = flattenSequenceStatement(); 
    Vector substats = VectorUtil.allSubsegments(fstats,2); 
    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (sq.syntacticComplexity() < UCDArea.CLONE_LIMIT) 
      { continue; } 
      String val = sq + ""; 
      Vector used = (Vector) clones.get(val); 
      if (used == null)
      { used = new Vector(); }
      if (rule != null && !used.contains(rule))
      { used.add(rule); }
      else if (op != null && !used.contains(op))
      { used.add(op); }
      clones.put(val,used);
      cdefs.put(val, sq); 
    } 
  } 
  public Vector allVariableNames()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = VectorUtil.union(res,
                         stat.allVariableNames()); 
    }
    return res; 
  } 
  public Map energyUse(Map uses, Vector rUses, Vector aUses)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.energyUse(uses, rUses, aUses);
      if (i < statements.size()-1 && 
          Statement.endsWithControlFlowBreak(stat))
      { int acount = (int) uses.get("amber"); 
        uses.set("amber", acount+1); 
        aUses.add("!! Unreachable code: statements after " + stat + " in sequence cannot be reached -- they should be deleted"); 
      } 
    }
    return uses; 
  } 
  public java.util.Map collectionOperatorUses(int lev, 
                              java.util.Map uses, 
                              Vector vars)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.collectionOperatorUses(lev, uses, vars); 
    }
    return uses; 
  } 
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      stat.findMagicNumbers(mgns,rule,op); 
    }
  } 
  public Statement optimiseOCL()
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      if (stat.isSkip()) 
      { continue; } 
      Statement newstat = stat.optimiseOCL();
      if (newstat instanceof SequenceStatement && 
          ((SequenceStatement) newstat).size() == 1)
      { Statement ss = 
          ((SequenceStatement) newstat).getStatement(0); 
        newstats.add(ss); 
      }  
      else 
      { newstats.add(newstat); }
      if (i < statements.size() - 1 &&
          Statement.endsWithControlFlowBreak(newstat)) 
      { System.out.println(">> Deleting statements after " + newstat); 
        break; 
      } 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement dereference(BasicExpression var)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = (Statement) stat.dereference(var); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement addContainerReference(BasicExpression ref,
                                         String var, 
                                         Vector excl)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat =
             stat.addContainerReference(ref,var,excl); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.checkConversions(e,propType,propElemType,interp); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement replaceModuleReferences(UseCase uc)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.replaceModuleReferences(uc); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement generateDesign(java.util.Map env, boolean local)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.generateDesign(env,local); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public Statement statLC(java.util.Map env, boolean local)
  { Vector newstats = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      Statement newstat = stat.statLC(env,local); 
      newstats.add(newstat); 
    } 
    SequenceStatement res = new SequenceStatement(newstats);
    res.setEntity(entity); 
    res.setBrackets(brackets); 
    return res;  
  } 
  public static Statement statLC(Vector preds, java.util.Map env, boolean local) 
  { if (preds.size() == 0) 
    { return new SequenceStatement(); } 
    else if (preds.size() == 1) 
    { Expression e = (Expression) preds.get(0); 
      return e.statLC(env,local); 
    } 
    else 
    { SequenceStatement sts = new SequenceStatement(); 
      for (int i = 0; i < preds.size(); i++) 
      { Expression p = (Expression) preds.get(i); 
        Statement st = p.statLC(env,local); 
        sts.addStatement(st);
      } 
      return sts; 
    } 
  } 
  public SequenceStatement()
  { statements = new Vector(); } 
  public int getSize()
  { return statements.size(); } 
  public void setEntity(Entity e)
  { entity = e;
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i);
      if (stat.entity == null) 
      { stat.setEntity(e); }  
    } 
  }
  public void addStatement(Statement s)
  { if (s != null) 
    { statements.add(s); }
  } 
  public void addStatements(Vector stats)
  { if (stats != null) 
    { statements.addAll(stats); }
  } 
  public void addStatement(int pos, Statement s)
  { if (pos >= statements.size() && s != null)
    { statements.add(s); } 
    else if (s != null) 
    { statements.add(pos,s); }
  }
  public void addBeforeEnd(Statement s)
  { int sz = statements.size(); 
    if (sz == 0 && s != null)
    { statements.add(s); } 
    else if (s != null) 
    { statements.add(sz-1,s); } 
  } 
  public Vector getStatements()
  { return statements; } 
  public Statement getStatement(int i) 
  { return (Statement) statements.get(i); } 
  public Statement substituteEq(String oldE, Expression newE)
  { Vector fstats = flattenSequenceStatement(); 
    Vector substats = VectorUtil.allSubsegments(fstats,2); 
    for (int i = 0; i < substats.size(); i++) 
    { Vector subs = (Vector) substats.get(i); 
      Statement sq = new SequenceStatement(subs); 
      if (oldE.equals(sq + "")) 
      { InvocationStatement istat = 
          new InvocationStatement(newE);
        Vector newv = new Vector(); 
        newv.add(istat); 
        Vector newfstats = 
          VectorUtil.replaceSubsequence(fstats, subs, newv); 
        return new SequenceStatement(newfstats); 
      } 
    }
    SequenceStatement stats = new SequenceStatement(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = 
        ((Statement) statements.get(i)).substituteEq(oldE,newE);
      stats.addStatement(stat);
    } 
    stats.entity = entity; 
    stats.setBrackets(brackets); 
    return stats;
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { SequenceStatement stats = new SequenceStatement(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = 
        (Statement) statements.get(i); 
      Statement stat1 = 
         stat.removeSlicedParameters(bf,fpars); 
      stats.addStatement(stat1);
    } 
    stats.entity = entity; 
    stats.setBrackets(brackets); 
    return stats;
  } 
  public void display()
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      System.out.print("  ");
      ss.display();  
      if (i < statements.size() - 1) 
      { System.out.println(" || "); }
    } 
  }
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      String ssid = ss.saveModelData(out); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      String ssid = ss.saveModelData(out, ent); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 
  public String bupdateForm()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      res = res + "  " + ss.bupdateForm(); 
      if (i < statements.size() - 1)
      { res = res + ";\n"; }
    } 
    return res; 
  }
}
