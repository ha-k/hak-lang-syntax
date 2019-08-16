//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

/**
 * This is the class of nonterminal symbols used by the parser
 * at parse time.
 *
 * @see         ParserSymbol
 *
 * @version	Last modified on Fri Apr 13 20:10:24 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright	&copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserNonTerminal extends ParserSymbol
{
  public ParserNonTerminal(String name, int index)
    { super(name,GenericParser.nonterminals,index);
    }
}
