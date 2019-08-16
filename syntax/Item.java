//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import hlt.language.util.*;

/**
 * This is the class of LR(0) items. An LR(0) item consists of
 * a pair <tt>&lt;rule,mark&gt;</tt> where <tt>mark</tt>
 * is the index of the dot in the RHS. Dot marks range
 * from 1 to <tt>rule.sequence.length</tt>.
 *
 * @version     Last modified on Fri Apr 13 20:03:28 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class Item extends AbstractListIndexed
{
  static final Grammar grammar = Grammar.currentGrammar;

  /**
   * The rule referred to by this item.
   */
  Rule rule;
  
  /**
   * The mark of the dot (= 1,...,length).
   */
  int mark;             

  /**
   * The FIRST set of the sequence that follows the symbol after the dot;
   * that is,  the value of FIRST(S) if this item is of the form A -> P.BS.
   */
  SetOf suffixFirst;    

  /**
   * This is set to <tt>true</tt> iff this item is of the form A -> P.BS
   * and all the symbols in FIRST(S) derive the empty symbol.
   */
  boolean isNullable = true;

  Item (Rule rule, int index, int mark)
    {
      super(rule.grammar.items,index);
      this.rule = rule;
      this.mark = mark;
    }

  /**
   * Returns <tt>true</tt> this item has a dot at the left
   * end of the RHS.
   */
  final boolean isInitial ()
    {
      return (mark == 1);
    }

  /**
   * Returns <tt>true</tt> this item has a dot at the right
   * end of the RHS.
   */
  final boolean isFinal ()
    {
      return (mark == rule.sequence.length);
    }

  /**
   * Returns <tt>true</tt> this item is empty production.
   */
  final boolean isEmpty ()
    {
      return isInitial() && isFinal();
    }

  /**
   * Returns the grammar symbol that is immediately after the
   * dot in this item. If the dot is at the right end of the RHS,
   * returns the EMPTY symbol.
   */
  final GrammarSymbol marker ()
    {
      return (isFinal() ? Grammar.EMPTY
                        : rule.sequence[mark]);
    }

  /**
   * Returns true iff the marker is a nonterminal;
   */
  final boolean markerIsNonTerminal ()
    {
      return marker() instanceof NonTerminal;
    }

  /**
   * Returns the item obtained from this one by shifting the
   * dot over one position to the right. If the dot is at the
   * end of the rule's RHS, returns this item.
   */
  final Item shift ()
    {
      if (isFinal()) return this;

      return (grammar.getItem(rule,mark+1));
    }

  /**
   * Returns the item obtained from this one by shifting the
   * dot over one position to the left. If the dot is at the
   * beginning of the rule's RHS, returns this item.
   */
  final Item unshift ()
    {
      if (isInitial()) return this;

      return (grammar.getItem(rule,mark-1));
    }

  /**
   * Returns <tt>true</tt> iff this item is a kernel item
   * (see definition on page 223 of the Dragon Book).
   */
  final boolean isKernel ()
    {
      if (isInitial())
        return (rule.head()).isSTART();
      return (mark > 1);
    }

  /**
   * Computes the FIRST set of the suffix of this item past the symbol
   * after the dot; that is,  the value of FIRST(S) if this item is of
   * the form A -> P.BS. See Dragon Book, page 189.
   */
  void computeSuffixFirst ()
    {
      suffixFirst = new SetOf(grammar.terminals);

      for (int i=mark+1; i<rule.sequence.length; i++)
        {
          suffixFirst.union(rule.sequence[i].first);
          isNullable &= rule.sequence[i].isNullable;
          if (!isNullable) break;
        }
    }

  /**
   * A table associating to a state S the set of states that lead
   * to S when following the sequence of symbols before this
   * item's dot.
   */
  HashMap predTable;

  /**
   * Returns the set of states in PRED(state,this) - if necessry
   * by initializing it to the empty set.
   */
  final SetOf pred (State state)
    {
      if (predTable == null) predTable = new HashMap();
      
      SetOf states = (SetOf)predTable.get(state);

      if (states == null)
        {
          states = new SetOf(grammar.states);
          predTable.put(state,states);
        }

      return states;
    }

  /**
   * Update PRED(state,this) with from, and returns <tt>true</tt>
   * iff this changes the previous set.
   */
  final boolean addPred (State state, State from)
    {
      SetOf preds = pred(state);

      if (preds.contains(from))
	return false;
      
      preds.add(from);
      return true;
    }

  /**
   * Update PRED(state,this) with froms, and returns <tt>true</tt>
   * iff this changes the previous set.
   */
  final boolean addPred (State state, SetOf froms)
    {
      SetOf preds = pred(state);

      if (froms.isSubsetOf(preds)) return false;

      preds.union(froms);

      return true;
    }

  /**
   * A table associating to each state in which this item occurs
   * a set of terminal symbols which are the lookahead symbols
   * that propagate to this item in that state.
   */
  HashMap lookaheadTable;

  /**
   * Initializes and returns this item's lookahead set in
   * the given state.
   */ 
  final SetOf initLookaheads (State state)
    {
      if (lookaheadTable == null) lookaheadTable = new HashMap();

      SetOf symbols = new SetOf(grammar.terminals);
      lookaheadTable.put(state,symbols);
      
      return symbols;
    }

  /**
   * Returns the set of lookahead symbols for this item in the
   * given state. NB: No check is made for null table or entry
   * because, by construction, it will always be used for a
   * state for which this is not the case.
   */ 
  final SetOf getLookaheads (State state)
    {
      return (SetOf)lookaheadTable.get(state);
    }

  public boolean equals (Object other)
    {
      if (!(other instanceof Item)) return false;

      return index() == ((Item)other).index();
    }

  public String toString ()
    {
      String s = "["+rule.index()+"] " + rule.sequence[0] + " -->";
      for (int i=1; i<rule.sequence.length; i++)
        s += ((i==mark)?" . ":" ")+rule.sequence[i];
      if (isFinal()) s += " .";

      return s;
    }
}

