//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.TimeStamped;

/**
 * This class is the type of objects used for token nodes in a dynamic parser.
 *
 * @version     Last modified on Fri Apr 13 19:57:56 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class DynamicToken extends ParseNode implements TimeStamped
{
  private long stamp;
  private ParseNode original;

  public DynamicToken (ParseNode node)
    {
      super(node);
    }

  public DynamicToken (ParserOperator operator, ParseNode node)
    {
      makeOperator(operator);
      original = node;
      setSpan(original);
    }

  final boolean isDynamic ()
    {
      return true;
    }

  public final long getTimeStamp ()
    {
      return stamp;
    }

  public final void setTimeStamp (long stamp)
    {
      this.stamp = stamp;
    }

  final ParseNode getOriginal ()
    {
      return original == null ? this : original;
    }

  final void setOriginal (ParseNode node)
    {
      original = node;
    }

  public final void makeOperator (ParserOperator operator)
    {
      setSymbol(operator.subCategory);
      setSvalue(operator.name());
      setOperator(operator);
    }

//   public String stringForm ()
//      {
//        return this + "/" + stamp +" [original = " + original + "]";
//      }

//   public String toString ()
//      {
//        return super.toString()
// 	 + " (stamp: " + stamp + ")"
// 	 +" [original = " + original + "]"
// 	 ;
//      }

}
