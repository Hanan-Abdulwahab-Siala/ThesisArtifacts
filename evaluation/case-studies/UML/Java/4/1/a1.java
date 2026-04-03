abstract class Statement implements Cloneable
{ private int indent = 0; 
  protected Entity entity = null;  
  protected boolean brackets = false; 
  protected boolean unusedStatement = false; 
  public static final int WHILE = 0; 
  public static final int FOR = 1; 
  public static final int REPEAT = 2; 
  public static final int NORMAL = 0; 
  public static final int CONTINUE = 1; 
  public static final int BREAK = 2;
  public static final int RETURN = 3; 
  public static final int EXCEPTION = 4; 
  public static final String[] spaces = { "", "  ", "    ", "      ", "        ", "          ", "            ", "              ", "                ", "                  ", "                    " }; 
  public void setEntity(Entity e)
  { entity = e; } 
  public void setBrackets(boolean b)
  { brackets = b; } 
  public boolean hasBrackets()
  { return brackets; } 
  abstract protected Object clone(); 
  abstract void display(); 
  abstract String getOperator(); 
  public static boolean isOclBasicStatement(Statement st)
  { if (st instanceof ContinueStatement) 
    { return true; } 
    if (st instanceof BreakStatement) 
    { return true; } 
    if (st instanceof ReturnStatement) 
    { ReturnStatement rt = (ReturnStatement) st; 
      if (rt.getExpression() == null) 
      { return true; } 
    } 
    return false; 
  } 
  public static boolean isCumulativeRecursion(
                    BehaviouralFeature bf, Statement stat)
  { 
    if (stat == null) 
    { return false; }
    Vector pars = bf.getParameters(); 
    if (pars.size() >= 1) 
    {  } 
    else 
    { return false; } 
    Vector parnames = VectorUtil.getStrings(pars); 
    Vector newvars = new Vector(); 
    Vector wrfr = stat.writeFrame();
    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { 
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return false; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return false; 
      } 
      else 
      { newvars.add(wrv); } 
    }  
    System.out.println(">> local variables " + newvars + " are written in " + stat); 
    Attribute par = (Attribute) pars.get(0); 
    Type partype = par.getType(); 
    if ("int".equals(partype.getName()) || 
        "long".equals(partype.getName()))
    { } 
    else 
    { return false; } 
    String pname = par.getName(); 
    String nme = bf.getName(); 
    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 
    if (stat instanceof ConditionalStatement) 
    { ConditionalStatement conds = 
        (ConditionalStatement) stat; 
      Expression tst = conds.getTest(); 
      Statement statif = conds.getIf();
      Statement statelse = conds.getElse();
      boolean boundedAbove = false; 
      boolean boundedBelow = false; 
      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedAbove(pname) 
          &&
          statif instanceof ReturnStatement)
      { Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector ifvars = 
          statif.variablesUsedIn(names2); 
        if (ifvars.contains(nme) || ifvars.contains(pname))
        { return false; }
        boundedAbove = true; 
      } 
      else 
      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedBelow(pname) 
          &&
          statelse instanceof ReturnStatement)
      { Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector elsevars = 
          statelse.variablesUsedIn(names2); 
        if (elsevars.contains(nme) || 
            elsevars.contains(pname))
        { return false; }
        boundedBelow = true; 
      } 
      else 
      { return false; } 
    }       
    Vector rets = getReturnValues(stat); 
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  
    Vector semitails = new Vector(); 
    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCallDecrement(bf, pname))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) 
           expr).isSemiTailRecursionDecrement(bf, pname)) 
      { semitail++; 
        semitails.add(expr); 
      } 
      else 
      { nontail++; } 
    } 
    System.err.println(">> " + bf + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive decrement returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive decrement returns: " + 
         semitails);
    if (nonrecursive == 1 &&  
        semitail >= 1 && nontail == 0)
    { if (BinaryExpression.allOperatorsSame(semitails))
      { BinaryExpression rec = 
                   (BinaryExpression) semitails.get(0);
        String op = rec.getOperator(); 
        if ("+".equals(op) || "*".equals(op))
        { return true; } 
      } 
    }  
    return false;  
  } 
  public static Expression conditionalBranches2Expressions(
      Statement st, BehaviouralFeature bf, String op, String par,
      Vector nonrecs, Vector tailrecs, Vector semirecs) 
  { 
    if (st instanceof ReturnStatement) 
    { ReturnStatement rs = (ReturnStatement) st; 
      Expression retval = rs.getValue(); 
      if (nonrecs.contains(retval))
      { return retval; } 
      if (tailrecs.contains(retval))
      { 
        Expression ifvalue = null; 
        if ("+".equals(op))
        { ifvalue = new BasicExpression(0); } 
        else
        { ifvalue = new BasicExpression(1); } 
        return ifvalue;
      } 
      if (semirecs.contains(retval) && 
          retval instanceof BinaryExpression)
      { 
        return 
          ((BinaryExpression) 
              retval).replacedSemiTailRecursionDecrement(
                                                   bf, par);
      } 
    }
    if (st instanceof ConditionalStatement)
    { ConditionalStatement cs = (ConditionalStatement) st;
      Expression tst = cs.getTest();
      Statement ifc = cs.getIf(); 
      Statement elsec = cs.getElse(); 
      Expression ifexpr = 
        Statement.conditionalBranches2Expressions(ifc, 
                 bf, op, par, nonrecs, tailrecs, semirecs);
      Expression remainder = 
        Statement.conditionalBranches2Expressions(
                 elsec, bf, op, par, 
                 nonrecs, tailrecs, semirecs); 
      return 
         new ConditionalExpression(tst, ifexpr, remainder);
    }
    return null; 
  } 
  public static Statement simplifyCumulativeRecursion(
                     BehaviouralFeature bf, Statement stat)
  { 
    Vector pars = bf.getParameters(); 
    if (pars.size() >= 1) 
    {  } 
    else 
    { return stat; } 
    Vector parnames = VectorUtil.getStrings(pars); 
    Vector newvars = new Vector(); 
    Vector wrfr = stat.writeFrame();
    for (int i = 0; i < wrfr.size(); i++) 
    { String wrv = (String) wrfr.get(i); 
      int k = wrv.indexOf("::"); 
      if (k >= 0) 
      { 
        System.err.println("! " + bf + 
                           " updates attribute " + wrv); 
        return stat; 
      } 
      else if (parnames.contains(wrv))
      { System.err.println("! " + bf + 
                           " updates parameter " + wrv); 
        return stat; 
      } 
      else 
      { newvars.add(wrv); } 
    }  
    Attribute par = bf.getParameter(0); 
    String pname = par.getName(); 
    String nme = bf.getName(); 
    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 
    ConditionalStatement conds = 
        (ConditionalStatement) stat; 
    BinaryExpression tst = (BinaryExpression) conds.getTest(); 
    Statement statif = conds.getIf();
    Statement statelse = conds.getElse();
    Expression n0 = null;  
    Expression expr0 = null;  
    Expression iterbound = null;   
    boolean boundedAbove = false; 
    boolean boundedBelow = false; 
    if (tst instanceof BinaryExpression && 
        ((BinaryExpression) tst).variableBoundedAbove(pname) &&
        statif instanceof ReturnStatement)
    { BinaryExpression btest = (BinaryExpression) tst; 
      Vector names2 = new Vector(); 
      names2.add(nme); 
      names2.add(pname); 
      Vector ifvars = 
          statif.variablesUsedIn(names2); 
      if (ifvars.contains(nme) || ifvars.contains(pname))
      { return stat; }
      boundedAbove = true;
      n0 = btest.variableBoundAbove(pname);
      iterbound = btest.iterationBoundAbove(pname); 
      expr0 = ((ReturnStatement) statif).getValue();  
    } 
    else 
      if (tst instanceof BinaryExpression && 
          ((BinaryExpression) tst).variableBoundedBelow(pname) &&
          statelse instanceof ReturnStatement)
      { BinaryExpression btest = (BinaryExpression) tst; 
        Vector names2 = new Vector(); 
        names2.add(nme); 
        names2.add(pname); 
        Vector elsevars = 
          statelse.variablesUsedIn(names2); 
        if (elsevars.contains(nme) || 
            elsevars.contains(pname))
        { return stat; }
        boundedBelow = true; 
        n0 = btest.variableBoundBelow(pname);
        iterbound = btest.iterationBoundBelow(pname); 
        expr0 = ((ReturnStatement) statelse).getValue();  
      } 
      else 
      { return stat; }       
    Vector rangepars = new Vector(); 
    rangepars.add(n0); 
    Expression parexpr = new BasicExpression(par);  
    rangepars.add(parexpr); 
    Vector pars1 = new Vector(); 
    pars1.add(iterbound); 
    pars1.add(parexpr); 
    Vector rets = getReturnValues(stat); 
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  
    Vector nonrecs = new Vector(); 
    Vector tailrecs = new Vector(); 
    Vector semirecs = new Vector(); 
    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++;
        nonrecs.add(expr); 
      } 
      else if (expr.isSelfCallDecrement(bf,pname))
      { tailrecursive++; 
        tailrecs.add(expr); 
      } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) 
             expr).isSemiTailRecursionDecrement(bf,pname)) 
      { semitail++; 
        semirecs.add(expr); 
      } 
      else 
      { nontail++; } 
    } 
    if (semitail == 1 && tailrecursive == 0)
    { 
      BinaryExpression rec = 
                (BinaryExpression) semirecs.get(0);
      String op = rec.getOperator(); 
      Expression expr = 
        rec.replacedSemiTailRecursionDecrement(bf,pname); 
      Expression subrange = 
        BasicExpression.newFunctionBasicExpression(
             "subrange", "Integer", rangepars);
      Type subrangetype = new Type("Sequence", null); 
      subrangetype.setElementType(new Type("int", null)); 
      subrange.setType(subrangetype); 
      Expression rng = 
        new BinaryExpression(":", parexpr, subrange);  
      Expression col = 
        new BinaryExpression("|C", rng, expr); 
      if (op.equals("+"))
      { Expression sumexpr = 
          new UnaryExpression("->sum", col); 
        Expression res = 
          new BinaryExpression("+", expr0, sumexpr); 
        return new ReturnStatement(res); 
      } 
      if (op.equals("*"))
      { Expression prdexpr = 
          new UnaryExpression("->prd", col); 
        Expression res = 
          new BinaryExpression("*", expr0, prdexpr); 
        return new ReturnStatement(res); 
      } 
    }
    else if (semitail >= 1) 
    { BinaryExpression rec = 
          (BinaryExpression) semirecs.get(0);
      String op = rec.getOperator(); 
      Expression collectexpr = 
        Statement.conditionalBranches2Expressions(
                        stat, bf, op, 
                        pname, nonrecs, tailrecs, semirecs); 
      Expression subrange = 
        BasicExpression.newFunctionBasicExpression("subrange", 
                                            "Integer", pars1);
      Type subrangetype = new Type("Sequence", null); 
      subrangetype.setElementType(new Type("int", null)); 
      subrange.setType(subrangetype); 
      Expression rng = 
        new BinaryExpression(":", parexpr, subrange);  
      Expression col = 
        new BinaryExpression("|C", rng, collectexpr); 
      if (op.equals("+"))
      { Expression sumexpr = 
          new UnaryExpression("->sum", col); 
        return new ReturnStatement(sumexpr); 
      } 
      if (op.equals("*"))
      { Expression prdexpr = 
          new UnaryExpression("->prd", col); 
        return new ReturnStatement(prdexpr); 
      } 
    } 
    return stat; 
  } 
}
