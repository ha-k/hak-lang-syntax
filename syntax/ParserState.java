//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import hlt.language.util.ArrayIndexed;

/**
 * This is the class of states of the parsing automaton used at
 * parse time.
 *
 * @version     Last modified on Fri Apr 13 20:11:16 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserState extends ArrayIndexed
{
  public ParserState (int index)
    {
      super(GenericParser.states, index);
    }

  HashMap actionTable;
  HashMap gotoTable;

  /**
   * An array of arrays of actions.
   */
  ParserAction[][] dynamicActions;

  public final void setTables (HashMap actionTable, HashMap gotoTable)
    {
      this.actionTable = actionTable;
      this.gotoTable = gotoTable;
    }

  public final ParserAction getAction (ParserTerminal symbol)
    {      
      return (ParserAction)actionTable.get(symbol);
    }

  public final ParserState getGoto (ParserNonTerminal symbol)
    {
      return (ParserState)gotoTable.get(symbol);
    }

}
