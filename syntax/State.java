//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import java.util.Iterator;
import hlt.language.util.*;

/**
 * A state is really a set of rule items, also represented as a hash
 * table for efficient computation of the next state. The fields are:
 * <dl>
 * <dt><b>items:</b></dt>
 * <dd>the set of items comprising the state</dd>
 * <dt><b>transitions:</b></dt>
 * <dd>a hash table associating the nonterminals after the dot in some item
 *     of this state (or the empty symbol) to a <tt>StateTransition</tt>.</dd>
 * </dl>
 *
 * @see		StateTransition
 * @version     Last modified on Fri Apr 13 20:12:33 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

class State extends AbstractListIndexed
{
  static final Grammar grammar = Grammar.currentGrammar;
  SetOf items = new SetOf(grammar.items);
  SetOf kernels = new SetOf(grammar.items);
//   HashMap transitions = new HashMap(); // maps symbols to StateTransitions
  Table transitions = new Table(); // maps symbols to StateTransitions

  State (int index)
    {
      super(grammar.states);
      add(grammar.getItem(index));
    }

  State (Item item)
    {
      super(grammar.states);
      add(item);
    }

  State (SetOf items)
    {
      super(grammar.states);
      add(items);
    }

  final StateTransition getTransition (GrammarSymbol symbol)
    {
      return (StateTransition)transitions.get(symbol);
    }

  final void add (Item item)
    {
      GrammarSymbol symbol = item.marker();
      StateTransition transition = getTransition(symbol);
      if (transition == null)
        {
          transition = new StateTransition(this,symbol,new SetOf(grammar.items));
          transitions.put(symbol,transition);
        }
      transition.items.add(item.index());
      items.add(item.index());
    }

  final void add (SetOf items)
    {
      for (Iterator e=items.iterator(); e.hasNext();)
        add((Item)e.next());
    }

  /**
   * Adds to this state all the necessary items that must be in its
   * LR(0) closure. An algorithm is described in the Dragon Book on page
   * 223. This is a better one, taken from the Park-Choe-Chang article.
   * It makes use of the pre-computed closure of the L-graph and thus
   * avoids repeatedly traversing the set of rules as done by the Dragon
   * Book's method. The formula is simply:
   * <pre>
   *        CLOSURE(s) = s U { C --> .W | A --> P.BS in s & B L* C }
   * </pre>
   */
  void closure ()
    {
      for (Iterator its=items.iterator(); its.hasNext();)
        {
          Item item = (Item)its.next();
          GrammarSymbol marker = item.marker();
          if (marker instanceof NonTerminal)
            for (Iterator ns=((NonTerminal)marker).LSet.iterator(); ns.hasNext();)
              {
                NonTerminal n = (NonTerminal)ns.next();
                for (Iterator rs=n.rules.iterator(); rs.hasNext();)
                  add(grammar.getItem((Rule)rs.next(),1));
              }
        }
    }  

  /**
   * Compute the next state for all the entries in the transition table.
   */
  void computeNextStates()
    {
      for (Iterator e=transitions.values().iterator(); e.hasNext();)
        ((StateTransition)e.next()).computeNextState();
    }

  /**
   * Extracts the kernel items from the set of items.
   */
  final void extractKernels ()
    {
      kernels = new SetOf(grammar.items);

      for (Iterator its=items.iterator(); its.hasNext();)
        {
          Item i = (Item)its.next();
          if (i.isKernel())
	    kernels.add(i.index());
        }
    }

  /**
   * Compute the <tt>PRED</tt> relation for this state, and returns
   * <tt>true</tt> iff this made an actual change in <tt>PRED(this,item)</tt>
   * for some item.
   */
  final boolean computePreds ()
    {
      boolean change = false;
      
      for (Iterator e=transitions.values().iterator(); e.hasNext();)
        change |= ((StateTransition)e.next()).computePreds();

      return change;
    }

  /**
   * Returns the next state for the given symbol, or <tt>null</tt>
   * if so such state exists.
   */
  final State next (GrammarSymbol symbol)
    {
      return getTransition(symbol).next;
    }

  /**
   * A table associating nonterminals to their follow sets in this
   * state.
   */
  HashMap followTable;
      
  /**
   * Returns the follow set for the given nonterminal in this state
   * if one has been computed; otherwise, returns the empty set.
   */
  final SetOf follow (NonTerminal n)
    {
      Follow f; 

      if (followTable == null || (f=(Follow)followTable.get(n)) == null)
        return new SetOf(grammar.terminals);

      return f.follows;
    }
      
  /**
   * For the given symbol, this checks whether Follow(this,symbol)
   * already exists. If so, it returns it. If not, this creates a new
   * Follow object, records it as appropriate, and returns it.
   */
  final Follow getFollow (NonTerminal n)
    {
      if (followTable == null)
	followTable = new HashMap();

      Follow f = (Follow)followTable.get(n);

      if (f == null)
        {
          f = new Follow(this,n);
          followTable.put(n,f);
          f.add();
          grammar.fcount++;
        }

      return f;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  ArrayList conflicts;

  void addConflict (String conflict)
    {
      if (conflicts == null)
	conflicts = new ArrayList(5);

      conflicts.add(conflict);
    }

  void showConflicts ()
    {
      if (conflicts == null)
	return;

      grammar.out.println("This state has conflicts:\n");

      for (Iterator i=conflicts.iterator(); i.hasNext();)
	grammar.out.println(i.next());

      grammar.out.println("-----------------------------");
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * A set of sets of actions for dynamic operators.
   */
  ArrayList dynamicActions = new ArrayList();

  Map actionTable = new Map();
  int ac_index;

  final Action getAction (Terminal symbol)
    {
      return (Action)actionTable.get(symbol);
    }

  final void setAction (Terminal symbol, Action action)
    {
      actionTable.put(symbol,action);
    }

  Map gotoTable = new Map();
  int gt_index;

  final State getGoto (NonTerminal symbol)
    {
      return (State)gotoTable.get(symbol);
    }

  final void setGoto (NonTerminal symbol, State next)
    {
      gotoTable.put(symbol,next);
    }

  String dynamicActionSet ()
    {
      StringBuilder b = new StringBuilder("{");
      int n = dynamicActions.size();
      
      for (int i=0; i<n; i++)
	b.append(i+":"+dynamicActions.get(i)).append(i == n-1 ? "" : ",");

      return b.append("}").toString();
    }      

  /**
   * Returns <tt>true</tt> iff the given <tt>Object</tt> is
   * also a <tt>State</tt> and one equal to this one.
   */
  public final boolean equals (Object other)  
    {
      if (other instanceof State)
        return this.isEqualTo((State)other);

      return false;
    }     

  /**
   * Returns <tt>true</tt> iff the given <tt>State</tt> is
   * equal to this one - <i>i.e.</i>, iff they have the same
   * set of items.
   */
  final boolean isEqualTo (State state)
    {
      return items.isEqualTo(state.items);
    }     

  /**
   * Returns a hashcode for this State. It is that of the state's
   * set of items.
   */
  public final int hashCode ()
    {
      return items.hashCode();
    }

  void show ()
    {
      grammar.out.println();
      grammar.out.println("=============================");
      grammar.out.println("STATE NUMBER: "+index());
      grammar.out.println("=============================");

      showConflicts();

      if (!dynamicActions.isEmpty())
	{
	  grammar.out.println("This state has dynamic actions:\n\t");
	  grammar.out.println(dynamicActionSet());
      grammar.out.println("-----------------------------");
	}

      for (Iterator e=items.iterator(); e.hasNext();)
        {
          Item item = (Item)e.next();
          grammar.out.print(item);
          if (item.isKernel())
            {
              grammar.out.print("\n\tPreceding states: "+item.pred(this));
              if (item.markerIsNonTerminal())
                grammar.out.print("\n\tFollow set: "+
                                  getFollow((NonTerminal)item.marker()).follows);
            }
          if (item.isInitial())
            grammar.out.print("\n\tPreceding states: "+item.pred(this));
          if (item.isFinal())
            grammar.out.print("\n\tLookahead set: "+item.getLookaheads(this));
          grammar.out.println();
        }
      grammar.out.println("-----------------------------");
      for (Iterator e=transitions.values().iterator(); e.hasNext();)
        {
          StateTransition transition = ((StateTransition)e.next());
          if (!transition.symbol.isEmpty())
            grammar.out.println("With " + transition.symbol + 
                                ", go to state " +
                                transition.next.index());
        }
    }
}

