//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import java.io.File;
import java.io.IOException;
import java.io.EOFException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.PrintStream;

import hlt.language.util.Stack;
import hlt.language.util.SetOf;
import hlt.language.util.ArrayList;
import hlt.language.util.Verbose;
import hlt.language.tools.Misc;
import hlt.language.io.StreamTokenizer;
import hlt.language.io.IncludeReader;

// BEGIN COMMENT OUT FOR NO XML ANNOTATION PARSER
import hlt.language.syntax.xml.*;
// END COMMENT OUT FOR NO XML ANNOTATION PARSER

/**
 * This defines a class for processing a <tt>Jacc</tt> grammar contained
 * in a file and specified using the popular UNIX yacc syntax for
 * building a table-driven LR-parser. The <b>yacc</b> syntax is also
 * adapted to handle Java code for semantic actions and extended with
 * several useful additionnal functionalities.
 *
 * <p> An instance of this class can then be used to generate a Java
 * program implementing an LR parser for the specified grammar.
 *
 * <p>
 * It provides methods for:
 * <ul>
 * <li> reading a grammar by parsing (a) file(s) containing it;
 * <li> building a grammar's objects (terminals, non-terminals, rules);
 * <li> analyzing the constructed grammar by computing the canonical
 *      LR(0) states of its viable-prefix finite state automaton and
 *      propagating the LALR(1) lookahead sets;
 * <li> showing the grammar with more details as indicated by a verbosity
 *      level.
 * </ul>
 *
 * The most critical (<i>i.e.</i>, complex) part of computation is, of course,
 * the propagation of the LALR(1) lookahead sets. Traditional <tt>yacc</tt>
 * implementations use the method developed by DeRemer and Penello. I use
 * an improved method due to Park, Choe, and Chang, which substantially
 * ameliorates the one by DeRemer and Penello. It is the most efficient
 * method as far as I know.
 *
 * <p> The format of the grammar file is essentially the same as that
 * required by <b>yacc</b>, with some minor differences, and a few
 * additional features. Not using the additional features makes it
 * essentially similar to the <b>yacc</b> format. For more details,
 * please read the description of the Jacc system and grammar format
 * assumed by this class and the rest of the <tt><a
 * href="http://hassan-ait-kaci.net/hlt/doc/hlt/api/hlt/language/syntax/package-summary.html">hlt.language.syntax</a></tt>
 * package.
 *
 * <p> For detailed explanations of most constructions and algorithms
 * used by this package, please refer to the following:
 *
 * <p>
 * <ul>
 * <li> Alfred Aho, Ravi Sethi, and Jeffrey Ullman, <i>Compilers.
 *      Principles, Techniques, and Tools.</i> Addison-Wesley, 1986.
 * <li> Joseph Park, K.M Choe, and C.H. Chang, "A new analysis of
 *      LALR formalisms", <i>ACM Transactions of Programming Languages
 *      and Systems</i>, <b>7</b>(1), pp. 159-175 (Jan. 85).
 * <li> Frank DeRemer, and Thomas Penello, "Efficient computation of
 *      lookahead sets", <i>ACM Transactions of Programming Languages
 *      and Systems</i>, <b>4</b>(4), pp. 615-749 (Oct. 82).
 * <li> Stephen C. Johnson, "Yacc: Yet Another Compiler Compiler,"
 *      <i>Computer Science Technical Report 32</i>. AT&amp;T Bell
 *      Labs, Murray Hill, NJ, 1975. (Reprinted in the <i>4.3BSD Unix
 *      Programmer's Manual, Supplementary Documents 1</i>, PS1:15.
 *      UC Berkeley, 1986.)
 * </ul>
 *
 * @see         Options
 * @see         ParserGenerator
 *
 * @version     Last modified on Wed Nov 28 12:42:23 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class Grammar
{
  /**
   * The constructor of a <i>Grammar</i> object.
   */
  Grammar () throws Exception
    {
      currentGrammar = this;

      EMPTY        = new Terminal("$EMPTY$");
      END_OF_INPUT = new Terminal("$E_O_I$");
      ERROR        = new Terminal("error");

      terminalTable.put("error",ERROR);

      START     = new NonTerminal("$START$");
      ROOTS     = new NonTerminal("$ROOTS$");

      GrammarSymbol[] seq = {START, ROOTS};
      new Rule(seq);

      readGrammar();
      if (Options.getDocOnly())
	new Documentor();
      else
	buildGrammar();
    }

  /**
   * The stream tokenizer from which this grammar is to be read.
   * This is initialized in the <tt>readGrammar()</tt> method.
   */
  StreamTokenizer st;
  
  /**
   * The stream where output is directed. Defaults to
   * <tt><i>System.out</i></tt>.
   * @see       Options
   */
  static PrintStream out = Options.getOutStream();
  
  /**
   * The stream where errors are reported. Defaults to
   * <tt><i>System.err</i></tt>.
   * @see       Options
   */
  static PrintStream err = Options.getErrStream();
  
  /**
   * Determines how much to show during analysis. Possible
   * values are, in increasing order:
   * <ul>
   * <li> <i><tt>hlt.language.util.Verbose.QUIET</tt></i>,
   * <li> <i><tt>hlt.language.util.Verbose.NORMAL</tt></i> (default),
   * <li> <i><tt>hlt.language.util.Verbose.VERBOSE</tt></i>,
   * <li> <i><tt>hlt.language.util.Verbose.DETAILED</tt></i>,
   * <li> <i><tt>hlt.language.util.Verbose.BABBLE</tt></i>.
   * </ul>
   *
   * @see       ../util/Verbose
   * @see       Options
   */
  private int verbosity = Options.getVerbosity();
  
  /**
   * The name of the file containing the grammar. Defaults to
   * <tt><i>Grammar.grm</i></tt>.
   * @see       Options
   */
  String grammarName = Options.getGrammarName();
  
  /**
   * The pathed name of the file containing the grammar. Defaults to
   * <tt><i>./Grammar.grm</i></tt>.
   * @see       Options
   */
  String grammarPathedName = Options.getGrammarPathedName()
    			   + "."
			   + Options.getGrammarSuffix();
  
  public final String name ()
    {
      return grammarName;
    }  

  /**
   * When this flag is <tt>true</tt>, an incomplete grammar is
   * tolerated (although a warning is issued). The default is
   * <tt>false</tt>; in which case, an incomplete grammar causes
   * a <tt>BadGrammarException</tt> to be thrown after the warning.
   * @see       Options
   */
  boolean permissible = Options.getPermissible();
  
  /**
   * The grammar being currently processed. This is a static reference
   * to the class itself as we assume that only one grammar is ever
   * processed at a time. It is used as a handle to access local data
   * such as symbols and rules from other classes in the package.
   */
  static Grammar currentGrammar;

  /**
   * Timers to compute how long processing takes.
   */
  static long startTime,
    readingStart,
    preprocessStart,
    buildingStart,
    propagationStart,
    analysisTime,
    totalTime;

  /** The set of terminals */
  ArrayList terminals      = new ArrayList(200);
  /** The set of non-terminals */
  ArrayList nonterminals   = new ArrayList(200);
  /** The set of grammar rules */
  ArrayList rules          = new ArrayList(500);
  /** The set of grammar items */
  ArrayList items          = new ArrayList(1000);
  /** The set of grammar states */
  ArrayList states         = new ArrayList(3000);
  /** The set of dynamic operators */
  ArrayList operators      = new ArrayList(100);

  /** When <tt>true</tt> sorting nonterminals gives rule order */
  boolean RULE_ORDER_MODE = false;

  /** The number of terminals */
  int tcount = 0;
  /** The number of non-terminals */
  int ncount = 0;
  /** The number of rules */
  int rcount = 0;
  /** The number of items */
  int icount = 0;
  /** The number of states */
  int scount = 0;
  /** The number of dynamic operators */
  int ocount = 0;

  /** A hash table for efficient retrieval of states */
  HashMap stateTable = new HashMap(500);

  /**
   * Returns the terminal symbol with the given name
   * @param index       the name.
   * @return            the desired symbol or <tt><i>null</i></tt>
   */
  final static Terminal getTerminal (String name)
    {
      return (Terminal)terminalTable.get(name);
    }

  /**
   * Returns the nonterminal symbol with the given name
   * @param index       the name.
   * @return            the desired symbol or <tt><i>null</i></tt>
   */
  final static NonTerminal getNonTerminal (String name)
    {
      return (NonTerminal)nonterminalTable.get(name);
    }

  /**
   * Returns the terminal symbol at a given index in the set
   * of terminals.
   * @param index       the index.
   * @return            the desired symbol or <tt><i>null</i></tt>
   */
  final Terminal getTerminal (int index)
    {
      return (Terminal)terminals.get(index);
    }

  /**
   * Returns the non-terminal symbol at a given index in the set
   * of non-terminals.
   * @param index       the index.
   * @return            the desired symbol or <tt><i>null</i></tt>
   */
  final NonTerminal getNonTerminal (int index)
    {
      return (NonTerminal)nonterminals.get(index);
    }

  /**
   * Returns the grammar rule at a given index in the set of rules.
   * @param index       the index.
   * @return            the desired rule or <tt><i>null</i></tt>
   */
  final Rule getRule (int index)
    {
      return (Rule)rules.get(index);
    }

  /**
   * Returns the grammar item at a given index in the set of items.
   * @param index       the index.
   * @return            the desired item or <tt><i>null</i></tt>
   */
  final Item getItem (int index)
    {
      return (Item)items.get(index);
    }

  /**
   * Returns the grammar item for a given rule and mark.
   * @param rule        the grammar rule.
   * @param mark        the mark in the rule (&gt;0).
   * @return            the desired item.
   */
  final Item getItem (Rule rule, int mark)
    {
      return (Item)items.get(rule.items[mark-1]);
    }

  /**
   * Returns the index in the set of all grammar items for the
   * item corresponding to the given rule and mark.
   * @param rule        the grammar rule.
   * @param mark        the mark in the rule (&gt;0).
   * @return            the desired index.
   */
  final int itemIndex (Rule rule, int mark)
    {
      return rule.items[mark-1];
    }

  /**
   * Returns the grammar state at a given index in the set
   * of states, or <tt><i>null</i></tt> if index is not within range.
   * @param index       the index.
   * @return            the desired state or <tt><i>null</i></tt>
   */
  final State getState (int index)
    {
      return (State)states.get(index);
    }

  /**
   * Returns the dynamic operator at a given index in the set
   * of operators, or <tt><i>null</i></tt> if index is not within range.
   * @param index       the index.
   * @return            the desired operator or <tt><i>null</i></tt>
   */
  final Operator getOperator (int index)
    {
      return (Operator)operators.get(index);
    }

  /* ********************************************************************* */

  /**
   * This symbol denotes <i>epsilon</i>, the empty symbol.
   */
  static Terminal EMPTY;

  /**
   * This symbol denotes the end of input marker.
   */
  static Terminal END_OF_INPUT;

  /**
   * This symbol denotes the artificial error token used
   * for error recovery.
   */
  static Terminal ERROR;

  /**
   * This symbol denotes the artificial start symbol added
   * for LR analysis.
   */
  static NonTerminal START;

  /**
   * This symbol denotes the actual (user-specified) start symbol
   * of the grammar. It is always a root.
   */
  static NonTerminal GRAMMAR_START;

  /**
   * This symbol denotes the first declared root, if any,
   * which may be used as the implicit start symbol if none
   * is declared explicitly. If no root is declared either
   * the implicit start symbol is defined as the first rule's
   * LHS.
   */
  static NonTerminal FIRST_ROOT;

  /**
   * The parser class's XML root's local name (declared with the command
   * <tt>%xmlroot <i>nsprefix localname</i></tt> (where
   * <tt><i>nsprefix</i></tt> is optional).
   */
  static String  xmlroot = null;

  static String xmlroot ()
    {
      if (xmlroot == null)
	xmlroot = FIRST_ROOT.name();
      return xmlroot;
    }

  /**
   * The parser class's XML root namespace prefix.
   * It is empty by default, and may be set with
   * the command <tt>%xmlroot</tt>.
   */
  static String  xmlRootNSPrefix = "";

  static String xmlRootNSPrefix ()
    {
      return xmlRootNSPrefix;
    }

  /**
   * This symbol denotes the artificial roots symbol added
   * for the support of partial parsing.
   */
  static NonTerminal ROOTS;

  /**
   * This contains the actual (user-specified) root symbols
   * of the grammar. The values are the artificial tokens
   * introduced to control the switch to a partial parse.
   */
  HashMap roots = new HashMap();

  /**
   * Returns the actual (user-specified) start symbol of the grammar.
   */
  final NonTerminal startSymbol ()
    {
      return GRAMMAR_START;
    }

  /**
   * Storage table for terminal symbols.
   */
  private final static HashMap terminalTable = new HashMap(100);

  /**
   * Storage table for nonterminal symbols.
   */
  private final static HashMap nonterminalTable = new HashMap(100);

  /**
   * A temporary storage for the symbol sequence of a production rule as it
   * is being read.
   */
  private final static ArrayList ruleSequence = new ArrayList(20);

  /* ********************************************************************* */

  /**
   * A constant string denoting the empty semantic action.
   */
  final static String EMPTY_ACTION      = "$empty$";

  /** A constant string denoting the default semantic action. */
  final static String DEFAULT_ACTION    = "$default$";

  /**
   * A constant denoting the start or end of an implicit token.
   */
  final static int SINGLE_QUOTE          = '\'';

  /**
   * A constant denoting the start or end of an implicit token.
   */
  final static int DOUBLE_QUOTE          = '"';

  /**
   * A constant denoting the start of a rule's body.
   */
  final static int RULE_NECK            = ':';

  /**
   * A constant denoting rule disjunction.
   */
  final static int OR_RULE_BODY         = '|';

  /**
   * A constant denoting the start of a semantic action.
   */
  final static int BEGIN_SCOPE          = '{';

  /**
   * A constant denoting the end of a semantic action.
   */
  final static int END_SCOPE            = '}';

  /**
   * A constant denoting the start of a rule annotation
   */
  final static int BEGIN_ANNOTATION     = '[';

  /**
   * A constant denoting the end of a rule annotation
   */
  final static int END_ANNOTATION       = ']';

  /**
   * A constant denoting the start of grammar's command.
   */
  final static int COMMAND_START        = '%';

  /**
   * A constant used to identify a pseudo-variable
   */
  final static int PSEUDO_VAR           = '$';

  /**
   * A constant denoting the end of a rule.
   */
  final static int END_OF_RULE          = ';';

  /**
   * A constant used as a list separator
   */
  final static int COMMA                = ',';

  /**
   * A constant used as a list separator
   */
  final static int SPACE                = ' ';

  /**
   * A constant used as a list separator
   */
  final static int NEWLINE              = '\n';

  /** A constant used as a list separator */
  final static int TAB                  = '\t';

  /**
   * The prefix for generated node class names
   */
  String nodePrefix   = "$";

  /** The suffix for generated node class names */
  String nodeSuffix   = "$";

  /** <h1>READING THE GRAMMAR</h1> */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                    READING     THE     GRAMMAR                     \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * 
   * <h2>Constants encoding the command identifiers</h2>
   *
   *  <tt>%start</tt> command
   */
  private final static String START_COMMAND_ID          = "start";

  /**
   *  <tt>%root</tt> command
   */
  private final static String ROOT_COMMAND_ID           = "root";

  /**
   * <tt>%token</tt> command
   */
  private final static String TOKEN_COMMAND_ID          = "token";

  /**
   * <tt>%left</tt> command
   */
  private final static String LEFT_COMMAND_ID           = "left";

  /**
   * <tt>%right</tt> command
   */
  private final static String RIGHT_COMMAND_ID          = "right";

  /**
   * <tt>%nonassoc</tt> command
   */
  private final static String NONASSOC_COMMAND_ID       = "nonassoc";

  /**
   * <tt>%{</tt> command
   */
  private final static String LBRACE_COMMAND_ID         = "{";

  /**
   * <tt>%}</tt> command
   */
  private final static String RBRACE_COMMAND_ID         = "}";

  /**
   * <tt>%usefile</tt> command
   */
  private final static String USEFILE_COMMAND_ID        = "usefile";

  /**
   * <tt>%%</tt> command
   */
  private final static String SECTION_COMMAND_ID        = "%";

  /**
   * <tt>%prec</tt> command
   */
  private final static String PREC_COMMAND_ID           = "prec";

  /**
   * <tt>%package</tt> command
   */
  private final static String PACKAGE_COMMAND_ID        = "package";

  /**
   * <tt>%import</tt> command
   */
  private final static String IMPORT_COMMAND_ID         = "import";

  /**
   * <tt>%precstep</tt> command
   */
  private final static String PRECSTEP_COMMAND_ID       = "precstep";

  /**
   * <tt>%nodeclass</tt> command
   */
  private final static String NODECLASS_COMMAND_ID      = "nodeclass";

  /**
   * <tt>%access</tt> command
   */
  private final static String ACCESS_COMMAND_ID         = "access";

  /**
   * <tt>%dynamic</tt> command
   */
  private final static String DYNAMIC_COMMAND_ID        = "dynamic";

  /**
   * <tt>%nodeprefix</tt> command
   */
  private final static String NODEPREFIX_COMMAND_ID     = "nodeprefix";

  /**
   * <tt>%nodesuffix</tt> command
   */
  private final static String NODESUFFIX_COMMAND_ID     = "nodesuffix";

  /**
   * <tt>%doc</tt> command
   */
  private final static String DOC_COMMAND_ID            = "doc";

  /**
   * <tt>%include</tt> command
   */
  private final static String INCLUDE_COMMAND_ID        = "include";

  /**
   * <tt>%xmlroot</tt> command
   */
  private final static String XMLROOT_COMMAND_ID        = "xmlroot";

  /**
   * <tt>%xmlns</tt> command
   */
  private final static String XMLNS_COMMAND_ID          = "xmlns";

  /**
   * <tt>%xmlinfo</tt> command
   */
  private final static String XMLINFO_COMMAND_ID        = "xmlinfo";

  /**
   * 
   * <h2>Constants encoding the commands</h2>
   *
   * Unknown command
   */
  private final static int UNKNOWN_COMMAND      = -1;

  /**
   * <tt>%start</tt> command
   */
  private final static int START_COMMAND        =  0;

  /**
   * <tt>%root</tt> command
   */
  private final static int ROOT_COMMAND         =  1;

  /**
   * <tt>%token</tt> command
   */
  private final static int TOKEN_COMMAND        =  2;

  /**
   * <tt>%left</tt> command
   */
  private final static int LEFT_COMMAND         =  3;

  /**
   * <tt>%right</tt> command
   */
  private final static int RIGHT_COMMAND        =  4;

  /**
   * <tt>%nonassoc</tt> command
   */
  private final static int NONASSOC_COMMAND     =  5;

  /**
   * <tt>%{</tt> command
   */
  private final static int LBRACE_COMMAND       =  6;

  /**
   * <tt>%}</tt> command
   */
  private final static int RBRACE_COMMAND       =  7;

  /**
   * <tt>%usefile</tt> command
   */
  private final static int USEFILE_COMMAND      =  8;

  /**
   * <tt>%%</tt> command
   */
  private final static int SECTION_COMMAND      =  9;

  /**
   * <tt>%prec</tt> command
   */
  private final static int PREC_COMMAND         = 10;

  /**
   * <tt>%package</tt> command
   */
  private final static int PACKAGE_COMMAND      = 11;

  /**
   * <tt>%import</tt> command
   */
  private final static int IMPORT_COMMAND       = 12;

  /**
   * <tt>%precstep</tt> command
   */
  private final static int PRECSTEP_COMMAND     = 13;

  /**
   * <tt>%nodeclass</tt> command
   */
  private final static int NODECLASS_COMMAND    = 14;

  /**
   * <tt>%access</tt> command
   */
  private final static int ACCESS_COMMAND       = 15;

  /**
   * <tt>%dynamic</tt> command
   */
  private final static int DYNAMIC_COMMAND      = 16;

  /**
   * <tt>%operator</tt> command
   */
  private final static int OPERATOR_COMMAND     = 17;

  /**
   * <tt>%nodeprefix</tt> command
   */
  private final static int NODEPREFIX_COMMAND   = 18;

  /**
   * <tt>%nodeprefix</tt> command
   */
  private final static int NODESUFFIX_COMMAND   = 19;
  
  /**
   * <tt>%doc</tt> command
   */
  private final static int DOC_COMMAND          = 20;
  
  /**
   * <tt>%include</tt> command
   */
  private final static int INCLUDE_COMMAND      = 21;
  
  /**
   * <tt>%xmlroot</tt> command
   */
  private final static int XMLROOT_COMMAND      = 22;
  
  /**
   * <tt>%xmlns</tt> command
   */
  private final static int XMLNS_COMMAND        = 23;
  
  /**
   * <tt>%xmlinfo</tt> command
   */
  private final static int XMLINFO_COMMAND      = 24;
  
  /**
   * Storage for the commands and their codes.
   */
  private final static HashMap commandCodeTable = new HashMap(50);

  final static void def_cmd(String cmd, int code)
    {
      commandCodeTable.put(cmd,Integer.valueOf(code));
    }

  static
    {
      def_cmd(START_COMMAND_ID,            START_COMMAND);
      def_cmd(ROOT_COMMAND_ID,             ROOT_COMMAND);
      def_cmd(TOKEN_COMMAND_ID,            TOKEN_COMMAND);
      def_cmd(LEFT_COMMAND_ID,             LEFT_COMMAND);
      def_cmd(RIGHT_COMMAND_ID,            RIGHT_COMMAND);
      def_cmd(NONASSOC_COMMAND_ID,         NONASSOC_COMMAND);
      def_cmd(LBRACE_COMMAND_ID,           LBRACE_COMMAND);
      def_cmd(RBRACE_COMMAND_ID,           RBRACE_COMMAND);
      def_cmd(USEFILE_COMMAND_ID,          USEFILE_COMMAND);
      def_cmd(SECTION_COMMAND_ID,          SECTION_COMMAND);
      def_cmd(PREC_COMMAND_ID,             PREC_COMMAND);
      def_cmd(PACKAGE_COMMAND_ID,          PACKAGE_COMMAND);
      def_cmd(IMPORT_COMMAND_ID,           IMPORT_COMMAND);
      def_cmd(PRECSTEP_COMMAND_ID,         PRECSTEP_COMMAND);
      def_cmd(NODECLASS_COMMAND_ID,        NODECLASS_COMMAND);
      def_cmd(ACCESS_COMMAND_ID,           ACCESS_COMMAND);
      def_cmd(DYNAMIC_COMMAND_ID,          DYNAMIC_COMMAND);
      def_cmd(NODEPREFIX_COMMAND_ID,       NODEPREFIX_COMMAND);
      def_cmd(NODESUFFIX_COMMAND_ID,       NODESUFFIX_COMMAND);
      def_cmd(DOC_COMMAND_ID,              DOC_COMMAND);
      def_cmd(INCLUDE_COMMAND_ID,          INCLUDE_COMMAND);
      def_cmd(XMLROOT_COMMAND_ID,          XMLROOT_COMMAND);
      def_cmd(XMLNS_COMMAND_ID,            XMLNS_COMMAND);
      def_cmd(XMLINFO_COMMAND_ID,          XMLINFO_COMMAND);
    }

  /**
   * The parser class' modifier.
   */
  String  accessTag = "";

  /**
   * A flag to indicate whether this grammar allows dynamic operators or
   * ambiguous tokens.
   */
  boolean isDynamic = false;

  /**
   * A flag to indicate whether this grammar allows dynamic operators.
   */
  boolean admitsOperators ()
    {
      return isDynamic && ocount > 0;
    }

  /**
   * <h2>Storage tables for dynamic operators</h2> They are two
   * hash tables. The first associates an operator category's name
   * to a set of all <tt>Operator</tt> objects in this category.
   * The second associates a specific operator's name to a set
   * containing all <tt>Operator</tt> objects with this name for
   * all categories.
   * <p>
   * Operator category table
   */
  final static HashMap operatorCategoryTable = new HashMap(20);
    
  /**
   * Operator name table
   */
  final static HashMap operatorNameTable = new HashMap(20);

  /**
   * The latest operator category command read.
   */
  private static NonTerminal operatorCategory;

  /**
   * Returns the code of the specified command.
   * @param command the command
   */
  private final static int commandCode (String command)
    {
      Integer code = (Integer)commandCodeTable.get(command);
      if (code != null) return code.intValue();

      if (operatorCategoryTable.get(command) == null)
        return UNKNOWN_COMMAND;
      operatorCategory = getNonTerminal(command);
      return OPERATOR_COMMAND;
    }

  /**
   * The head symbol of the latest rule read.
   */
  private GrammarSymbol currentLHS;

  /**
   * Returns true iff GRAMMAR_START has been defined.
   */
  private boolean startIsDefined ()
    {
      return GRAMMAR_START != null;
    }

  /**
   * True iff reading a section in which comments must be accumulated
   * as main documentation.
   */
  private boolean mainDocSection = true;

  /**
   * True iff reading a code section (<i>i.e.</i>, between <tt>%{</tt>
   * and  <tt>%}</tt>.
   */
  private boolean codeSection = false;

  /**
   * The package name, if any.
   */
  String packageName = null;

  /**
   * The list of imports.
   */

  ArrayList imports = new ArrayList();

  /**
   * The declarations to include as part of the parser class.
   */
  ArrayList parserDeclarations = new ArrayList();

  /**
   * Other non public classes outside the parser class.
   */
  ArrayList ancillaryClasses = new ArrayList();

  /**
   * Other public classes outside the parser class.
   */
  HashMap publicClasses = new HashMap();

  /**<hr>*/

  /**
   * True iff reading the third section (i.e., past the second
   * <tt>%%</tt>).
   */
  private boolean readingRemainderSection = false;

  /**
   * A default size for the input string buffer (= 80 chars).
   */
  private final static int bufferSize = 80;

  /**
   * A storage for recording xml annotation of a production rule as it
   * is being read.
   */
  private XmlInfo ruleXmlInfo = null;

  /**
   * The latest semantic <i>forward</i> (or <i>bottom-up</i>)
   * action having been, or being, read.
   */
  private String ruleAction = null;

  /**
   * The latest semantic <i>backward</i> (or <i>undo</i>) action
   * having been, or being, read.  This is executed <i>top-down</i>
   * upon backtracking over ambiguous dynamic operators.
   */
  private String ruleUndoAction = null;

  /**
   * Possible type casts for stack variables in <tt>ruleAction</tt>.
   */
  private String ruleActionCast = "";

  /**
   * Possible type casts for stack variables in <tt>ruleUndoAction</tt>.
   */
  private String ruleUndoActionCast = "";

  /**
   * The terminal symbol "tagging" the latest rule having been, or
   * being, read. This symbol is usually the last (rightmost) terminal
   * symbol that occurs in the RSH of the production rule), or
   * <tt>null</tt> if none exists, but may also be specified by
   * <tt>%prec</tt>.
   */
  private Taggable ruleTag = null;

  /**
   * True iff the rule being read contains a symbol whose node type
   * has been refined by the <tt>%nodeclass<\tt> command.
   */
  private boolean nodeCast = false;

  /**
   * True iff the latest action that has been read contains a "$$" pseudo-variable.
   */
  private boolean containsHeadReference = false;

  /**
   * A counter to generate new symbols.
   */
  private int newSymbolCount = 0;

  /**
   * <h3>Methods used while reading the grammar.</h3>
   */

  /**
   * Returns a new nonterminal symbol.
   */
  private final NonTerminal newSymbol()
    {
      return new NonTerminal
          ("$ACTION"+String.valueOf(newSymbolCount++)+"$");
    }

  /**
   * <h2>Associativity values</h2>
   *
   * Denotes left associativity
   */
  final static int LEFT_ASSOCIATIVE   = 0;
  /**
   * Denotes right associativity
   */
  final static int RIGHT_ASSOCIATIVE  = 1;
  /**
   * Denotes absence of associativity
   */
  final static int NON_ASSOCIATIVE    = 2;

  /**
   * <h2>Precedence level bounds</h2>
   *
   * Denotes minimum (weakest) precedence
   */
  final static int MIN_PRECEDENCE = 1;
  /**
   * Denotes maximum (strongest) precedence
   * (1200 for Prolog's compatibility)
   */
  final static int MAX_PRECEDENCE = 1200;

  /**
   * Current precedence value.
   */
  private static int currentPrecedence;
  /**
   * Current precedence level.
   */
  private static int precedenceLevel = MIN_PRECEDENCE;
  /**
   * Precedence increment.
   */
  private static int precedenceIncrement = 10;

//   /**
//    * Sets the precedence increment to the specified number.
//    * @param n the increment
//    */
//   public final static void setPrecedenceIncrement (int n)
//     {
//       precedenceIncrement = n;
//     }

  /**
   * Checks whether the specified precedence level is within legal bounds.
   * If it is not so, this issues a warning and returns MIN_PRECEDENCE.
   * @param p the precedence level
   */
  public final static int checkPrecedenceLevel (int p)
    {
      if (p < MIN_PRECEDENCE || p > MAX_PRECEDENCE)
          {
            warning("Token precedence value out of range: "+p
                    +" (precedence set to "+MIN_PRECEDENCE+")");
            return MIN_PRECEDENCE;
          }
      return p;
    }

  /**
   * Converts yacc precedence level value into Prolog's binding tightness
   * measure (i.e., anti-gravity, or repulsion level).
   * @param p the yacc precedence level
   */
  final static int prologPrecedence (int p)
    {
      return MAX_PRECEDENCE+1 - p;
    }

  /**
   * Defines and records a new terminal.
   */
  private final Terminal newTerminal (String token)
    {
      Terminal symbol = new Terminal(token);
      terminalTable.put(token,symbol);
      return symbol;      
    }

  /**
   * Defines and records a new operator terminal.
   */
  private final Terminal newTerminal (String token, boolean isOperator)
    {
      Terminal symbol = new Terminal(token,isOperator);
      terminalTable.put(token,symbol);
      return symbol;      
    }

  /**
   * Defines and records a new nonterminal.
   */
  private final NonTerminal newNonTerminal (String token)
    {
      NonTerminal symbol = new NonTerminal(token);
      nonterminalTable.put(token,symbol);
      return symbol;      
    }

  /**
    * Creates a new grammar rule for the current LHS using the current rule
    * sequence of grammar symbols, and resets all the global parameters to
    * read a new rule. Note the use of <tt>intern()</tt> for faster comparison
    * in the <a href=ParserGenerator.html>ParserGenerator.java</a> class.
    */
  private final void newRule ()
    {
      GrammarSymbol[] sequence = new GrammarSymbol[ruleSequence.size()];

      for (int i=0; i<sequence.length; i++)
        sequence[i] = (GrammarSymbol)ruleSequence.get(i);

      if (ruleAction == null || ruleAction.length() == 0)
        if (sequence.length == 1)
          ruleAction = EMPTY_ACTION;
        else
          ruleAction = DEFAULT_ACTION;

      if (ruleUndoAction == null || ruleUndoAction.length() == 0)
        ruleUndoAction = EMPTY_ACTION;

      Rule r = new Rule(sequence,ruleAction,ruleUndoAction,nodeCast);
      r.setXmlInfo(ruleXmlInfo);

      if (ruleTag != null)
        {
          r.tag = ruleTag;
          r.tagPosition = -1;
        }

      if (Options.getDocOnly() && doc != null)
        {
          r.doc = doc;
          doc = null;
        }

      resetRuleParameters();      
    }

  /**
   * Constants denoting syntax mode used at different points
   * while reading a grammar.
   */
  
  final static int RAW_MODE    = 0;     // all chars alike and ordinary
  final static int NORMAL_MODE = 1;     // default grammar syntax mode
  final static int EOL_MODE    = 2;     // EOL is significant
  final static int NO_EOL_MODE = 3;     // EOL is NOT significant

  static boolean isRAW_MODE;
  static boolean isEOL_MODE;

  static boolean wasRAW_MODE;
  static boolean wasEOL_MODE;

  /**
   * Sets the syntax mode to the specified mode.
   */
  final void setSyntax (int mode)
    {
      switch (mode)
        {
        case RAW_MODE:
          st.resetSyntax();
          isRAW_MODE = true;
          break;
        case NORMAL_MODE:
          setGrammarSyntax();
          isRAW_MODE = false;
          break;
        case EOL_MODE:
          st.eolIsSignificant(true);
          isEOL_MODE = true;
          break;
        case NO_EOL_MODE:
          st.eolIsSignificant(false);
          isEOL_MODE = false;
          break;
        }
    }

  final void saveSyntax ()
    {
      wasRAW_MODE = isRAW_MODE;
      wasEOL_MODE = isEOL_MODE;
    }

  final void restoreSyntax ()
    {
      setSyntax(wasRAW_MODE ? RAW_MODE : NORMAL_MODE);
      setSyntax(wasEOL_MODE ? EOL_MODE : NO_EOL_MODE);
    }

  /**
   * Returns the next token after checking for potential slash-star
   * comments. These are ignored and skipped most of the time, except
   * when in documentation mode and the comment is a javadoc style
   * comment (<i>i.e.</i>, starting with a slash immediately followed
   * by two stars. Such comments are specially processed to generate
   * documentation. They are treated like normal javadoc comments, with
   * additional annotation specific to Jacc grammars.
   */
  private final int getToken () throws IOException
    {
      int t = st.nextToken();

      if (isRAW_MODE)
	return t;

      if (t == '/' && st.peek() == '*')
        {
          saveSyntax();

          if (Options.getDocOnly() && !codeSection)
            processSlashStarComment();
          else
            skipSlashStarComment();

          restoreSyntax();

          return getToken();
        }

      return t;
    } 

  /**
   * A <tt>GrammarSymbol</tt> to hold and accumulate the current grammar
   * symbol to which all documentation comments entered as javadoc
   * comments) are to be attached. This is reset explicitly with the
   * <tt>%doc</tt> command, or implicitly in the rules section of a grammar
   * with each reading of a new rule (whereby it is set to the next
   * nonterminal - the head of the next rule). The comments are accumulated
   * in the <tt>doc</tt> attribute of a <tt>GrammarSymbol</tt>.
   */
  GrammarSymbol docSymbol;

  /**
   * A <tt>StringBuilder</tt> to hold and accumulate the documentation
   * (<i>i.e.</i>, javadoc) comments for the main page of the grammar's
   * documentation.
   */
  StringBuilder mainDoc;

  /**
   * A <tt>StringBuilder</tt> to hold and accumulate the documentation
   * (<i>i.e.</i>, javadoc) comments for the current grammar symbol's
   * documentation.
   */
  StringBuilder doc;

  private final void processSlashStarComment () throws IOException
    {
      st.skipChar();            // skip the first star

      if (st.peek() != '*')
        { // This is not a javadoc-style comment: skip it!
          skipSlashStarComment();
          return;
        }

      // This is a javadoc-style comment: record it...

      StringBuilder doc = new StringBuilder();

      setSyntax(RAW_MODE);
      st.nextToken();           // skip the slash

      while (st.nextToken() != '*' || st.peek() != '/')
	if (st.ttype != '*')
	  doc.append((char)st.ttype);

      doc.append("\n<P>\n");    // add a paragraph break
      st.nextToken();           // skip the slash

      if (docSymbol != null)
	{
	  docSymbol.addDoc(doc);
	  docSymbol = null;
	  doc = null;
	  return;
	}      

      if (mainDocSection)
        {
          if (mainDoc == null)
	    mainDoc = new StringBuilder();
          mainDoc.append(doc);
        }
      else
	{
	  if (this.doc == null)
	    this.doc = new StringBuilder();
	  this.doc.append(doc);
	}
    }

  private final void skipSlashStarComment () throws IOException
    {
      st.pushBack();
      setSyntax(RAW_MODE);

      while (!(st.nextToken() == '*' && st.peek() == '/'));

      st.skipChar();    // skip final slash
      return;
    }

  /**
   * [Re]Initializes the configuration of the <tt>StreamTokenizer</tt>
   * reading the grammar to its default settings.
   */
  private final void setGrammarSyntax ()
    {
      st.setDefaultSyntax();
      st.quoteChar(SINGLE_QUOTE);
      st.quoteChar(DOUBLE_QUOTE);
      st.slashStarComments(false);
    }

  /**
   * The reader used to read the grammar files.
   */
  IncludeReader rd;
      
  private final String location ()
    {
      if (rd == null)
	return "unlocatable Reader";

      return "(file " + rd.getFile() + ", line " + rd.getLineNumber() + ")";
    }

  private final void complain (String msg) throws Exception
    {
      throw new BadGrammarException(msg+" "+location());
    }

  /**
   * This is the main reading method.
   */
  private final void readGrammar () throws Exception
    {
      reportProgress();

      try
        {
          rd = new IncludeReader(new BufferedReader(new FileReader(grammarPathedName)));
	  rd.setFile(grammarPathedName);
          st = new StreamTokenizer(rd);
        }
      catch (Exception e)
        {
          complain("File not found: "+e.getMessage());
// 	  e.printStackTrace();
        }

      setSyntax(NORMAL_MODE);      
      try
        {
          readDeclarations();
          readRules();
          readRemainder();
        }
      catch (EOFException e)
        {
          try
            {
              rd.close();
            }
          catch (IOException i)
            {
              complain("An IOException happened when closing the input reader");
            }
        }
//       catch (Exception e)
//         {
//           complain(e.getMessage());
//         }
    }

  private final void readDeclarations () throws Exception
    {
      while (readDeclaration());
    }

  private final boolean readDeclaration () throws Exception
    {
      switch (getToken())
        {
        case COMMAND_START:
          return executeNextCommand();
        case StreamTokenizer.TT_EOF:
          complain("Premature end of file");
        case StreamTokenizer.TT_EOL:
          return readDeclaration();
        default:
          complain("Ill-formed syntax in grammar declarations section");
        }

      return false;
    }

  private final boolean executeNextCommand () throws Exception
    {
      String command = readCommand();

      switch (commandCode(command))
        {
        case SECTION_COMMAND:
	  setSyntax(NO_EOL_MODE);
          mainDocSection = false;
          return false;
        case UNKNOWN_COMMAND:
          complain("Unknown grammar command: "
		   +(char)COMMAND_START+command); 
        case START_COMMAND:
          declareStart();
          break;
        case ROOT_COMMAND:
          declareRoot();
          break;
        case LEFT_COMMAND:
          declareLeftAssoc();
          break;
        case RIGHT_COMMAND:
          declareRightAssoc();
          break;
        case TOKEN_COMMAND:     // non-associative by default
        case NONASSOC_COMMAND:
          declareNonassoc();
          break;
        case LBRACE_COMMAND:
	  codeSection = true;
          parserDeclarations
	    .add(verbatim(String.valueOf((char)COMMAND_START)+RBRACE_COMMAND_ID));
	  codeSection = false;
          break;
        case USEFILE_COMMAND:
          declareList(parserDeclarations);
          break;
        case PACKAGE_COMMAND:
          declarePackage();
          break;
        case IMPORT_COMMAND:
          declareList(imports);
          break;
        case PRECSTEP_COMMAND:
          declarePrecstep();
          break;
        case NODECLASS_COMMAND:
          declareNodeClass();
          break;
        case NODEPREFIX_COMMAND:
          declareNodePrefix();
          break;
        case NODESUFFIX_COMMAND:
          declareNodeSuffix();
          break;
        case DOC_COMMAND:
          processDoc();
          break;
        case INCLUDE_COMMAND:
          processInclude();
          break;
        case XMLROOT_COMMAND:
          processXmlRoot();
          break;
        case XMLNS_COMMAND:
          processXmlNs();
          break;
// BEGIN COMMENT OUT FOR NO XML ANNOTATION PARSER
        case XMLINFO_COMMAND:
          processTerminalXmlAnnotation();
          break;
// END COMMENT OUT FOR NO XML ANNOTATION PARSER
        case DYNAMIC_COMMAND:
          declareDynamic();
          break;
        case OPERATOR_COMMAND:
          declareOperator();
          break;
        case ACCESS_COMMAND:
          declareAccess();
          break;
        default:
          complain("Command "+command+" is not yet implemented - sorry!");
        }

      return true;
    }

  private final String readCommand () throws Exception
    {
      checkCommandSyntax();

      String command = null;

      if (getToken() == StreamTokenizer.TT_WORD)
        command = st.sval.intern();
      else
        {
          command = String.valueOf((char)st.ttype).intern();
          if (command != LBRACE_COMMAND_ID && command != SECTION_COMMAND_ID)
            complain("Ill-formed grammar command: "+command);
        }

      return command;
    }

  private final void checkCommandSyntax () throws Exception
    {
      if (!validCommandStartCharacter(st.peek()))
        complain("Invalid command start: "
		 +(char)COMMAND_START+(char)st.peek()+"...");
    }

  private final boolean validCommandStartCharacter (int c)
    {
      for (Iterator e=commandCodeTable.keySet().iterator(); e.hasNext();)
        if ((char)c == ((String)e.next()).charAt(0)) return true;
      
      for (Iterator e=operatorCategoryTable.keySet().iterator(); e.hasNext();)
        if ((char)c == ((String)e.next()).charAt(0)) return true;

      return false;
    }      

  private final void declareStart () throws Exception
    {
      if (startIsDefined())
        complain("Start symbol ("+startSymbol()+") cannot be redefined");

      if (getToken() != StreamTokenizer.TT_WORD)
        complain("Ill-formed start symbol declaration");
      
      GrammarSymbol symbol = getTerminal(st.sval);
      if (symbol != null)
        complain("Token "+symbol+" cannot be the start symbol!");
      
      symbol = getNonTerminal(st.sval);

      if (symbol != null)
        if (((NonTerminal)symbol).isStart())
          complain("Symbol "+symbol+" is already the start symbol!");
        else
          defineStart((NonTerminal)symbol);
      else
        defineStart(newNonTerminal(st.sval));
    }

  private final void defineStart (NonTerminal symbol)
    {
      (GRAMMAR_START = symbol).makeStart();
      GrammarSymbol[] seq = {ROOTS, GRAMMAR_START};
      new Rule(seq);

      if (!symbol.isRoot())
        defineRoot(symbol);
    }

  private final void declareRoot () throws Exception
    {
      if (getToken() != StreamTokenizer.TT_WORD)
        complain("Ill-formed root symbol declaration");
      
      GrammarSymbol symbol = getTerminal(st.sval);
      if (symbol != null)
        complain("Token "+symbol+" cannot be a root symbol!");
      
      symbol = getNonTerminal(st.sval);

      if (symbol != null)
        if (((NonTerminal)symbol).isRoot())
          complain("Symbol "+symbol+" is already a root symbol!");
        else
          defineRoot((NonTerminal)symbol);
      else
        defineRoot(newNonTerminal(st.sval));
    }

  private final void defineRoot (NonTerminal symbol)
    {
      symbol.makeRoot();
      Terminal token = newTerminal("$"+symbol.name()+"_switch$");
      roots.put(symbol,token);
      
      GrammarSymbol[] seq = {ROOTS, token, symbol};
      new Rule(seq,"$head$ = $head$.copy(node($rule$,2));");

      if (FIRST_ROOT == null)
        FIRST_ROOT = symbol;
    }

  private final void defineTokens (ArrayList tokens, int associativity) throws Exception
    {
      for (Iterator e = tokens.iterator(); e.hasNext();)
        defineToken((String)e.next(),associativity);
    }      

  private final void defineToken (String token, int associativity) throws Exception
    {
      GrammarSymbol symbol = getNonTerminal(token);
      if (symbol != null)
        complain("Nonterminal "+symbol+" cannot be a token!");
      
      symbol = getTerminal(token);
      if (symbol != null)
        complain("Duplicate token declaration: "+symbol);

      symbol = newTerminal(token);
      ((Terminal)symbol).precedence = currentPrecedence;
      ((Terminal)symbol).associativity = associativity;
    }

  private final ArrayList tokensInLine () throws Exception
    {
      ArrayList tokens = new ArrayList(10);
      setSyntax(EOL_MODE);

      currentPrecedence = -1;

      while (getToken() != StreamTokenizer.TT_EOL)
        {
          if (st.ttype == '\\' && st.peek() == NEWLINE)
            {
              st.skipChar();
              getToken();
            }

          switch (st.ttype)
            {
            case SINGLE_QUOTE:
            case DOUBLE_QUOTE:
            case StreamTokenizer.TT_WORD:
              tokens.add(st.sval);
              break;
            case StreamTokenizer.TT_NUMBER:
              if (currentPrecedence != -1)
                complain("Duplicate precedence specified in token declaration");
              currentPrecedence =
                checkPrecedenceLevel(prologPrecedence((int)st.nval));
              break;
            case StreamTokenizer.TT_EOF:
              complain("Premature end of file");
            default:
              tokens.add(String.valueOf((char)st.ttype));
            }
        }

      if (tokens.isEmpty())
        complain("No token found in token declaration");

      if (currentPrecedence == -1) currentPrecedence = nextPrecedenceLevel();
      setSyntax(NO_EOL_MODE);
      return tokens;
    }

  private final int nextPrecedenceLevel ()
    {
      int p = precedenceLevel;
      precedenceLevel += precedenceIncrement;
      return checkPrecedenceLevel(p);
    }

  private final void declareLeftAssoc () throws Exception
    {
      defineTokens(tokensInLine(),LEFT_ASSOCIATIVE);
    }

  private final void declareRightAssoc () throws Exception
    {
      defineTokens(tokensInLine(),RIGHT_ASSOCIATIVE);
    }

  private final void declareNonassoc () throws Exception
    {
      defineTokens(tokensInLine(),NON_ASSOCIATIVE);
    }

  private final void declareNodePrefix () throws Exception
    {
      setSyntax(EOL_MODE);

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE: case DOUBLE_QUOTE:
	  nodePrefix = st.sval.intern();
	  break;
        default:
          complain("Missing node prefix specifier");
        }

      setSyntax(NO_EOL_MODE);
    }      

  private final void declareNodeSuffix () throws Exception
    {
      setSyntax(EOL_MODE);

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE: case DOUBLE_QUOTE:
	  nodeSuffix = st.sval.intern();
	  break;
        default:
          complain("Missing node suffix specifier");
        }

      setSyntax(NO_EOL_MODE);
    }      

  /**
   * Processes a <tt>%doc</tt> command, whose argument is assumed
   * to be a nonterminal symbol.
   */
  private final void processDoc () throws Exception
    {
      setSyntax(EOL_MODE);

      switch (getToken())
	{
	case SINGLE_QUOTE:
	case DOUBLE_QUOTE:
	  docSymbol = getTerminal(st.sval);
	  if (docSymbol == null)
	    docSymbol = newTerminal(st.sval);
	  break;
	  
	case StreamTokenizer.TT_WORD:
	  docSymbol = getTerminal(st.sval);
	  if (docSymbol == null)
	    {
	      docSymbol = getNonTerminal(st.sval);
	      if (docSymbol != null)
		docSymbol = newTerminal(st.sval);
	    }
	  break;

	default:
	  complain("Missing symbol in doc command");
	}

      setSyntax(NO_EOL_MODE);
  }

  private final void declareAccess () throws Exception
    {
      setSyntax(EOL_MODE);

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
           accessTag = st.sval.intern();
           if (!(accessTag == "public"
              || accessTag == "private"
              || accessTag == "protected"))
             complain("Bad access tag value ("+accessTag+")");
           accessTag += " ";
           break;
        default:
          complain("Missing tag in access command");
        }

      setSyntax(NO_EOL_MODE);
    }

  private SetOf nodeClasses = new SetOf(nonterminals);
  private SetOf superNodeClasses = new SetOf(nonterminals);

  private final void declareNodeClass () throws Exception
    {
      if (getToken() != StreamTokenizer.TT_WORD)
        complain("Ill-formed node class declaration");

      String publInfo = "";

      if (st.sval.intern() == "public")
        {
          publInfo = "public";

          if (getToken() != StreamTokenizer.TT_WORD)
            complain("Ill-formed nodeclass declaration");
        }

      if (getTerminal(st.sval) != null)
        complain("Can't define a node class for a terminal ("+st.sval+")");

      NonTerminal symbol = getNonTerminal(st.sval);

      if (symbol != null)
        {
          if (nodeClasses.contains(symbol))
            complain("Duplicate node class declaration for symbol "+symbol);
        }
      else
        symbol = newNonTerminal(st.sval);

      nodeClasses.add(symbol);

      NonTerminal superSymbol = null;

      if (getToken() == StreamTokenizer.TT_WORD && st.sval.intern() == "extends")
        {
          if (getToken() != StreamTokenizer.TT_WORD
              || st.sval.intern() == "implements"
              || st.sval.intern() == "locates")
            complain
	      ("Error in node class declaration: identifier missing after 'extends'");

          if (getTerminal(st.sval) != null)
            complain("Can't define node class "+symbol
		     +" as a subclass of a terminal ("+st.sval+")");

          if ((superSymbol = getNonTerminal(st.sval)) == null)
            superSymbol = newNonTerminal(st.sval);

          superNodeClasses.add(superSymbol);
        }
      else
        st.pushBack();

      String implInfo = "";

      if (getToken() == StreamTokenizer.TT_WORD && st.sval.intern() == "implements")
        {
          implInfo = "implements ";

          if (getToken() != StreamTokenizer.TT_WORD || st.sval.intern() == "locates")
            complain
	      ("Error in nodeclass declaration: identifier missing after 'implements'");

          do
            {
              implInfo += st.sval;
              if (getToken() == COMMA)
                implInfo += ", ";
              else
                break;
            }
          while (getToken() == StreamTokenizer.TT_WORD && st.sval.intern() != "locates");
        }

      st.pushBack();

      String locateInfo = null;

      if (getToken() == StreamTokenizer.TT_WORD && st.sval.intern() == "locates")
        {
          if (getToken() != StreamTokenizer.TT_WORD)
            complain
	      ("Error in nodeclass declaration: identifier missing after 'locates'");

          locateInfo = st.sval;
        }
      else
        st.pushBack();

      if (getToken() != BEGIN_SCOPE)
        complain("Missing '"+(char)BEGIN_SCOPE+"' in nodeclass declaration");

      String body = bracedString();

      symbol.nodeType = nodePrefix + symbol.name + nodeSuffix;
      String superClass = superSymbol == null
          ? "ParseNode"
          : nodePrefix + superSymbol.name + nodeSuffix;

      StringBuilder nodeClass = new StringBuilder(bufferSize);

      nodeClass
        .append(publInfo)
        .append(publInfo.length()>0 ? " " : "")
        .append("class ")
        .append(symbol.nodeType)
        .append(" extends "+superClass+" ")
        .append(implInfo)
        .append("\n{\n  ")
        .append(publInfo)
        .append(publInfo.length()>0 ? " " : "")
        .append(symbol.nodeType)
        .append(" (ParseNode node)\n    {\n      super(node);\n    }\n\n  ")
        .append(body.trim());

      if (locateInfo != null)
        nodeClass
            .append("\n")
            .append("\n  public final void locate ()")
            .append("\n    {")
            .append("\n      if ("+locateInfo+" != null)")
            .append("\n        {")
            .append("\n          "+locateInfo+".setStart(getStart());")
            .append("\n          "+locateInfo+".setEnd(getEnd());")
            .append("\n        }")
            .append("\n    }");

      nodeClass.append("\n}");

      if (publInfo.intern() != "public")
        ancillaryClasses.add(nodeClass);
      else
        publicClasses.put(symbol.nodeType,nodeClass);
    }

  private final String annotationString () throws Exception
    {
      return bracedString(BEGIN_ANNOTATION,END_ANNOTATION);
    }

  private final String bracedString () throws Exception
    {
      return bracedString(BEGIN_SCOPE,END_SCOPE);
    }

  private final String bracedString (int start, int end) throws Exception
    {
      StringBuilder buffer = new StringBuilder(bufferSize);
      setSyntax(RAW_MODE);   // Make all characters ordinary
      int nesting = 1;       // Nesting of braces

      while (nesting > 0)
        {
	  int tk = getToken();

	  if (tk == start)
	    {
	      buffer.append((char)start);
              nesting++;
	      continue;
	    }

	  if (tk == end)
	    {
              if (nesting > 1)
		buffer.append((char)end);
              nesting--;
	      continue;
	    }

	  buffer.append((char)tk);
	}

      setSyntax(NORMAL_MODE);
      return buffer.toString();
    }

  private final void declarePackage () throws Exception
    {
      if (packageName != null)
        complain("Duplicate package declaration");
        
      if (getToken() != StreamTokenizer.TT_WORD)
        complain("Bad package declaration");

      packageName = st.sval;

      setSyntax(EOL_MODE);
      while (getToken() != StreamTokenizer.TT_EOL);
      setSyntax(NO_EOL_MODE);
    }

  private final void declareList (ArrayList v) throws Exception
    {
      setSyntax(RAW_MODE);
      StringBuilder buffer = new StringBuilder(bufferSize);
      String element = "";

    out:      
      for (;;)
        switch (getToken())
          {
          case StreamTokenizer.TT_EOF:
            if (!readingRemainderSection)
              complain("Premature end of file");
	    break;
          case COMMAND_START:
            st.pushBack();
          case END_OF_RULE:
            element = buffer.toString().trim();
            if (element.length()>0)
	      v.add(element);
            break out;
          case COMMA: case SPACE: case NEWLINE: case TAB:
            element = buffer.toString().trim();
            if (element.length()>0)
              {
                v.add(element);
                buffer = new StringBuilder(bufferSize);
              }
            break;
          default:
            buffer.append((char)st.ttype);
          }

      if (!readingRemainderSection)
	setSyntax(NORMAL_MODE);
    }

  private final void processInclude () throws Exception
    {
      setSyntax(EOL_MODE);

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE: case DOUBLE_QUOTE:
	  rd.include(Options.includeBase()+File.separator+st.sval);
	  break;
        default:
          complain("Bad %include argument");
        }

      setSyntax(NO_EOL_MODE);

      if (verbosity > Verbose.QUIET)
        out.println("*** Including file "+rd.getFile()+" ...");
    }

  private final String readLine () throws Exception
    {
      StringBuilder buffer = new StringBuilder(bufferSize);

      setSyntax(RAW_MODE);

      int c;
      while ((c=getToken()) != StreamTokenizer.TT_EOL)
	buffer.append((char)c);

      setSyntax(NORMAL_MODE);
      return buffer.toString();
    }

  private final void processXmlRoot () throws Exception
    {
      if (xmlroot != null)
	complain("Duplicate %xmlroot command");

      setSyntax(EOL_MODE);

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE: case DOUBLE_QUOTE:
	  xmlRootNSPrefix = st.sval;
	  break;
        default:
          complain("Ill-formed XML element name in %xmlroot command");
        }

      switch (getToken())
        {
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE: case DOUBLE_QUOTE:
	  xmlroot = st.sval;
	  break;
	case NEWLINE:
	  xmlroot = xmlRootNSPrefix;
	  xmlRootNSPrefix = "";
	  break;	  
        default:
          complain("Ill-formed XML element name in %xmlroot command");
        }

      if (verbosity > Verbose.QUIET)
	{
	  if (xmlRootNSPrefix != "")
	    {
	      out.println("*** Setting XML root's namespace to "+xmlRootNSPrefix);
	      out.println("*** Setting XML root to "+xmlRootNSPrefix+":"+xmlroot);
	    }
	  else
	    out.println("*** Setting XML root to "+xmlroot);
	}

      setSyntax(NORMAL_MODE);

    }

  private ArrayList namespaces = new ArrayList();

  ArrayList namespaces ()
    {
      return namespaces;
    }

  private final void processXmlNs () throws Exception
    {
      setSyntax(EOL_MODE);

      getToken();
      if (st.ttype != SINGLE_QUOTE && st.ttype != DOUBLE_QUOTE
	  && st.ttype != StreamTokenizer.TT_WORD)
	complain("Bad XML namespace identifier in '%xmlns' command");

      String prefix = st.sval;
      namespaces.add(prefix);

      getToken();
      if (st.ttype != SINGLE_QUOTE && st.ttype != DOUBLE_QUOTE)
	complain("Bad XML namespace value: missing quote in '%xmlns' command");

      namespaces.add(st.sval);

      if (verbosity > Verbose.QUIET)
        out.println("*** Defining XML namespace prefix: "+prefix+" = \""+st.sval+"\"");


      setSyntax(NORMAL_MODE);
    }

  private final Terminal getDefinedTerminal (String token) throws Exception
    {
      GrammarSymbol symbol = getNonTerminal(token);
      if (symbol != null)
        complain("Can't annotate non-terminal symbol '"+token+"'");
      
      symbol = getTerminal(token);
      if (symbol == null)
        complain("Can't annotate undefined terminal symbol '"+token+"'");

      return (Terminal)symbol;
    }

  private final void declarePrecstep () throws Exception
    {
      if (getToken() != StreamTokenizer.TT_NUMBER)
        complain("Precedence increment must be a number");

      precedenceIncrement = (int)st.nval;
    }

  private final void declareDynamic () throws Exception
    {
      setSyntax(EOL_MODE);

      String cat = null;

      switch (getToken())
        {
        case StreamTokenizer.TT_EOF:
          complain("Premature end of file");
        case StreamTokenizer.TT_EOL:
	  break;
        case StreamTokenizer.TT_WORD:
          cat = st.sval;
          break;
        default:
          complain("Bad dynamic operator declaration");
        }

      isDynamic = true;

      if (cat == null) // simply allows dynamic tokenizing, no dynamic operators
	return;

      GrammarSymbol catSymbol = getNonTerminal(cat);

      if (catSymbol == null)
        catSymbol = newNonTerminal(cat);

      ((NonTerminal)catSymbol).isOperator = true;

      if (getTerminal(cat) == null)
        {
          operatorCategoryTable.put(cat,new ArrayList());
	  cat = cat.toUpperCase();
          declareSubCategory(catSymbol,cat+"_");
          declareSubCategory(catSymbol,"_"+cat+"_");
          declareSubCategory(catSymbol,"_"+cat);
        }
      else
        complain("Terminal "+cat+" cannot be used as an operator category!");

      setSyntax(NO_EOL_MODE);
    }

  private final void declareSubCategory (GrammarSymbol catSymbol, String subCat)
    {
      GrammarSymbol subCatSymbol = getTerminal(subCat);
      
      if (subCatSymbol == null)
        subCatSymbol = newTerminal(subCat,true);
      else
        warning("operator subcategory "+subCat+" is multiply defined");

      GrammarSymbol[] sequence = {catSymbol, subCatSymbol};
      
      new Rule(sequence,DEFAULT_ACTION);
    }     

  private final void declareOperator () throws Exception
    {
      String name = null;

      setSyntax(EOL_MODE);

      ArrayList ops = (ArrayList)operatorCategoryTable.get(operatorCategory.name);

      switch (getToken())
        {
        case StreamTokenizer.TT_EOF:
          complain("Premature end of file");
        case StreamTokenizer.TT_EOL:
          complain("Premature end of dynamic operator declaration");
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE:
        case DOUBLE_QUOTE:
          name = st.sval;
          break;
        case StreamTokenizer.TT_NUMBER:
          complain("Bad dynamic operator declaration");
        default:
          name = String.valueOf((char)st.ttype);
        }

      if (getNonTerminal(name) != null)
        complain("Nonterminal "+name+" cannot be used as an operator!");
            
      String specifier = null;
      int precedence = 0;

      switch (getToken())
        {
        case StreamTokenizer.TT_EOF:
          complain("Premature end of file");
        case StreamTokenizer.TT_EOL:
          complain("Premature end of dynamic operator declaration");
        case StreamTokenizer.TT_WORD:
        case SINGLE_QUOTE:
        case DOUBLE_QUOTE:
          specifier = st.sval;
          switch (getToken())
            {
            case StreamTokenizer.TT_NUMBER:
              precedence =
                checkPrecedenceLevel(prologPrecedence((int)st.nval));
              break;
            case StreamTokenizer.TT_EOL:
              precedence = nextPrecedenceLevel();
              st.pushBack();
              break;
            default:
              complain("Bad dynamic operator declaration");
            }
          break;
        case StreamTokenizer.TT_NUMBER:
          precedence = checkPrecedenceLevel(prologPrecedence((int)st.nval));
          switch (getToken())
            {
            case StreamTokenizer.TT_WORD:
            case SINGLE_QUOTE:
            case DOUBLE_QUOTE:
              specifier = st.sval;
              break;
            default:
              complain("Bad dynamic operator declaration");
            }
          break;
        default:
          complain("Bad dynamic operator declaration");
        }

      Operator operator = new Operator(name,operatorCategory,precedence,specifier);

      int i = ops.indexOf(operator);

      if (i == -1)
        {
          operator.add();
          ocount++;
          ops.add(operator);
          ops = (ArrayList)operatorNameTable.get(name);
          if (ops == null) operatorNameTable.put(name,ops = new ArrayList());
          ops.add(operator);
        }
      else
        ((Operator)operators.get(i)).redefine(precedence,specifier);
      
      setSyntax(NO_EOL_MODE);
    }

  /**
   * This reads from the input in raw mode (<i>i.e.</i>, with all
   * characters made ordinary), accumulating what is read into a string
   * buffer until a string matching the specified end marker is found -
   * upon which the string buffer is returned.
   */
  private final StringBuilder verbatim (String endMarker) throws Exception
    {
      StringBuilder buffer       // character accumulator
        = new StringBuilder(bufferSize);
      StringBuilder endMatch     // matched prefix of end marker
        = new StringBuilder(bufferSize);

      setSyntax(RAW_MODE);      // make all characters ordinary

      int fullMatch = endMarker.length();

      if (fullMatch > 0)
        {
          int matchCount;
          char c = (char)getToken();

          for (;;)
            {
              if (c != endMarker.charAt(0))
                {
                  buffer.append(c);
                  c = (char)getToken();
                  continue;
                }
              matchCount = 0;
              endMatch.setLength(0);
              while (matchCount < fullMatch && c == endMarker.charAt(matchCount))
                {
                  endMatch.append(c);
                  matchCount++;
                  c = (char)getToken();
                }
              if (matchCount == fullMatch)
                {
                  st.pushBack();
                  break;
                }
              buffer.append(endMatch);
            }
        }
      
      setSyntax(NORMAL_MODE);
      return buffer;
    }

    // Note for future work [by hak]:
    // After readRules, one may optionally partially evaluate the
    // grammar rules by replacing the LHS of a singular rule (i.e., one
    // such that there is only one rule for this LHS) by the rules RHS
    // in all the other rules, and delete the rule whenever the LHS
    // neither recursive nor $START$. NB: it's useless for a singular
    // recursive rule ...

  private final void readRules () throws Exception
    {
      if (!(superNodeClasses.minus(nodeClasses)).isEmpty())
        {
          System.err.println
	    ("*** The following non-terminals must be declared as node classes:");
          System.err.println();
          for (Iterator i = superNodeClasses.iterator(); i.hasNext();)
            System.err.println("\t"+i.next());
          System.err.println();
          complain("Incomplete node class declarations");
        }

      while (readRule());

      if (!startIsDefined())
        {
          String msg = "There are no rules in this grammar!";
          if (permissible)
            warning(msg);
          else
            complain(msg);
        }

      if (undefinedSymbols() | unreachableSymbols())
        {
          String msg = "This grammar has disconnected symbols";
          if (permissible)
            warning(msg);
          else
            complain(msg);  
        }
    }

  private final boolean readRule () throws Exception
    {
      if (!readRuleHead())
	return false;
      readRuleBody();
      return true;
    }

  private final boolean readRuleHead () throws Exception
    {
      switch (getToken())
        {
        case StreamTokenizer.TT_EOF:
          return false;
        case StreamTokenizer.TT_WORD:
          checkSymbol(st.sval,true);
          if (getToken() != RULE_NECK)
            complain("Missing RULE_NECK after left-hand side symbol in rule");
          break;
        case RULE_NECK:
          if (currentLHS == null)
            complain("Missing left-hand side symbol in rule");
          break;
        case COMMAND_START:
          setSyntax(RAW_MODE);
          if (st.peek() == COMMAND_START)
	    return false;
	  // Only %include is allowed here
	  return executeIncludeCommand() && readRuleHead();
        default:
	  System.out.println("Read grammar token: "+st);
          complain("Ill-formed left-hand side in rule");
        }

      return true;
    }

  private final boolean executeIncludeCommand () throws Exception
    {
      setSyntax(NORMAL_MODE);
      String command = readCommand();

      switch (commandCode(command))
        {
        case INCLUDE_COMMAND:
          processInclude();
          break;
        default:
          complain("Command "+command+" is not allowed here");
        }

      return true;
    }

  private final void readRuleBody () throws Exception
    {
      for (;;)
        {
	  int tok = getToken();
          switch (tok)
            {
            case StreamTokenizer.TT_EOF:
              complain("Premature end of file");
            case StreamTokenizer.TT_WORD:
              checkSymbol(st.sval,false);
              break;
            case SINGLE_QUOTE:
            case DOUBLE_QUOTE:
              checkTerminal(st.sval);
              break;
            case OR_RULE_BODY:
              newRule();
              ruleSequence.add(currentLHS);
              break;
            case BEGIN_SCOPE:
              readAction();
              break;
            case COMMAND_START:
              processRulePrec();
              break;
// BEGIN COMMENT OUT FOR NO XML ANNOTATION PARSER
	    case BEGIN_ANNOTATION:
	      processRuleXmlAnnotation();
	      break;
// END COMMENT OUT FOR NO XML ANNOTATION PARSER
            case END_OF_RULE:
              newRule();
              return;
            default:
              complain("Ill-formed syntax in rule body"
		       +" ('"+String.valueOf((char)tok)+"')");
            }
        }
    }

  boolean hasXmlSerialization = false;

// BEGIN COMMENT OUT FOR NO XML ANNOTATION PARSER
  private XmlAnnotationParser _xmlAnnotationParser;

  XmlAnnotationParser xmlAnnotationParser (String annotation)
    {
      if (_xmlAnnotationParser == null)
	{
	  XmlAnnotationParser
	    p = new XmlAnnotationParser(new XmlAnnotationTokenizer(annotation));
	  p.errorManager().reportErrors(false);
	  return p;
	}

      _xmlAnnotationParser.reset(annotation);
      return _xmlAnnotationParser;
    }

  /**
   * This is where a terminal's XML serialization annotation is read and
   * processed.
   */
  private final void processTerminalXmlAnnotation () throws Exception
    {
      hasXmlSerialization = true;

      setSyntax(EOL_MODE);

      getToken();
      if (st.ttype != SINGLE_QUOTE && st.ttype != DOUBLE_QUOTE
	  && st.ttype != StreamTokenizer.TT_WORD)
	complain("Bad terminal identifier in '%xmlinfo' command");

      Terminal terminal = getDefinedTerminal(st.sval.intern());
      
      if (getToken() != BEGIN_ANNOTATION)
	complain("Missing XML annotation for terminal '"+terminal+"'");

      String annotation = annotationString();
      XmlAnnotationParser p = xmlAnnotationParser(annotation);
      try
	{
//     	  System.err.println("Parsing terminal annotation:\n"+annotation);
//    	  p.toggleTrace();
	  p.parse();
//     	  System.err.println("Parsed terminal annotation:\n"+p.xmlInfo().setIsTerminal());
	}
      catch (Exception e)
	{
//  	  e.printStackTrace();
	  complain("Bad terminal XML annotation: "+annotation);
	}

      XmlInfo info = p.xmlInfo().setIsTerminal();
      if (info.localName() == null)
	info.setLocalName(terminal.name());
      terminal.setXmlInfo(info);

      setSyntax(NORMAL_MODE);
    }

  /**
   * This is where a rule's XML serialization annotation is read and
   * processed.
   */
  private final void processRuleXmlAnnotation () throws Exception
    {
      if (ruleXmlInfo != null)
	complain("Duplicate rule annotation");

      hasXmlSerialization = true;

      String annotation = annotationString();
      XmlAnnotationParser p = xmlAnnotationParser(annotation);
      try
	{
//     	  System.err.println("Parsing rule annotation:\n"+annotation);
//    	  p.toggleTrace();
	  p.parse();
//     	  System.err.println("Parsed rule annotation:\n"+p.xmlInfo());
	}
      catch (Exception e)
	{
//     	  e.printStackTrace();
	  complain("Bad rule XML annotation: "+annotation);
	}

      ruleXmlInfo = p.xmlInfo();
    }
  
// END COMMENT OUT FOR NO XML ANNOTATION PARSER

  private final void processRulePrec () throws Exception
    {
      if (st.peek() == PREC_COMMAND_ID.charAt(0))
      // e.g., in %prec, a 'p' must follow %
        {
          getToken();
          if (commandCode(st.sval) == PREC_COMMAND)
            {
              if (ruleTag != null)
                complain("Duplicate %prec command in rule");
              readRuleTag();
              return;
            }
        }
      complain("Bad %prec command in grammar rule body");
    }  

  private final void readRuleTag () throws Exception
    {
      getToken();

      if (st.ttype == StreamTokenizer.TT_WORD
          || st.ttype == SINGLE_QUOTE
          || st.ttype == DOUBLE_QUOTE)
        {
          ruleTag = getTerminal(st.sval);

          if (ruleTag == null)
            complain("Unknown token as %prec argument "+st.sval);

          if (ruleTag.isOperator())
            complain("Dynamic operators cannot be used as %prec argument "+st.sval);

          ((Terminal)ruleTag).isTag = true;

          return;
        }

      if (st.ttype == StreamTokenizer.TT_NUMBER)
        {
          int precedence = checkPrecedenceLevel(prologPrecedence((int)st.nval));

          getToken();

          if (!(st.ttype == StreamTokenizer.TT_WORD
                || st.ttype == SINGLE_QUOTE
                || st.ttype == DOUBLE_QUOTE))
            complain("Bad %prec associativity specifier");

          try
            {
              ruleTag = new RuleTag(precedence,st.sval);
            }
          catch (NonFatalParseErrorException e)
            {
              loudWarning("Ill-formed specifier "+location()+" - "+st.sval
                          +"; %prec command ignored");
            }
        }
      else
        complain("Ill-formed %prec argument in grammar rule body");
    }
          
  /**
   * Returns <tt>true</tt> iff not all non-terminal symbols are the LHS
   * of at least one rule.
   */
  private final boolean undefinedSymbols()
    {
      ArrayList undefs = new ArrayList();
      NonTerminal n;
      for (Iterator e = nonterminals.iterator(); e.hasNext();)
        {
          n = (NonTerminal)e.next();
          if (n.rules.isEmpty()) undefs.add(n);
        }
      if (!undefs.isEmpty())
        {
          if (undefs.size() > 1)
            err.println("*** These non-terminal symbols have no rules:\n");
          else
            err.println("*** This non-terminal symbol has no rules:\n");
          for (Iterator e = undefs.iterator(); e.hasNext();)
            err.println("\t"+e.next());
          err.println();
          if (undefs.size() > 1)
            err.println
                ("*** Declare them as tokens or give rules for them.");
          else
            err.println
                ("*** Declare it as a token or give rules for it.");      
          return true;
        }
      return false;
    }

  /**
   * Traverses the rules starting at the specified root symbol and
   * recording all the grammar symbols it crosses on the way in the
   * provided hash set, until no new one is found.
   */
  private final void traverseRules(GrammarSymbol root, HashSet dejaVu)
    {
      dejaVu.add(root);
      if (root instanceof NonTerminal)
        for (Iterator e = ((NonTerminal)root).rules.iterator();
             e.hasNext();)
          {
            Rule r = (Rule)e.next();
            for (int i = 1; i<r.sequence.length; i++)
              if (!dejaVu.contains(r.sequence[i]))
                traverseRules(r.sequence[i],dejaVu);
          }
    }      

  /**
   * Returns <tt>true</tt> iff the grammar contains symbols that are not
   * reachable from the grammar's start symbol. It traverses the rules
   * starting from the start symbol and recording all symbols it meets
   * in the <tt>dejaVu</tt> hash set. It then verifies that all the
   * known symbols are in <tt>dejaVu</tt>, returning <tt>true</tt> iff
   * any is missing...
   */
  private final boolean unreachableSymbols()
    {
      HashSet dejaVu = new HashSet();
      traverseRules(START,dejaVu);

      HashSet unreached = new HashSet();
      GrammarSymbol s;

      for (Iterator e = nonterminals.iterator(); e.hasNext();)
        {
          s = (GrammarSymbol)e.next();
          if (!dejaVu.contains(s)) unreached.add(s);
        }
      for (Iterator e = terminals.iterator(); e.hasNext();)
        {
          s = (GrammarSymbol)e.next();
          if (s.isSpecial()) continue;
          if (!dejaVu.contains(s)) unreached.add(s);
        }
      if (!unreached.isEmpty())
        {
          if (unreached.size() > 1)
            err.print("*** These symbols are ");
          else
            err.print("*** This symbol is ");
          err.println("not reachable from any root:\n");
          for (Iterator e = unreached.iterator(); e.hasNext();)
            err.println("\t"+e.next());
          err.println();
          err.println("*** Check the grammar in File "+grammarPathedName+
                      " and correct this.");
          return true;
        }
      return false;
    }

  /**
   * This method checks whether the specified string token is a known
   * (terminal or non-terminal) grammar symbol. If it is found to be
   * a known terminal and <tt>isLHS</tt> is <tt>true</tt>, it reports
   * an error because a terminal may not be used as a LHS symbol. If not,
   * and it is neither a know nonterminal, it creates a new nonterminal
   * symbol for it and records it. It also sets the grammar's start symbol
   * if need be and takes care of recording doc comments associated with
   * this symbol. In all cases, it then checks whether this token follows
   * (what would be) an intermediate semantic action, which necessitates
   * creating an implicit empty rule.
   *
   * @param token a string token
   * @param isLHS <tt>true</tt> iff the token is the LHS of a rule
   */
  private final void checkSymbol (String token, boolean isLHS)
      throws Exception
    {
      GrammarSymbol symbol = getTerminal(token);

      if (symbol != null) // this symbol is a terminal
        {
          if (isLHS)    // it cannot be a left-hand side!
            complain("Terminal "+symbol+" cannot be a left-hand side!");
        }
      else
        {
          if ((symbol = getNonTerminal(token)) == null)
            symbol = newNonTerminal(token);

          if (isLHS)
            {
              currentLHS = symbol;

              if (!startIsDefined())
                defineStart(FIRST_ROOT == null ? (NonTerminal)symbol : FIRST_ROOT);

              symbol.addDoc(doc);
              doc = null;
            }
        }

      checkAction(symbol);
    }

  /**
   * This method checks whether the specified string token is
   * a known terminal symbol. If not, creates it and records it in
   * the terminal table. In all cases, it then checks if this token
   * follows (what would be) an intermediate semantic action which
   * would necessitate creation of an implicit empty rule.
   *
   * @param token a string token
   */
  private final void checkTerminal (String token)
    {
      Terminal symbol = getTerminal(token);
      if (symbol == null)
        symbol = newTerminal(token);
      checkAction(symbol);
    }

  /**
   * This method checks whether an intermediate semantic action has
   * been read immediately prior to the specified grammar symbol. If
   * so, it creates a new empty rule whose LHS is a new non-terminal
   * symbol corresponding to the action (as in Yacc), and resets action
   * reading. Otherwise, this simply adds the grammar symbol to the
   * symbol sequence for the rule being read.
   *
   * @param symbol a grammar symbol
   */
  private final void checkAction (GrammarSymbol symbol)
    {
      if (ruleAction != null)
        // Do we have an intermediate action?
        { // Yes - create new empty rule for it
          NonTerminal n = newSymbol();
          GrammarSymbol[] sequence = new GrammarSymbol[1];
          int offset = ruleSequence.size()-1;
          sequence[0] = n;
          new Rule(sequence,
                   offsetActionNodes(ruleAction,offset),
                   offsetActionNodes(ruleUndoAction,offset),
                   nodeCast).setXmlInfo(ruleXmlInfo);

          ruleSequence.add(n);
          if (containsHeadReference)
            warning("pseudo-variable $$ in intermediate action "+location()); 
          resetActionParameters();
        }
      // No - just record the symbol.
      ruleSequence.add(symbol);
    }

  private final void resetRuleParameters ()
    {
      ruleTag = null;           // reset the tag to nil
      ruleSequence.clear();     // reset the sequence of grammar symbols
      ruleXmlInfo = null;	// reset the xml info to nil
      resetActionParameters();
    }

  private final void resetActionParameters ()
    {
      ruleAction = null;                // reset the bottom-up (forward) semantic action
      ruleUndoAction = null;            // reset the top-down (backward) semantic action
      ruleActionCast = "";              // reset the bottom-up action's type cast
      ruleUndoActionCast = "";          // reset the top-down action's type cast
      nodeCast = false;                 // reset the LHS's node's type cast to none
      containsHeadReference = false;    // reset the head reference flag to false
    }

  /**
    * Returns a <tt>String</tt> which corresponds to the input <tt>String</tt>
    * <tt>action</tt> in which all pseudo-variables have been replaced by the
    * appropriate expressions denoting the corresponding stack reference offsets.
    * The <tt>offset</tt> input parameter is the length <i>n</i> of the rule's RHS:
    *
    * <pre>
    *          r[0] -> r[1], ..., r[n]
    * </pre>
    * so that <tt>$i</tt> is mapped to the RHS's symbol <tt>r[i]</tt> which
    * will be located at the stack reference <i>n-i</i> at parse time.
    *
    * This scans the action for node reference patterns of the form
    * <tt>"($rule$,</tt><i>i</i><tt>)"</tt> and replaces them with
    * <tt>"($rule$,offset-</tt><i>i</i><tt>)"</tt>. This works correctly
    * for calls such that <tt>offset == RHS.size()-1</tt>.
    *
    * @param action a String (program code)
    * @param offset an integer (rule's RHS's size)
    * @return the action where rule node references are now parse stack references.
    */
  private final String offsetActionNodes (String action, int offset)
    {
      if (action == null || action.length() == 0)
        // if no action, get out!
        return EMPTY_ACTION;

      StringBuilder buffer  // result accumulator
        = new StringBuilder(bufferSize);

      int length = action.length();

      int i = 0;
      char c = action.charAt(i);

      for (;;)
        {
          if (c < '0' || c > '9')
            { // copy verbatim all chars but numerals
              buffer.append(c);
              if (++i == length) break;
              c = action.charAt(i);
              continue;
            }
          if (nodeReference(action,i))
            // Is this numeral following a node reference?
            { // yes: read off the node's number
              int v = 0;
              do
                {
                  v = v * 10 + (c - '0');
                  if (++i == length) break;
                  c = action.charAt(i);
                }
              while ('0' <= c && c <= '9');
              // and translate it as its stack offset
              buffer.append(String.valueOf(v-offset));
            }
          else
            { // copy it, and all the numerals following it, verbatim
              do
                {
                  buffer.append(c);
                  if (++i == length) break;
                  c = action.charAt(i);
                }
              while ('0' <= c && c <= '9');
            }
          if (i == length) break;
        }

      // return the string value of the result accumulator
      return buffer.toString();
    }

  /**
    * Returns <tt>true</tt> iff there is a node reference (of the form
    * "<tt>pattern</tt>") ending at index <tt>i</tt> in the input
    * <tt>String action</tt>.
    */
  private final boolean nodeReference (String action, String pattern, int i)
    {
      int width = pattern.length();
      return (i>width-1 && action.substring(i-width,i).equals(pattern));
    }

  /**
    * Returns <tt>true</tt> iff there is a node reference (of the form
    * "<tt>($rule$,</tt>") ending at index <tt>i</tt> in the input
    * <tt>String action</tt>.
    */
  private final boolean nodeReference (String action, int i)
    {
      return nodeReference (action,"($rule$,",i);
    }

  /**
    * Reads a semantic action into either of this grammar's
    * <tt>ruleAction</tt> (resp., <tt>ruleUndoAction</tt>)
    * variable (a <tt>String</tt>).
    */
  private final void readAction () throws Exception
    {
      if (ruleAction == null)
        // No semantic action has been read:
        // Translate forward into the semantic action
        translateRuleAction(true);
      else
        // The forward semantic action has already been read:
        if (isDynamic && ruleUndoAction == null)
          // Undo actions are allowed but no undo action has been read:
          // Translate backward into the undo semantic action.
          translateRuleAction(false);
        else
          complain("Illegal action occurrence in rule");
    }

  private final HashMap nodeCastTable = new HashMap();

  /**
   * Translates the forward or backward semantic action and
   * sets it into the global variable <tt>String</tt>s:
   * <tt>ruleAction</tt> and <tt>ruleUndoAction</tt>.
   * Each of these is assumed equal to <tt>null</tt> when
   * <tt>translateRuleAction</tt> is called.
   *
   * @param forward     the action's direction
   */
  private final void translateRuleAction (boolean forward) throws Exception
    {
      setSyntax(RAW_MODE);      // Make all characters ordinary
      nodeCastTable.clear();    // Clear the cast table
      StringBuilder buffer = new StringBuilder(bufferSize);
      int nesting = 1;          // Nesting of braces

      while (nesting > 0)
        {
          switch (getToken())
            {
            case BEGIN_SCOPE:
              buffer.append((char)BEGIN_SCOPE);
              nesting++;
              break;
            case END_SCOPE:
              if (nesting > 1) buffer.append((char)END_SCOPE);
              nesting--;
              break;
            case PSEUDO_VAR:
              buffer.append(stackReference(forward));
              break;
            case NEWLINE:
              buffer.append("\n  ");
              break;
            case TAB:
//               buffer.append("        ");
              buffer.append("\t  ");
              break;
            default:
              buffer.append((char)st.ttype);
            }
        }

      setSyntax(NORMAL_MODE);

      if (forward)
        ruleAction = ruleActionCast+buffer.toString();
      else
        ruleUndoAction = ruleUndoActionCast+buffer.toString();
    }

  private final int readNumber (int c) throws Exception
    {
      int v = 0;
      do
        {
          v = v * 10 + (c - '0');
          c = getToken();
        }
      while ('0' <= c && c <= '9');
      st.pushBack();
      return v;
    }

  /**
   * Returns the <tt>String</tt> corresponding to the translation
   * into a stack reference of a pseudo-variable in a semantic action.
   *
   * @param forward is <tt>true</tt> if top-down action, <tt>false</tt> otherwise.
   */
  private final String stackReference (boolean forward) throws Exception
    {
      String stref = null;
      int n;                            // the stack offset;
      int token = getToken();
      // NB: we read in ordinary mode (one char at a time)
      switch (token)
        {
        case PSEUDO_VAR: // This is the result (LHS) to push on the stack
          // check if typecast is needed & translate as '$head$'
          stref = typeCast(forward,0,"$head$");
          containsHeadReference = true;
          break;
        case '1': case '2': case '3':   // translate as reference at offset n
        case '4': case '5': case '6':   // after checking whether to typecast
        case '7': case '8': case '9':
          n = readNumber(token);
          stref = typeCast(forward,n,"node($rule$,"+n+")");
          break;
        case '0':  // translate as deep reference
          stref = deepStackReference(0,"node($rule$,0)");
          break;
        case '-':  // translate as deep reference
          n = -readNumber(token);
          stref = deepStackReference(n,"node($rule$,"+n+")");
          break;
        default:
          complain("Ill-formed pseudovariable");
        }
      return stref;
    }

  /**
   * Returns the <tt>String</tt> corresponding to the translation
   * of a stack reference at given offset <i>n</i> by generating
   * all appropriate and necessary type adjustments in the case
   * where the parse node it references has been declared of a
   * more specific type by the <tt>%nodeclass</tt> command. In
   * the process, when a type cast must be generated, the action
   * (or undo action) is prepended with the appropriate local
   * declarations and type checking code.
   *
   * @param forward is <tt>true</tt> if top-down action, <tt>false</tt> otherwise.
   * @param n the stack offset
   * @param reference the (partial) reference translation thus far
   */
  private final String typeCast (boolean forward, int n, String reference)
      throws Exception
    {
      StringBuilder buffer =new StringBuilder(bufferSize);
 
      if (n >= ruleSequence.size())
        complain("Pseudovariable out of range: $"+n);

      GrammarSymbol symbol = (GrammarSymbol)ruleSequence.get(n);

      String type = symbol instanceof NonTerminal ? ((NonTerminal)symbol).nodeType : null;
      String ref;

      if (type == null)
        ref = reference;
      else
        {
          nodeCast = true;
          ref = "$node"+n+"$";
          if (!nodeCastTable.containsKey(ref))
            {
              buffer.append("    ").append(type).append(" ").append(ref);
              if (n == 0)
                buffer.append(" = new ").append(type).append("($head$);")
                      .append("\n                 $head$ = (").append(type)
                      .append(")").append(ref).append(";\n");
              else
                buffer.append(";\n                if (").append(reference)
                      .append(" instanceof ").append(type).append(")")
                      .append("\n                   ").append(ref).append(" = (")
                      .append(type).append(")").append(reference).append(";")
                      .append("\n                 else").append("\n                 {")
                      .append("\n                     ").append(ref).append(" = new ")
                      .append(type).append("(").append(reference).append(");")
                      .append("\n                     replaceStackNode($rule$,")
                      .append(n).append(",").append(ref).append(");")
                      .append("\n                   }\n");
              nodeCastTable.put(ref,ref);
            }
        }
      // Export the local buffer according to the direction:
      if (forward)
        ruleActionCast += buffer.toString();
      else
        ruleUndoActionCast += buffer.toString();
      // Return the translated referent:

      return ref;
    }

  private final String deepStackReference (int n, String reference) throws Exception
    {
      if (!nodeCastTable.containsKey(reference))
        {
          nodeCastTable.put(reference,reference);
          warning("Unsafe stack reference: "+n+" "+location()
		  +" - no type cast was generated");
        }
      return reference;
    }

  private final void readRemainder () throws Exception
    {
      readingRemainderSection = true;

      // all characters are ordinary at this point
      // and the next token to be read is COMMAND_START.
      getToken();       // skip the COMMAND_START

      StringBuilder buffer = new StringBuilder(bufferSize);

      for (;;)
        {
          switch (getToken())
            {
            case StreamTokenizer.TT_EOF:
              if (!isVacuous(buffer))
                ancillaryClasses.add(buffer);
              return;
            case COMMAND_START:
              if (st.peek() == USEFILE_COMMAND_ID.charAt(0))
                // e.g., a 'u' must follows the COMMAND_START (%usefile)
                {
                  setSyntax(NORMAL_MODE);
                  getToken();
                  if (commandCode(st.sval) == USEFILE_COMMAND)
                    {
                      if (!isVacuous(buffer))
                        {
                          ancillaryClasses.add(buffer);
                          buffer = new StringBuilder(bufferSize);
                        }
                      declareList(ancillaryClasses);
                    }
                  else
		    buffer.append(st.sval);
                  continue;
                }
            default:
              buffer.append((char)st.ttype);
            }
        }
    }

  private static final boolean isVacuous (StringBuilder buffer)
    {
      for (int i = 0; i<buffer.length(); i++)
        if (!Character.isWhitespace(buffer.charAt(i))) return false;
      return true;
    }

  /** <h1>BUILDING THE GRAMMAR</h1> */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                    BUILDING     THE     GRAMMAR                    \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  static final void warning (String msg)
    {
      out.println("!!! WARNING: "+msg);
    }    

  static final void loudWarning (String msg)
    {
      //      Misc.beep();
      warning(msg);
    }    

  private final void buildGrammar () throws Exception
    {
      reportProgress();
      preprocessGrammar();
      reportProgress();
      computeStates();
      reportProgress();
      propagateLookaheads();
      reportProgress();
    }

  /** <h1>ANALYZING THE GRAMMAR</h1> */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                    ANALYZING    THE     GRAMMAR                    \\  
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  private final void preprocessGrammar () throws Exception
    {
      computeFirsts();
      computeLGraph();
      computePaths();
    }

  /**
   * This computes the FIRST set for all the symbols in the grammar.
   * This set is defined in the Dragon Book, page 189. This method
   * uses the fix-point algorithm described there. It also computes
   * the FIRST sets for all item suffixes for all the rules.
   */
  private final void computeFirsts () throws Exception
    {
      // Initialize the FIRST set of all terminals to contain the
      // terminal itself.
      Iterator e = terminals.iterator();
      while (e.hasNext())
        {
          Terminal t = (Terminal)e.next();
          t.first = new SetOf(terminals);
          if (t.isEmpty())
            t.isNullable = true;
          else
            t.first.add(t);
        }

      // Initialize the FIRST set of all non terminals to the empty set.
      e = nonterminals.iterator();
      while (e.hasNext())
        {
          NonTerminal n = (NonTerminal)e.next();
          n.first = new SetOf(terminals);
        }
      
      // Close all the FIRST sets by fixpoint iteration.
      int index;
      SetOf previous;
      boolean progress;

      do
        {
          progress = false;
          e = rules.iterator();
          while (e.hasNext())
            {
              Rule rule = (Rule)e.next();
              NonTerminal head = rule.head();

              index = 1;
              previous = new SetOf(head.first);
                  
              for (int i=1; i<rule.sequence.length; i++)
                {
                  GrammarSymbol s = rule.sequence[i];
                  head.first.union(s.first);
                  if (!s.isNullable) break;
                  index++;
                }

              progress |= !head.first.isEqualTo(previous);

              if (index > rule.nullableIndex)
                {
                  progress = true;                
                  rule.nullableIndex = index;
                }

              if (rule.nullableIndex == rule.sequence.length)
                {
                  progress |= !head.isNullable;
                  head.isNullable = true;
                }
            }
        }
      while (progress);

      // Checks whether some non-terminals are groundless (i.e., never
      // derive any terminal).
      checkEmptyFirsts();

      // Compute FIRST sets for all item suffixes.
      for (e = items.iterator(); e.hasNext();)
        ((Item)e.next()).computeSuffixFirst();
    }

  private final void checkEmptyFirsts () throws Exception
    {
      ArrayList groundless = new ArrayList();
      Iterator e = nonterminals.iterator();

      while (e.hasNext())
        {
          NonTerminal n = (NonTerminal)e.next();
          if (n.first.isEmpty() && !n.isNullable)
	    groundless.add(n);
        }

      if (!groundless.isEmpty())
        {
          warning("Groundless nonterminal symbols.");   
          if (groundless.size() > 1)
            err.print("\n*** These non-terminal symbols do ");
          else
            err.print("\n*** This non-terminal symbol does ");
          err.println("not derive any terminal:\n");

          e = groundless.iterator();
          while (e.hasNext()) err.println("\t"+e.next());
          err.println();

          if (!permissible)
            {
              err.println("*** Check the grammar in File "+grammarPathedName+
                          ": correct it, or use Jacc with the -i option.");
              complain("Groundless nonterminal symbols");
            }
        }
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * The domain of the L relation. It will contain all the nonterminals
   * <tt>X</tt> such that <tt>X L Y</tt> for some <tt>Y</tt>.
   */
  private SetOf LDomain;

  /**
   * The range of the L relation. It will contain all the nonterminals
   * <tt>Y</tt> such that <tt>X L Y</tt> for some <tt>X</tt>.
   */
  private SetOf LRange;

  /**
   * The roots of the L relation (<i>i.e.,</i> <tt>LDomain-LRange</tt>).
   */
  private SetOf LRoots;

  /**
   * The inner nodes of the L relation (<i>i.e.,</i> in both <tt>LDomain</tt>
   * and <tt>LRange</tt>).
   */
  private SetOf LInners;

  /**
   * A set containing all the L-related nonterminals in topological
   * order (w.r.t. a depth-first spanning tree of the L-graph). This
   * ordering used backwards allows to minimize the number of fixpoint
   * iterations when computing the transitive closure of the L relation
   * and in the computation of the PATH sets.
   */
  private ArrayList LOrder = new ArrayList();

  /**
   * This computes the "<tt><i><b>L</b></i></tt>-graph" of the
   * grammar. It is the reflexive transitive closure of the
   * <tt><i><b>L</b></i></tt> relation on nonterminals defined as: <tt>A
   * <i><b>L</b></i> B</tt> <i>iff</i> <tt>A -> B ...</tt> for some
   * production rule. It sets the <tt>LSet</tt> field of all
   * nonterminals to the set of their
   * <tt><i><b>L</b></i></tt>-successors in 0 or more steps.
   */
   private void computeLGraph ()
    {
      NonTerminal n;

      // Initialize each nonterminal's LSet to contain that nonterminal
      // and the set of nonterminals that are the leftmost symbols in a
      // rule for that nonterminal. In other words, computes the reflexive
      // closure of the L relation. It also initializes the paths for
      // each nonterminal with an empty path to itself.
      LDomain = new SetOf(nonterminals);
      LRange  = new SetOf(nonterminals);

      for (Iterator ns=nonterminals.iterator(); ns.hasNext();)
        {
          n = (NonTerminal)ns.next();
          n.initPaths();
          n.LSet = new SetOf(nonterminals);
          n.LSet.add(n);
          for (Iterator rs=n.rules.iterator(); rs.hasNext();)
            {
              GrammarSymbol s = ((Rule)rs.next()).leftMost();
              if (s instanceof NonTerminal)
                {
                  n.LSet.add(s);
                  LDomain.add(n);
                  LRange.add(s);
                }
            }
        }

      LRoots = SetOf.minus(LDomain,LRange);
      LInners = SetOf.intersection(LDomain,LRange);

      // Build the (generalized) topological ordering of nonterminals
      // using a depth-first traversal of the L-relation graph. Note
      // that we first push the inner nodes into the stack and then
      // the roots. This guarantees that all paths through the L-graph
      // will be explored, including cycles unreached from any root.

      Stack stack = new Stack();
      for (Iterator ns=LInners.iterator(); ns.hasNext();)
        stack.push(ns.next());
      for (Iterator ns=LRoots.iterator(); ns.hasNext();)
        stack.push(ns.next());
          
      SetOf deja_vu = new SetOf(nonterminals);

      while (!stack.empty())
        {
          n = (NonTerminal)stack.pop();
          if (!deja_vu.contains(n))
            {
              LOrder.add(n);
              deja_vu.add(n);
              for (Iterator ns=n.LSet.iterator(); ns.hasNext();)
                stack.push(ns.next());
            }
        }

      // Iterate backwards through the ordered L-related nonterminals
      // (i.e., from leaves to roots) closing the LSets until nothing
      // more is added to any LSet.
      boolean progress;
      SetOf temp;
      do
        {
          progress = false;
          for (int i = LOrder.size()-1; i>=0; i--)
            {
              n = (NonTerminal)LOrder.get(i);
              temp = new SetOf(n.LSet);
              for (Iterator ns=n.LSet.iterator(); ns. hasNext();)
                temp.union(((NonTerminal)ns.next()).LSet);
              progress |= !n.LSet.isEqualTo(temp);
              n.LSet = temp;
            }
        }
      while (progress);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * This computes the sets <tt>PATH(A,B)</tt> for nonterminals
   * <tt>A</tt> and <tt>B</tt> such that <tt>A <i><b>L</b></i>\*
   * B</tt>. It is defined as the union of all <tt>FIRST(<span
   * style="font-family:Symbol">S</span><sub>n</sub>...<span
   * style="font-family:Symbol">S</span><sub>1</sub>)</tt> such that
   * <tt>A -> A<sub>1</sub> <span
   * style="font-family:Symbol">S</span><sub>1</sub></tt>, ..., and
   * <tt>A<sub>n-1</sub> -> B <span
   * style="font-family:Symbol">S</span><sub>n</sub></tt>, where the
   * <tt><span style="font-family:Symbol">S</span><sub>i</sub></tt>'s
   * are <i>sentential forms</i> (<i>i.e.</i>, sequences of terminal and
   * non-terminal symbols).
   *
   * <p>
   *
   * It is computed like the <tt><i><b>L</b></i>\*</tt> relation but now
   * the gathered information is finer. Indeed, it is not sufficient
   * only to record that <tt>X <i><b>L</b></i>\* Y</tt>, we also need to
   * remember through which particular rule this relation is
   * realized. Therefore, we now need to compute the relation <tt>A
   * <i><b>L<sub>R</sub></b></i> B</tt> which holds iff <tt>A
   * <i><b>L</b></i> B</tt> using the specific rule <tt><i>R</i> = A ->
   * B <span style="font-family:Symbol">S</span></tt>.  Thus, we can no
   * longer rely only on just a set of nonterminals like
   * <tt>LSet</tt>. We now need, for each nonterminal <tt>A</tt> in the
   * domain of <tt><i><b>L</b></i></tt>, a table associating a
   * nonterminal <tt>B</tt> to a set of rules <tt><i><b>R</b></i> =
   * {<i>R</i><sub>1</sub>, ..., <i>R</i><sub>k</sub>}</tt> such that
   * <tt>A <i><b>L<sub>R<sub>i</sub></sub></b></i> B</tt>, for some
   * <tt><i>i</i></tt> in <tt>{1,...,k}</tt>. However, it is more
   * convenient to have these edges backwards for calculating the path
   * sets.
   *
   * <p>
   *
   * Therefore, this method builds, for each nonterminal <tt>A</tt> in
   * the range of L, a table associating a nonterminal <tt>B</tt> to a
   * set of rules <tt><i><b>R</b></i></tt> such that <tt>B
   * <i><b>L<sub>R<sub>i</sub></sub></b></i> A</tt>, for some
   * <tt><i>i</i></tt> in <tt>{1,...,k}</tt>. These reverse links are
   * recorded in a nonterminal's <tt>LTable</tt>.
   */
   private void computePaths ()
    {
      NonTerminal n;
      Rule r;

      // Initialize LTables for nonterminals in the domain of L by
      // creating reverse rule-labeled links from each non-terminal
      // N to P whenever P L N.
      for (Iterator ns=LDomain.iterator(); ns.hasNext();)
        {
          n = (NonTerminal)ns.next();
          for (Iterator rs=n.rules.iterator(); rs.hasNext();)
            {
              r = (Rule)rs.next();
              GrammarSymbol s = r.leftMost();
              if (s instanceof NonTerminal)
                ((NonTerminal)s).addLRule(n,r); // Note the reverse links!
            }
        }

      // Iterate backwards through the topologically ordered L-related
      // nonterminals (i.e., from leaves to roots) building the rule paths
      // until none needs to be added; that is, until FIRST sets along
      // all paths are stationary.
      boolean progress;
      ArrayList newPaths = null;

      do
        {
          progress = false;

          // For all nonterminals in the L-Graph:
          for (int i = LOrder.size()-1; i>=0; i--)
            {
              n = (NonTerminal)LOrder.get(i);

              // If n is not a root
              if (!LRoots.contains(n))
                {
                  ArrayList paths = n.paths;

                  newPaths = new ArrayList();

                  // For each path starting from n:
                  for (Iterator ps = paths.iterator(); ps.hasNext();)
                    {
                      RulePath p = (RulePath)ps.next();

                      // For each L-predecessor pn of n:
                      for (Iterator pns = n.LTable.keySet().iterator();
                           pns.hasNext();)
                        {
                          NonTerminal pn = (NonTerminal)pns.next();

                          SetOf rules = n.getLRules(pn);

                          // For each rule linking pn to n:
                          for (Iterator rls = rules.iterator(); rls.hasNext();)
                            {
                              r = (Rule)rls.next();

                              // Record the path p.r:
                              newPaths.add(p.prepend(r));
                            }
                        }
                    }

                  // Validate the new paths:
                  for (Iterator ps = newPaths.iterator(); ps.hasNext();)
                    {
                      RulePath newp = (RulePath)ps.next();
                      progress |= newp.start.addPath(newp);                   
                    }
                }
            }
        }
      while (progress);
    }

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  /**
   * A stack to store newly generated states before they can be
   * processed to generate their successors.
   */
  private final Stack new_states = new Stack();

  /**
   * Adds the given state to the set of states, and records it
   * in the <tt>new_state</tt> stack.
   */
  private final void addNewState (State state)
    {
      state.add();
      scount++;
//        Misc.printErase(String.valueOf(scount));
      stateTable.put(state,state);
      new_states.push(state);
    }

  /**
   * Given a newly generated state, this checks whether it already
   * exists. If so, returns the previously recorded state. If not,
   * records this as a new state and returns it.
   */
  final State checkNewState(State state)
    {
      State old_state = (State)stateTable.get(state);
      if (old_state != null) return old_state;
      addNewState(state);
      return state;
    }

  private State initState;
      
  /**
   * This computes the LALR states. It generates the states
   * using the LR(0) set-of-items construction (see Dragon Book,
   * pages 223-225), and then separates the kernel items from the
   * set of items set in each state and computes the PRED relation
   * defined in the Park-Choe-Chang article by fixpoint iteration.
   */
  private final void computeStates()
    {
      Item startitem = getItem((Rule)(START.rules.get(0)),1);    
      initState = new State(startitem);
      initState.closure();
      addNewState(initState);

      do ((State)(new_states.pop())).computeNextStates();
      while (!new_states.isEmpty());
      
      State state = null;
      boolean progress = false;
      
      for (int i=0; i<scount; i++)
        {
          (state = getState(i)).extractKernels();
          progress |= state.computePreds();
        }

      while (progress)
        {
          progress = false;

          for (int i=0; i<scount; i++)
            progress |= getState(i).computePreds();
        }
    }

  /**
   * This propagates lookahead symbols through the LALR(1) kernel
   * states as explained in the Park-Choe-Chang article.
   */
  private final void propagateLookaheads ()
    {
      computeFollows();
      computeLookaheads();
    }

  /**
   * This computes the DeRemer-Penello state-dependent FOLLOW sets
   * using the digraph method due to them but with the technique of
   * precomputed L-graph paths described in the Park-Choe-Chang article.
   */
  private final void computeFollows ()
    {
      buildFollowGraph();
      orderFollowGraph();
      closeFollowGraph();
    }

  /**
   * The number of Follow objects.
   */
  int fcount = 0;

  /**
   * The set of all follow objects - to serve as the reference base
   * of <tt>SetOf(follows)</tt> objects.
   */
  ArrayList follows = new ArrayList();
  
  /**
   * The domain of the Follow digraph. 
   */
  SetOf FDomain = new SetOf(follows);

  /**
   * The range of the Follow digraph. 
   */
  SetOf FRange  = new SetOf(follows);

  /**
   * The set of roots of the Follow digraph (<i>i.e.</i>, <tt>FDomain
   * - FRange</tt>).
   */
  SetOf FRoots  = new SetOf(follows);

  /**
   * The set of inner nodes of the Follow digraph (<i>i.e.</i>, in both
   * <tt>FDomain</tt> and <tt>FRange</tt>).
   */
  SetOf FInners  = new SetOf(follows);

  /**
   * This builds the Follow digraph using the paths and the state kernels.
   * It also generates and initializes the set of all possible follow sets.
   * This is Algorithm ND1 in the Park-Choe-Chang article.
   */
  private final void buildFollowGraph ()
    {
//        // Put EOI in the follow set of the user-defined start symbol in state 0:
//        initState.getFollow(startSymbol())
//          .addFollows(new SetOf(terminals).add(END_OF_INPUT));

      // Put EOI in the follow set of the ROOTS symbol in state 0:
      initState.getFollow(ROOTS)
        .addFollows(new SetOf(terminals).add(END_OF_INPUT));

      // For each state:
      for (Iterator sts=states.iterator(); sts.hasNext();)
        {
          State state = (State)sts.next();

          // For each kernel item in the state:
          for (Iterator its = state.kernels.iterator(); its.hasNext();)
            {
              Item item = (Item)its.next();

              // If the item has a nonterminal marker, let item = A -> PB.S
              if (item.markerIsNonTerminal())
                {
                  NonTerminal marker = (NonTerminal)item.marker();

                  // Let f be FOLLOW(state,B)
                  Follow f = state.getFollow(marker);

                  // Add FIRST(S) to FOLLOW(state,B)
                  f.addFollows(item.suffixFirst);

                  // If FIRST(S) derives the empty symbol
                  if (item.isNullable)
                    {
                      // For each state pred in PRED(state,P)
                      for (Iterator prds=item.pred(state).iterator(); prds.hasNext();)
                        {
                          State pred = (State)prds.next();

                          // For each kernel item pitem in pred
                          for (Iterator pits = pred.kernels.iterator(); pits.hasNext();)
                            {
                              Item pitem = (Item)pits.next();

                              // If pitem has a nonterminal marker - let pitem = C -> QD.T
                              if (pitem.markerIsNonTerminal())
                                {
                                  NonTerminal pmarker = (NonTerminal)pitem.marker();
                                  NonTerminal head = item.rule.head();
                                  
                                  // if D L* A
                                  if (pmarker.LSet.contains(head))
                                    {
                                      SetOf pathset = pmarker.path(head);

                                      // Add PATH(D,A) to FOLLOW(state,B)
                                      f.addFollows(pathset);
                                      
                                      Follow pf = pred.getFollow(pmarker);
                                      if (pmarker.pathIsNullable(head))
                                        {
                                          pf.addPred(f);
                                          FDomain.add(pf);
                                          FRange.add(f);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

      FRoots = SetOf.minus(FDomain,FRange);
      FInners = SetOf.intersection(FDomain,FRange);
    }

  ArrayList FOrder;

  /**
   * This builds the topological ordering of a depth-first traversal of
   * the follow digraph.
   */
  private final void orderFollowGraph ()
    {
      Follow f;
      FOrder = new ArrayList(fcount);

      // Build the (generalized) topological ordering of Follow nodes
      // using a depth-first traversal of the follow digraph. Again,
      // we first push the inner nodes into the stack and then the
      // roots to guarantee that all paths through the digraph will
      // be explored, including cycles unreached from any root.

      Stack stack = new Stack();
      for (Iterator fs=FInners.iterator(); fs.hasNext();)
        stack.push(fs.next());
      for (Iterator fs=FRoots.iterator(); fs.hasNext();)
        stack.push(fs.next());      
  
      SetOf deja_vu = new SetOf(follows);
      while (!stack.empty())
        {
          f = (Follow)stack.pop();
          if (!deja_vu.contains(f))
            {
              FOrder.add(f);
              deja_vu.add(f);
              for (Iterator fs=f.preds.iterator(); fs.hasNext();)
                stack.push(fs.next());
            }
        }
    }

  /**
   * This traverses the digraph using the ordering and computes the
   * follow sets until there is no change.
   */
  private final void closeFollowGraph ()
    {
      boolean progress;
      Follow f;

      do
        {
          progress = false;

          for (int i = 0; i<FOrder.size(); i++)
            {
              f = (Follow)FOrder.get(i);

              for (Iterator fs=f.preds.iterator(); fs.hasNext();)
                {
                  Follow pf = (Follow)fs.next();
                  progress |= pf.addFollows(f.follows);
                }
            }
        }
      while (progress);
    }

  /**
   * Computes the lookahead sets using the FOLLOW sets and the paths.
   * This is Algorithm ND3 in the Park-Choe-Chang article.
   */
  final void computeLookaheads ()
    {
      // For each state:
      for (Iterator sts=states.iterator(); sts.hasNext();)
        {
          State state = (State)sts.next();

          // For each item in this state:
          for (Iterator its=state.items.iterator(); its.hasNext();)
            {
              Item item = (Item)its.next();

              // If this item corresponds to a reduction:
              if (item.isFinal())
                {
                  SetOf la = item.initLookaheads(state);
                  NonTerminal head = item.rule.head();

                  // For each state pred in PRED(state,item):
                  for (Iterator prds=item.pred(state).iterator(); prds.hasNext();)
                    {
                      State pred = (State)prds.next();

                      // If this is the initial symbol, add its FOLLOW set:
                      if (head.isSTART())
                        la.union(pred.follow(startSymbol()));

                      // For each item pitem in the kernels of pred:
                      for (Iterator pits = pred.kernels.iterator(); pits.hasNext();)
                        {
                          Item pitem = (Item)pits.next();
                          // If pitem's marker is a nonterminal:
                          if (pitem.markerIsNonTerminal())
                            {
                              NonTerminal marker = (NonTerminal)pitem.marker();

                              // If pitem's marker L* item's head:
                              if (marker.LSet.contains(head))
                                {
                                  SetOf pathset = marker.path(head);

                                  // Add PATH(marker,head):
                                  la.union(pathset);

                                  // If the path derives empty, add FOLLOW(pred,marker):
                                  if (marker.pathIsNullable(head))
                                    la.union(pred.follow(marker));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

  /** <h1> COMPUTING THE XML DTD FROM ANNOTATIONS </h1> */

  /**
   * <b>IDEA</b>: Define classes for DTD contents models and constructs,
   * and extract the DTD from the grammar's XML serialization
   * annotations starting from the grammar start symbol and navigating
   * down the rules to the terminals. These DTD classes are the types of
   * DTD objects and are all subclasses of the abstract class
   * <b><tt>DtdObject</tt></b>. These classes and the DTD constructs
   * they denote are:
   *
   * <p>
   *
   * <center>
   * <table border="1" cellpadding="10">
   * <tr><td><i><b>class</b></i></td>        <td><i><b>DTD construct</b></i></td></tr>
   * <tr><td><b><tt>DtdEmpty</tt></b></td>   <td><tt>EMPTY</tt></td></tr>
   * <tr><td><b><tt>DtdPcdata</tt></b></td>  <td><tt>#PCDATA</tt></td></tr>
   * <tr><td><b><tt>DtdElement</tt></b></td> <td><i>element name</i></td></tr>
   * <tr><td><b><tt>DtdConcat</tt></b></td>  <td><i>concatenation</i></td></tr>
   * <tr><td><b><tt>DtdChoice</tt></b></td>  <td><i>union</i></td></tr>
   * <tr><td><b><tt>DtdOption</tt></b></td>  <td><i>optional</i></td></tr>
   * <tr><td><b><tt>DtdStar</tt></b></td>    <td><i>0 or more</i></td></tr>
   * <tr><td><b><tt>DtdPlus</tt></b></td>    <td><i>1 or more</i></td></tr>
   * </table>
   * </center>
   *
   * <p>
   *
   * We can thus inductively build the DTD implicit structure actually
   * <i>computing</i> it <i>algebraically</i> along the way as
   * illustrated on the following annotated grammar example.
   *
   * <p>
   *
   * <h5>Example</h5>
   *
   * <pre>
   *
   * Expr
   *  : Term PartialExpr
   *  ;
   *
   * PartialExpr
   *  : // empty
   *  | '+' Term PartialExpr
   *  ;
   *
   * Term
   *  : Factor PartialTerm
   *  ;
   *
   * PartialTerm
   *  : // empty
   *  | '\*' Factor PartialTerm
   *  ;
   *
   * Factor
   *  : NUMBER
   *  | IDENT
   *  | VAR
   *  ;
   * </pre>
   *
   * <p>
   *
   * <ol>
   *
   * <li> For any non-terminal, consider only its subset of defining
   * rules that have a non-null <tt>_xmlinfo</tt>. Using the remaining
   * rules, if any, compute the corresponding DTD contents model of the
   * LHS from those computed for the grammar symbols in the RHS. If the
   * remaining rule set is empty, make that a <tt>DtdEmpty</tt>
   * object. If there are remaininf rules, use the information in its
   * <tt>_xmlInfo</tt> and algebra on the types of DTD objects to
   * compute the value of its resulting DTD.

   * <li> For any terminal that is not punctuation and has a non-null
   * <tt>_xmlinfo</tt>, set its DTD contents model to
   * <tt>DtdEmpty(_xmlinfo.localName())</tt>,
   * <tt>DtdPcdata(_xmlinfo.localName())</tt>, or
   * <tt>DtdElement(_xmlinfo.localName())</tt> depending on the case.

   * </ol>
   *
   */


  /** <h1> SHOWING THE GRAMMAR</h1> */

  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
  //                      SHOWING    THE     GRAMMAR                    \\
  //\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

  int progress = 0;

  private final void reportProgress ()
    {
      switch (++progress)
        {
        case 1: reportProgress_1(); break;
        case 2: reportProgress_2(); break;
        case 3: reportProgress_3(); break;
        case 4: reportProgress_4(); break;
        case 5: reportProgress_5(); break;
        }
    }

  private final long now ()
    {
      return System.currentTimeMillis();
    }

  private final void reportProgress_1 ()
    {
      startTime = now();

      if (verbosity > Verbose.QUIET)
	{
	  out.println("*** This is Jacc: Just another compiler compiler... ");
// 	  out.println("*** Version "+Options.getVersion()+
// 		      " of "+Options.getGenDate());
	  out.println("*** Run of "+new Date());
	  out.println("*** Reading grammar in file "+grammarPathedName+" ... ");
	}

      readingStart = now();      
    }

  private final void reportProgress_2 ()
    {
      if (verbosity > Verbose.QUIET)
        {
          if (verbosity > Verbose.NORMAL)
            {
              out.println("***\t... in "+
                          (now()-readingStart)+" ms");
              if (verbosity > Verbose.VERBOSE) showRules();
            }

          out.println("*** Starting grammar analysis ... ");

          if (verbosity > Verbose.NORMAL)
            out.println("***\tPreprocessing the grammar ... ");
        }

      preprocessStart = now();
    }

  private final void reportProgress_3 ()
    {
      if (verbosity > Verbose.NORMAL)
        {
          out.println("***\t... in "+
                      (now()-preprocessStart)+" ms");
          if (verbosity > Verbose.VERBOSE) showSymbols();
        }

      if (verbosity > Verbose.NORMAL)
        out.println("***\tBuilding canonical LR states ... ");

      buildingStart = now();      
    }

  private final void reportProgress_4 ()
    {
      if (verbosity > Verbose.NORMAL)
        {
          out.println("***\t ... in "+
                      (now()-buildingStart)+" ms");
          out.println("***\tPropagating lookahead symbols ... ");
        }

      propagationStart = now();      
    }

  private final void reportProgress_5 ()
    {
      if (verbosity > Verbose.NORMAL)
        out.println("***\t ... in "+
                    (now() - propagationStart)+" ms");

      analysisTime = now() - preprocessStart;

      if (verbosity > Verbose.QUIET)
        out.println("*** Grammar analysis completed in "+analysisTime+" ms.");
      
      totalTime = now() - startTime;
    }

  final void showStates()
    {
      out.println("\n");
      for (Iterator e=states.iterator(); e.hasNext();)
        ((State)(e.next())).show();
    }

  private final void showRules ()
    {
      out.println("\nRULES:\n");
      for (Iterator e=rules.iterator(); e.hasNext();)
        out.println(e.next());
      out.println();
    }

  final static String associativity (Terminal t)
    {
      if (t.isOperator) return "dynamic";

      switch (t.associativity)
        {
        case LEFT_ASSOCIATIVE:
          return "left";
        case RIGHT_ASSOCIATIVE:
          return "right";
        }

      return "none";
    }     

  private final void showSymbols ()
    {
      out.println("\nTERMINALS:\n");
      out.println("\t----------\t-------------\t--------");
      out.println("\tPRECEDENCE\tASSOCIATIVITY\tTERMINAL");
      out.println("\t----------\t-------------\t--------");

      for (Iterator e=terminals.iterator(); e.hasNext();)
        {
          Terminal t = (Terminal)(e.next());
          out.println("["+t.index()+"]"
                      //+"\t"+prologPrecedence(t.precedence)
                      +"\t"+t.precedence
                      +"\t\t"+associativity(t)
                      +"\t\t"+t
                      );
        }
      out.println("\t----------------------------------------");

      if (!operators.isEmpty())
        {
          out.println("\nDYNAMIC OPERATORS:\n");
          out.println("\t----------\t---------\t--------\t--------");
          out.println("\tPRECEDENCE\tSPECIFIER\tOPERATOR\tCATEGORY");
          out.println("\t----------\t---------\t--------\t--------");
          for (Iterator e=operators.iterator(); e.hasNext();)
            {
              Operator o = (Operator)(e.next());
              out.println("["+o.index()+"]"
                          //+"\t"+prologPrecedence(o.precedence)
                          +"\t"+o.precedence
                          +"\t\t"+o.specifier()
                          +"\t\t"+o.name
                          +"\t\t"+o.category.name
                          );
            }
          out.println("\t---------------------------------------------------------");
        }

      out.println("\nNON TERMINALS:\n");
      for (Iterator e=nonterminals.iterator(); e.hasNext();)
        {
          NonTerminal n = (NonTerminal)(e.next());
          out.println("  [" + n.index() + "]\t" + n
                            + (n.isNullable?"\t(nullable)":"")
                            + "\n\tFIRST:\t " + n.first
                            + "\n\tLSet:\t " + n.LSet
                            + "\n"
                            );
        }
      out.println();
    }

}

