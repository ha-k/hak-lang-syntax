//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.util.*;
import hlt.language.tools.Misc;

/**
 * This is the class of production rules used at parser construction time.
 *
 * @version     Last modified on Sun Feb 02 16:44:11 2014 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Rule extends AbstractListIndexed
{
  /**
   * The symbol sequence (head is at 0)
   */
  GrammarSymbol[] sequence;

  /**
   * This rule's action
   */
  String action;

  /**
   * This rule's undo action
   */
  String undoAction;

  /**
   * Flag to indicate node type cast
   */
  boolean nodeCast = false;

  StringBuilder doc;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  Rule (GrammarSymbol[] sequence)
    {
      super(grammar.rules,grammar.rcount++);
      setupRule(sequence,"$empty$","$empty$");
    }

  Rule (GrammarSymbol[] sequence, String action)
    {
      super(grammar.rules,grammar.rcount++);
      setupRule(sequence,action,"$empty$");
    }

  Rule (GrammarSymbol[] sequence, String action, String undo)
    {
      super(grammar.rules,grammar.rcount++);
      setupRule(sequence,action,undo);
    }

  Rule (GrammarSymbol[] sequence, String action, boolean nodeCast)
    {
      super(grammar.rules,grammar.rcount++);
      setupRule(sequence,action,"$empty$");
      this.nodeCast = nodeCast;
    }

  Rule (GrammarSymbol[] sequence, String action, String undo, boolean nodeCast)
    {
      super(grammar.rules,grammar.rcount++);
      setupRule(sequence,action,undo);
      this.nodeCast = nodeCast;
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  static final Grammar grammar = Grammar.currentGrammar;

  final int precedence ()
    {
      return tag.precedence();
    }

  final int associativity ()
    {
      return tag.associativity();
    }

  final boolean isOperator ()
    {
      return tag.isOperator();
    }

  /**
   * This rule's disambiguator tag symbol.
   */
  Taggable tag = Grammar.EMPTY;

  /**
   * This rule's tag's index in the rule's sequence. If -1, the tag does
   * not correspond to any symbol in the rule (i.e, either the rule's tag
   * is <tt>Grammar.EMPTY</tt> or it was set by <tt>%prec</tt>).
   */
  int tagPosition = -1;

  /**
   * The indices for this rule's items in <tt>Grammar.items</tt>.
   */
  int[] items;

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Initializes a Rule object with the sequence of symbols comprising it,
   * and its bottom-up and top-down actions, and adds this rule to the
   * set of rules of its head non-terminal. It also sets the rule's tag
   * to the last terminal symbol in its RHS (if any), and records its
   * position. Finally, it generates all the LR items for this rule.
   */ 
  void setupRule(GrammarSymbol[] sequence, String action, String undo)
    {
      this.sequence = sequence;
      this.action = action;
      this.undoAction = undo;
      ((NonTerminal)(sequence[0])).rules.add(this);
      for (int i=sequence.length-1; i>0; i--)
        if (sequence[i] instanceof Terminal)
          {
            this.tag = (Terminal)sequence[i];
            tagPosition = i;
            break;
          }           
      generateItems();
    }

  /**
   * Returns the head of this rule.
   */
  final NonTerminal head ()
    {
      return (NonTerminal)sequence[0];
    }

  /**
   * Returns the grammar symbol in the body of this rule at specified
   * index.
   */
  final GrammarSymbol body (int index)
    {
      return (GrammarSymbol)sequence[index];
    }

  /**
   * This is the value of the least index in the rule sequence
   * such that all symbols before it derive the EMPTY symbol.
   * The head of the rule will be determined to derive EMPTY
   * whenever this index is equal to the sequence length. It
   * initialized to 1 and possibly increased when computing
   * the FIRST sets.
   */
  int nullableIndex = 1;

  /**
   * Returns the FIRST set of the first suffix of this rule.
   * That is, if this is <tt>A -> X S</tt>, for some symbol
   * <tt>X</tt>, this returns FIRST(S).
   */
  final SetOf suffixFirst ()
    {
      return grammar.getItem(this,1).suffixFirst;
    }

  /**
   * Returns <tt>true</tt> whenever this is <tt>A -> X S</tt>,
   * for some symbol <tt>X</tt> and <tt>S</tt> derives EMPTY.
   */
  final boolean suffixIsNullable ()
    {
      return grammar.getItem(this,1).isNullable;
    }

  /**
   * Returns this rule's RHS's leftmost symbol, or EMPTY if
   * this is an empty production.
   */
  final GrammarSymbol leftMost ()
    {
      if (sequence.length == 1) return Grammar.EMPTY;

      return sequence[1];
    }

  /**
   * Generates all the LR items for this rule.
   */ 
  void generateItems()
    {
      items = new int[sequence.length];
      for (int i=0; i<sequence.length; i++)
        {
          new Item(this,grammar.icount,i+1);
          items[i] = grammar.icount++;
        }
    }

  /**
   * <tt>refName</tt> is the extensionless name of this rule's generated
   * HTML documentation file.
   */ 
  String refName;
  final String refName ()
    {
      refName = Options.getGrammarPrefix()
              + "_GR_"+Misc.zeroPaddedString(index(),
					     Misc.numWidth(grammar.rcount));
      return refName;
    }

  /**
   * Returns the full HTML documentation file name for this rule's
   * generated documentation.
   */ 
  final String htmlFileName ()
    {
      return refName()+".html";
    }

  public String toString ()
    {
      String s = "[" + index() + "]\t" + sequence[0] + " -->";
      for (int i=1; i<sequence.length; i++)
        s += " " + sequence[i];
      return s + "\n\tprecedence = " + tag.precedence()
               + (_xmlInfo == null ? "" : "\n\tXML form = " + _xmlInfo);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  // XML serialization information
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * A storage for recording xml annotation (if any) for this production rule.
   */
  private XmlInfo _xmlInfo = null;

  XmlInfo xmlInfo ()
    {
      return _xmlInfo;
    }
    
  public boolean hasXmlInfo ()
    {
      return _xmlInfo != null;
    }

  Rule setXmlInfo (XmlInfo info) throws BadXmlAnnotationException
    {
      if (info != null)
	info.checkConsistency(this);
      _xmlInfo = info;
      return this;
    }
}
