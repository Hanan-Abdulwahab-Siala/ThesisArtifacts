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
  public int execute(ModelSpecification sigma, ModelState beta)
  { return Statement.NORMAL; } 
  public static Statement cumulativeCode(Expression var,
                                         Expression rng, 
                                         Statement st)
  { if (st == null) 
    { return null; }
    System.out.println(">> Converting cumulative code: " + var + 
                       " : " + rng + " @ " + st); 
    if (st instanceof AssignStatement)
    { 
      AssignStatement asm = (AssignStatement) st; 
      Expression lhs = asm.getLeft(); 
      Expression rhs = asm.getRight();
      rhs.setBrackets(false); 
      if ((lhs + " + " + var).equals("" + rhs))
      { 
        Expression smm = new UnaryExpression("->sum", rng); 
        Expression newrhs = 
            new BinaryExpression("+", lhs, smm); 
        return new AssignStatement(lhs, newrhs); 
      } 
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("+") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        if (VectorUtil.containsEqualString(var+"", vuses))
        { 
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->sum", coll); 
          Expression newrhs = 
            new BinaryExpression("+", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 
        Expression sze = Expression.simplifySize(rng);
        sze.setBrackets(true); 
        Expression newrhs = 
            new BinaryExpression("+", lhs, 
              new BinaryExpression("*", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " - " + var).equals("" + rhs))
      { 
        Expression smm = new UnaryExpression("->sum", rng); 
        Expression newrhs = 
            new BinaryExpression("-", lhs, smm); 
        return new AssignStatement(lhs, newrhs); 
      } 
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("-") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        if (VectorUtil.containsEqualString(var+"", vuses))
        { 
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->sum", coll); 
          Expression newrhs = 
            new BinaryExpression("-", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 
        Expression sze = Expression.simplifySize(rng);
        sze.setBrackets(true); 
        Expression newrhs = 
            new BinaryExpression("-", lhs, 
              new BinaryExpression("*", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " * " + var).equals("" + rhs))
      { 
        Expression prd = new UnaryExpression("->prd", rng); 
        Expression newrhs = 
            new BinaryExpression("*", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("*") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        if (VectorUtil.containsEqualString(var+"", vuses))
        { 
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->prd", coll); 
          Expression newrhs = 
            new BinaryExpression("*", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 
        Expression sze = Expression.simplifySize(rng);
        Expression newrhs = 
            new BinaryExpression("*", lhs, 
              new BinaryExpression("->pow", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " / " + var).equals("" + rhs))
      { 
        Expression prd = new UnaryExpression("->prd", rng); 
        Expression newrhs = 
            new BinaryExpression("/", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("/") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        if (VectorUtil.containsEqualString(var+"", vuses))
        { 
          Expression coll = 
            new BinaryExpression("|C", 
              new BinaryExpression(":", var, rng), expr); 
          Expression smm = new UnaryExpression("->prd", coll); 
          Expression newrhs = 
            new BinaryExpression("/", lhs, smm); 
          return new AssignStatement(lhs, newrhs);
        } 
        Expression sze = Expression.simplifySize(rng);
        Expression newrhs = 
            new BinaryExpression("/", lhs, 
              new BinaryExpression("->pow", expr, sze)); 
        return new AssignStatement(lhs, newrhs); 
      }         
      else if ((lhs + " & " + var).equals("" + rhs))
      { 
        Type elemt = rng.getElementType(); 
        BasicExpression selfvar = 
          BasicExpression.newVariableBasicExpression(
                                             "self",elemt); 
        Expression prd = 
          new BinaryExpression("->forAll", rng, selfvar); 
        Expression newrhs = 
            new BinaryExpression("&", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("&") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        Expression coll = 
            new BinaryExpression("!", 
              new BinaryExpression(":", var, rng), expr); 
        Expression newrhs = 
            new BinaryExpression("&", lhs, coll); 
        return new AssignStatement(lhs, newrhs);
      }         
      else if ((lhs + " or " + var).equals("" + rhs))
      { 
        Type elemt = rng.getElementType(); 
        BasicExpression selfvar = 
          BasicExpression.newVariableBasicExpression(
                                             "self",elemt); 
        Expression prd = 
          new BinaryExpression("->exists", rng, selfvar); 
        Expression newrhs = 
            new BinaryExpression("or", lhs, prd); 
        return new AssignStatement(lhs, newrhs); 
      }   
      else if (rhs instanceof BinaryExpression && 
        ((BinaryExpression) rhs).getOperator().equals("or") &&
        (((BinaryExpression) rhs).getLeft() + "").equals(lhs + ""))
      { 
        Expression expr = ((BinaryExpression) rhs).getRight(); 
        Vector vuses = expr.getVariableUses();
        if (VectorUtil.containsEqualString(lhs+"", vuses))
        { return null; } 
        Expression coll = 
            new BinaryExpression("#", 
              new BinaryExpression(":", var, rng), expr); 
        Expression newrhs = 
            new BinaryExpression("or", lhs, coll); 
        return new AssignStatement(lhs, newrhs);
      }         
    }
    else if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 0) 
      { return null; }
      if (sq.size() == 1)
      { 
        Statement stat0 = sq.getStatement(0); 
        if (stat0 instanceof SequenceStatement)
        { return Statement.cumulativeCode(var,rng,stat0); } 
        if (stat0 instanceof AssignStatement) 
        { return Statement.cumulativeCode(var,rng,stat0); }
        return null; 
      }
      if (sq.size() == 2)
      { 
        Statement stat1 = sq.getStatement(0); 
        Statement stat2 = sq.getStatement(1);
        if (stat1 instanceof AssignStatement && 
            stat2 instanceof AssignStatement)
        { AssignStatement ast1 = (AssignStatement) stat1; 
          AssignStatement ast2 = (AssignStatement) stat2;
          Expression lhs1 = ast1.getLeft(); 
          Expression rhs1 = ast1.getRight();
          Expression lhs2 = ast2.getLeft(); 
          Expression rhs2 = ast2.getRight();
          if ((var + "").equals("" + lhs1) || 
              (var + "").equals("" + lhs2))
          { return null; } 
          Vector vars1 = rhs1.getVariableUses(); 
          Vector vars2 = rhs2.getVariableUses(); 
          if (VectorUtil.containsEqualString(var + "", vars1) || 
              VectorUtil.containsEqualString(var + "", vars2))
          { return null; } 
          rhs1.setBrackets(false); 
          rhs2.setBrackets(false); 
          if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " + " + lhs1).equals("" + rhs2))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("+", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs1); 
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("+", lhs2, 
                  new BinaryExpression("+", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " + " + lhs2).equals("" + rhs1))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("-", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs2); 
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("+", lhs1, 
                  new BinaryExpression("+", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
          else if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " - " + lhs1).equals("" + rhs2))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = new BinaryExpression("+", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs1); 
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("-", lhs2, 
                  new BinaryExpression("-", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " - " + lhs2).equals("" + rhs1))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
               new BinaryExpression("-", rsize, 
                    new BasicExpression(1));
            rsize1.setBrackets(true);  
            Expression prd = new BinaryExpression("*", rsize,
                                                  rsize1);
            prd.setBrackets(true);  
            BinaryExpression isum = 
              new BinaryExpression("/", prd, 
                                   new BasicExpression(2));
            BinaryExpression nsum = 
              new BinaryExpression("*", rsize, lhs2); 
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("-", lhs1, 
                  new BinaryExpression("-", nsum, isum)));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
          else if ((lhs1 + " + 1").equals("" + rhs1) && 
              (lhs2 + " * " + lhs1).equals("" + rhs2))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
                         new BinaryExpression("+", lhs1, rsize);
            Expression fact1 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", rsize1);
            Expression fact2 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", lhs1);
            BinaryExpression idiv = 
              new BinaryExpression("/", fact1, fact2);
            AssignStatement asn1 = 
              new AssignStatement(lhs2,
                new BinaryExpression("*", lhs2, idiv));
            AssignStatement asn2 = 
              new AssignStatement(lhs1, 
                new BinaryExpression("+", lhs1, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
          else if ((lhs2 + " + 1").equals("" + rhs2) && 
              (lhs1 + " * " + lhs2).equals("" + rhs1))
          { 
            Expression rsize = 
               Expression.simplifySize(rng);
            Expression rsize1 = 
               new BinaryExpression("+", lhs2, 
                    new BinaryExpression("-", rsize, 
                           new BasicExpression(1)));
            Expression fact1 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", rsize1);
            Expression fact2 = 
              BasicExpression.newStaticCallExpression("MathLib",
                                            "factorial", lhs2);
            BinaryExpression idiv = 
              new BinaryExpression("/", fact1, fact2);
            AssignStatement asn1 = 
              new AssignStatement(lhs1,
                new BinaryExpression("*", lhs1, idiv));
            AssignStatement asn2 = 
              new AssignStatement(lhs2, 
                new BinaryExpression("+", lhs2, rsize)); 
            SequenceStatement ss = new SequenceStatement(); 
            ss.addStatement(asn1); 
            ss.addStatement(asn2);
            return ss;    
          } 
        }
      }
    } 
    return null; 
  }
  public static boolean isAdditionToCollection(Statement stat, Expression x, Expression st)
  { if (stat instanceof AssignStatement) 
    { AssignStatement ifassign = (AssignStatement) stat; 
      Expression ifvar = ifassign.getLhs(); 
      ifvar.setBrackets(false); 
      if (ifassign.getRhs() instanceof BinaryExpression) 
      { BinaryExpression ifbe = 
                  (BinaryExpression) ifassign.getRhs(); 
        Expression ifbeLeft = ifbe.getLeft(); 
        Expression ifbeRight = ifbe.getRight(); 
        ifbeLeft.setBrackets(false);  
        ifbeRight.setBrackets(false);  
        if ((st + "").equals(ifvar + "") && 
            (st + "").equals(ifbeLeft + "") && 
            (x + "").equals(ifbeRight + ""))
        { if (ifbe.getOperator().equals("->including") || 
              ifbe.getOperator().equals("->append"))
          { return true; } 
        } 
      } 
    } 
    else if (stat instanceof ImplicitInvocationStatement) 
    { Expression callExpr = 
        ((ImplicitInvocationStatement) stat).callExp;
      if (callExpr instanceof BinaryExpression)
      { BinaryExpression bexpr = (BinaryExpression) callExpr; 
        Expression bleft = bexpr.getLeft(); 
        Expression bright = bexpr.getRight(); 
        bleft.setBrackets(false); 
        bright.setBrackets(false); 
        if (":".equals(bexpr.getOperator()) && 
            (x + "").equals(bleft + "") && 
            (st + "").equals(bright + ""))
        { return true; } 
      }  
    } 
    return false; 
  } 
  public static boolean isCumulativeBody(Expression var,
                                         Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof AssignStatement)
    { 
      AssignStatement asm = (AssignStatement) st; 
      Expression lhs = asm.getLeft(); 
      Expression rhs = asm.getRight();
      rhs.setBrackets(false); 
      if ((lhs + " + " + var).equals("" + rhs))
      { 
        return true; 
      } 
      else if ((lhs + " - " + var).equals("" + rhs))
      { 
        return true; 
      } 
      else if ((lhs + " * " + var).equals("" + rhs))
      { 
        return true; 
      }   
      else if ((lhs + " / " + var).equals("" + rhs))
      { 
        return true; 
      } 
      else if ((lhs + " & " + var).equals("" + rhs))
      { 
        return true; 
      } 
      else if ((lhs + " or " + var).equals("" + rhs))
      { 
        return true; 
      } 
      else if (rhs instanceof BinaryExpression && 
        (((BinaryExpression) rhs).getLeft() + "").equals(
                                                    lhs + ""))
      { BinaryExpression brhs = (BinaryExpression) rhs; 
        Expression expr = brhs.getRight();
        String oper = brhs.getOperator(); 
        Vector vars = expr.getVariableUses(); 
        if (
            VectorUtil.containsEqualString(lhs + "", vars))
        { return false; } 
        if ("+".equals(oper) || "-".equals(oper) || 
            "*".equals(oper) || "/".equals(oper) ||
            "&".equals(oper) || "or".equals(oper))
        { return true; }
        return false;
      } 
    }
    else if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 0) 
      { return false; }
      if (sq.size() == 1)
      { 
        Statement stat0 = sq.getStatement(0); 
        if (stat0 instanceof SequenceStatement)
        { return Statement.isCumulativeBody(var,stat0); } 
        if (stat0 instanceof AssignStatement) 
        { return Statement.isCumulativeBody(var, stat0); }
        return false; 
      } 
      else if (sq.size() == 2)
      { 
        Statement stat1 = sq.getStatement(0); 
        Statement stat2 = sq.getStatement(1);
        if (stat1 instanceof AssignStatement && 
            stat2 instanceof AssignStatement)
        { AssignStatement ast1 = (AssignStatement) stat1; 
          AssignStatement ast2 = (AssignStatement) stat2;
          Expression lhs1 = ast1.getLeft(); 
          Expression rhs1 = ast1.getRight();
          Expression lhs2 = ast2.getLeft(); 
          Expression rhs2 = ast2.getRight();
          if ((var + "").equals("" + lhs1) || 
              (var + "").equals("" + lhs2))
          { return false; } 
          Vector vars1 = rhs1.getVariableUses(); 
          Vector vars2 = rhs2.getVariableUses(); 
          if (VectorUtil.containsEqualString(var + "", vars1) || 
              VectorUtil.containsEqualString(var + "", vars2))
          { return false; } 
          return true; 
        } 
      }  
    } 
    return false; 
  } 
  public static boolean isEmpty(Statement st)
  { if (st == null) { return true; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 0) 
      { return true; } 
    } 
    return false; 
  } 
  public static boolean isPathEnd(Statement st) 
  { if (isSingleReturnStatement(st))
    { return true; } 
    if (isSingleBreakStatement(st))
    { return true; } 
    return false; 
  } 
  public static void addBeforeEnd(Statement blk, Statement st)
  { if (blk instanceof SequenceStatement && st != null) 
    { SequenceStatement ss = (SequenceStatement) blk; 
      ss.addBeforeEnd(st); 
    } 
  } 
  public static boolean isSingleReturnStatement(Statement st)
  { if (st == null) 
    { return false; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof ReturnStatement ||
            isSingleReturnStatement(stat)) 
        { return true; }
      } 
      return false;  
    } 
    return (st instanceof ReturnStatement); 
  } 
  public static boolean isSingleBreakStatement(Statement st)
  { if (st == null) 
    { return false; } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof BreakStatement || 
            isSingleBreakStatement(stat)) 
        { return true; }
      } 
      return false;  
    } 
    return (st instanceof BreakStatement); 
  } 
  public static Expression getReturnExpression(Statement st)
  { if (st == null) 
    { return new BasicExpression("null"); }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { Statement stat = sq.getStatement(0); 
        if (stat instanceof ReturnStatement) 
        { return getReturnExpression(stat); }
      } 
      return new BasicExpression("null");
    } 
    if (st instanceof ReturnStatement)
    { ReturnStatement ret = (ReturnStatement) st; 
      Expression res = ret.getExpression(); 
      if (res == null) 
      { return new BasicExpression("null"); }
      return res; 
    } 
    return new BasicExpression("null");
  } 
  public static Statement getFirstStatement(Statement st)
  { if (st == null) 
    { return null; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() >= 1) 
      { Statement stat = sq.getStatement(0); 
        return Statement.getFirstStatement(stat); 
      } 
      return null;
    } 
    return st; 
  } 
  public static boolean hasSingleStatement(Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      if (sq.size() == 1) 
      { return true; } 
      return false;
    } 
    return true; 
  } 
  public static Vector getReturnValues(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getReturnValues(stat));
        }  
      } 
      return res;
    } 
    if (st instanceof ReturnStatement)
    { ReturnStatement ret = (ReturnStatement) st; 
      Expression retExpr = ret.getExpression(); 
      if (retExpr == null) 
      { return res; }
      res.add(retExpr); 
      return res; 
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getReturnValues(cs.ifPart())); 
      res.addAll(getReturnValues(cs.elsePart())); 
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getReturnValues(ws.getLoopBody())); 
      return res; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getReturnValues(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getReturnValues(stat));
        }  
      } 
      res.addAll(getReturnValues(ts.getEndStatement())); 
    } 
    return res;
  } 
  public static boolean hasLoopStatement(Statement st)
  { boolean res = false; 
    if (st == null) 
    { return res; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          if (Statement.hasLoopStatement(stat))
          { return true; }
        }  
      } 
      return res;
    } 
    if (st instanceof ReturnStatement)
    { return res; } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.hasLoopStatement(cs.ifPart()))
      { return true; }  
      return Statement.hasLoopStatement(cs.elsePart());
    } 
    if (st instanceof WhileStatement) 
    { return true; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.hasLoopStatement(ts.getBody()))
      { return true; } 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          if (Statement.hasLoopStatement(stat))
          { return true; } 
        }  
      } 
      return Statement.hasLoopStatement(ts.getEndStatement()); 
    } 
    return res;
  } 
  public Vector allVariableNames()
  { return new Vector(); } 
  public abstract Statement optimiseOCL();
  public Expression definedness()
  { return new BasicExpression(true); } 
  public abstract Map energyUse(Map uses, 
                                Vector rUses, Vector oUses);
  public abstract java.util.Map collectionOperatorUses(
                             int nestingLevel, 
                             java.util.Map operatorsAtLevel, 
                             Vector vars);
  public static boolean isSemiTailRecursive(
            BehaviouralFeature bf, String nme, Statement st)
  { 
    Vector pars = bf.getParameters(); 
    Vector parnames = VectorUtil.getStrings(pars); 
    Vector newvars = new Vector(); 
    Vector wrfr = st.writeFrame();
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
    System.out.println(">> Local variables of " + st + 
                       " are " + newvars); 
    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 
    Vector rets = getReturnValues(st); 
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  
    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCall(bf))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) expr).isSemiTailRecursion(bf)) 
      { semitail++; } 
      else 
      { nontail++; } 
    } 
    System.err.println(">> " + nme + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive returns: " + rets);
    if (nonrecursive == 1 && semitail == 1 && nontail == 0)
    { return true; } 
    return false;  
  } 
  public static boolean isTailRecursion(
            BehaviouralFeature bf, String nme, Statement st)
  { 
    Vector pars = bf.getParameters(); 
    Vector parnames = VectorUtil.getStrings(pars); 
    Vector newvars = new Vector(); 
    Vector wrfr = st.writeFrame();
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
    System.out.println(">> Local variables of " + st + 
                       " are " + newvars); 
    Vector names = new Vector(); 
    names.add(nme); 
    names.addAll(newvars); 
    Vector rets = getReturnValues(st); 
    int nontail = 0; 
    int nonrecursive = 0; 
    int tailrecursive = 0;
    int semitail = 0;  
    for (int i = 0; i < rets.size(); i++) 
    { Expression expr = (Expression) rets.get(i); 
      Vector uses = expr.variablesUsedIn(names); 
      if (uses.size() == 0) 
      { nonrecursive++; } 
      else if (expr.isSelfCall(bf))
      { tailrecursive++; } 
      else if (expr instanceof BinaryExpression && 
        ((BinaryExpression) expr).isSemiTailRecursion(bf)) 
      { semitail++; } 
      else 
      { nontail++; } 
    } 
    System.err.println(">> " + nme + " has " + 
         nonrecursive + " non-recursive returns, " + 
         tailrecursive + " tail recursive returns,\n>>" + 
         " and " + 
         nontail + " non-tail recursive returns,\n>> " + 
         semitail + " semi-tail recursive returns: " + rets);
    if (semitail == 0 && nontail == 0)
    { return true; } 
    return false;  
  } 
  public static boolean isTailRecursive(
            BehaviouralFeature bf, String nme, Statement st)
  { 
    if (st == null) 
    { return true; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      for (int i = 0; i < stats.size(); i++) 
      { Statement stat = (Statement) stats.get(i); 
        if (stat.isTailRecursive(bf,nme,stat)) { } 
        else 
        { return false; } 
      } 
      return true; 
    } 
    Vector names = new Vector(); 
    names.add(nme); 
    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      Expression expr = invok.getCallExp();
      Vector vars1 =
        expr.variablesUsedIn(names);
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      else if (expr != null && vars1.size() > 0)
      { return false; } 
      return true; 
    } 
    if (st instanceof ImplicitInvocationStatement)
    { ImplicitInvocationStatement invok = 
        (ImplicitInvocationStatement) st;
      Expression expr = invok.getCallExp();
      Vector vars1 =
        expr.variablesUsedIn(names);
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      else if (expr != null && vars1.size() > 0)
      { return false; } 
      return true; 
    } 
    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      Expression expr = retstat.getReturnValue();
      if (expr == null) { return true; } 
      Vector vars1 =
        expr.variablesUsedIn(names);
      if (expr.isSelfCall(bf))
      { return true; } 
      if (vars1.size() > 0)
      { return false; } 
      return true; 
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.isTailRecursive(bf,nme,cs.ifPart()))
      { return Statement.isTailRecursive(
                                 bf,nme,cs.elsePart()); 
      } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.isTailRecursive(bf,nme,ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.isTailRecursive(bf,nme,stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }
      if (ts.getEndStatement() == null) 
      { return true; } 
      return Statement.isTailRecursive(
                               bf,nme,ts.getEndStatement()); 
    } 
    return true;
  } 
  public static boolean endsWithSelfCall(
            BehaviouralFeature bf, String nme, Statement st)
  { 
    if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithSelfCall(bf,nme,stat);
    } 
    if (st instanceof InvocationStatement)
    { InvocationStatement invok = 
        (InvocationStatement) st;
      Expression expr = invok.getCallExp();
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      return false; 
    } 
    if (st instanceof ReturnStatement)
    { ReturnStatement retstat = (ReturnStatement) st; 
      Expression expr = retstat.getReturnValue();
      if (expr != null && expr.isSelfCall(bf))
      { return true; } 
      return false; 
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithSelfCall(bf,nme,cs.ifPart()))
      { return Statement.endsWithSelfCall(
                                 bf,nme,cs.elsePart()); 
      } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.endsWithSelfCall(bf,nme,ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithSelfCall(bf,nme,stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }
      if (ts.getEndStatement() == null) 
      { return false; } 
      return Statement.endsWithSelfCall(
                               bf,nme,ts.getEndStatement()); 
    } 
    return false;
  } 
  public static boolean endsWithReturn(Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements();
      if (stats.size() == 0) 
      { return false; } 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithReturn(stat);
    } 
    if (st instanceof ReturnStatement)
    { return true; } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithReturn(cs.ifPart()))
      { return Statement.endsWithReturn(cs.elsePart()); } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.endsWithReturn(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithReturn(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithReturn(ts.getEndStatement()); 
    } 
    return false;
  } 
  public static boolean endsWithContinue(Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithContinue(stat);
    } 
    if (st instanceof ContinueStatement)
    { return true; } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithContinue(cs.ifPart()))
      { return Statement.endsWithContinue(cs.elsePart()); } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.endsWithContinue(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithContinue(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithContinue(ts.getEndStatement()); 
    } 
    return false;
  } 
  public static boolean endsWithBreak(Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithBreak(stat);
    } 
    if (st instanceof BreakStatement)
    { return true; } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithBreak(cs.ifPart()))
      { return Statement.endsWithBreak(cs.elsePart()); } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.endsWithBreak(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithBreak(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithBreak(ts.getEndStatement()); 
    } 
    return false;
  } 
  public static boolean endsWithExit(Statement st)
  { if (st == null) 
    { return false; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      if (stats.size() == 0) 
      { return false; } 
      Statement stat = (Statement) stats.get(stats.size()-1); 
      return Statement.endsWithExit(stat);
    } 
    if (st instanceof InvocationStatement)
    { String called = 
        ((InvocationStatement) st).calledOperation(); 
      if ("exit".equals(called))
      { return true; }
      return false; 
    }  
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      if (Statement.endsWithExit(cs.ifPart()))
      { return Statement.endsWithExit(cs.elsePart()); } 
      return false; 
    } 
    if (st instanceof WhileStatement) 
    { return false; } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      if (Statement.endsWithExit(ts.getBody())) 
      { Vector stats = ts.getClauses(); 
        for (int i = 0; i < stats.size(); i++) 
        { if (stats.get(i) instanceof Statement)
          { Statement stat = (Statement) stats.get(i); 
            if (Statement.endsWithExit(stat)) { } 
            else 
            { return false; } 
          }
          else 
          { return false; } 
        }  
      }
      else 
      { return false; }  
      if (ts.getEndStatement() == null) { return false; } 
      return Statement.endsWithExit(ts.getEndStatement()); 
    } 
    return false;
  } 
  public static boolean endsWithControlFlowBreak(Statement stat)
  { if (Statement.endsWithReturn(stat))
    { return true; } 
    if (Statement.endsWithBreak(stat))
    { return true; } 
    if (Statement.endsWithContinue(stat))
    { return true; } 
    return Statement.endsWithExit(stat); 
  } 
  public static boolean isControlFlowEnd(Statement stat)
  { if (Statement.hasSingleStatement(stat)) { } 
    else 
    { return false; } 
    return Statement.endsWithControlFlowBreak(stat); 
  } 
  public static Statement replaceReturnBySkip(Statement st)
  { if (st == null) 
    { return st; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector newstats = new Vector(); 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceReturnBySkip(stat);
          newstats.add(newstat); 
        }  
      } 
      SequenceStatement newsq = 
            new SequenceStatement(newstats);
      newsq.setBrackets(sq.hasBrackets());  
      return newsq; 
    } 
    if (st instanceof ReturnStatement)
    { return new InvocationStatement("skip"); } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement newif = 
         Statement.replaceReturnBySkip(cs.ifPart()); 
      Statement newelse = 
         Statement.replaceReturnBySkip(cs.elsePart());
      ConditionalStatement res = 
         new ConditionalStatement(cs.getTest(), 
                                  newif, newelse);  
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      Statement newbody = 
         Statement.replaceReturnBySkip(ws.getLoopBody());
      WhileStatement wsnew = 
        new WhileStatement(ws.getTest(), newbody); 
      wsnew.loopKind = ws.loopKind;  
      wsnew.loopVar = ws.loopVar;
      wsnew.loopRange = ws.loopRange;
      return wsnew; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement newbody = 
         Statement.replaceReturnBySkip(ts.getBody());
      Vector newclauses = new Vector();  
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
             Statement.replaceReturnBySkip(stat);
          newclauses.add(newstat); 
        }  
      } 
      Statement newend = 
         Statement.replaceReturnBySkip(ts.getEndStatement()); 
      TryStatement newtry = 
         new TryStatement(newbody, newclauses, newend); 
      return newtry; 
    } 
    return st;
  } 
  public static Statement replaceElseBySequence(Statement st)
  { if (st == null) 
    { return st; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector newstats = new Vector(); 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
            Statement.replaceElseBySequence(stat);
          newstats.add(newstat); 
        }  
      } 
      SequenceStatement newsq = 
            new SequenceStatement(newstats);
      newsq.setBrackets(sq.hasBrackets());  
      return newsq; 
    } 
    if (st instanceof ReturnStatement)
    { return st; } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      Statement newif = 
         Statement.replaceElseBySequence(cs.ifPart()); 
      Statement newelse = 
         Statement.replaceElseBySequence(cs.elsePart());
      if (Statement.endsWithReturn(newif))
      { ConditionalStatement res = 
          new ConditionalStatement(cs.getTest(), 
                newif, 
                new InvocationStatement("skip"));
        SequenceStatement ss = new SequenceStatement(); 
        ss.addStatement(res); 
        ss.addStatement(newelse); 
        return ss; 
      } 
      else 
      { ConditionalStatement res = 
          new ConditionalStatement(cs.getTest(), 
                newif, 
                newelse);
        return res;
      } 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      Statement newbody = 
         Statement.replaceElseBySequence(ws.getLoopBody());
      WhileStatement wsnew = 
        new WhileStatement(ws.getTest(), newbody); 
      wsnew.loopKind = ws.loopKind;  
      wsnew.loopVar = ws.loopVar;
      wsnew.loopRange = ws.loopRange;
      return wsnew; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      Statement newbody = 
         Statement.replaceElseBySequence(ts.getBody());
      Vector newclauses = new Vector();  
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          Statement newstat = 
             Statement.replaceElseBySequence(stat);
          newclauses.add(newstat); 
        }  
      } 
      Statement newend = 
         Statement.replaceElseBySequence(
                             ts.getEndStatement()); 
      TryStatement newtry = 
         new TryStatement(newbody, newclauses, newend); 
      return newtry; 
    } 
    return st;
  } 
  public static Statement tryInsertCloneDeclaration(
                         Statement st,
                         Expression expr, Type typ, Type et)
  { 
    if (st == null || expr == null) 
    { return st; }
    if (typ == null) 
    { typ = new Type("OclAny", null); } 
    String vname = 
        Identifier.nextIdentifier("factored_expr");
    CreationStatement dec = 
      CreationStatement.newCreationStatement(vname,typ,expr);
    dec.setElementType(et); 
    BasicExpression var = 
      BasicExpression.newVariableBasicExpression(vname,typ); 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.flattenSequenceStatement(); 
      Vector precedingStats = new Vector(); 
      for (int i = 0; i < stats.size(); i++) 
      { Statement stat = (Statement) stats.get(i); 
        if (stat.containsSubexpression(expr))
        { 
          precedingStats.add(dec);
          for (int j = i; j < stats.size(); j++) 
          { Statement ss = 
                 (Statement) stats.get(j); 
            Statement newstat = 
                        ss.substituteEq(expr + "", var); 
            precedingStats.add(newstat);
          }  
          SequenceStatement remStat = 
              new SequenceStatement(precedingStats);
          return remStat;  
        } 
        else 
        { precedingStats.add(stat); }   
      } 
      return st; 
    } 
    Statement newstat = st.substituteEq(expr + "", var); 
    Vector newstats = new Vector(); 
    newstats.add(dec); 
    newstats.add(newstat);
    return new SequenceStatement(newstats); 
  }  
  public static Vector getLocalDeclarations(Statement st)
  { 
    Vector res = new Vector(); 
    if (st == null) 
    { return res; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getLocalDeclarations(stat));
        }  
      } 
      return res;
    } 
    if (st instanceof CreationStatement)
    { res.add(st); 
      return res; 
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getLocalDeclarations(cs.ifPart())); 
      res.addAll(getLocalDeclarations(cs.elsePart())); 
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getLocalDeclarations(ws.getLoopBody())); 
      return res; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getLocalDeclarations(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getLocalDeclarations(stat));
        }  
      } 
      res.addAll(getLocalDeclarations(ts.getEndStatement())); 
    } 
    return res;
  } 
  public static Vector getBreaksContinues(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getBreaksContinues(stat));
        }  
      } 
      return res;
    } 
    if (st instanceof ContinueStatement)
    { res.add(st); 
      return res; 
    } 
    if (st instanceof BreakStatement)
    { res.add(st); 
      return res; 
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getBreaksContinues(cs.ifPart())); 
      res.addAll(getBreaksContinues(cs.elsePart())); 
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getBreaksContinues(ws.getLoopBody())); 
      return res; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getBreaksContinues(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getBreaksContinues(stat));
        }  
      } 
      res.addAll(getBreaksContinues(ts.getEndStatement())); 
    } 
    return res;
  } 
  public static Vector getAssignments(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof AssignStatement)
        { res.add(stats.get(i)); } 
        else if (stats.get(i) instanceof SequenceStatement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(Statement.getAssignments(stat));
        }  
      } 
      return res;
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getAssignments(cs.ifPart())); 
      res.addAll(getAssignments(cs.elsePart())); 
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getAssignments(ws.getLoopBody())); 
      return res; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getAssignments(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getAssignments(stat));
        }  
      } 
      res.addAll(getAssignments(ts.getEndStatement())); 
    } 
    return res;
  } 
  public static Vector getOperationCalls(Statement st)
  { Vector res = new Vector(); 
    if (st == null) 
    { return res; }
    if (st instanceof InvocationStatement)
    { if ("skip".equals(st + "")) { } 
      else 
      { res.add(st); }  
      return res; 
    } 
    if (st instanceof SequenceStatement) 
    { SequenceStatement sq = (SequenceStatement) st; 
      Vector stats = sq.getStatements(); 
      for (int i = 0; i < stats.size(); i++) 
      { Statement ss = (Statement) stats.get(i); 
        res.addAll(Statement.getOperationCalls(ss)); 
      } 
      return res;
    } 
    if (st instanceof ConditionalStatement) 
    { ConditionalStatement cs = (ConditionalStatement) st; 
      res.addAll(getOperationCalls(cs.ifPart())); 
      res.addAll(getOperationCalls(cs.elsePart())); 
      return res; 
    } 
    if (st instanceof IfStatement) 
    { IfStatement cs = (IfStatement) st; 
      System.err.println("! Warning: do not use IfStatement"); 
      Statement ifpart = cs.getIfPart(); 
      if (ifpart != null) 
      { res.addAll(getOperationCalls(ifpart)); } 
      Statement elsepart = cs.getElsePart(); 
      if (elsepart != null) 
      { res.addAll(getOperationCalls(elsepart)); }  
      return res; 
    } 
    if (st instanceof WhileStatement) 
    { WhileStatement ws = (WhileStatement) st; 
      res.addAll(getOperationCalls(ws.getLoopBody())); 
      return res; 
    } 
    if (st instanceof TryStatement) 
    { TryStatement ts = (TryStatement) st; 
      res.addAll(getOperationCalls(ts.getBody())); 
      Vector stats = ts.getClauses(); 
      for (int i = 0; i < stats.size(); i++) 
      { if (stats.get(i) instanceof Statement)
        { Statement stat = (Statement) stats.get(i); 
          res.addAll(getOperationCalls(stat));
        }  
      } 
      res.addAll(getOperationCalls(ts.getEndStatement())); 
    } 
    return res;
  } 
}
