//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.SetOf;
import java.util.ArrayList;//import hlt.language.util.ArrayList;
import hlt.language.util.AbstractListIndexed;

/**
 * This is the class of object denoting the FOLLOW set
 * for a given state and nonterminal. It is the type of
 * object stored in the a state's <tt>followTable</tt>.
 * Such an object is also a node in the follow digraph
 * used for efficiently computing the follow set closures.
 * These objects are <tt>AbstractListIndexed</tt> as they are
 * manipulated in sets which are <tt>SetOf(grammar.follows)</tt>.
 *
 * @version     Last modified on Fri Apr 13 19:59:25 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class Follow extends AbstractListIndexed
{
  static final Grammar grammar = Grammar.currentGrammar;

  /**
   * The state for this follow set.
   */
  State state;

  /**
   * The nonterminal for this follow set.
   */
  NonTerminal symbol;

  /**
   * Constructs a follow object indexed in the ArrayList
   * <tt>grammar.follows</tt> with the given state and nonterminal.
   */
  Follow (State state, NonTerminal symbol)
    {
      super(grammar.follows);
      this.state = state;
      this.symbol = symbol;
    }

  /**
   * The set of nonterminals in this follow set.
   */
  SetOf follows = new SetOf(grammar.terminals);

  /**
   * Adds the given set of terminal symbols to this follow set,
   * and returns <tt>true</tt> iff new symbols have been actually
   * added.
   */
  final boolean addFollows (SetOf symbols)
    {
      if (symbols.isSubsetOf(follows))
        return false;

      follows.union(symbols);

      return true;
    }

  /**
   * The set of <tt>Follow</tt> objects that are predecessors
   * of this one in the follow digraph. In other words, this
   * follow set will have to be included in each of its predecessor's
   * which contains <tt>EMPTY</tt>. These are the edges of the digraph.
   * So a <tt>Follow</tt> objects points to those that must include it.
   */
  SetOf preds = new SetOf(grammar.follows);

  /**
   * Adds the given <tt>Follow</tt> object to the predecessors of
   * this one.
   */
  final void addPred (Follow follow)
    {
      preds.add(follow);
    }

  /**
   * Returns <tt>true</tt> iff the given <tt>Follow</tt> object
   * is equal to this one (<i>i.e.</i>, they have the same state
   * and symbol).
   */
  final boolean isEqualTo (Follow other)
    {
      return state.index() == other.state.index()
          && symbol.index() == other.symbol.index();
    }

  /**
   * Returns <tt>true</tt> iff the given object is a <tt>Follow</tt>
   * object which is  is equal to this one.
   */
  public final boolean equals (Object object)
    {
      if (!(object instanceof Follow)) return false;

      return isEqualTo((Follow)object);
    }

  /**
   * Returns a string representation of thsi Follow object.
   */
  public final String toString ()
    {
      return "FOLLOW("+state+","+symbol+") = "+follows;
    }
}

    
