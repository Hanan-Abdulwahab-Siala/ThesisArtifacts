class IfStatement extends Statement
{ Vector cases = new Vector();    
  public String getOperator() 
  { return "if"; } 
  public IfStatement() { } 
  public IfStatement(Expression test, Statement ifpart, Statement elsepart)
  { IfCase ic1 = new IfCase(test,ifpart); 
    cases.add(ic1); 
    if ("skip".equals(elsepart + "")) { } 
    else 
    { IfCase ic2 = new IfCase(new BasicExpression(true),elsepart); 
      cases.add(ic2);
    }  
  } 
  public IfStatement(Expression test, Statement ifpart)
  { IfCase ic1 = new IfCase(test,ifpart); 
    cases.add(ic1); 
  } 
  public IfStatement(Statement ifpart, Statement elsepart)
  { if (ifpart instanceof IfStatement)
    { cases.addAll(((IfStatement) ifpart).cases); } 
    else 
    { cases.add(new IfCase(new BasicExpression(true), ifpart)); }  
    cases.add(new IfCase(new BasicExpression(true), elsepart)); 
  } 
  public Object clone() 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.clone(); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  
  public Statement optimiseOCL() 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.optimiseOCL(); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  
  public Statement generateDesign(java.util.Map env, boolean local)
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.generateDesign(env,local); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  
  public Expression getTest()
  { if (cases.size() > 0)
    { IfCase case1 = (IfCase) cases.get(0); 
      return case1.getTest(); 
    } 
    return new BasicExpression(true); 
  } 
  public Statement getIfPart()
  { if (cases.size() > 0)
    { IfCase case1 = (IfCase) cases.get(0); 
      return case1.getIf(); 
    } 
    return null; 
  } 
  public Statement getElsePart()
  { if (cases.size() > 1)
    { IfCase case1 = (IfCase) cases.get(1); 
      return case1.getIf(); 
    } 
    return null; 
  } 
  public void setElse(Statement s)
  { if (cases.size() > 1)
    { IfCase case1 = (IfCase) cases.get(1); 
      case1.setIf(s); 
    } 
  } 
  public void findClones(java.util.Map clones, String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findClones(clones,rule,op); 
    } 
  }
  public void findClones(java.util.Map clones, java.util.Map cdefs, String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findClones(clones,cdefs,rule,op); 
    } 
  }
  public Map energyUse(Map uses, Vector ruses, Vector ouses)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.energyUse(uses, ruses, ouses); 
    } 
    return uses; 
  }
  public java.util.Map collectionOperatorUses(int lev, java.util.Map uses, Vector vars)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.collectionOperatorUses(lev, uses, vars); 
    } 
    return uses; 
  }
  public void findMagicNumbers(java.util.Map mgns, String rule, String op)
  { for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      cse.findMagicNumbers(mgns,rule,op); 
    } 
  }
  public Statement dereference(BasicExpression var) 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = (IfCase) cse.dereference(var); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  
  public Statement addContainerReference(BasicExpression ref, String var, Vector excl) 
  { Vector newcases = new Vector(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase cse = (IfCase) cases.get(i); 
      IfCase newcse = 
           (IfCase) cse.addContainerReference(ref,var,excl); 
      newcases.add(newcse); 
    } 
    IfStatement res = new IfStatement(); 
    res.cases = newcases; 
    res.setEntity(entity); 
    return res; 
  }  
  public void setEntity(Entity e)
  { entity = e; 
    for (int i = 0; i < cases.size(); i++)
    { IfCase ic = (IfCase) cases.get(i); 
      ic.setEntity(e); 
    }
  }
  public boolean isEmpty() 
  { return cases.size() == 0; } 
  public void addCase(Expression test, Statement action) 
  { IfCase ic = new IfCase(test,action); 
    cases.add(ic); 
  } 
  public void addCase(IfCase ic)
  { cases.add(ic); }
  public void addCases(IfStatement stat) 
  { cases.addAll(stat.cases); } 
  public Statement substituteEq(String oldE, Expression newE)
  { IfStatement istat = new IfStatement(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      IfCase ic2 = ic.substituteEq(oldE,newE); 
      istat.addCase(ic2); 
    } 
    return istat; 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { IfStatement istat = new IfStatement(); 
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      IfCase ic2 = ic.removeSlicedParameters(bf,fpars); 
      istat.addCase(ic2); 
    } 
    return istat;
  } 
  public void display()
  { int n = cases.size();
    if (n == 0) 
    { System.out.println("      skip");
      return; } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      System.out.print("    "); 
      ic.display(); 
      if (j < n-1) 
      { System.out.println("    ELSE"); } 
    }
    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { System.out.print("  END"); }
    System.out.println(""); 
  }
  public String bupdateForm()
  { String res = ""; 
    int n = cases.size();
    if (n == 0) 
    { res = res + "      skip\n";
      return res; 
    } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      res = res + "    "; 
      res = res + ic.bupdateForm(); 
      if (j < n-1) 
      { res = res + "    ELSE\n"; } 
    }
    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { res = res + "  END"; }
    res = res + "\n"; 
    return res;
  }
  public BStatement bupdateForm(java.util.Map env, boolean local)
  { int n = cases.size();
    if (n == 0) 
    { return new BBasicStatement("skip"); }
    IfCase ic1 = (IfCase) cases.get(0); 
    Expression test1 = ic1.getTest(); 
    Statement if1 = ic1.getIf(); 
    BIfStatement res = new BIfStatement(test1.binvariantForm(env,local), if1.bupdateForm(env,local)); 
    BIfStatement bifelse = res; 
    for (int j = 1; j < n; j++) 
    { IfCase ic = (IfCase) cases.get(j); 
      Expression tst = ic.getTest(); 
      Statement ifstat = ic.getIf(); 
      BIfStatement remif = new BIfStatement(tst.binvariantForm(env,local), ifstat.bupdateForm(env,local)); 
      bifelse.setElse(remif);
      bifelse = remif;  
    }
    return res;  
  }
  public void displayImp(String var)
  { int n = cases.size();
    if (n == 0) 
    { System.out.println("      skip");
      return; } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      System.out.print("    "); 
      ic.displayImp(var); 
      if (j < n-1) 
      { System.out.println("    ELSE"); } }
    System.out.print("  "); 
    for (int k = 0; k < n; k++)
    { System.out.print("  END"); }
      System.out.println(""); }
  public void displayImp(String var, PrintWriter out)
  { int n = cases.size();
    if (n == 0)
    { out.println("      skip");
      return; }
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      out.print("    ");
      ic.displayImp(var,out);
      if (j < n-1)
      { out.println("    ELSE"); } }
    out.print("  ");
    for (int k = 0; k < n; k++)
    { out.print("  END"); }
    out.println(""); 
  }
  public void display(PrintWriter out)
  { int n = cases.size();
    if (n == 0) 
    { out.println("      skip");
      return; } 
    for (int j = 0; j < n; j++)
    { IfCase ic = (IfCase) cases.elementAt(j);
      out.print("    "); 
      ic.display(out); 
      if (j < n-1) 
      { out.println("    ELSE"); } }
    out.print("  "); 
    for (int k = 0; k < n; k++)
    { out.print("  END"); }
      out.println(""); 
  }
  public String saveModelData(PrintWriter out)
  { Statement cs = convertToConditionalStatement(); 
    return cs.saveModelData(out); 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { Statement cs = convertToConditionalStatement(); 
    return cs.saveModelData(out, ent); 
  } 
   public void displayJava(String target)
   { int n = cases.size();
     if (n == 0) 
     { return; } 
     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       System.out.print("    "); 
       ic.displayJava(target); 
       if (j < n-1) 
       { System.out.println("    else"); } }
   } 
   public void displayJava(String target, PrintWriter out)
   { int n = cases.size();
     if (n == 0) 
     { return; } 
     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       out.print("    "); 
       ic.displayJava(target, out); 
       if (j < n-1) 
       { out.println("    else"); } 
     }
   } 
   public String toStringJava()
   { String res = ""; 
     int n = cases.size();
     if (n == 0) 
     { return res; } 
     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       res = res + "    "; 
       res = res + ic.toStringJava(); 
       if (j < n-1) 
       { res = res + "    else\n"; } 
     }
     return res; 
   } 
   public String toEtl()
   { String res = ""; 
     int n = cases.size();
     if (n == 0) 
     { return res; } 
     for (int j = 0; j < n; j++)
     { IfCase ic = (IfCase) cases.elementAt(j);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat; }
       else 
       { res = res + "  if (" + test + ") { " + stat.toEtl() + " }\n";
         if (j < n-1)
         { res = res + "  else "; }
       }       
     }
     return res; 
   } 
   public String toString()
   { int n = cases.size();
     String res = "";
     for (int i = 0; i < n; i++)
     { IfCase ic = (IfCase) cases.get(i);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat; }
       else 
       { res = res + "  if " + test + " then " + stat;
         if (i < n-1)
         { res = res + " else "; }
       }
     }
     return res;
   }
   public String toAST()
   { int n = cases.size();
     String res = "(OclStatement if ";
     for (int i = 0; i < n; i++)
     { IfCase ic = (IfCase) cases.get(i);
       Expression test = ic.getTest();
       Statement stat = ic.getIf();
       if ("true".equals(test + ""))
       { res = res + stat.toAST() + " "; }
       else 
       { res = res + "  if " + test.toAST() + " then " + stat.toAST() + " ";
         if (i < n-1)
         { res = res + " else "; }
       }
     }
     res = res + ")";
    return res; 
  }
  public boolean containsSubexpression(Expression expr)
  { return false; } 
  public Vector singleMutants() 
  { return new Vector(); } 
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { boolean res = true;
    for (int i = 0; i < cases.size(); i++) 
    { IfCase ic = (IfCase) cases.get(i); 
      res = ic.typeCheck(types,entities,cs,env) && res; 
    }
    return res;
  }
}
