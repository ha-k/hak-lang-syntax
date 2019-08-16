//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.TimeStamped;

/**
 * This is the type of elements pushed on the dynamic parser's trail
 * stack for undoing reductions and semantic actions effects when
 * backtracking over dynamic operators tokens. It consists of a pair
 * &#9001;handle,rule&#9002;, where the handle is the sequence of parser
 * stack elements corresponding to the RHS of the rule, which was popped
 * off the parser stack upon reduction using the rule.
 *
 * @see         DynamicParser
 *
 * @version     Last modified on Tue Nov 20 13:36:17 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class TrailEntry implements TimeStamped
{
  /**
   * This is the sequence of parser stack elements popped by the
   * reduction using this entry's rule.
   */
  public ParserStackElement[] handle () { return handle; }
  protected ParserStackElement[] handle;

  /**
   * The reduction rule.
   */
  public ParserRule rule () { return rule; }
  protected ParserRule rule;

  /**
   * The time stamp of the current choice point.
   */
  private long stamp;

  TrailEntry (ParserStackElement[] handle, ParserRule rule)
    {
      this.handle = handle;
      this.rule = rule;
    }

  public final long getTimeStamp ()
    {
      return stamp;
    }

  public final void setTimeStamp (long stamp)
    {
      this.stamp = stamp;
    }

  public String toString ()
    {
      String s = "(stamp:"+stamp+") " + rule.toString();
      for (int i=0; i<rule.length; i++) s += handle[i].getNode() + " ";
      return s;
    }
}
