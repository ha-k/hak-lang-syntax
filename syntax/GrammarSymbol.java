//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayList;
import hlt.language.util.SetOf;

/**
 * This class is an abstract representation of a grammar symbol,
 * whether terminal or nonterminal. It is used by the grammar at
 * parser construction time.
 *
 * @see         Symbol
 * @see         Terminal
 * @see         NonTerminal
 *
 * @version     Last modified on Fri Apr 13 20:03:08 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */
abstract public class GrammarSymbol extends Symbol
{
  GrammarSymbol(String name, ArrayList set, int index)
    {
      super(name,set,index);
    }

  static final Grammar grammar = Grammar.currentGrammar;

  /**
   * The FIRST set for this symbol.
   */
  SetOf first;

  /**
   * This is set ot <tt>true</tt> iff this grammar symbol
   * derives the empty symbol.
   */
  boolean isNullable = false;

  final boolean isSTART ()
    {
      return (this == Grammar.START);
    }

  final boolean isROOTS ()
    {
      return (this == Grammar.ROOTS);
    }

  final boolean isEmpty ()
    {
      return (this == Grammar.EMPTY);
    }

  final boolean isEndOfInput ()
    {
      return (this == Grammar.END_OF_INPUT);
    }

  final boolean isError ()
    {
      return (this == Grammar.ERROR);
    }

  final boolean isTag ()
    {
      return (this instanceof Terminal) && ((Terminal)this).isTag;
    }

  final boolean isAction ()
    {
      return name.startsWith("$ACTION") && name.endsWith("$");
    }

  final boolean isSpecial ()
    {
      return name.startsWith("$") && name.length() != 1 && name.endsWith("$")
	  || isTag() || isError();
    }

  /**
   * A <tt>StringBuilder</tt> to hold the documentation string attached to this
   * grammar symbol.
   */
  StringBuilder doc;

  final void addDoc (StringBuilder doc)
    {
      if (doc != null)
        {
          if (this.doc == null)
            this.doc = new StringBuilder();
          this.doc.append(doc);
        }
    }
   
  abstract void link (Rule rule);
  abstract String refName ();
  abstract String label ();

  String refName;
  SetOf ruleOccurrences;

  final String htmlRef (String label)
    {
      return "<A CLASS=\"SYMBOL\" HREF=\""+htmlFileName()+
             "\" TARGET=\"MAIN\" ONCLICK=\"opener.focus()\">"+
             label+"</A>";
    }

  final String htmlRef ()
    {
      return "<A CLASS=\"SYMBOL\" HREF=\""+htmlFileName()+
             "\" TARGET=\"MAIN\" ONCLICK=\"opener.focus()\">"+
             label()+"</A>";
    }

  final String htmlFileName ()
    {
      return refName()+".html";
    }

  public boolean equals (Object other)
    {
      if (other instanceof Terminal)
        return ((Terminal)other).equals(this);
      else
        return ((NonTerminal)other).equals(this);
    }
}
