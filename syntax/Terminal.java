//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.SetOf;
import hlt.language.tools.Misc;

/**
 * This is the class of terminal symbols used by the grammar
 * at parser construction time.
 *
 * @see         GrammarSymbol
 *
 * @version     Last modified on Sun Feb 02 16:42:45 2014 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Terminal extends GrammarSymbol implements Taggable
{
  Terminal(String name)
    {
      super(name,grammar.terminals,grammar.tcount++);
    }

  Terminal(String name, boolean isOperator)
    {
      super(name,grammar.terminals,grammar.tcount++);
      this.isOperator = isOperator;
    }

  /**
   * This symbol's precedence.
   */
  int precedence = Grammar.MIN_PRECEDENCE;

  /**
   * This symbol's associativity.
   */
  int associativity = Grammar.NON_ASSOCIATIVE;

  /**
   * This symbol's precedence.
   */
  public final int precedence ()
    {
      return precedence;
    }

  /**
   * This symbol's associativity.
   */
  public final int associativity ()
    {
      return associativity;
    }

  /**
   * An indicator that this is a dynamic operator
   */
  boolean isOperator = false;

  public final boolean isOperator ()
    {
      return isOperator;
    }

  /**
   * An indicator that this is used as a rule tag
   */
  boolean isTag = false;

  final String htmlName ()
    {
      return Misc.htmlString(name);
    }

  final String label ()
    {
      return "<TT><B>"+htmlName()+"</B></TT>";
    }

  final void link (Rule rule)
    {
      Documentor.hasError = isError();

      if (ruleOccurrences == null)
        ruleOccurrences = new SetOf(grammar.rules);

      ruleOccurrences.add(rule);
    }

  final String refName ()
    {
      if (refName == null)
	refName = Options.getGrammarPrefix()
	  + "_TT_"+Misc.zeroPaddedString(index(),
					 Misc.numWidth(grammar.tcount));
      return refName;
    }

  public boolean equals (Object other)
    {
      if (!(other instanceof Terminal)) return false;

      Terminal t = (Terminal)other;

      return (index() == t.index());        
    }

  public String toString()
    {
      return '\''+name+'\'';
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // XML serialization information
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * A storage for recording xml annotation (if any) for this terminal symbol.
   */
  private XmlInfo _xmlInfo = null;

  public XmlInfo xmlInfo ()
    {
      return _xmlInfo;
    }

  public boolean hasXmlInfo ()
    {
      return _xmlInfo != null;
    }

  public Terminal setXmlInfo (XmlInfo info) throws BadXmlAnnotationException
    {
      if (info != null)
	info.checkConsistency(this);
      _xmlInfo = info;
      return this;
    }
}
