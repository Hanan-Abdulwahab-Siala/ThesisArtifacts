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
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { BParallelStatement res = new BParallelStatement(false); 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      res.addStatement(ss.bupdateForm(env,local)); 
    } 
    return res; 
  }
  public void displayImp(String var)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      System.out.print("  "); ss.displayImp(var); 
      if (i < statements.size() - 1)
      { System.out.println(";"); }
    } 
  }
  public void displayImp(String var, PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      out.print("  "); ss.displayImp(var,out);
      if (i < statements.size() - 1)
      { out.println(";"); }
    }
  }
  public void display(PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      out.print("  "); ss.display(out);
      if (i < statements.size() - 1)
      { out.println(" || "); }
    } 
  }    
  public void displayJava(String target)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { System.out.print("  "); }
      if (ss != null)
      { ss.displayJava(target); } 
      System.out.println(); 
    } 
  }
  public void displayJava(String target, PrintWriter out)
  { for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { out.print("  "); }
      if (ss != null)
      { ss.displayJava(target,out); }
      out.println(); 
    } 
  }
  public String toStringJava()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.toStringJava(); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String toEtl()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.toEtl(); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String toString()
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { if (statements.get(i) instanceof Statement) 
	  { Statement ss = (Statement) statements.elementAt(i);
        if (i < statements.size() - 1)     
        { res = res + ss + " ; "; }
        else 
        { res = res + ss + " "; }
      }  
    }
    if (brackets)
    { res = "( " + res + " )"; } 
    return res; 
  }
  public static boolean isBlock0(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() == 0) 
      { return true; } 
    }
    return false; 
  } 
  public static boolean isBlock1(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() == 1) 
      { return true; } 
    }
    return false; 
  } 
  public static boolean isBlockN(Statement tt)
  { if (tt instanceof SequenceStatement)
    { SequenceStatement ss = (SequenceStatement) tt; 
      if (ss.statements.size() > 1) 
      { return true; } 
    }
    return false; 
  } 
  public Vector flattenSequenceStatement()
  { Vector res = new Vector(); 
    if (statements.size() == 0) 
    { return res; } 
    for (int i = 0; i < statements.size(); i++) 
    { Statement si = (Statement) statements.get(i);
      if (si instanceof SequenceStatement)
      { Vector subseq = 
          ((SequenceStatement) si).flattenSequenceStatement(); 
        res.addAll(subseq); 
      } 
      else 
      { res.add(si); } 
    } 
    return res; 
  } 
  public String toFlatAST()
  { String res = ""; 
    if (statements.size() > 0)
    { res = res + " ; "; } 
    for (int i = 0; i < statements.size(); i++)
    { Statement si = (Statement) statements.get(i);
      res = res + si.toAST(); 
      if (i < statements.size()-1) 
      { res = res + " ; "; } 
    } 
    return res; 
  } 
  public String toAST()
  { String res = "";  
    if (statements.size() == 0)
    { res = "(OclStatement call skip)"; }
    else 
    { Vector stats = flattenSequenceStatement(); 
      res = "(OclStatement ( (OclStatementList "; 
      Statement s1 = (Statement) stats.get(0); 
      res = res + s1.toAST() + " "; 
      for (int i = 1; i < stats.size(); i++) 
      { Statement s2 = (Statement) stats.get(i); 
        res = res + " ; " + s2.toAST();  
      }  
      res = res + " ) ) )";
      return res;   
    }
    return res; 
  }
  public boolean containsSubexpression(Expression expr)
  { for (int i = 0; i < statements.size(); i++)
    { Statement si = (Statement) statements.get(i);
      if (si.containsSubexpression(expr)) 
      { return true; }
    }  
    return false; 
  } 
  public Vector singleMutants()
  { 
    Vector res = new Vector(); 
    if (statements.size() == 0)
    { return res; }
    if (statements.size() == 1)
    { Statement s1 = (Statement) statements.get(0); 
      res = s1.singleMutants(); 
    } 
    else 
    { Statement s1 = (Statement) statements.get(0); 
      Vector s1muts = s1.singleMutants();
      Vector remstats = new Vector(); 
      remstats.addAll(statements); 
      remstats.remove(0); 
      for (int k = 0; k < s1muts.size(); k++)
      { Statement s1mut = (Statement) s1muts.get(k); 
        Vector s1rem = new Vector(); 
        s1rem.add(s1mut); 
        s1rem.addAll(remstats); 
        Statement seqrem = new SequenceStatement(s1rem); 
        res.add(seqrem);
      }
      Statement srest = new SequenceStatement(remstats); 
      Vector restmuts = srest.singleMutants(); 
      for (int k = 0; k < restmuts.size(); k++)
      { Statement restmut = (Statement) restmuts.get(k); 
        Vector s1rem = new Vector(); 
        s1rem.add(s1); 
        s1rem.add(restmut); 
        Statement seqrem = new SequenceStatement(s1rem); 
        res.add(seqrem);
      }
    }
    return res; 
  } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = true;  
    for (int i = 0; i < statements.size(); i++) 
    { Statement s = (Statement) statements.get(i); 
      Vector context = new Vector(); 
      Entity ee = s.entity; 
      if (ee != null) 
      { if (cs.size() > 0 && (ee + "").equals(cs.get(0) + "")) { } 
        else 
        { context.add(ee); }
      } 
      context.addAll(cs); 
      res = s.typeCheck(types,entities,context,env);
    } 
    return res; 
  }  
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { boolean res = true; 
    for (int i = 0; i < statements.size(); i++) 
    { Statement s = (Statement) statements.get(i); 
      Vector context = new Vector(); 
      Entity ee = s.entity; 
      if (ee != null) 
      { if (cs.size() > 0 && 
            (ee + "").equals(cs.get(0) + "")) 
        { } 
        else 
        { context.add(ee); }
      } 
      context.addAll(cs); 
      res = s.typeInference(types,entities,context,env,vartypes);
    } 
    return res; 
  }  
  public Expression wpc(Expression post)
  { Expression e1 = (Expression) post.clone();
    for (int i = statements.size()-1; i >= 0; i--)
    { Statement stat = (Statement) statements.get(i);
      Expression e2 = stat.wpc(e1);
      e1 = e2;
    } 
    return e1; 
  }
  public Expression wpc(Expression inv, Expression post)
  { Expression e1 = (Expression) inv.clone();
    for (int i = statements.size()-1; i >= 0; i--)
    { Statement stat = (Statement) statements.get(i);
      Expression e2 = stat.wpc(e1,post);
      e1 = e2;
    } 
    return e1; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Vector v = stat.dataDependents(allvars, vbls); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    return vbls; 
  }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      Vector v = stat.dataDependents(allvars, vbls, mp, dlin); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    return vbls; 
  }  
  public Vector slice(Vector allvars, Vector vars)
  { Vector vbls = new Vector(); 
    vbls.addAll(vars); 
    Vector deleted = new Vector(); 
    for (int i = statements.size() - 1; i >= 0; i--) 
    { Statement stat = (Statement) statements.get(i); 
      if (stat instanceof SequenceStatement)
      { SequenceStatement stat1 = (SequenceStatement) stat; 
        Vector ss = stat1.slice(allvars,vbls); 
        statements.remove(stat); 
        statements.add(i,new SequenceStatement(ss)); 
      } 
      else if (stat.updates(vbls)) 
      { System.out.println(stat + " updates " + vbls); } 
      else 
      { deleted.add(stat); 
        System.out.println(">> Deleting statement: " + stat); 
      } 
      Vector v = stat.dataDependents(allvars, vbls); 
      vbls = new Vector(); 
      vbls.addAll(v); 
    } 
    for (int j = 0; j < deleted.size(); j++) 
    { statements.remove(deleted.get(j)); } 
    return statements; 
  } 
  public boolean updates(Vector v) 
  { for (int i = 0; i < statements.size(); i++) 
    { Statement st = (Statement) statements.get(i);
      if (st.updates(v)) 
      { return true; }
    }
    return false; 
 } 
  public Expression toExpression()
  { Expression res = new BasicExpression("skip");
    for (int i = 0; i < statements.size(); i++)
    { Statement st = (Statement) statements.get(i);
      Expression e = st.toExpression();
      if (i > 0)
      { res = new BinaryExpression("&",res,e); }
      else 
      { res = e; }
    }
    return res;
  }
  public void mergeSequenceStatements(Statement s)
  { if (s instanceof SequenceStatement)
    { statements.addAll(((SequenceStatement) s).statements); }
    else 
    { statements.add(s); }
  }
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities,
                           Vector vars)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateForm(env,local,types,entities,vars); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String deltaUpdateForm(java.util.Map env, boolean local)
  { String res = "";   
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { if (ss instanceof InvocationStatement) 
        { res = res + ((InvocationStatement) ss).deltaUpdateForm(env,local); }
        else if (ss instanceof SequenceStatement) 
        { res = res + ((SequenceStatement) ss).deltaUpdateForm(env,local); } 
        else 
        { res = res + ss.updateForm(env,local,new Vector(), new Vector(), new Vector()); }
      } 
      res = res + "\n"; 
    } 
    return res; 
  }
  public String updateFormJava6(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormJava6(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String updateFormJava7(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormJava7(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormCSharp(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public String updateFormCPP(java.util.Map env, boolean local)
  { String res = ""; 
    for (int i = 0; i < statements.size(); i++)
    { Statement ss = (Statement) statements.elementAt(i);
      if (i > 0)                  
      { res = res + "  "; }
      if (ss != null)
      { res = res + ss.updateFormCPP(env,local); }
      res = res + "\n"; 
    } 
    return res; 
  }
  public Vector allPreTerms()
  { Vector res = new Vector();
    for (int i = 0; i < statements.size(); i++) 
    { res.addAll(((Statement) statements.get(i)).allPreTerms()); }  
    return res; 
  }  
  public Vector allPreTerms(String var)
  { Vector res = new Vector();
    for (int i = 0; i < statements.size(); i++) 
    { res.addAll(((Statement) statements.get(i)).allPreTerms(var)); }  
    return res; 
  }  
  public Vector readFrame()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.readFrame()); 
    } 
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = VectorUtil.union(res, stat.writeFrame()); 
    } 
    return res; 
  } 
  public int syntacticComplexity()
  { int res = 0; 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.syntacticComplexity(); 
    } 
    if (res > 0) 
    { res = res + statements.size() - 1; } 
    return res; 
  } 
  public int cyclomaticComplexity()
  { int res = 0; 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.cyclomaticComplexity(); 
    } 
    return res; 
  } 
  public int epl()
  { int res = 0; 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res = res + stat.epl(); 
    } 
    return res; 
  } 
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.allOperationsUsedIn()); 
    } 
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.allAttributesUsedIn()); 
    } 
    return res; 
  } 
  public Vector getUses(String var)
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.getUses(var)); 
    } 
    return res; 
  } 
  public Vector getVariableUses()
  { Vector res = new Vector(); 
    if (statements.size() == 0)
    { return res; } 
    Statement s1 = (Statement) statements.get(0); 
    if (statements.size() == 1) 
    { return s1.getVariableUses(); } 
    Vector tailseq = new Vector(); 
    tailseq.addAll(statements); 
    tailseq.remove(0); 
    SequenceStatement sstail = 
        new SequenceStatement(tailseq); 
    res = sstail.getVariableUses(); 
    if (s1 instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) s1; 
      String var = cs.getDefinedVariable(); 
      Expression use = 
        ModelElement.lookupExpressionByName(var,res); 
      if (use == null) 
      { System.err.println("!! Code smell (UVA): no use of local variable " + var + " in statements " + sstail); 
        System.err.println(); 
        cs.unusedStatement = true; 
      } 
      res = ModelElement.removeExpressionByName(var,res); 
      return res; 
    } 
    res.addAll(s1.getVariableUses()); 
    return res; 
  } 
  public Vector getVariableUses(Vector unused)
  { Vector res = new Vector(); 
    if (statements.size() == 0)
    { return res; } 
    Statement s1 = (Statement) statements.get(0); 
    if (statements.size() == 1) 
    { res = s1.getVariableUses(unused); 
      return res; 
    } 
    Vector tailseq = new Vector(); 
    tailseq.addAll(statements); 
    tailseq.remove(0); 
    SequenceStatement sstail = 
        new SequenceStatement(tailseq); 
    res = sstail.getVariableUses(unused); 
    if (s1 instanceof CreationStatement)
    { CreationStatement cs = (CreationStatement) s1; 
      String var = cs.getDefinedVariable(); 
      Expression use = 
        ModelElement.lookupExpressionByName(var,res); 
      if (use == null) 
      { System.err.println("!! Code smell (UVA): no use of local variable " + var + " in statements " + sstail); 
        System.err.println(); 
        unused.add(var); 
        cs.unusedStatement = true; 
      } 
      res = ModelElement.removeExpressionByName(var,res); 
      return res; 
    } 
    res.addAll(s1.getVariableUses()); 
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      res.addAll(stat.equivalentsUsedIn()); 
    } 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    for (int i = 0; i < statements.size(); i++) 
    { Statement stat = (Statement) statements.get(i); 
      { res.addAll(stat.metavariables()); } 
    } 
    return res; 
  } 
  public Vector segments()
  { 
    Vector res = new Vector(); 
    if (statements.size() == 0)
    { return res; }
    Vector allstatements = flattenSequenceStatement(); 
    Vector segment = new Vector();  
    Statement previous = null; 
    for (int i = 0; i < allstatements.size(); i++) 
    { Statement ss = (Statement) allstatements.get(i); 
      if (ss instanceof CreationStatement)
      { if (previous == null) 
        { segment.add(ss); 
          previous = ss; 
        } 
        else if (previous instanceof CreationStatement) 
        { segment.add(ss); 
          previous = ss; 
        } 
        else 
        { res.add(segment); 
          segment = new Vector(); 
          segment.add(ss); 
          previous = ss; 
        } 
      } 
      else 
      { segment.add(ss); 
        previous = ss; 
      } 
    }  
    res.add(segment); 
    return res; 
  } 
}
