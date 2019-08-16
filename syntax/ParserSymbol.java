//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayIndexed;
import hlt.language.util.Named;

/**
 * This class is a representation of a grammar symbol at parse time,
 * whether terminal or nonterminal.
 *
 * @see		ParserTerminal
 * @see		ParserNonTerminal
 *
 * @version	Last modified on Fri Apr 13 20:11:25 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright	&copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserSymbol extends ArrayIndexed implements Named
{
  ParserSymbol(String name, ParserSymbol[] set)
    {
      super(set);
      _name = name.intern();      
    }

  ParserSymbol(String name, ParserSymbol[] set, int index)
    {
      super(set,index);
      _name = name.intern();      
    }

  /**
   * The name of the symbol.
   */
  private String _name;

  public String name()
    {
      return _name;
    }

  public String toString()
    {
      return _name;
    }
}
