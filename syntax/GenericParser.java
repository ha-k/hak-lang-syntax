//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.HashMap;
import java.util.Iterator;
import java.io.Reader;
import java.io.IOException;
import java.io.PrintStream;

import hlt.language.io.IncludeReader;
import hlt.language.util.Error;
import hlt.language.util.Stack;
import hlt.language.util.Locatable;
import hlt.language.tools.Debug;
import hlt.language.tools.Misc;

import org.jdom2.Document;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * This is the generic parser that is inherited by all parser classes
 * generated by <tt>ParserGenerator</tt>. It is further subclassed by
 * two other abstract classes:
 * <tt>StaticParser</tt> and <tt>DynamicParser</tt>.
 *
 * @see         ParserGenerator
 * @see         StaticParser
 * @see         DynamicParser
 * @version     Last modified on Wed Jul 25 07:08:01 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public abstract class GenericParser
{
  
  /**
   * The members of this class are divided into a static group and non-static
   * group. The latter is the information proper of each individual instance.
   * The former is all the (read-only) information that is intrinsic to
   * the parser's <i>class</i> and shared by all parser instances. It comprises
   * all the tables defining the grammar symbols and rules, and the parsing
   * automaton as well as all such read-only shareable ressources. These are
   * initialized in static blocks by the generated parser's class, and are
   * therefore defined at most once per application upon loading the parser's
   * class. By constrast, the members that are proper to an individual parser's
   * <i>instance</i> are created when the parser's constructor is called.
   * Thus, a single application may create, and run concurrently, several
   * parsers that are instances of the same class without interference,
   * while still avoiding the substantial waste of having redundant tables. 
   */

  /* **************************************************************************** */
  
  /* INITIALIZATION */

  public GenericParser ()
    {
      initialize();
    }

  /**
   * One may override this method to execute whatever initializations one wishes.
   * The default method is empty.
   */
  public void initialize ()
    {
    }

  /* **************************************************************************** */

  /* STATIC INFORMATION */


  /**
   * A value indicating to build no parse tree.
   */
  public final static int NO_TREE = 0;

  /**
   * A value indicating to build a compact parse tree.
   */
  public final static int COMPACT_TREE = 1;

  /**
   * A value indicating to build a full (concrete) parse tree.
   */
  public final static int FULL_TREE    = 2;

  /**
   * A value indicating to build an XML serialization tree.
   */
  public final static int XML_TREE     = 3;

  /**
   * The set of parser terminals.
   */
  protected static ParserTerminal[] terminals;
  /**
   * The set of parser nonterminals.
   */
  protected static ParserNonTerminal[] nonterminals;
  /**
   * The set of parser rules.
   */
  protected static ParserRule[] rules;
  /**
   * The set of parser states.
   */
  protected static ParserState[] states;
  /**
   * The set of parser actions.
   */
  protected static ParserAction[] actions;
  /**
   * The <i>action</i> table.
   */
  protected static HashMap[] actionTables;
  /**
   * The <i>goto</i> table.
   */
  protected static HashMap[] gotoTables;
  /**
   * The table associating identifiers to terminals.
   */
  protected static final HashMap terminalTable = new HashMap();
  /**
   * The table associating identifiers to nonterminals.
   */
  protected static final HashMap nonterminalTable = new HashMap();

  /* **************************************************************************** */

  /**
   * Canonical token denoting the end of input.
   * It gets initialized by the method <tt>newTerminal</tt>.
   */
  public static ParseNode E_O_I;

  /**
   * Returns the end of input token with the current location.
   */
  public static final ParseNode eoi ()
    {
      return E_O_I;
    }

  /**
   * Canonical terminal to identify error tokens.
   * It gets initialized by the method <tt>newTerminal</tt>.
   */
  public static ParserTerminal ERROR_SYMBOL;

  /**
   * Returns an error token whose <tt>svalue</tt> is set to the supplied string.
   */
  public static final ParseNode error (String errval)
    {
      ParseNode ERROR = new ParseNode(ERROR_SYMBOL);
      ERROR.setSvalue(errval);
      return ERROR;
    }

  /**
   * Returns an error token whose <tt>svalue</tt> is set to the supplied ParseNode's
   * printed value, and whose span is set to that of the supplied node.
   */
  public static final ParseNode error (ParseNode node)
    {
      if (node instanceof DynamicToken)
	{
          ((DynamicToken)node).setOriginal(node.copy());
          node.setSvalue(node.toString());
          node.setSymbol(ERROR_SYMBOL);
          return node;
        }

      ParseNode ERROR = new ParseNode(ERROR_SYMBOL);

      ERROR.setSvalue(node.toString());
      ERROR.setSpan(node);
      return ERROR;
    }

  /* **************************************************************************** */

  /**
   * The following are conveniences initializing some of the above.
   * They are used only by the generated parser to set up its parameters.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       INITIALIZATION  METHODS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  protected static final void newTerminal
    (int index, String name, int precedence, int associativity)
    {
      name = name.intern();
      terminalTable.put(name,
                        new ParserTerminal(name,index,precedence,associativity));
      if (name == "$E_O_I$")
	E_O_I = new ParseNode(terminals[index]);
      if (name == "error")
	ERROR_SYMBOL = terminals[index];
    }

  protected static final void newNonTerminal (int index, String name)
    {
      nonterminalTable.put(name,new ParserNonTerminal(name,index));
    }

  protected static final ParserTerminal terminal (String name)
    {
      return (ParserTerminal)terminalTable.get(name.intern());
    }

  protected static final ParserNonTerminal nonterminal (String name)
    {
      return (ParserNonTerminal)nonterminalTable.get(name.intern());
    }

  protected static final void newAction (int index, int type, int info)
    {
      new ParserAction(type,info,index);
    }

  protected static final void newState (int index)
    {
      new ParserState(index);
    }

  protected static final void setAction (int table, int terminal, int action)
    {
      actionTables[table].put(terminals[terminal],actions[action]);
    }

  protected static final void setGoto (int table, int nonterminal, int state)
    {
      gotoTables[table].put(nonterminals[nonterminal],states[state]);
    }

  protected static final void newActionTables (int size)
    {
      actionTables = new HashMap[size];
    }

  protected static final void newActionTable (int state, int size)
    {
      actionTables[state] = (size>0) ? new HashMap(size)
                                     : new HashMap();
    }

  protected static final void newGotoTables (int size)
    {
      gotoTables = new HashMap[size];
    }

  protected static final void newGotoTable (int state, int size)
    {
      gotoTables[state] = (size>0) ? new HashMap(size)
                                   : new HashMap();
    }

  protected static final void setTables (int state, int actions, int gotos)
    {
      states[state].setTables(actionTables[actions],gotoTables[gotos]);
    }

  /**
   * The following methods are public conveniences that may be used by the
   * class implementing the tokenizer interface.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       TOKENIZING      METHODS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  public static final ParseNode symbolToken (String symbol, String token)
    {
      ParserTerminal term = terminal(symbol);
      return term == null ? error(symbol + "( " + token + ")")
			  : new ParseNode(term,token.intern());
    }
  
  public static final ParseNode numberToken (String symbol, double num)
    {
      ParserTerminal term = terminal(symbol);
      return term == null ? error(symbol + "( " + num + ")")
			  : new ParseNode(term,num);
    }

  public static final ParseNode numberToken (String symbol, int num)
    {
      ParserTerminal term = terminal(symbol);
      return term == null ? error(symbol + "( " + num + ")")
			  : new ParseNode(term,num);
    }

  public static ParseNode literalToken (String symbol)
    {
      ParserTerminal term = terminal(symbol);
      return term == null ? error(symbol)
			  : new ParseNode(term);
    }

  /* ****************************************************************************
   * The following are static local utilities...
   * ****************************************************************************/

  /**
   * Returns the initial parse state.
   */
  static final ParserState initialState ()
    {
      return states[0];
    }

  /**
   * Returns the canonical error action.
   */
  static final ParserAction errorAction ()
    {
      return actions[0];
    }

  /**
   * Returns the canonical accept action.
   */
  static final ParserAction acceptAction ()
    {
      return actions[1];
    }

  /**
   * Static XML pretty-printing outputter for all our needs.
   */
  private static XMLOutputter _o = new XMLOutputter(Format.getPrettyFormat());

  public static final XMLOutputter xmlWriter ()
    {
      return _o;
    }

  /* **************************************************************************** */

  /* NON-STATIC INFORMATION */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       INHERITED   INFORMATION                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * Serializes the XML tree rooted in this node on the specified output
   * stream.
   */
  static public String xmlForm (Document document)
    {
      return _o.outputString(document);
    }
 
  /**
   * Serializes the XML tree rooted in this node on the specified output
   * stream.
   */
  public void writeXml (ParseNode node, PrintStream out) throws Exception
    {
      _o.output(makeXmlDocument(node),out);
      out.println();
    }
 
 /**
   * The XML tree's root's local name.
   */
  protected String  xmlroot = null;

 /**
   * The namespace prefix of the XML tree's root's local name.
   */
  protected String  xmlRootNSPrefix = "";

 /**
   * The namespace prefix of the XML tree's root's local name.
   */
  protected String[] namespaces = null;

  /**
   * Returns the XML document rooted in the specified node.
   */
  public Document makeXmlDocument (ParseNode node) throws Exception
    {
      return node.xmlDocument(xmlroot,xmlRootNSPrefix,namespaces);
    }
 
  /**
   * The value used by the parser to build, or not, a parse tree.
   * It defaults to <tt>NO_TREE</tt>.
   */
  public int parseTreeType = NO_TREE;

  protected PrintStream out = Options.getOutStream();
  protected PrintStream err = Options.getErrStream();

  /**
   * The parser stack.
   */
  protected Stack parserStack = new Stack();

  /**
   * The following items must be supplied by the generated parser subclass.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       PARSER'S     PARAMETERS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  
  /**
   * The tokenizer.
   */
  protected Tokenizer input;

  /**
   * The semantic action method.
   */
  abstract protected ParseNode semanticAction (ParserRule r) throws IOException;

  /**
   * Reads the next token into <tt>tokenNode</tt>.
   */
  abstract void readToken () throws IOException;
  
  /**
   * Performs the parse action and returns false iff it is an <tt>ACCEPT</tt>
   * action.
   */
  abstract  boolean performParseAction () throws IOException;

  /**
   * Sets the appropriate parser action for the current token in the current
   * state. If none exists, the parser action is set to the canonical error
   * action.
   */
  abstract void getParseAction () throws IOException;

  /**
   * Whenever this flag is <tt>true</tt>, this indicates that a new token needs
   * to be read.
   */
  protected boolean readTokenFlag = false;

  /**
   * Returns an error token.
   */
  public ParseNode error ()
    {
      return new ParseNode(ERROR_SYMBOL);
    }

  /**
   * Returns the current node; <i>i.e.</i>, the result returned by
   * the most recent semantic action.
   */
  public ParseNode currentNode ()
    {
      if (parsedNode.symbol().name() == "$ROOTS$" && parsedNode.children() != null)
	return (ParseNode)parsedNode.lastChild();
      
      return parsedNode;
    }

  /**
   * Returns the current token.
   */
  protected final ParseNode tokenNode () throws IOException
    {
      if (readTokenFlag)
	readToken();
      return tokenNode;
    }

  /**
   * Returns the token that was the last one actually read. For a <a
   * href="StaticParser.html"><tt>StaticParser</tt></a>, this is
   * equivalent to <tt>tokenNode()</tt>, but not necessarily for a <a
   * href="DynamicParser.html"><tt>DynamicParser</tt></a>, where it is
   * the bottom of the read stack if it is not empty - it is
   * <tt>tokenNode()</tt> otherwise.
   */
  protected ParseNode latestToken () throws IOException
    {
      return tokenNode();
    }

  /**
   * Pushes the current state and the given node on the parser stack.
   * This method is overridden in <a href="DynamicParser.html">
   * <tt>DynamicicParser</tt></a>.
   */
  void push (ParseNode node)
    {
      parserStack.push(new ParserStackElement(parseState,node));
    }

  abstract void trace (ParserAction a) throws IOException;

  /**
   * The following are methods used for parsing.
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       PARSING         METHODS                      \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private ParseNode switchToken = null;

  final ParseNode nextToken () throws IOException
    {
      if (switchToken == null)
	return input.nextToken();

      ParseNode token = switchToken;
      switchToken = null;

      return token;
    }

  public final void setSwitchToken (ParseNode token)
    {
      switchToken = token;
    }

  public final void setTokenizer (Tokenizer input)
    {
      this.input = input;
    }

  public final Tokenizer getTokenizer ()
    {
      return input;
    }

  public void resetParser ()
    {
      parseState      = null;     // latest state of the parse
      previousState   = null;     // previous state of the parse
      parseAction     = null;     // parse action to perform
      parseRule       = null;     // rule to use for reduction
      parseHandle     = null;     // recognized handle being reduced
      tokenNode       = null;     // latest token read
      parsedNode      = null;     // result returned by the action
      previousCulprit = null;	  // previous error-causing token
      parserStack.clear();
    }

  /**
   * Sets the parse tree type to the specified value (one of
   * <tt>NO_TREE</tt>, <tt>COMPACT_TREE</tt>, <tt>FULL_TREE</tt>, or
   * <tt>XML_TREE</tt>). Values outside this sets are ignored.
   */
  public final void setTreeType (int type)
    {
      switch (type)
	{
	case NO_TREE: case COMPACT_TREE:
	case FULL_TREE: case XML_TREE:
	  parseTreeType = type;
	  return;
	default:
	  /* do nothing */
	  ;
	}
    }
    
  /**
   * Sets the parse tree type according to the specified string (one of
   * <tt>"NONE"</tt>, <tt>"COMPACT"</tt>, <tt>"FULL"</tt>, or
   * <tt>"XML"</tt>). Values outside this sets are ignored.
   */
  public final void setTreeType (String type)
    {
      if (type.equals("FULL"))
	{
	  parseTreeType = FULL_TREE;
	  return;
	}
      
      if (type.equals("COMPACT"))
	{
	  parseTreeType = COMPACT_TREE;
	  return;
	}
      
      if (type.equals("XML"))
	{
	  parseTreeType = XML_TREE;
	  return;
	}
      
      if (type.equals("NONE"))
	{
	  parseTreeType = NO_TREE;
	  return;
	}
    }
    
  /**
   * This is the method to invoke for parsing a token stream using the
   * <tt>Tokenizer</tt> instance specified as input.
   */
  public final void parse () throws IOException
    {
      resetParser();    
      parserStack.push(new ParserStackElement(initialState(),E_O_I));
      readTokenFlag = true;;
      do
        {
          setParseState(currentState());
          getParseAction();
        }
      while (performParseAction());
    }

  /**
   * Same as <tt>parse()</tt> but sets the tokenizer's reader to the specified one
   * before doing the parse.
   */
  public final void parse (Reader reader) throws IOException
    {
      if (input.getReader() != null)
	input.getReader().close();
      input.setReader(reader);
      parse();
    }

  /**
   * Same as <tt>parse()</tt> but sets the tokenizer's reader to one
   * reading from the file having the specified name before doing the
   * parse.
   */
  public final void parse (String file) throws IOException
    {
      parse(new IncludeReader(file));
    }

  /**
   * Same as <tt>parse()</tt> but sets the tokenizer to the specified one before doing
   * the parse.
   */
  public final void parse (Tokenizer input) throws IOException
    {
      this.input = input;
      parse();
    }

  /**
   * This is the same as <tt>parse()</tt>, but specifies what type of
   * parse tree to build. The value must be one of <tt>NO_TREE</tt>,
   * <tt>COMPACT_TREE</tt>, <tt>FULL_TREE</tt>, or <tt>XML_TREE</tt>.
   * Otherwise, it is ignored.
   */
  public final void parse (int treeType) throws IOException
    {
      setTreeType(treeType);
      parse();
    }

  /**
   * This is the same as <tt>parse()</tt>, but specifies building
   * the parse tree: <tt>true</tt> is equivalent to <tt>FULL_TREE</tt>,
   * and <tt>false</tt> is equivalent to <tt>COMPACT_TREE</tt>.
   */
  public final void parse (boolean fullTree) throws IOException
    {
      parseTreeType = fullTree ? FULL_TREE : COMPACT_TREE;
      parse();
    }

  /**
   * This method returns the node in the parser stack corresponding
   * to the <tt>n</tt>th symbol of the current handle in the process
   * of being shifted onto the stack (the rule <tt>r</tt>'s RHS).
   * This node is at offset <tt>l-n</tt> from the top of the stack,
   * where <tt>l</tt> is the length of the rule <tt>r</tt>. This
   * method is used essentially by the semantic actions and its
   * uses result from translating the pseudo-variables occurring
   * in grammar rules.
   */
  protected final ParseNode node (ParserRule r, int n)
    {
      return ((ParserStackElement)parserStack.peek(r.length-n)).getNode();
    }

  /**
   * This method replaces the node corresponding to the <tt>n</tt>th
   * symbol of the current handle (the rule <tt>r</tt>'s RHS) on the
   * stack with the supplied <tt>ParseNode</tt>.
   */
  protected final void replaceStackNode (ParserRule r, int n,ParseNode node)
    {
      ((ParserStackElement)parserStack.peek(r.length-n)).setNode(node);
    }

  /**
   * These are global variables shared by the parsing methods.
   * They avoid passing so many arguments everywhere.
   */
  protected ParserState parseState;             // latest state of the parse
  protected ParserState previousState;          // previous state of the parse
  protected ParserAction parseAction;           // parse action to perform
  protected ParserRule parseRule;               // rule to use for reduction
  protected ParserStackElement[] parseHandle;   // recognized handle being reduced
  protected ParseNode tokenNode;                // latest token read
  protected ParseNode parsedNode;               // result returned by the action

  private ErrorManager _errorManager = new DefaultErrorManager();

  public final ErrorManager errorManager ()
    {
      return _errorManager;
    }

  public final void setErrorManager (ErrorManager errorManager)
    {
      _errorManager = errorManager;
    }

  /**
   * The following are local utilities...
   */

  /**
   * Returns the parse state currently on top of the parser stack.
   */
  final ParserState currentState ()
    {
      return ((ParserStackElement)parserStack.peek()).getState();
    }

  public final Error syntaxError (String msg)
    {
      return new Error().setLabel("Syntax Error: ").setMsg(msg);
    }

  public final Error syntaxError (String msg, Locatable extent)
    {
      return syntaxError(msg).setExtent(extent);
    }

  public final Error fatalError (String msg)
    {
      return new Error().setLabel("Fatal Error: ").setMsg(msg).setSee(" - aborting");
    }

  public final Error fatalError (String msg, Locatable extent)
    {
      return fatalError(msg).setExtent(extent);
    }

  /**
   * Signals an error, and then attempts error recovery if this is enabled,
   * by rewinding the parser stack to the most recent error-handling state.
   * If this works, the parser action that handles the canonical <tt>'error'</tt>
   * token in that state is performed. Then, tokens are read and ignored until
   * one is read that can be handled by the current state.
   */
  protected final void recoverFromError () throws IOException
    {
      findErrorCulprit();
      rewindErrorStack();
      performErrorAction();
      skipErrorTokens();
    }

  ParseNode previousCulprit;

  /**
   * Identifies the error-causing token and puts the error manager in quiet
   * mode while in error recovery.
   */
  private final void findErrorCulprit () throws IOException
    {
      ParseNode culprit = latestToken();

      if (culprit.isEOI())
	{
          _errorManager.reportError(syntaxError("unexpected end of input",culprit));
          abort();
        }

      String cause = (culprit.symbol() == ERROR_SYMBOL)
		   ? (culprit.svalue() == null ? "garbage": culprit.svalue())
		   : culprit.toString();

      if (_errorManager.isReportingErrors())
	{
	  if (!(previousCulprit != null && previousCulprit.equals(culprit)))
	    _errorManager.reportError(syntaxError("unexpected "+cause,culprit));
	  previousCulprit = culprit;
	  _errorManager.reportErrors(false);
	}

      if (!_errorManager.isRecoveringErrors())
	abort();

      if (trace)
	err.println("*** Recovering from error...");
    }

  /**
   * Keeps popping the stack until an error-handling state is found.
   */
  private final void rewindErrorStack () throws IOException
    {
      tokenNode = error();
      while (!symbolIsHandled(tokenNode.symbol()))
        {
          if (setParseState(currentState()) == initialState())
	    {
	      Error error = fatalError("unrecoverable syntax error");
	      _errorManager.reportError(error);
              abort();
            }
          parserStack.pop();
        }
    }

  /**
   * In an error-handling state, performs action to shift 'error'.
   * <p>
   * <b>Note to myself:</b> this is not safe as it is not guaranteed
   * that (1) the action is a shift, nor (2) that even performing all
   * actions until error-shifting will not put the parser in a strange
   * state. <b>Possible fix:</b> either handle all these cases, or
   * enforce at parser generation time that only shifts be allowed as
   * error-handling actions.
   * <p>
   * The current code works when error-handling rules are of the form:
   * <pre>
   * Foo : 'error' { semantic action; } 'token' ;
   * </pre>
   */
  private final void performErrorAction () throws IOException
    {
      setParseState(currentState());
      parseAction = parseState.getAction((ParserTerminal)tokenNode.symbol());
      performParseAction();
    }

  /**
   * Reads and skips tokens until a legal post-error token is read.
   */
  private final void skipErrorTokens () throws IOException
    {
      do readToken();
      while (!tokenNode.isEOI() && !symbolIsHandled(tokenNode.symbol()));	

      if (tokenNode.isEOI())
	{
	  Error error = fatalError("can't recover past the end of input");
	  _errorManager.reportError(error);
	  abort();
	}
    }

  final boolean symbolIsHandled (ParserSymbol symbol)
    {
      return currentState().actionTable.containsKey(symbol);
    } 

  final ParserState setParseState (ParserState state)
    {
      previousState = parseState;
      parseState = state;
      return parseState;
    }    

  final void changeState () throws IOException
    {
      setParseState(currentState().getGoto((ParserNonTerminal)parsedNode.symbol()));
    }

  final void shift () throws IOException
    {
      setParseState(states[parseAction.info]);
      ParseNode shiftedNode = tokenNode();
      if (shiftedNode.isTerminal())
	shiftedNode.setXmlInfo(((ParserTerminal)shiftedNode.symbol()).xmlInfo());
      push(shiftedNode);
      if (trace)
	trace(parseAction);
      readTokenFlag = true;
    }

  final void reduce () throws IOException
    {
      parseRule = rules[parseAction.info];
      parsedNode = semanticAction(parseRule);
      popHandle();
      changeState();
      push(parsedNode);
      if (trace)
	trace(parseAction);
    }

  /**
   * Pops the <i>n</i> latest elements on the parser stack, where
   * <i>n</i> is the length of the current rule's RHS. This may also
   * build a parse tree as specified by <tt>parseTreeType</tt>.
   */
  void popHandle ()
    {
      parseHandle = new ParserStackElement[parseRule.length];

      for (int i = parseRule.length; i-->0;)
        parseHandle[i] = (ParserStackElement)parserStack.pop();

      if (parseHandle.length > 0)
	parsedNode.setSpan(parseHandle);
      else
	parsedNode.setSpan(tokenNode.getStart(),
			   tokenNode.getStart());

      switch (parseTreeType)
	{
	case NO_TREE:
	  break;
	case XML_TREE:
	  // Set the XML info of parsedNode to that of the reducing
	  // rule:
	  parsedNode.setXmlInfo(parseRule.xmlInfo());
	  // NB: This falls through to the default case on purpose!
	  // This is to enable the full concrete tree to be built. Only
	  // a second pass will build the actual XML tree, and even then
	  // only if and when it is accessed through the ParseNode's
	  // xmlify(...) method using the XmlInfo from the reducing
	  // rule's info.
	default:
	  // FULL, COMPACT, or XML: add the child to parsedNode using
	  // parsedNode.addChild(ParseNode, TreeType) according to the
	  // tree type.
	  for (int i=0; i<parseHandle.length; i++)
	    parsedNode.addChild(parseHandle[i].getNode(),
				parseTreeType);
	}
    }

  /**
   * The following is for showing items - for debugging purposes...
   */
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                       DISPLAYING       METHODS                     \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  public String fileName ()
    {
      if (input instanceof FileTokenizer)
	return ((FileTokenizer)input).fileName();

      return null;
    }

  protected String location () throws IOException
    {
      if (tokenNode().isLocated())
	return tokenNode().locationString();

      String file = fileName();
      String location = "line "+String.valueOf(input.lineNumber());

      if (file != null)
	location = "file "+file+", "+location;

      return location;
    }

  public final ParseNode locate (ParseNode node)
    {
      node.setLineNumber(input.lineNumber());
      node.setFile(fileName());
      return node;
    }

  /**
   * Aborts the parsing upon an unrecoverable error and throws an exception
   * signaling so.
   */
  public final void abort ()
    {
      throw new FatalParseErrorException();
    }

  protected boolean trace = false;

  public final boolean tracingIsOn ()
    {
      return trace;
    }

  public final void setTrace (boolean flag)
    {
      trace = flag;
      err.print("*** Tracing is turned "+(trace?"on":"off"));
      if (trace)
	err.print(" (enter '"+Debug.getQuitString()+
                         "' to exit trace mode)");
      err.println("\n");
    }      

  public final void toggleTrace ()
    {
      setTrace(!trace);
    }

  protected final void step ()
    {
      String input = Debug.step();

      if (Debug.matchesQuitString(input))
	toggleTrace();
    }

  public final void trace () throws IOException
    {
      if (trace)
	{
          show();
          step();
        }
    }

  public final void traceAction (ParserAction a) throws IOException
    {
      switch (a.type)
        {
        case Action.SHIFT:
          err.println("Shifting token: "+tokenNode()+
                    "\n    located in: "+location()+
                    "\n    from state: "+previousState+
                    "\n      to state: "+parseState);
          break;
        case Action.REDUCE:
          err.println("  Seeing token: "+tokenNode()+
                    "\n    located in: "+location()+
                    "\n      in state: "+previousState+
                    "\n reducing with: "+rule()+
                    "\n      to state: "+parseState);
//           err.println("    parse tree:");
//           parsedNode.show(16,err);
          break;
        }

      err.println(Misc.view(parserStack," parser stack",0,50));
    }

  void show () throws IOException
    {
      showParseState();
    }

  String rule ()
    {
      if (parseRule != null)
	{
          String s = parseRule.toString();
          for (int i=0; i<parseRule.length; i++)
            s += parseHandle[i].getNode() + " ";
          return s;
        }
      return null;
    }

  protected final void showParseState () throws IOException
    {
      err.println
        ("\n-------------------------------------------------------------------");
      err.println("parseState\t= "     + parseState);
      if (previousState != null)
	err.println("previousState\t= "+ previousState);
      err.println("tokenNode\t= "      + tokenNode());
      err.println("latestToken\t= "    + latestToken());
      err.println("parsedNode\t= "     + parsedNode);
      err.println("parseAction\t= "    + parseAction);
      err.println("parseRule\t= "      + rule());
      err.println(Misc.view(parserStack,"parserStack",0,50));
      err.println("handledSymbols\t= " + currentState().actionTable);
      err.println
        ("-------------------------------------------------------------------");
    }
}
