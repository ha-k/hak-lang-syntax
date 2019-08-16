//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.ArrayList;
import hlt.language.util.Indexed;
import hlt.language.util.SetOf;

/**
 * This this the class defining rule paths. It is essentially a sequence of
 * rules. NB: The rule paths are constructed backwards; <i>i.e.,</i> from
 * end to start by the grammar analysis (the computePaths method in Grammar).
 * Thus, the rule sequence going from <tt>start</tt> to <tt>end</tt> is the
 * <i>reverse</i> of the <tt>rules</tt> sequence. This allows incrementally
 * computing <tt>first</tt>, which <i>must</i> be done backwards.
 *
 * @version     Last modified on Fri Apr 13 20:12:08 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class RulePath
{
  static final Grammar grammar = Grammar.currentGrammar;

  /**
   * The nonterminal from which this path starts.
   */
  NonTerminal start;
 
  /**
   * The nonterminal at which this path ends.
   */
  NonTerminal end;
 
  /**
   * This is the sequence containing the sequence of rules along
   * this path.
   */
  ArrayList rules;

  /**
   * The FIRST set along this path.
   */
  SetOf first = new SetOf(grammar.terminals);
 
  /**
   * This is <tt>true</tt> iff this path derives EMPTY.
   */
  boolean isNullable = true;
 
  /**
   * Constructs a copy of the given rule path. 
   */
  private RulePath (RulePath p)
    {
      rules = p.rules == null ? null : (ArrayList)p.rules.clone();
      start = p.start;
      end   = p.end;
      first = new SetOf(p.first);
      isNullable = p.isNullable;      
    }

  /**
   * Constructs an empty rule path from the given nonterminal to itself.
   */
  RulePath (NonTerminal n)
    {
      start = end = n;
    }

  /**
   * Adds the given rule at the start of this rule path (and
   * therefore at the <i>end</i> of <tt>rules</tt>).
   */
  final void add (Rule rule)
  {
    start = rule.head();
    
    if (rules == null) rules = new ArrayList();

    rules.add(rule);

    if (isNullable)
      {
        first.union(rule.suffixFirst());
        isNullable = rule.suffixIsNullable();
      }
  }

  /**
   * Returns a new rule path constructed by prepending the given
   * rule to a copy of this path.
   */
  final RulePath prepend (Rule rule)
  {
    RulePath newpath = new RulePath(this);
    newpath.add(rule);
    return newpath;
  }

  /**
   * Returns <tt>true</tt> iff this path is empty.
   */
  final boolean isEmpty ()
    {
      if (rules == null) return true;

      return rules.size() == 0;
    }

  /**
   * Returns the length of this rule path.
   */
  final int length ()
    {
      if (isEmpty()) return 0;

      return rules.size();
    }

  /**
   * Returns <tt>true</tt> iff the given <tt>Object</tt> is
   * a <tt>RulePath</tt> that is equal to this one.
   */
  public final boolean equals (Object object)
    {
      if (!(object instanceof RulePath)) return false;

      return isEqualTo((RulePath)object);
    }

  /**
   * Returns <tt>true</tt> iff the given <tt>RulePath</tt>
   * that is equal to this one.
   */
  final boolean isEqualTo (RulePath other)
    {
      if (length() != other.length())
        return false;

      if (start.index() != other.start.index()
          || end.index() != other.end.index())
        return false;

      if (!first.isEqualTo(other.first))
        return false;

       if (rules == null || other.rules == null)
        return false;

      for (int i=0; i<rules.size(); i++)
        if (((Indexed)rules.get(i)).index()
            != ((Indexed)other.rules.get(i)).index())
        return false;

      return true;
    }

  /**
   * Returns a printable representation of this rule path.
   */
  public final String toString ()
    {
      StringBuilder buff = new StringBuilder("["+start+"-");
      if (rules != null)
        for (int i=rules.size()-1; i>=0; i--)
          buff.append(((Indexed)rules.get(i)).index()+"-");
      buff.append(end+"]");

      buff.append(" first = "+first);
      if (isNullable) buff.append(" (nullable)");

      return buff.toString();
    }    
}  
