//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.Iterator;
import hlt.language.util.SetOf;

/**
 * This class is the type of objects denoting transitions from a
 * state to another following a grammar symbol - the one shifted
 * over by moving the dot one mark to the right, or the empty symbol
 * if at the end of an item. (All items in a state having the same
 * marker are grouped.) A <tt>StateTransition</tt> consists of:
 * <ul>
 * <li> the shifted symbol - or EMPTY in the case of final items;
 * <li> the set of items in this state with the symbol as marker
 *      (<i>i.e.</i>, right after the dot) - or with the dot at
 *      the end in the case of final items; and,
 * <li> the next state reached upon shifting this symbol - or
 *      reducing in the case of final items.
 * </ul>
 * 
 * @see State
 *
 * @version     Last modified on Fri Apr 13 20:12:44 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */
class StateTransition
{
  static final Grammar grammar = Grammar.currentGrammar;

  SetOf items;                  // items with same marker symbol
  GrammarSymbol symbol;         // symbol after the dot in items
  State from = null;            // this transition's origin state
  State next = null;            // this transition's next State

  StateTransition(State from, GrammarSymbol symbol, SetOf items)
    {
      this.from = from;
      this.symbol = symbol;
      this.items = items;
    }

  /**
   * Computes the next state to go to from the <tt>from</tt> state
   * when shifting over this transition's marker (which is the same for all
   * the items in <tt>items</tt> by construction).
   */
  void computeNextState ()
    { 
      SetOf nextItems = new SetOf(grammar.items);

      // Compute the set of shifted items from each item in items:
      for (Iterator e=items.iterator(); e.hasNext();)
        {
          Item item = (Item)e.next();
          if (!item.isFinal())
            nextItems.add((item.shift()).index());
        }

      // Compute the next state from the closure of the new item set:
      State state = new State(nextItems);
      if (!state.transitions.isEmpty())
        { 
          state.closure();
          next = grammar.checkNewState(state);
        }
    }

  /**
   * Computes the set of preceding states for each item in this transition.
   * Returns <tt>true</tt> iff some change was detected.
   */
  boolean computePreds ()
    {
      boolean change = false;

      for (Iterator e=items.iterator(); e.hasNext();)
        {
          Item item = (Item)e.next();

          if (item.isInitial())
            change |= item.addPred(from,from);
          
          if (!item.isFinal())
            change |= item.shift().addPred(next,item.pred(from));
        }
      return change;
    }
}
