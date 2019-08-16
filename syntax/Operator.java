//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.Named;
import hlt.language.util.Comparable;

/**
 * This class is the type of tokens that are dynamic operators.
 * It is used by the grammar at parser construction time.
 *
 * @see         OperatorSymbol
 *
 * @version     Last modified on Fri Apr 13 20:06:03 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Operator extends OperatorSymbol
{
  Operator (String name, NonTerminal category, int precedence, String specifier)
    throws NonFatalParseErrorException
    {
      super(name,grammar.operators,precedence,specifier);
      this.category = category;
    }

  Operator (String name, NonTerminal category,
	    int precedence, int associativity, int fixity)
    {
      super(name,grammar.operators,precedence,associativity,fixity);
      this.category = category;
    }

  NonTerminal category;

  NonTerminal category ()
    {
      return category;
    }

  /**   
   * Operators are ordered by category names, and precedences in their categories.
   * Note: this overrides <tt>Symbol.lessThan(Comparable)</tt>.
   */
  public boolean lessThan (Comparable other)
    {
      if (!(other instanceof Operator))
	return false;

      Operator op = (Operator)other;
      int comparison = category.name().compareToIgnoreCase(op.category().name());

      if (comparison < 0)
	return true;
      if (comparison > 0)
	return false;

      if (precedence < op.precedence())
	return true;
      if (precedence > op.precedence())
	return false;

      return name.compareToIgnoreCase(op.name()) < 0;
    }

}
