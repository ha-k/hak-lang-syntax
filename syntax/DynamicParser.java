//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import java.util.Iterator;
import java.util.AbstractList;

import java.io.IOException;

import hlt.language.tools.Misc;
import hlt.language.util.Stack;
import java.util.ArrayList;//import hlt.language.util.ArrayList;
import hlt.language.util.FiniteStack;
import hlt.language.util.TimeStamped;
import hlt.language.util.TimeStampManager;

/**
 * This is the generic parser that is inherited by all parser classes
 * generated by ParserGenerator for a grammar which uses dynamic operators.
 *
 * @see         ParserGenerator
 * @see         GenericParser
 * @see         StaticParser
 * @version     Last modified on Fri Apr 13 19:57:45 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */
public abstract class DynamicParser extends GenericParser
{
  /**
   * Returns the precedence of the current handle (corrresponding
   * to the given rule).
   */
  protected final int precedence (ParserRule r)
    {
      if (r.precedence != -1)
	return r.precedence;
      return node(r,r.tagPosition).precedence();
    }

  /**
   * Returns the associativity of the current handle (corrresponding
   * to the given rule).
   */
  protected final int associativity (ParserRule r)
    {
      if (r.associativity != -1)
	return r.associativity;
      return node(r,r.tagPosition).associativity();
    }

  /**
   * Returns <tt>true</tt> iff the current handle (determined
   * from the given rule) has a terminal symbol tag corresponding
   * to the given <tt>ParseNode</tt>.
   */
  protected final boolean hasTag (ParserRule r, ParseNode node)
    {
      if (r.tagPosition != -1)
	{
          ParseNode tag = node(r,r.tagPosition);
          if (tag.isTerminal())
	    {
              if (tag.isOperator() && node.isOperator())
		return (tag.operator() == node.operator());
              if (!tag.isOperator() && !node.isOperator())
		return (tag.symbol() == node.symbol());
            }
        }
      return false;
    }

  /**
   * The undo semantic action method.
   */
  protected abstract void undoSemanticAction(ParserRule r,ParseNode n) throws IOException;

  /**
   * The following items must be supplied by the generated parser subclass.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       PARSER'S     PARAMETERS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * The set of parser operators.
   */
  public AbstractList operators;

  /**
   * The table associating identifiers to operators.
   */
  protected final HashMap operatorTable = new HashMap();

  /**
   * Storage tables for dynamic operators. They are two hash tables.
   * The first associates an operator category's name to a set of
   * all <tt>ParserOperator</tt> objects in this category. The second
   * associates a specific operator's name to a set containing all
   * <tt>ParserOperator</tt> objects with this name for all categories.
   */
  protected final HashMap operatorCategoryTable = new HashMap();
  protected final HashMap operatorNameTable = new HashMap();

  /**
   * The generic method for defining dynamic operators.
   */
  protected final void defineOperator (String category, String name,
                                       String specifier, int precedence)
    throws NonFatalParseErrorException
    {
      int prec = Grammar.checkPrecedenceLevel(Grammar.prologPrecedence(precedence));
      AbstractList ops = operatorsInCategory(category);
      ParserNonTerminal cat = nonterminal(category);
      ParserOperator operator = new ParserOperator(this,name,cat,prec,specifier);

      int i = ops.indexOf(operator);

      if (i == -1)
	{
          operator.add();
          ops.add(operator);
          ops = operators(name);        // NB: this is another ops!
          if (ops == null)
	    operatorNameTable.put(name,ops = new ArrayList(3));
          ops.add(operator);
        }
      else
        ((ParserOperator)operators.get(i)).redefine(prec,specifier);
    }

  /**
   * The following are conveniences initializing some of the above.
   * They are used only by the generated parser to set up its parameters.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       INITIALIZATION  METHODS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  protected final void newOperator
    (String name, int category, int precedence, int associativity, int fixity)
    {
      ParserOperator operator
        = new ParserOperator(this,name,nonterminals[category],
                             precedence,associativity,fixity);
      operatorTable.put(name,operator);
      operator.add();

      AbstractList ops = operatorsInCategory(nonterminals[category].name());
      if (ops == null)
	operatorCategoryTable.put(nonterminals[category].name(),
				  ops = new ArrayList(5));
      ops.add(operator);

      ops = operators(name);    // NB: this is another ops!
      if (ops == null)
	operatorNameTable.put(name,ops = new ArrayList(5));
      ops.add(operator);
    }

  protected static final void newDynamicActionTable (int state, int size)
    {
      states[state].dynamicActions = new ParserAction[size][];
    }

  protected static final void newDynamicActions (int state, int index, int size)
    {
      states[state].dynamicActions[index] = new ParserAction[size];
    }

  protected static final void setDynamicAction
    (int state, int index, int position, int action)
    {
      states[state].dynamicActions[index][position] = actions[action];
    }

  /**
   * The following are methods used for parsing.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       PARSING         METHODS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Returns the set of operators with this name; or <tt>null</tt> in there
   * are none.
   */
  public final AbstractList operators (String name)
    {
      return (AbstractList)operatorNameTable.get(name);
    }  

  /**
   * Returns the set of operators in this category; or <tt>null</tt> in there
   * are none.
   */
  public final AbstractList operatorsInCategory (String category)
    {
      return (AbstractList)operatorCategoryTable.get(category);
    }  

   /**
   * The following are local utilities...
   */

  private TimeStampManager tsm = new TimeStampManager();

  private void stamp (TimeStamped object)
    {
      tsm.setTimeStamp(object);
    }

  private Stack readStack = new Stack();
  protected FiniteStack choiceStack;
  protected FiniteStack trailStack;

  protected boolean resolveRRsWithPrecedence;

  public void resetParser ()
    {
      super.resetParser();
      readStack.clear();
      choiceStack.flush();
      trailStack.flush();
    }

  /**
   * Returns an error token. Overrides the default method.
   */
  public ParseNode error ()
    {
      return new DynamicToken(super.error());
    }

  protected static boolean admitsOperators = true;

  /**
   * Overrides the implementation in <tt>GenericParser</tt> so that it
   * does not return an error token if the symbol is not a known
   * terminal symbol and the parser admits operators, in which case it
   * is returned as a literal token anyway that will be resolved as a
   * potential dynamic operator by the <tt>readToken</tt> method.
   */
  public static final ParseNode literalToken (String symbol)
    {
      ParserTerminal term = terminal(symbol);
      return term == null ? (admitsOperators ? new ParseNode(symbol.intern())
					     : error(symbol))
			  : new ParseNode(term);
    }

  /**
   * Returns the token that was the last one actually read off the input
   * stream (as opposed to the read stack); <i>i.e.</i>, it is the
   * bottom of the read stack if it is not empty, otherwise the current
   * token.
   */
  protected ParseNode latestToken () throws IOException
    {
      if (readStack.isEmpty())
	return ((DynamicToken)tokenNode()).getOriginal();

      return (ParseNode)readStack.get(0);
    }

  /**
   * Reads the next token into <tt>tokenNode</tt>. If the read stack is
   * not empty, the token is read from it; otherwise, it is read from
   * the input stream. In either case, the new token is checked for
   * potential ambiguities. Because a dynamic token may be a symbol
   * unknown at parser generation time, it may mutate into one or
   * several dynamic operators if the symbol has been declared a a
   * dynamic operator of a category expected in the current state.
   * Therefore, the newly read token is systematically wrapped inside a
   * time-stamped <tt>DynamicToken</tt>. If one or more candidates are
   * found, this token it is transformed into the first of them. Thus,
   * it must be restored as originally read and to be time-stamped anew
   * when read again from the read stack if backtracking takes the parser
   * to an earlier stage.
   *
   * @see DynamicToken
   */
  final void readToken () throws IOException
    {
      if (readStack.isEmpty())
	{
          stamp((TimeStamped)(tokenNode = new DynamicToken(nextToken())));
          if (trace)
	    err.println("*** Read token: "+tokenNode+" from input.");
        }
      else
        {
          stamp((TimeStamped)(tokenNode = new DynamicToken((ParseNode)readStack.pop())));
          if (trace)
	    err.println("*** Read token: "+tokenNode+" from read stack.");
        }

      if (tokenNode.isError())
	cutAll();
      else
	{
	  Choice choice = new Choice();

	  if (tokenNode.hasAlternatives())
	    tallyAlternatives(choice);

	  if (admitsOperators)
	      tallyOperators(choice);

	  if (!choice.isEmpty())
	    pushChoice(choice);
	}

      readTokenFlag = false;
    }

  /**
   * Adds to given choice point all alternative forms of the current token.
   */
  private final void tallyAlternatives (Choice choice)
    {
      for (Iterator i=tokenNode.alternatives().iterator(); i.hasNext();)
	{
	  DynamicToken token = new DynamicToken((ParseNode)i.next());
	  token.setOriginal(tokenNode);
	  stamp(token);
	  choice.addOption(token);
	}
    }

  private void pushChoice (Choice choice)
    {
      stamp(choice);
      
      if ((choice = (Choice)choiceStack.push(choice)) != null)
	while (!trailStack.isEmpty()
	       && ((TrailEntry)trailStack.oldest()).getTimeStamp() < choice.getTimeStamp())
	  trailStack.drop();
    }

  /**
   * Determines whether the current token is a potential dynamic operator,
   * and if so, adds it to given choice point as appropriate for potential
   * backtracking.
   */
  private final void tallyOperators (Choice choice)
    {
      Stack ops = admissibleOperators();

      if (tokenNode.isUnknown())
	if (ops != null)
	  {
            tokenNode = (DynamicToken)ops.pop();
            if (ops.isEmpty())
	      ops = null;
          }
        else
	  tokenNode = error(tokenNode);

      if (ops != null)
	choice.addOptions(ops);
    }

  /**
   * Returns a stack of operator tokens found by looking up <tt>operatorNameTable</tt>
   * for all operators symbols that could stand for <tt>tokenNode</tt> in the
   * current state. Returns <tt>null</tt> if no admissible operator is found.
   */
  private final Stack admissibleOperators ()
    {
      String name = tokenNode.svalue() == null ? tokenNode.symbol().name()
                                               : tokenNode.svalue().intern();
      AbstractList ops = operators(name);
      if (ops == null)
	return null;

      Stack admissibles = new Stack();
      for (Iterator e=ops.iterator(); e.hasNext();)
        {
          ParserOperator operator = (ParserOperator)e.next();
          if (symbolIsHandled(operator.subCategory))
	    {
              if (tokenNode.isUnknown() && operator.subCategory == tokenNode.symbol())
		{
                  ((DynamicToken)tokenNode).makeOperator(operator);
                  continue;
                }
	      DynamicToken token = new DynamicToken(operator,
						    ((DynamicToken)tokenNode).getOriginal());
	      stamp(token);
              admissibles.push(token);
            }
        }

      if (admissibles.isEmpty())
	return null;

      return admissibles;
    }

  /**
   * Pushes the current state and the given node on the parser stack, and marks
   * this new stack element with a time stamp.
   */
  final void push (ParseNode node)
    {
      super.push(node);
      stamp((TimeStamped)parserStack.peek());
    }

  /**
   * A switch to prevent setting the parse action from the current state upon
   * backtracking over a <tt>CHOICE</tt> action.
   */
  private boolean getParseActionFlag = true;

  /**
   * Sets the appropriate parser action for the current token in the current
   * state. If none exists, the parser action is set to the canonical error
   * action.
   */
  final void getParseAction () throws IOException
    {
      if (getParseActionFlag)
	{
	  parseAction = parseState.getAction((ParserTerminal)tokenNode().symbol());
	  if (parseAction == null || nonassociativeUnaryOperator())
	    parseAction = errorAction();
	}

      getParseActionFlag = true;
    }

  /**
   * Returns <tt>true</tt> iff this is a non associative unary operator
   * composed with itself.
   */
  final boolean nonassociativeUnaryOperator () throws IOException
    {
      ParseNode node = tokenNode();

      return (node.associativity() == Grammar.NON_ASSOCIATIVE
              && (node.fixity() == OperatorSymbol.POSTFIX
                  && parseAction.type == Action.REDUCE
                  && hasTag(rules[parseAction.info],node)
                  ||
                  node.fixity() == OperatorSymbol.PREFIX
                  && previousTokenIsSameOperator()));         
    }
  
  /**
   * Returns <tt>true</tt> iff the given node's operator is the same
   * as the last token's on the stack.
   */
  final boolean previousTokenIsSameOperator () throws IOException
    {
      ParseNode node = null;
        
      for (int i=parserStack.size()-1; i>=0; i--)
        {
          node = ((ParserStackElement)parserStack.get(i)).getNode();
          if (node.isTerminal())
	    return node.isOperator() && node.operator().equals(tokenNode().operator());
        }

      return false;
    }
  
  final String stringForm (ParserStackElement[] handle)
    {
      StringBuilder buf = new StringBuilder("[");

      for (int i=0; i<handle.length; i++)
        {
          buf.append(handle[i].getNode().nodeInfo());
          if (i < handle.length-1)
	    buf.append(", ");
        }

      buf.append("]");
      return buf.toString();
    }

  /**
   * Pops the <i>n</i> latest elements on the parser stack, where <i>n</i>
   * is the length of the current rule's RHS. This may also build a parse
   * tree as specified by <tt>parseTreeType</tt>.
   */
  final void popHandle ()
    {
      super.popHandle();

      if (!choiceStack.isEmpty())
	{
	  TrailEntry entry = new TrailEntry(parseHandle,parseRule);
	  stamp(entry);

          if ((entry = (TrailEntry)trailStack.push(entry)) != null)
	    {
              while (!choiceStack.isEmpty()
                     && ((Choice)choiceStack.oldest()).getTimeStamp() < entry.getTimeStamp())
                choiceStack.drop();
              
              if (choiceStack.isEmpty())
		trailStack.flush();
            }
        }         
    }

  /**
   * Restores the parser's state from the choice point and trail stacks
   * to the most recent recoverable configuration.
   */
  private final void backtrack () throws IOException
    {
      if (trace)
	out.println("Backtracking... ");

      Choice choice = (Choice)choiceStack.peek();  // guaranteed non null thanks to test before
					           // backtrack() is called in perforemParseAction()

      undo(choice);

      Object nextChoice = choice.options.pop();
      if (choice.options.isEmpty())
	choiceStack.pop();

      if (choice.isTokenChoice())
	{
	  tokenNode = (DynamicToken)nextChoice;
	  readTokenFlag = false;
	  getParseActionFlag = true;
	}
      else
	{
	  parseAction = (ParserAction)nextChoice;
	  readTokenFlag = true;
	  getParseActionFlag = false;
	}

      if (choiceStack.isEmpty())
	trailStack.flush();

      if (trace)
	showDynamicState();
    }

  /**
   * Undoes all work done after the specified choice point.
   */
  private final void undo (Choice choice) throws IOException
    {
      long stamp = choice.getTimeStamp();

      DynamicToken token = (DynamicToken)tokenNode;

      if (trace)
	err.println("Undoing stamp: "+stamp);

      if (!choice.isTokenChoice() || token.getTimeStamp() > stamp)
	{	  
	  readStack.push(token.getOriginal());
	  if (trace)
	    {
	      err.println("Unreading token: "+token);
	      err.println(Misc.view(readStack,"   read stack",0,80));
	    }
	}

      ParserStackElement element = (ParserStackElement)parserStack.peek();
      
      while (element.getTimeStamp() > stamp)
        {
	  if (trace)
	    err.println("Popping parser stack element: "+element);

	  parserStack.pop();

          if (element.getNode().isTerminal())
	    {
              token = (DynamicToken)element.getNode();
	      if (!choice.isTokenChoice() || token.getTimeStamp() > stamp)
		{
		  readStack.push(token.getOriginal());
		  if (trace)
		    {
		      err.println("Unreading token: "+token);
		      err.println(Misc.view(readStack,"   read stack",0,80));
		    }
		}
            }
          else
            {
              TrailEntry trail = (TrailEntry)trailStack.pop();

              for (int i=0; i<trail.handle.length; i++)
                parserStack.push(trail.handle[i]);

	      undoSemanticAction(trail.rule,element.getNode());
            }

          element = (ParserStackElement)parserStack.peek();
        }
    }

  /**
   * Erases all backtracking information.
   */
  public final void cutAll ()
    {
      choiceStack.flush();
      trailStack.flush();
    }

  /** 
   * Erases the latest backtracking information.
   */
  public final void cut ()
    {
      choiceStack.pop();
      if (choiceStack.isEmpty())
	trailStack.flush();
    }

  final void trace (ParserAction a) throws IOException
    {
      traceAction(a);
      showDynamicState();
      step();
    }

  /**
   * Performs the parse action and returns false iff it is an <tt>ACCEPT</tt>
   * action.
   */
  final boolean performParseAction () throws IOException
    {
      switch (parseAction.type)
        {
        case Action.ACCEPT:
          return false;
        case Action.DYNAMIC:
          resolveDynamicAction();
          return performParseAction();
        case Action.CHOICE:
          resolveChoiceAction();
          return performParseAction();
        case Action.SHIFT:
	  shift();
          break;
        case Action.REDUCE:
          reduce();
          if (parseState != null)
	    break;
        case Action.ERROR:
          if (!choiceStack.isEmpty())
	    backtrack();
          else
            recoverFromError();
        }

      return true;
    }

  /**
   * Looks up the current state's dynamic actions array and chooses the
   * most appropriate action according to the current parser stack and
   * token node.
   */
  private final void resolveDynamicAction () throws IOException
    {
      ParserAction[] actions = currentState().dynamicActions[parseAction.info];
      parseAction = actions[0];
      for (int i=1; i<actions.length; i++)
        parseAction = chooseAction(parseAction,actions[i]);

      if (nonassociativeUnaryOperator())
	parseAction = errorAction();
    }

  /**
   * Sets up a choice point for a multiple choice action.
   */
  private final void resolveChoiceAction () throws IOException
    {
      ParserAction[] actions = currentState().dynamicActions[parseAction.info];
      parseAction = actions[0];

      Choice choice = new Choice(actions.length-1);
      choice.setIsTokenChoice(false);

      for (int i=1; i<actions.length; i++)
        choice.addOption(actions[i]);

      if (!choice.isEmpty())
	pushChoice(choice);      
    }

  /**
   * Returns the action that takes precedence over the other according
   * to the current parser stack and token node.
   */
  private final ParserAction chooseAction (ParserAction a1, ParserAction a2) throws IOException
    {
      if (a1.type == Action.REDUCE)
	{
          if (a2.type == Action.REDUCE)
	    {
	      int a1_prec = precedence(rules[a1.info]);
	      int a2_prec = precedence(rules[a2.info]);

	      if (!resolveRRsWithPrecedence || a1_prec == a2_prec)
		// favor the earlier rule:
		return (a1.info < a2.info) ? a1 : a2;

	      // favor the rule with higher precedence:
	      return (a1_prec > a2_prec) ? a1 : a2;
	    }

          // a2 is a shift:

          ParserRule r = rules[a1.info];

          if (precedence(r) > tokenNode().precedence())
	    return a1;          // favor reduction

          if (precedence(r) < tokenNode().precedence())
	    return a2;          // favor shifting

          if (associativity(r) == Grammar.LEFT_ASSOCIATIVE)
	    return a1;          // favor reduction

          if (tokenNode().associativity() == Grammar.NON_ASSOCIATIVE
              && hasTag(r,tokenNode()))
	    return errorAction();       // bad operator composition

          return a2;            // otherwise, favor shifting
        }

      return chooseAction(a2,a1);
    }

  /**
   * The following is for showing items - for debugging purposes...
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       DISPLAYING       METHODS                     \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  void show () throws IOException
    {
      showParseState();
      showDynamicState();
    }

  void showDynamicState ()
    {
      if (!readStack.isEmpty())
	err.println(Misc.view(readStack,"   read stack",0,80));
      if (!trailStack.isEmpty())
	err.println(Misc.view(trailStack,"  trail stack",0,80));
      if (!choiceStack.isEmpty())
	err.println("choices ==> "+choiceStack);
    }

  public final void showOperators ()
    {
      if (!operators.isEmpty())
	{
          err.println("\nDYNAMIC OPERATORS:\n");
          err.println("  -----------------------------------------------");
          err.println("\tCATEGORY PRECEDENCE SPECIFIER OPERATOR");
          err.println("  -----------------------------------------------");
          for (Iterator e=operators.iterator(); e.hasNext();)
            {
              ParserOperator o = (ParserOperator)(e.next());
              err.println("  ["     + o.index()
			  + "]\t "  + o.category.name()
                          + "\t   " + Grammar.prologPrecedence(o.precedence())
                          + "\t\t"  + o.specifier()
                          + "\t  "  + o.name()
                          );
            }
          err.println("  -----------------------------------------------");
        }
    }
}
