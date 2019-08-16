//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import java.util.Iterator;
import hlt.language.util.Comparable;
import hlt.language.util.ArrayList;
import hlt.language.util.SetOf;
import hlt.language.tools.Misc;

/**
 * This is the class of nonterminal symbols used by the grammar
 * at parser construction time.
 *
 * @see         GrammarSymbol
 *
 * @version     Last modified on Fri Apr 13 20:05:47 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class NonTerminal extends GrammarSymbol
{
  NonTerminal (String name)
    {
      super(name,grammar.nonterminals,grammar.ncount++);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * The rules for this non-terminal
   */
  ArrayList rules = new ArrayList();

  /**
   * When <tt>true</tt>, this nonterminal is a dynamic operator category.
   */
  boolean isOperator = false;

  /**
   * The class name of a parse stack node for this symbol
   * (if != null, the name of a subclass of ParseNode).
   */
  public String nodeType = null;

  /**
   * Set of nonterminals <tt>N</tt> such that <tt>this L* N</tt>.
   */
  SetOf LSet;

  /**
   * Table of nonterminals and rules <tt>N, N -> this ...</tt>
   * such that <tt>N L this</tt>.
   */
  HashMap LTable;

  /**
   * Returns the appropriate set of rules from <tt>LTable</tt>
   * for the given nonterminal, or </tt>null<tt> if none exists.
   */
  final SetOf getLRules (NonTerminal n)
    {
      return (SetOf)LTable.get(n);
    }

  /**
   * Add the given rule for the given nonterminal in this
   * symbol's </tt>LTable<tt>.
   */
  final void addLRule (NonTerminal n, Rule r)
    {
      if (LTable == null) LTable = new HashMap();

      SetOf rules = getLRules(n);

      if (rules == null)
	{
	  rules = new SetOf(grammar.rules);
	  LTable.put(n,rules);
	}

      rules.add(r.index());
    }

  /**
   * This table is similar to the <tt>LTable</tt>, but the entry keys
   * are nonterminals that are <tt>L*</tt>-related to this one. Also,
   * unlike in </tt>LTable<tt>, the links for <tt>pathTable</tt> are
   * kept in the right direction. A key in <tt>N.pathTable</tt> is a
   * nonterminal <tt>P</tt> such that <tt>N L* P</tt> and its entry is an
   * ArrayList of <tt>RulePath</tt> objects. Namely, let <tt>N</tt> be this
   * node and <tt>P</tt> be a node such that <tt>N L* P</tt>. Each path
   * between <tt>N</tt> and <tt>P</tt> is characterized by the sequence
   * of rules leading from <tt>N</tt> to <tt>P</tt>. Since there may be
   * several differents paths between <tt>N</tt> and <tt>P</tt>, each
   * such path must be recorded in an ArrayList which the stored as
   * <tt>N.pathTable(P)</tt>.
   */
  HashMap pathTable;

  /**
   * For convenient iteration over all paths starting at this nonterminal
   * we also maintain this ArrayList.
   */
  ArrayList paths;

  /**
   * This initializes the path structures, creates an empty path
   * from this nonterminal to itself, and records it where appropriate.
   */
  final void initPaths ()
    {
      paths = new ArrayList();
      pathTable = new HashMap();

      pathTable.put(this, new Paths(new RulePath(this)));
    }

  /**
   * Adds the given path to the paths starting at this nonterminal if
   * it is not already there. Returns <tt>true</tt> if the given path
   * contributes to the FIRST set of the paths between this nonterminal
   * and the end of the path.
   */
  final boolean addPath (RulePath path)
    {
      Paths paths = (Paths)pathTable.get(path.end);

      if (paths == null)
	{
	  pathTable.put(path.end,new Paths(path));
	  return true;
	}

      return paths.add(path);
    }

  /**
   * Returns the FIRST set of terminals in PATH(this,n).
   * NB: By construction this will always be well-defined
   * by the time it is called because all nonterminals
   * have their paths initialized when the grammar builds
   * its L-graph. This cannot be done in the constructor
   * because then the grammar may not have been read all
   * its data yet.
   */
  final SetOf path (NonTerminal n)
    {
      return ((Paths)pathTable.get(n)).first;
    }

  /**
   * Returns <tt>true</tt> iff this.path(n) derives EMPTY.   
   */
  final boolean pathIsNullable (NonTerminal n)
    {
      return ((Paths)pathTable.get(n)).isNullable;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private boolean isStart = false;

  final boolean isStart ()
    {
      return isStart;
    }
    
  final void makeStart ()
    {
      isStart = true;
    }

  private boolean isRoot = false;

  final boolean isRoot ()
    {
      return isRoot;
    }
    
  final void makeRoot ()
    {
      isRoot = true;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

    
  final String label ()
    {
      String label = "<TT><I>"+Misc.htmlString(name)+"</I></TT>";

      return isStart ? "<B>"+label+"</B>" : label;
    }

  final String ruleLabel (Rule r)
    {
      int index = 1;
      String head = Misc.htmlString(name);
      for (Iterator rls=rules.iterator(); rls.hasNext();)
	{
	  if (r == rls.next())
	    return "<TT><I>"+head+"_"+index+"</I></TT>";
	  index++;
	}

      return head;
    }

  final void link (Rule rule)
    {
      if (isSpecial()
	  || rule.sequence[0].isSpecial()
	  || this == rule.sequence[0])
	return;

      if (ruleOccurrences == null)
	ruleOccurrences = new SetOf(grammar.rules);

      ruleOccurrences.add(rule);
    }

  final String refName ()
    {
      if (refName == null)
	refName = Options.getGrammarPrefix()
	  + "_NT_"+Misc.zeroPaddedString(index(),
					 Misc.numWidth(grammar.ncount));
      return refName;
    }

  public boolean equals (Object other)
    {
      if (!(other instanceof NonTerminal)) return false;

      NonTerminal n = (NonTerminal)other;

      return (index() == n.index());        
    }

  public final boolean lessThan (Comparable other)
    {
      if (grammar.RULE_ORDER_MODE)
	{
	  if (!(other instanceof NonTerminal))
	    return false;

	  NonTerminal n = (NonTerminal)other;

	  if (rules.isEmpty() || n.rules.isEmpty())
	    return false;

	  return ((Rule)rules.get(0)).index() < ((Rule)n.rules.get(0)).index();
	}

      return super.lessThan(other);
    }
}
