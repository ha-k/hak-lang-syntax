//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.AbstractList;

import hlt.language.util.AbstractListIndexed;
import hlt.language.util.Named;
import hlt.language.util.Comparable;
import hlt.language.tools.Misc;

/**
 * This class is a representation of symbols used at
 * parser construction time.
 *
 * @see         GrammarSymbol
 * @see         OperatorSymbol
 *
 * @version     Last modified on Fri Apr 13 20:13:11 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Symbol extends AbstractListIndexed implements Named, Comparable
{
  /**
   * The name of the symbol.
   */
  String name;

  Symbol (String name, AbstractList set)
    {
      super(set);
      this.name = name.intern();      
    }

  Symbol (String name, AbstractList set, int index)
    {
      super(set,index);
      this.name = name.intern();      
    }

  public String name ()
    {
      return name;
    }

  String letterName;

  final String letterName ()
    {
      if (letterName == null)
        letterName = Misc.letterSubstring(name);
      return letterName;
    }

  final char initChar ()
    {      
      return Character.toUpperCase(letterName().charAt(0));
    }

  /**   
   * Symbols other than operators are ordered by their names, ignoring
   * all leading non-letter characters. Operators are ordered by their
   * precedences in their category - <i>i.e.</i>, this method is overridden
   * in the subclass <tt>Operator</tt>.
   */
  public boolean lessThan (Comparable other)
    {
      //      if (this instanceof Operator)
      //	return ((Operator)this).lessThan(other);

      String name = letterName();
      String oname = ((Symbol)other).letterName();
    
      char c1 = name.charAt(0);
      char c2 = oname.charAt(0);

      if (Character.isLetter(c1) || Character.isDigit(c1))
        {
          if (!(Character.isLetter(c2) || Character.isDigit(c2)))
            return false;
        }
      else
        if (Character.isLetter(c2) || Character.isDigit(c2))
          return true;  

      return (name.compareToIgnoreCase(oname) < 0);
    }

  public String toString ()
    {
      return name;
    }
}
