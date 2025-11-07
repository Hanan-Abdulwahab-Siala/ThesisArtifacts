class CaseStatement extends Statement
{ Map cases = new Map();
  public Object clone() { return this; } 
  public String getOperator() 
  { return "case"; } 
  public Statement dereference(BasicExpression var) { return this; } 
  public Statement substituteEq(String oldE, Expression newE)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement stat = ((Statement) mm.dest).substituteEq(oldE,newE); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 
  public Statement removeSlicedParameters(BehaviouralFeature bf, Vector fpars)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement stat = 
        ((Statement) mm.dest).removeSlicedParameters(bf,fpars); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 
  public Statement addContainerReference(
                    BasicExpression ref, String var,
                    Vector excl)
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement cse = (Statement) mm.dest; 
      Statement stat = 
        cse.addContainerReference(ref,var,excl); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 
  public Statement optimiseOCL()
  { CaseStatement cs = new CaseStatement(); 
    Vector ss = cases.elements; 
    for (int i = 0; i < ss.size(); i++) 
    { Maplet mm = (Maplet) ss.get(i); 
      Statement cse = (Statement) mm.dest; 
      Statement stat = cse.optimiseOCL(); 
      Maplet nn = new Maplet(mm.source,stat); 
      cs.addCase(nn); 
    } 
    return cs; 
  } 
  public void addCase(Maplet mm)
  { cases.add_element(mm); }
  public void addCase(Named n, Statement s)
  { Maplet mm = new Maplet(n,s);
    cases.add_element(mm); }
  public Statement getCaseFor(Named nn)
  { Statement res = (Statement) cases.apply(nn);
    return res; }
  public void display()   
  { int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("IF " + ((Named) mm.source).label + " THEN ");
      System.out.print("  "); 
      ((Statement) mm.dest).display(); 
      if (i < n-1) 
      { System.out.println("ELSE"); } } 
    for (int j = 0; j < n; j++)
    { System.out.print("END  "); } 
    System.out.println(" "); 
  } 
  public String toAST()
  { String res = "(OclStatement "; 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "if " + ((Named) mm.source).label + " then ";
      res = res + ((Statement) mm.dest).toAST() + " "; 
      if (i < n-1) 
      { res = res + "else "; } 
    }
    res = res + ")";
    return res; 
  } 
  public boolean containsSubexpression(Expression expr)
  { int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      if (ss.containsSubexpression(expr)) 
      { return true; } 
    } 
    return false; 
  } 
  public Vector singleMutants() 
  { return new Vector(); } 
  public String saveModelData(PrintWriter out)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".kind = choice");
    for (int i = 0; i < cases.elements.size(); i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      String ssid = ss.saveModelData(out); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 
  public String saveModelData(PrintWriter out, Entity ent)
  { String res = Identifier.nextIdentifier("sequencestatement_"); 
    out.println(res + " : SequenceStatement");
    out.println(res + ".statId = \"" + res + "\"");  
    out.println(res + ".kind = choice");
    for (int i = 0; i < cases.elements.size(); i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement ss = (Statement) mm.dest; 
      String ssid = ss.saveModelData(out, ent); 
      out.println(ssid + " : " + res + ".statements"); 
    } 
    return res; 
  } 
  public String bupdateForm()   
  { int n = cases.elements.size();
    String res = ""; 
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "IF " + ((Named) mm.source).label + " THEN ";
      res = res + "  "; 
      res = res + ((Statement) mm.dest).bupdateForm() + "\n"; 
      if (i < n-1) 
      { res = res + "ELSE\n"; } } 
    for (int j = 0; j < n; j++)
    { res = res + "END  "; } 
    res = res + " \n"; 
    return res; 
  } 
  public BStatement bupdateForm(java.util.Map env, boolean local) 
  { return new BBasicStatement("skip"); } 
  public void display(PrintWriter out)    
  { int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("IF " + ((Named) mm.source).label + " THEN ");
      out.print("  ");
      ((Statement) mm.dest).display(out);
      if (i < n-1)
      { System.out.println("ELSE"); } }
    for (int j = 0; j < n; j++)
    { out.print("END  "); }
    out.println(" "); }  
  public void display(String s)
  { int n = cases.elements.size();
    if (n == 0) 
    { System.out.println("  skip"); 
      return; 
    } 
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  IF " + s + " = " + 
                         ((Named) mm.source).label + " THEN ");
      System.out.print("    ");
      ((Statement) mm.dest).display();  
      if (i < n-1)
      { System.out.println("  ELSE"); } 
    }
    for (int j = 0; j < n; j++)
    { System.out.print("  END"); }
  }
  public void displayMult(String s)
  { int n = cases.elements.size();
    if (n == 0) 
    { System.out.println("  skip");
      return;
    }
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  IF " + s + "(oo) = " + 
                         ((Named) mm.source).label + " THEN ");
      System.out.print("    ");
      ((Statement) mm.dest).display();  
      if (i < n-1)
      { System.out.println("  ELSE"); } 
    }
    for (int j = 0; j < n; j++)
    { System.out.print("  END"); }
  }
  public void display(String s, PrintWriter out)
  { int n = cases.elements.size();
    if (n == 0)
    { out.println("  skip");
      return;
    }
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("  IF " + s + " = " +
                         ((Named) mm.source).label + " THEN ");
      out.print("    ");
      ((Statement) mm.dest).display(out);
      if (i < n-1)
      { out.println("  ELSE"); } 
    }
    for (int j = 0; j < n; j++)
    { out.print("  END"); }
  }
  public void displayJava(String s)
  { int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      System.out.println("  if (M" + s + "." + s + " == " + 
                          ((Named) mm.source).label + ")");
       System.out.print("    { ");
       ((Statement) mm.dest).displayJava("M" + s);
       System.out.println("    }"); 
       if (i < n-1)
       { System.out.println("  else {"); } 
    }
  } 
  public void displayJava(String s, PrintWriter out)
  { int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      out.println("  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")");
      out.print("    { ");
      ((Statement) mm.dest).displayJava("M" + s, out);
      out.println("    }"); 
      if (i < n-1)
      { out.println("  else {"); } 
    }
  }
  public String toStringJava()
  { int n = cases.elements.size();
    String res = ""; 
    String s = "s"; 
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")";
      res = res + "    {\n";
      res = res + ((Statement) mm.dest).toStringJava();
      res = res + "    }\n"; 
      if (i < n-1)
      { res = res + "  else {\n"; } 
    }
    return res; 
  }
  public String toEtl()
  { int n = cases.elements.size();
    String res = ""; 
    String s = "s"; 
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      res = res + "  if (M" + s + "." + s + " == " + 
                  ((Named) mm.source).label + ")";
      res = res + "    {\n";
      res = res + ((Statement) mm.dest).toEtl();
      res = res + "    }\n"; 
      if (i < n-1)
      { res = res + "  else {\n"; } 
    }
    return res; 
  }
  public boolean typeCheck(Vector types, Vector entities, Vector cs, Vector env)
  { return true; }   
  public boolean typeInference(Vector types, Vector entities, Vector cs, Vector env, java.util.Map vartypes)
  { return true; }   
  public Expression wpc(Expression post)
  { return post; }  
  public Expression wpc(Expression inv, Expression post)
  { return inv; }  
  public Vector dataDependents(Vector allvars, Vector vars)
  { return vars; }  
  public Vector dataDependents(Vector allvars, Vector vars, Map mp, Map dlin)
  { return vars; }  
  public boolean updates(Vector v) 
  { return false; } 
  public String updateForm(java.util.Map env, boolean local, Vector types, Vector entities, 
                           Vector vars)
  { return toStringJava(); }
  public String updateFormJava6(java.util.Map env, boolean local)
  { return toStringJava(); }
  public String updateFormJava7(java.util.Map env, boolean local)
  { return toStringJava(); }
  public String updateFormCSharp(java.util.Map env, boolean local)
  { return toStringJava(); }
  public String updateFormCPP(java.util.Map env, boolean local)
  { return toStringJava(); }
  public Vector readFrame()
  { Vector res = new Vector(); 
    return res; 
  } 
  public Vector writeFrame()
  { Vector res = new Vector(); 
    return res; 
  } 
  public Statement checkConversions(Entity e, Type propType, Type propElemType, java.util.Map interp)
  { return this; } 
  public Statement replaceModuleReferences(UseCase uc)
  { return this; } 
  public int syntacticComplexity() 
  { int res = 0; 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.syntacticComplexity() + 1; 
    }
    return res; 
  }
  public int cyclomaticComplexity() 
  { int res = 0; 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.cyclomaticComplexity() + 1; 
    }
    return res; 
  }
  public Map energyUse(Map uses, Vector ruses, Vector ouses) 
  { 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      cse.energyUse(uses, ruses, ouses); 
    }
    return uses; 
  }
  public java.util.Map collectionOperatorUses(int lev, 
                              java.util.Map uses, Vector vars) 
  { 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      cse.collectionOperatorUses(lev, uses, vars); 
    }
    return uses; 
  }
  public int epl() 
  { int res = 0; 
    int n = cases.elements.size();
    for (int i = 0; i < n; i++)
    { Maplet mm = (Maplet) cases.elements.elementAt(i);
      Statement cse = (Statement) mm.dest;
      res = res + cse.epl() + 1; 
    }
    return res; 
  }
  public Vector allOperationsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).allOperationsUsedIn()); 
    } 
    return res; 
  } 
  public Vector allAttributesUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).allAttributesUsedIn()); 
    } 
    return res; 
  } 
  public Vector equivalentsUsedIn()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).equivalentsUsedIn()); 
    } 
    return res; 
  } 
  public Vector metavariables()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.addAll(((Statement) mm.dest).metavariables()); 
    } 
    return res; 
  } 
  public Vector cgparameters()
  { Vector res = new Vector(); 
    for (int i = 0; i < cases.elements.size(); i++) 
    { Maplet mm = (Maplet) cases.elements.get(i); 
      res.add(mm.dest); 
    } 
    return res; 
  } 
  public void findClones(java.util.Map clones, String op, String rule)
  { return; } 
  public void findClones(java.util.Map clones, 
                         java.util.Map cdefs, 
                         String op, String rule)
  { return; } 
}