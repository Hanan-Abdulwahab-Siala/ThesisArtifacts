public class Type extends ModelElement
{
}
class EnumLiteral
{ String value = ""; 
  EnumLiteral(String v)
  { value = v; } 
  public String toString()
  { return value; } 
  public String cg(CGSpec cgs)
  { String typetext = this + "";
    Vector args = new Vector();
    args.add(typetext); 
    CGRule r = cgs.matchedEnumerationRule(this,typetext);
    if (r != null)
    { return r.applyRule(args); }
    return typetext;
  }
} 