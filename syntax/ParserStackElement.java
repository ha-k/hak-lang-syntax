//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.TimeStamped;

/**
 * This implements the class of the objects pushed on the parser's stack.
 * It is simply a pair consisting of a state and a token node. For dynamic
 * parsing, a time stamp is also provided.
 *
 * @version     Last modified on Fri Apr 13 20:10:57 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */
class ParserStackElement implements TimeStamped
{
  private ParserState state;
  private ParseNode node;
  private long stamp;

  ParserStackElement (ParserState state, ParseNode node)
    {
      this.state = state;
      this.node  = node;
    }

  public final void setState (ParserState state)
    {
      this.state = state;
    }

  public final ParserState getState ()
    {
      return state;
    }

  public final void setNode (ParseNode node)
    {
      this.node  = node;
    }

  public final ParseNode getNode ()
    {
      return node;
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
      return "stamp: "+stamp+", "+"state: "+state+", "+"node: "+node;
    }
}
