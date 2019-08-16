//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import hlt.language.util.ArrayList;
import hlt.language.tools.Misc;
import hlt.language.util.Map;
import hlt.language.util.Verbose;

/**
 * This is the class that uses the analysis provided by a grammar object
 * (see <a href="Grammar.html"><tt>Grammar</tt></a>) and builds an LALR
 * parser's <i>action</i> and <i>goto</i> automata, reporting and
 * resolving conflicts as they may occur. It writes out a Java source
 * file which defines a class that extends either the class <a
 * href="StaticParser.html"><tt>StaticParser</tt></a> or the class <a
 * href="DynamicParser.html"><tt>DynamicParser</tt></a>, both of which
 * extend the class <a
 * href="GenericParser.html"><tt>GenericParser</tt></a>, wherein all
 * necessary data is initialized for the parser's <tt>parse()</tt>
 * method to work.  The <tt>parse()</tt> method assumes that the parser
 * disposes of a class implementing the <a
 * href="Tokenizer.html"><tt>Tokenizer</tt></a> interface. This
 * interface defines the <tt>nextToken()</tt> method, which provides the
 * stream of tokens to be parsed. Such a tokenizer is necessary for the
 * complete generated parser to work. It typically looks like:
 *
 * <p>
 * <pre>
 * <span style="color:brown">
 *             <b>import hlt.language.syntax.\*;</b>
 *
 *             class <b>MyTokenizer</b> implements <b>Tokenizer</b>
 *               {
 *                 MyTokenizer ( ... )
 *                   {
 *                     ...
 *                   }
 *
 *                 <b>public ParseNode nextToken()</b>
 *                   {
 *                     ...
 *                   }
 *               }
 * </span>
 * </pre>
 * <p>
 *
 * For detailed explanations of all constructions and algorithms used by this class, please refer to
 * the following:
 * <p>
 * <ul>
 * <li> Alfred Aho, Ravi Sethi, and Jeffrey Ullman, <i>Compilers. Principles.
 *      Techniques, and Tools.</i> Addison-Wesley, 1986.
 * </ul>
 *
 * @see         Grammar
 * @see         Options
 * @see         GenericParser
 * @see         StaticParser
 * @see		DynamicParser
 *
 * @version     Last modified on Wed Jul 25 05:11:40 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class ParserGenerator
{
  public ParserGenerator ()
    {
      buildParser();
    }
  
  static long startTime,
              tableBuildingStart,
              compressionStart,
              totalTime;

  Grammar grammar;

  BufferedWriter p_out;

  PrintStream out          = Options.getOutStream();
  PrintStream err          = Options.getErrStream();
  int verbosity            = Options.getVerbosity();
  String parserDestination = Options.destination();
  String parserPrefix      = Options.getParserPrefix();

  String parserFile     = parserPrefix+".java";
  String fullParserFile = parserDestination
                        + Options.getSeparator()
                        + parserFile;

  /**
   * The default action.
   */
  final String defaultAction = "$head$ = $head$.copy(node($rule$,1));";

  /**
   * Maximum index of parser table per initialization method;
   * This is to make sure that java does not complain of  a
   * method longer than 65535 bytes when initializing large
   * tables.
   */
  int initMethodSize = Options.getInitMethodSize();

  /**
   * Continuation method name for parser table initialization.
   * It is used when the number of instructions in a method
   * initializing a table exceeds <tt>initMethodSize</tt>.
   */
  String initContinuation = null;

  /**
   * Continuation counter for parser table initialization continuations.
   */
  int initContinuationCount;

  /**
   * Test and (maybe) generate a continuation for a method initializing
   * a parser table at index <tt>i</tt>.
   */
  final void testInitContinuation (int i) throws IOException
    {
      if (i>0 && i%initMethodSize == 0)
        {
          pl();
          pl("      "+initContinuation+"_"+(++initContinuationCount)+"();");
          pl("    }");
          pl();
          pl("  static void "+initContinuation+"_"+initContinuationCount+" ()");
          pl("    {");
        }
    }

  /***/
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                    BUILDING     THE      PARSER                    \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  final void buildParser ()
    {
      try
        {
          grammar = new Grammar();
          if (!Options.getDocOnly())
            {
              buildTables();
              writeParser();
            }
        }
      catch (Exception e)
        {
          Grammar.warning("Parser generation aborted!");
	  if (!(e instanceof BadGrammarException))
	    e.printStackTrace(err);
          System.exit(1); // exit with non-zero status code
        }
    }

  /**
   * Set to true if there are action conflicts.
   */
  private boolean conflict = false;

  /**
   * Number of unresolved S/R conflicts.
   */
  int usrCount;

  /**
   * Number of resolved S/R conflicts.
   */
  int rsrCount;

  /**
   * Number of R/R conflicts.
   */
  int rrCount;

  /**
   * This builds the action and goto tables from the information in each
   * state.  This is a straightforward procedure (see, <i>e.g.</i>,
   * Algorithm 4.10, Page 234, of the Dragon Book).
   */
  final void buildTables ()
    {
      State state;
      GrammarSymbol symbol;
      Item item;
      Terminal lookahead;
      Action action;

      reportProgress_1();
      
      action = new Action(Action.ERROR);
      action.add();
      acount++;

      action = new Action(Action.ACCEPT);
      action.add();
      acount++;

      Iterator s = grammar.states.iterator();
      while (s.hasNext())
        {
          state = (State)s.next();
          Iterator e = state.transitions.values().iterator(); 
          while (e.hasNext())
            {
              StateTransition transition = (StateTransition)e.next();
              symbol = transition.symbol;
              if (symbol instanceof NonTerminal)
                state.setGoto((NonTerminal)symbol,transition.next);
              else
                if (symbol.isEmpty())
                  {
                    Iterator i = transition.items.iterator();
                    while (i.hasNext())
                      {
                        item = (Item)i.next();
                        if ((item.rule.head()).isSTART())
                          state.setAction(Grammar.END_OF_INPUT,acceptAction());
                        else
                          {
                            Iterator f = item.getLookaheads(state).iterator();
                            while (f.hasNext())
                              {
                                lookahead = (Terminal)f.next();
                                action = new Action(Action.REDUCE,item.rule.index());
                                conflict |= checkConflict(lookahead,state,action);
                              }
                          }
                      }
                  }
                else
                  {
                    action = new Action(Action.SHIFT,transition.next.index());
                    conflict |= checkConflict((Terminal)symbol,state,action);
                  }
            }
        }

      reportProgress_2();
      
      compressTables();

      reportProgress_3();      
    }

  /** The set of action tables. */
  ArrayList ac_tables = new ArrayList();
  /** The number of action tables. */
  int ac_count = 0;
  /** The number of eliminated action tables. */
  int ac_compression = 0;

  /** The set of goto tables. */
  ArrayList gt_tables = new ArrayList();
  /** The number of goto tables. */
  int gt_count = 0;
  /** The number of eliminated goto tables. */
  int gt_compression = 0;

  final void compressTables ()
    {
      State state;
      int index;

      reportProgress_4();      
      
      for (Iterator e = grammar.states.iterator(); e.hasNext();)
        {
          state = (State)e.next();

          index = ac_tables.indexOf(state.actionTable);
          if (index < 0)
            {
              ac_tables.add(state.actionTable);
              state.ac_index = ac_count++;
            }
          else
            {
              state.actionTable = (Map)ac_tables.get(index);
              state.ac_index = index;
              ac_compression++;
            }

          index = gt_tables.indexOf(state.gotoTable);
          if (index < 0)
            {
              gt_tables.add(state.gotoTable);
              state.gt_index = gt_count++;
            }
          else
            {
              state.gotoTable = (Map)gt_tables.get(index);
              state.gt_index = index;
              gt_compression++;
            }
        }

      reportProgress_5();      
    }

  /**
   * The set of actions.
   */
  static ArrayList actions = new ArrayList();
  /**
   * A hash table for efficient retrieval of actions.
   */
  static HashMap actionTable = new HashMap();
  /**
   * The number of actions.
   */
  static int acount = 0;

  /**
   * Returns the canonical <tt>ERROR</tt> action.
   */
  final static Action errorAction ()
    {
      return (Action)actions.get(0);
    }

  /**
   * Returns the canonical <tt>ACCEPT</tt> action.
   */
  final static Action acceptAction ()
    {
      return (Action)actions.get(1);
    }

  /**
   * Returns the canonical representative for this action.
   */
  final Action checkAction (Action action)
    {
      Action a = (Action)actionTable.get(action);

      if (a != null) return a;
      
      action.add();
      acount++;
      actionTable.put(action,action);

      return action;
    }

  /**
   * Set to <tt>true</tt> whenever a conflict is unresolved.
   */
  private boolean conflictIsUnresolved = false;

  /**
   * Checks if an action already exists for this symbol in this
   * state. If no previous action exists, this simply installs
   * <tt>action</tt> as a static action for this symbol in this
   * state. Otherwise, let <tt>old</tt> be the previous action.  Note
   * that <tt>action</tt>, the new action to be checked, is always a
   * simple action - <i>i.e.</i>, it is never a dynamic nor a choice
   * action. On the other hand, <tt>old</tt> may be of any type,
   * including <tt>DYNAMIC</tt> or <tt>CHOICE</tt>.
   *
   * <p>
   *
   * Let us first consider the simpler case where <tt>old</tt> is
   * <b>not</b> a choice action.  Then, what must be done depends on
   * what the case is among six to consider:
   *
   * <ol>
   * <li><span style="color:navy">
   *     <tt>old</tt> is <tt>DYNAMIC</tt> and <tt>symbol</tt> or <tt>action</tt>
   *     involves an operator:</span>
   *     <ul>
   *     <li> if <tt>state.dynamicActions[old.info]</tt> does not already contain 
   *          <tt>action</tt>, add <tt>checkAction(action)</tt> to it;
   *     </ul>
   *
   * <li><span style="color:navy">
   *     <tt>old</tt> is <tt>DYNAMIC</tt> and neither <tt>symbol</tt> nor 
   *     <tt>action</tt> involves an operator:</span>
   *     <ul>
   *     <li> if <tt>state.dynamicActions[old.info]</tt> contains one
   *          non-operator action, resolve <tt>action</tt> with it and
   *          keep only the winner in there (note that, by construction,
   *          a state's dynamic actions at the index of a dynamic action
   *          necessarily never contains more than one such non-operator
   *          action); if no winner may be determined, add <tt>checkAction(action)</tt>
   *          to <tt>state.dynamicActions[old.info]</tt>, and transform
   *          <tt>old</tt> into a <tt>CHOICE</tt> action with the same
   *          <tt>info</tt> index;
   *     <li> otherwise, add <tt>checkAction(action)</tt> to it;
   *     </ul>
   *
   * <li><span style="color:navy">
   *     <tt>old</tt> is an operator action and neither <tt>symbol</tt> nor <tt>action</tt>
   *     involves an operator:</span>
   * <li><span style="color:navy">
   *     <tt>old</tt> is an operator action and <tt>symbol</tt> or <tt>action</tt> involves
   *     an operator:</span>
   * <li><span style="color:navy">
   *     <tt>old</tt> is not an operator action and <tt>symbol</tt> or
   *     <tt>action</tt> involves an operator:</span>
   *     <ul>
   *     <li> set the state's action for <tt>symbol</tt> to
   *          <tt>(DYNAMIC,state.dynamicActions.size())</tt>; 
   *     <li> install both <tt>old</tt> and <tt>checkAction(action)</tt> in a new ArrayList
   *          <tt>actions</tt>; 
   *     <li> add <tt>actions</tt> to <tt>state.dynamicActions</tt>;
   *     </ul>
   *
   * <li><span style="color:navy">
   *     <tt>old</tt> is not an operator action and neither <tt>symbol</tt> nor
   *     <tt>action</tt> involves an operator:</span>
   *     <ul>
   *     <li> resolve statically, or create a <tt>CHOICE</tt> action, as
   *          appropriate.
   *     </ul>
   * </ol>
   *
   * <p>
   *
   * Let us consider now the general case where the <tt>old</tt> action may
   * be a <tt>CHOICE</tt> action. Note that this affects only cases
   * (5) and (6) above. This gives two additional cases: 
   *
   * <ol start=7>
   *
   * <li><span style="color:navy">
   *     <tt>old</tt> is <tt>CHOICE</tt>  and <tt>symbol</tt> or
   *     <tt>action</tt> involves an operator:</span>
   *     <ul>
   *     <li> if <tt>state.dynamicActions[old.info]</tt> does not already contain 
   *          <tt>action</tt>, add <tt>checkAction(action)</tt> to it;
   *     </ul>
   *
   * <li><span style="color:navy">
   *     <tt>old</tt> is <tt>CHOICE</tt>  and neither <tt>symbol</tt> nor
   *     <tt>action</tt> involves an operator:</span>
   *     <ul>
   *     <li> if <tt>action</tt> can be determined statically to win over
   *          <i>all</i> actions in <tt>state.dynamicActions[old.info]</tt>,
   *          then replace <tt>old</tt> with <tt>checkAction(action)</tt>;
   *     <li> otherwise, add <tt>checkAction(action)</tt> to
   *          <tt>state.dynamicActions[old.info]</tt> if it does
   *          not already contain it.
   *     </ul>
   *
   * </ol>
   * */
  final boolean checkConflict (Terminal symbol, State state, Action action)
    {
      Action old = state.getAction(symbol);

      if (old == null)
        {
          state.setAction(symbol,checkAction(action));
          return false;
        }

      if (old.type == Action.DYNAMIC)
        {
          ArrayList actions = (ArrayList)state.dynamicActions.get(old.info);
          
          if (symbol.isOperator || isOperator(action))
            {
              // Case 1
              if (!actions.contains(action))
                actions.add(checkAction(action));
            }
          else
            {
              // Case 2
              int i = findContender(actions);
              if (i == -1)
                actions.add(checkAction(action));
              else
                {
                  Action contender =  (Action)actions.get(i);
                  Action choice = resolveConflict(contender,action,symbol);

		  if (conflictIsUnresolved && Options.allowChoiceActions())
		    {
		      actions.add(checkAction(action));
		      old.type = Action.CHOICE;
		      choice = old;
		      conflictIsUnresolved = false;
		    }
		  else
		    if (choice == action)
		      actions.set(i,action);
		    else
		      contender = action;

		  tallyConflict(choice,contender,state,symbol);
                }
            }
        }
      else // old is not a dynamic action
	if (old.type != Action.CHOICE)
	  if (isOperator(old) || symbol.isOperator || isOperator(action))
	    {
	      // Cases 3, 4, and 5
	      ArrayList actions = new ArrayList();
	      actions.add(old);
	      actions.add(checkAction(action));

	      int index = state.dynamicActions.indexOf(actions);

	      if (index == -1)
		{
		  index = state.dynamicActions.size();
		  state.dynamicActions.add(actions);
		}

	      state.setAction(symbol,
			      checkAction(new Action(Action.DYNAMIC,index)));
	    }
	  else
	    {
	      // Case 6
	      Action choice = resolveConflict(old,action,symbol);

	      if (conflictIsUnresolved && Options.allowChoiceActions())
		{
		  ArrayList actions = new ArrayList(2);
		  actions.add(old);
		  actions.add(checkAction(action));

		  int index = state.dynamicActions.indexOf(actions);

		  if (index == -1)
		    {
		      index = state.dynamicActions.size();
		      state.dynamicActions.add(actions);
		    }

		  choice = new Action(Action.CHOICE,index);
		  state.setAction(symbol,checkAction(choice));
		  conflictIsUnresolved = false;
		}
	      else
		if (choice == action)
		  state.setAction(symbol,checkAction(choice));
		else
		  old = action;
	      
	      tallyConflict(choice,old,state,symbol);
	    }
	else // old is a choice action
	  {
	    ArrayList actions = (ArrayList)state.dynamicActions.get(old.info);

	    if (symbol.isOperator || isOperator(action))
	      {
		// case 7
		if (!actions.contains(action))
		  actions.add(checkAction(action));
	      }
	    else
	      {
		// case 8
		boolean winAll = true;

		for (Iterator i=actions.iterator(); i.hasNext();)
		  {
		    Action option = (Action)i.next();
		    if (isOperator(option)
			|| option == resolveConflict(option,action,symbol))
		      {
			winAll = false;
			break;
		      }
		  }

		if (winAll)
		  state.setAction(symbol,checkAction(action));
		else
		  if (!actions.contains(action))
		    actions.add(checkAction(action));		      
	      }
	  }

      return conflictIsUnresolved;
    }

  /**
   * Records a message for reporting a conflict between the two
   * specified actions, in the given state, for the given symbol, as
   * appropriate according to optional conditions.
   */
  private final void tallyConflict (Action choice, Action contender,
				    State state,   Terminal symbol)
    {
      boolean reportConflict = conflictIsUnresolved
	   && !Options.allowChoiceActions();

      String conflict = choice.conflict(contender) + " conflict: choosing "
                      + choice + "\tover " + contender;

      state.addConflict((reportConflict ? "Unresolved " : "Resolved   ") +
			conflict + ", \ton input "+symbol);

      if (reportConflict && verbosity > Verbose.NORMAL)
	out.println("*** Unresolved "+conflict+",\tin state "+state
		    +", \ton input "+symbol);
    }

  /**
   * Returns <tt>true</tt> iff the action is a reduction with a rule tagged
   * by a dynamic operator.
   */
  final boolean isOperator (Action action)
    {
      return (action.type == Action.REDUCE
              && grammar.getRule(action.info).isOperator());
    }

  /**
   * Returns the position of <i>the</i> non-operator action in this ArrayList
   * of dynamic actions, or -1 if none such exists.
   */
  final int findContender (ArrayList actions)
    {
      for (int i=0; i<actions.size(); i++)
        if (!isOperator((Action)actions.get(i))) return i;

      return -1;
    }

  /**
   * This resolves statically conflicting actions based on rule precedence or
   * order, or precedence and associativity information attached to a terminal.
   * Such information is also associated with a rule by way of its <i>tag</i>.
   * A rule's tag is the last terminal that occurs in its body, or specified by
   * the <tt>%prec</tt> command. If a rule has no explicit tag, then it
   * defaults to the empty symbol (<i>i.e.</i>, <tt>Grammar.EMPTY</tt>), whose
   * precedence is <tt>Grammar.MIN_PRECEDENCE</tt> and associativity is
   * <tt>Grammar.NON_ASSOCIATIVE</tt>.
   *
   * <p>
   *
   * <b>N.B.</b>: By default, R/R conflicts are resolved as yacc would -
   * namely, choosing the rule that appears first in the grammar. The
   * disadvantage of this is that rule order becomes significant. Thus,
   * another mode may be used that allows the rule with higher tag precedence
   * to win, and in case of equality, the earlier rule wins. With this, one
   * can use the <tt>%prec</tt> command to control R/R conflict resolutions
   * explicitly. The class <tt>Options</tt> offers the static methods
   * <tt>Options.resolveRRsWithPrecedence()</tt> and
   * <tt>Options.setResolveRRsWithPrecedence(boolean)</tt> to use this
   * convenience. <b><i>Caveat Emptor</i></b>: Using this mode to resolve
   * R/R conflict may interfere in subtle ways with S/R conflict resolution
   * which will quietly use the specified rule precedence to choose between
   * a shift and a reduce action <i>without reporting a conflict!</i>
   * One must thus be acutely aware of all such interplays as they may give
   * surprising results.
   *
   * <p>
   *
   * Only implicitly resolved conflicts are reported as warnings - namely,
   * S/R conflicts that cannnot be resolved thanks to terminal symbol's
   * precedence and associativiy, and R/R conflicts resolved in favor of
   * an earlier rule (that is, if the yacc resolution mode is used, or if
   * not and the rules have equal precedences).
   *
   * @returns the winning action
   * @see Options
   *
   */
  final Action resolveConflict (Action old, Action action, Terminal t)
    {
      conflictIsUnresolved = false;

      if (old.type == Action.REDUCE)
        {
          if (action.type == Action.REDUCE)
            {
	      int old_prec = grammar.getRule(old.info).precedence();
	      int new_prec = grammar.getRule(action.info).precedence();

	      if (!Options.resolveRRsWithPrecedence() || old_prec == new_prec)
		{
		  conflictIsUnresolved = true;
		  rrCount++;
		  // favor the earlier rule:
		  return (old.info < action.info) ? old : action;
		}

	      // favor the rule with higher precedence:
	      return (old_prec > new_prec) ? old : action;
            }
          
          // So the new action is a shift:
          
          Rule r = grammar.getRule(old.info);

          rsrCount++;
          
          if (r.precedence() > t.precedence)
            return old;               // favor reduction

          if (r.precedence() < t.precedence)
            return action;            // favor shifting

          if (r.associativity() == Grammar.LEFT_ASSOCIATIVE)
            return old;               // favor reduction

          if (r.associativity() == Grammar.RIGHT_ASSOCIATIVE)
            return action;            // favor shifting

          if ((t.associativity == Grammar.NON_ASSOCIATIVE) && (t == r.tag))
            {
              if (verbosity > Verbose.NORMAL)
                Grammar.warning("Rule "+r.index()+
                                " may compose the non-associative symbol: "+t);
              return errorAction();
            }

          rsrCount--;
          usrCount++;

          conflictIsUnresolved = true;
          return action;        // otherwise, favor shifting
        }
      
      return resolveConflict(action,old,t);
    }

  /***/
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                     WRITING     THE      PARSER                    \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  final void pl () throws IOException
    {
      p_out.write('\n');
    }

  final void pl (String line) throws IOException
    {
      p_out.write(line+"\n");
    }

  final void p (String s) throws IOException
    {
      p_out.write(s);
    }

  final void p (int c) throws IOException
    {
      p_out.write(c);
    }

  final void setOutput (String fileName) throws IOException
    {
      p_out = new BufferedWriter(new FileWriter(fileName));
    }

  final void writePreamble () throws IOException
    {
      pl("// *******************************************************************");
      pl("// This file has been automatically generated from the grammar in file");
      pl("// "+grammar.grammarName+" by hlt.language.syntax.ParserGenerator on");
      pl("// "+(new Date())+" --- !!! PLEASE DO NO EDIT !!!");
      pl("// *******************************************************************");
      pl();

      if (grammar.packageName != null) pl("package "+grammar.packageName+";\n");

      pl("import java.io.Reader;");
      pl("import java.io.StringReader;");
      pl("import java.io.IOException;");
      if (grammar.isDynamic)
	{
	  pl("import hlt.language.util.ArrayList;");
	  pl("import hlt.language.util.FiniteStack;");
	}
      pl("import hlt.language.syntax.*;");

      for (Iterator e = grammar.imports.iterator(); e.hasNext();)
        pl("import "+e.next()+";");

      pl();
    }
  
  final void writePublicClasses () throws IOException
    {
      for (Iterator k = grammar.publicClasses.keySet().iterator(); k.hasNext();)
        {
          String className = (String)k.next();
          String classDef  = grammar.publicClasses.get(className).toString();

          setOutput(parserDestination + Options.getSeparator() + className + ".java");
          writePreamble();
          pl(classDef);
          p_out.close();
        }
    }

  String usefile;

  final void dumpCode (ArrayList code, String banner) throws IOException
    {
      if (!code.isEmpty())
        {
          pl(banner);

          for (Iterator e = code.iterator(); e.hasNext();)
            {
              Object element = e.next();

              if (element instanceof String)
                { usefile = (String)element;
                  try
                    {
                      BufferedReader rd = new BufferedReader(new FileReader(usefile));
                      reportProgress_6();
                      pl("\n  /* START OF CONTENTS OF FILE: "+usefile+" */\n");
                      for (int c=rd.read(); c!=-1; c=rd.read()) p(c);
                      rd.close();
                      reportProgress_7();
                      pl("  /* END OF CONTENTS OF FILE: "+usefile+" */\n");
                    }
                  catch (FileNotFoundException nofile)
                    {
                      Grammar.warning("Cannot include file: "
                                      +usefile+" (not found!)");
                    }
                  catch (IOException io)
                    {
                      Grammar.warning("Something wrong happened while reading: "
                                      +usefile+": "+io);
                    }
                  continue;
                }

              pl(element.toString());
              pl();
            }
        }
    }

  final void writeParser ()
    {
      if (Options.getNoParser())
	return;

      reportProgress_8();

      Terminal t;
      NonTerminal n;
      Rule r;
      Action a;
      Map m;
      State s;

      String superClass = (grammar.isDynamic ? "Dynamic" : "Static");
      
      try
        {
          setOutput(fullParserFile);

          writePreamble();
          
          pl();
          pl("/* ************ */");
          pl("/* PARSER CLASS */");
          pl("/* ************ */");
          pl();

          pl(grammar.accessTag+"class "+parserPrefix
             +" extends "+superClass+"Parser\n{");

          pl("  /* ************************ */");
          pl("  /* PARSER CLASS CONSTRUCTOR */");
          pl("  /* ************************ */");
          pl();

          pl("  public "+parserPrefix+" (Tokenizer t)\n    {\n      input = t;");
	  if (grammar.xmlroot() != null)
	    pl("      xmlroot = \""+grammar.xmlroot()+"\";");
	  if (grammar.xmlRootNSPrefix() != "")
	    pl("      xmlRootNSPrefix = \""+grammar.xmlRootNSPrefix()+"\";");
	  if (!grammar.namespaces().isEmpty())
	    {
	      pl("      String[] ns = "
		 +Misc.arrayToString(grammar.namespaces().toStringArray(),"{",",","}")
		 +";");
	      pl("      namespaces = ns;");
	    }

	  if (grammar.isDynamic)
	    {
	      pl("      choiceStack = new FiniteStack("+Options.getChoiceHistory()+");");
	      pl("      trailStack = new FiniteStack("+Options.getTrailHistory()+");");
	      pl("      resolveRRsWithPrecedence = "+Options.resolveRRsWithPrecedence()+";");
	    }

          if (grammar.admitsOperators())
            {
              pl();
              pl("      /* **************** */");
              pl("      /* OPERATOR SYMBOLS */");
              pl("      /* **************** */");
              pl();

              pl("      operators = new ArrayList("
                 +grammar.operators.size()
                 +");\n");

              for (int i=0; i<grammar.ocount; i++)
                {
                  Operator o = grammar.getOperator(i);
                  pl("      newOperator(\""+Misc.quotify(o.name)+"\","+o.category.index()+","
                     +o.precedence+","+o.associativity+","+o.fixity+");");
                }
            }
// 	  else
// 	    pl("      admitsOperators = false;");
	  
          pl("    }\n");

          dumpCode(grammar.parserDeclarations,
                   "  /* ************************* */\n"+
                   "  /* PARSER CLASS DECLARATIONS */\n"+
                   "  /* ************************* */");

          pl("  /* ********************** */");
          pl("  /* STATIC INITIALIZATIONS */");
          pl("  /* ********************** */");
          pl();
          pl("  static");
          pl("    {");
          pl("      initializeTerminals();");
          pl("      initializeNonTerminals();");
          pl("      initializeRules();");
          pl("      initializeParserActions();");
          pl("      initializeParserStates();");
          pl("      initializeActionTables();");
          pl("      initializeGotoTables();");
          pl("      initializeStateTables();");
          pl("    }\n");

          pl("  /* ********************* */");
          pl("  /* PARTIAL PARSE METHODS */");
          pl("  /* ********************* */");
          pl();

          for (Iterator e=grammar.roots.keySet().iterator(); e.hasNext();)
            {
              NonTerminal root = (NonTerminal)e.next();
              Terminal token = (Terminal)grammar.roots.get(root);

              String rootName = Misc.capitalize(root.name());
              String tokenName = token.name().toUpperCase();

              pl("  final static ParseNode "+tokenName+
                 " = new ParseNode(terminals["+token.index()+"]);");
              pl();

              pl("  public final void parse"+rootName+" (String s) throws IOException");
              pl("    {");
              pl("      parse"+rootName+"(new StringReader(s));");
              pl("    }");
              pl();

              pl("  public final void parse"+rootName+" (Reader r) throws IOException");
              pl("    {");
              pl("      input.setReader(r);");
              pl("      errorManager().recoverFromErrors(false);");
              pl("      setSwitchToken("+tokenName+");");
              pl("      parse();");
              pl("    }");
              pl();
            }

          pl("  /* **************** */");
          pl("  /* SEMANTIC ACTIONS */");
          pl("  /* **************** */");
          pl();

          pl("  protected ParseNode semanticAction(ParserRule $rule$) throws IOException\n    {");

          pl("      ParseNode $head$ = new ParseNode($rule$.head);\n");

          ArrayList emptyCases = new ArrayList();

          pl("      switch($rule$.index())\n        {");
          for (int i=0; i<grammar.rcount; i++)
            {
              r = grammar.getRule(i);
              if (r.action.equals(Grammar.EMPTY_ACTION))
                emptyCases.add(Integer.valueOf(i));
              else
                if (!r.action.equals(Grammar.DEFAULT_ACTION))
                  pl("          case "+i+":\n            "+
                     // NB: each semantic rule has its own lexical scope:
                     "{\n            "+
                     r.action.trim()+"\n            break;"+
                     "\n            }");
            }

          if (!emptyCases.isEmpty())
            {
              p("          ");
              for (Iterator e = emptyCases.iterator(); e.hasNext();)
                p("case "+((Integer)e.next())+": ");
              pl("\n            break;");
            }         

          pl("          default:\n            "+defaultAction+"\n            break;");
          pl("        }\n      return $head$;\n    }");
          pl();

          if (grammar.isDynamic)
            {
              pl("  /* ********************* */");
              pl("  /* UNDO SEMANTIC ACTIONS */");
              pl("  /* ********************* */");
              pl();

              pl("  protected void undoSemanticAction(ParserRule $rule$,ParseNode $head$) throws IOException");
              pl("    {");

              pl("      switch($rule$.index())\n        {");
              for (int i=0; i<grammar.rcount; i++)
                {
                  r = grammar.getRule(i);
                  if (!r.undoAction.equals(Grammar.EMPTY_ACTION))
                  pl("          case "+i+":\n            "+
                     // NB: each semantic rule has its own lexical scope:
                     "{\n            "+
                     r.undoAction.trim()+"\n            break;"+
                     "\n            }");;
                }
              pl("        }\n      }");
              pl();

              pl("  /* *************************** */");
              pl("  /* OPERATOR DEFINITION METHODS */");
              pl("  /* *************************** */");
              pl();

              for (Iterator e=grammar.operatorCategoryTable.keySet().iterator(); e.hasNext();)
                {
                  String cat = (String)e.next();
                  pl("  public final void "+cat+
                     " (String o, String s, int p) throws NonFatalParseErrorException");
                  pl("    {");
                  pl("      defineOperator(\""+Misc.quotify(cat)+"\",o,s,p);");
                  pl("    }");
                  pl();
                }
            }

          pl("  /* **************** */");
          pl("  /* TERMINAL SYMBOLS */");
          pl("  /* **************** */");
          pl();
          
          initContinuation = "initializeTerminals";
          initContinuationCount = 0;

          pl("  static void initializeTerminals ()");
          pl("    {");

          pl("      terminals = new ParserTerminal["+grammar.tcount+"];\n");

          for (int i=0; i<grammar.tcount; i++)
            {
              testInitContinuation(i);
              t = grammar.getTerminal(i);
              pl("      newTerminal("+i+",\""+Misc.quotify(t.name)+"\","
                                     +t.precedence+","+t.associativity+");");
	      if (t.xmlInfo() != null)
		t.xmlInfo().generateTerminalXmlInfo(this,i);
            }
          pl("    }");

          pl();
          pl("  /* ******************** */");
          pl("  /* NON-TERMINAL SYMBOLS */");
          pl("  /* ******************** */");
          pl();

          initContinuation = "initializeNonTerminals";
          initContinuationCount = 0;

          pl("  static void initializeNonTerminals ()");
          pl("    {");
          pl("      nonterminals = new ParserNonTerminal["+grammar.ncount+"];\n");

          for (int i=0; i<grammar.ncount; i++)
            {
              testInitContinuation(i);
              pl("      newNonTerminal("+i+",\""
		 +Misc.quotify(grammar.getNonTerminal(i).name)+"\");");
            }
          pl("    }");

          pl();
          pl("  /* **************** */");
          pl("  /* PRODUCTION RULES */");
          pl("  /* **************** */");
          pl();

          initContinuation = "initializeRules";
          initContinuationCount = 0;

          pl("  static void initializeRules ()");
          pl("    {");
          pl("      rules = new ParserRule["+grammar.rcount+"];");
	  pl();

          for (int i=0; i<grammar.rcount; i++)
            {
              testInitContinuation(i);
              r = grammar.getRule(i);
              if (r.isOperator())
                pl("      rules["+i+"] = new ParserRule("+r.head().index()+","+
                   +(r.sequence.length-1)+","+i+","+r.tagPosition+");");
              else
		{
		  pl("      rules["+i+"] = new ParserRule("+r.head().index()+","+
		     +(r.sequence.length-1)+","+i+","
		     +r.precedence()+","+r.associativity()+");");
		  if (r.xmlInfo() != null)
		    r.xmlInfo().generateRuleXmlInfo(this,i);
		}
	    }
          pl("    }");

          pl();
          pl("  /* ************** */");
          pl("  /* PARSER ACTIONS */");
          pl("  /* ************** */");
          pl();

          initContinuation = "initializeParserActions";
          initContinuationCount = 0;

          pl("  static void initializeParserActions ()");
          pl("    {");
          pl("      actions = new ParserAction["+acount+"];\n");

          for (int i=0; i<acount; i++)
            {
              testInitContinuation(i);
              a = (Action)actions.get(i);
              pl("      newAction("+i+","+a.type+","+a.info+");");
            }
          pl("    }");

          pl();
          pl("  /* ************* */");
          pl("  /* PARSER STATES */");
          pl("  /* ************* */");
          pl();

          pl("  static void initializeParserStates ()");
          pl("    {");
          pl("      states = new ParserState["+grammar.scount+"];\n");
          
          pl("      for (int i=0; i<"+grammar.scount+"; i++) newState(i);");
          pl("    }");

          pl();
          pl("  /* ************* */");
          pl("  /* ACTION TABLES */");
          pl("  /* ************* */");
          pl();

          initContinuation = "initializeActionTables";
          initContinuationCount = 0;

          pl("  static void initializeActionTables ()");
          pl("    {");
          pl("      newActionTables("+ac_count+");\n");

          int lines = 0;
          for (int i=0; i<ac_count; i++)
            {
              testInitContinuation(lines);
              m = (Map)ac_tables.get(i);
              pl("      newActionTable("+i+","+m.size()+");");
              for (Iterator e = m.keySet().iterator(); e.hasNext();)
                {
                  testInitContinuation(lines);
                  t = (Terminal)e.next();
                  a = (Action)m.get(t);
                  pl("\tsetAction("+i+","+t.index()+","+a.index()+");");
                  lines++;
                }
              pl();
            }
          pl("    }\n");

          pl("  /* *********** */");
          pl("  /* GOTO TABLES */");
          pl("  /* *********** */");
          pl();

          initContinuation = "initializeGotoTables";
          initContinuationCount = 0;

          pl("  static void initializeGotoTables ()");
          pl("    {");
          pl("      newGotoTables("+gt_count+");\n");

          lines = 0;
          for (int i=0; i<gt_count; i++)
            {
              testInitContinuation(lines);
              m = (Map)gt_tables.get(i);
              pl("      newGotoTable("+i+","+m.size()+");");
              for (Iterator e = m.keySet().iterator(); e.hasNext();)
                {
                  testInitContinuation(lines);
                  n = (NonTerminal)e.next();
                  s = (State)m.get(n);
                  pl("\tsetGoto("+i+","+n.index()+","+s.index()+");");
                  lines++;
                }
              pl();
            }
          pl("    }\n");

          pl("  /* ************ */");
          pl("  /* STATE TABLES */");
          pl("  /* ************ */");
          pl();

          initContinuation = "initializeStateTables";
          initContinuationCount = 0;

          pl("  static void initializeStateTables ()");
          pl("    {");

          lines = 0;
          for (int i=0; i<grammar.scount; i++)
            {
              testInitContinuation(lines);
              s = (State)grammar.states.get(i);
              pl("      setTables("+i+","+s.ac_index+","+s.gt_index+");");
              if (s.dynamicActions.size() > 0)
                {
                  pl("//    Dynamic Actions in State "+i+":");
                  pl("\t newDynamicActionTable("+i+","+s.dynamicActions.size()+");");
                  lines++;
                  for (int j=0; j<s.dynamicActions.size(); j++)
                    {
                      testInitContinuation(lines);
                      ArrayList actions = (ArrayList)s.dynamicActions.get(j);
                      pl("\t     newDynamicActions("+i+","+j+","+actions.size()+");");
                      lines++;
                      for (int k=0; k<actions.size(); k++)
                        {
                          testInitContinuation(lines);
                          a = (Action)actions.get(k);
                          pl("\t      setDynamicAction("+i+","+j+","+k+","+a.index()+");");
                          lines++;
                        }
                    }
                }
            }
          pl("    }");

          pl("}");

          dumpCode(grammar.ancillaryClasses,
                   "\n/* ***************** */"+
                   "\n/* ANCILLARY CLASSES */"+
                   "\n/* ***************** */\n");

          p_out.close();

          writePublicClasses();
        }

      catch (IOException e)
        {
          grammar.warning("Something wrong happened while generating parser file(s) "
                          +parserFile);
          err.println(e);
          return;
        }

      reportProgress_9();      
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private final void reportProgress_1 ()
    {
      startTime = System.currentTimeMillis();

      if (verbosity > Verbose.QUIET)
        out.println("*** Building parsing tables ... ");

      tableBuildingStart = System.currentTimeMillis();
    }

   private final void reportProgress_2 ()
    {
      if (verbosity > Verbose.NORMAL)
        out.println("***\t... in "+
                    (System.currentTimeMillis()-tableBuildingStart)+" ms");

      if (verbosity > Verbose.DETAILED)
        grammar.showStates();

      if (!Options.allowChoiceActions() && usrCount + rrCount > 0)
	{
	  String msg = "unresolved conflicts: ";
	  if (usrCount > 0)
	    {
	      msg += usrCount + " shift/reduce";
	      if (rrCount >0)
		msg += "; ";
	    }
	  if (rrCount > 0)
	    msg += rrCount + " reduce/reduce";
	  Grammar.warning(msg);
	}
    }

   private final void reportProgress_3 ()
    {
      if (verbosity > Verbose.DETAILED)
	showTables();
    }

   private final void reportProgress_4 ()
    {
      if (verbosity > Verbose.NORMAL)
        out.println("*** Compressing parsing tables ... ");

      compressionStart = System.currentTimeMillis();    
    }

   private final void reportProgress_5 ()
    {
      if (verbosity > Verbose.NORMAL)
        {
          out.println("***\t"+ac_compression+" rows eliminated in action table");
          out.println("***\t"+gt_compression+" rows eliminated in goto table");
          out.println("*** Table compression completed in "+
                      (System.currentTimeMillis()-compressionStart)+" ms");
        }
    }

   private final void reportProgress_6 ()
    {
      if (verbosity > Verbose.QUIET)
        out.print("***\tIncluding file "+usefile+"... ");
    }

   private final void reportProgress_7 ()
    {
      if (verbosity > Verbose.QUIET)
        out.println("Done.");                 
    }

   private final void reportProgress_8 ()
    {
      if (verbosity > Verbose.QUIET)
        out.println("*** Writing parser file "+fullParserFile);
    }

   private final void reportProgress_9 ()
    {
      totalTime = System.currentTimeMillis()-startTime;
      if (verbosity > Verbose.QUIET)
        {
          out.println("*** Parser generation completed in "+totalTime+" ms.");
          out.println("*** Total processing time: "+
                      (grammar.totalTime+totalTime)+" ms.\n");
        }
    }

  final void showTables()
    {
      int i,j;
      State s,g;
      Terminal t;
      NonTerminal n;
      Action a;
      boolean isDynamic = false;

      out.println("\n\nACTION TABLE:\n");

      for (i=0; i<grammar.tcount; i++)
        {
          t = grammar.getTerminal(i);
          if (!t.isEmpty())
            out.print("\t["+i+"]");
        }
      
      out.print("\n\t");
      for (i=0; i<grammar.tcount-1;i++)
        out.print("________");

      for (i=0; i<grammar.scount; i++)
        {
          s = grammar.getState(i);
          isDynamic |= (s.dynamicActions.size() > 0);             
          out.print("\n["+i+"]");
          for (j = 0; j<grammar.tcount; j++)
            {
              t = grammar.getTerminal(j);
              if (!t.isEmpty())
                {
                  out.print("\t");
                  a = s.getAction(t);
                  if (a == null)
                    out.print(" - ");
                  else
                    out.print(" "+a);
                }
            }
        }

      if (isDynamic)
        {
          out.println("\n\n\nDYNAMIC ACTIONS:\n");
          for (i=0; i<grammar.scount; i++)
            {
              s = grammar.getState(i);
              if (s.dynamicActions.size() > 0) out.print("["+i+"]");
              for (j = 0; j<s.dynamicActions.size(); j++)
                {
                  ArrayList actions = (ArrayList)s.dynamicActions.get(j);
                  out.print("\tD"+j+" -> { ");
                  for (int k = 0; k<actions.size(); k++)
                    {
                      out.print((Action)actions.get(k));
                      if (k<actions.size()-1)
                        out.print(", ");
                      else
                        out.print(" }");
                    }
                  out.print("\n");
                }
            }
        }

      out.println("\n\nGOTO TABLE:\n");
      
      for (i=0; i<grammar.ncount; i++)
        {
          n = grammar.getNonTerminal(i);
          if (!n.isSTART()) out.print("\t["+i+"]");
        }
      
      out.print("\n\t");
      for (i=0;i<grammar.ncount;i++)
        out.print("_______");

      for (i=0; i<grammar.scount; i++)
        {
          s = grammar.getState(i);
          out.print("\n["+i+"]");
          for (j=0; j<grammar.ncount; j++)
            {
              n = grammar.getNonTerminal(j);
              if (!n.isSTART())
                {
                  out.print("\t");
                  g = s.getGoto(n);
                  if (g == null)
                    out.print(" - ");
                  else
                    out.print(" "+g+" ");
                }
            }
        }

      out.println("\n");
    }
}
