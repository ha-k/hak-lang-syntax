//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import java.util.Date;
import java.io.PrintStream;
import hlt.language.util.Verbose;

/**
 * This class centralizes all the options that are properties
 * of a given grammar. That is, it defines a property's name,
 * its default value, as well as its accessor and modifier methods.
 *
 * @version     Last modified on Mon Mar 26 08:34:45 2018 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public final class Options
{
  /* ******************************************************************* */
  /**
   * The resources location path.
   */  
     
  private static String resourcesPath =
    "~/hlt/classes/resources";

  public final static String getResourcesPath ()
    {
      return resourcesPath;
    }

  public final static String setResourcesPath (String path)
    {
      System.err.println("*** Setting Options.resourcesPath = \""+path+"\"");
      return (resourcesPath = path);
    }

  /* ******************************************************************* */

  /**
   * The Jacc generation date.
   */  
     
  private static String genDate = "(no date)";

  public final static String getGenDate ()
    {
      return genDate;
    }

  public final static String setGenDate ()
    {
      return (genDate = (new Date()).toString());
    }

  /* ******************************************************************* */

  /**
   * The Jacc Version number string.
   */  
     
  private static String jaccVersion = "(no number)";
  
  public final static String getVersion ()
    {
      return jaccVersion;
    }

  public final static void setVersion (String version)
    {
      jaccVersion = version;
    }

  /* ******************************************************************* */

  /**
   * The maximal number of parser table initialization constructions
   * per method. This may be set to a lesser value than the default
   * (1000) if the parser's initialization method is too large.
   */  
     
  private static int initMethodSize = 1000;
  
  public final static int getInitMethodSize ()
    {
      return initMethodSize;
    }

  public final static void setInitMethodSize (int size)
    {
        initMethodSize = size;
    }

  /* ******************************************************************* */

  /**
   * The output stream (default: <tt>System.out</tt>).
   */  
     
  private static PrintStream out = System.out;

  public final static PrintStream getOutStream()
    {
      return out;
    }

  public final static void setOutStream(PrintStream o)
    {
      out = o;
    }

  /* ******************************************************************* */

  /**
   * The error stream (default: <tt>System.err</tt>).
   */  
     
  private static PrintStream err = System.err;

  public final static PrintStream getErrStream ()
    {
      return err;
    }

  public final static void setErrStream (PrintStream e)
    {
      err = e;
    }

  /* ******************************************************************* */

  /**
   * The level of verbosity (default: <tt>Verbose.NORMAL</tt>).
   */  
     
  private static int verbosity = Verbose.NORMAL;

  public final static int getVerbosity ()
    {
      return verbosity;
    }

  public final static void setVerbosity (int v)
    {
      verbosity = v;
    }

  /* ******************************************************************* */

  /**
   * The file path separator (default: <tt>/</tt>).
   */  
     
  private static String separator = "/";

  public final static String getSeparator ()
    {
      return separator;
    }

  public final static void setSeparator (String s)
    {
      separator = s;
    }

  /* ******************************************************************* */

  /**
   * The pathed prefix of the grammar's file name (default: <tt>./Grammar</tt>).
   */  
     
  private static String grammarPathedName = "./Grammar";

  public final static String getGrammarPathedName ()
    {
      return grammarPathedName;
    }

  public final static void setGrammarPathedName (String path)
    {
      grammarPathedName = path;
    }

  /* ******************************************************************* */

  /**
   * The prefix of the grammar's file name (default: <tt>Grammar</tt>).
   */  
     
  private static String grammarPrefix = "Grammar";

  public final static String getGrammarPrefix ()
    {
      return grammarPrefix;
    }

  public final static void setGrammarPrefix (String g)
    {
      grammarPrefix = g;
    }

  /* ******************************************************************* */

  /**
   * The suffix of the grammar's file name (default: <tt>grm</tt>).
   */  
     
  private static String grammarSuffix = "grm";

  public final static String getGrammarSuffix ()
    {
      return grammarSuffix;
    }

  public final static void setGrammarSuffix (String e)
    {
      grammarSuffix = e;
    }

  /* ******************************************************************* */

  /**
   * The name of the grammar's file.
   */  
     
  public final static String getGrammarName ()
    {
      return grammarPrefix+"."+ grammarSuffix;
    }

  /* ******************************************************************* */

  /**
   * Sets and gets the <tt>%include</tt> command's file base reference.
   */  
     
  private static String _includeBase = ".";

  public static final void setIncludeBase (String base)
    {
      _includeBase = base;
    }

  public static final String includeBase ()
    {
      return _includeBase;
    }

  /* ******************************************************************* */

  /**
   * Sets and gets the destination directory where to generate the parser.
   */  
     
  private static String _destination = ".";

  public static final void setDestination (String dest)
    {
      _destination = dest;
    }

  public static final String destination ()
    {
      return _destination;
    }

  /* ******************************************************************* */

  /**
   * The prefix of the parser's file name (default: <tt>Parser</tt>).
   */  
     
  private static String parserPrefix = "Parser";

  public final static String getParserPrefix ()
    {
      return parserPrefix;
    }

  public final static void setParserPrefix (String str)
    {
      parserPrefix = str;
    }

  /* ******************************************************************* */

  /**
   * When set, this prevents building the parser (default: <tt>false</tt>).
   */  
     
  private static boolean noParser = false;

  public static boolean getNoParser ()
    {
      return noParser;
    }

  public final static void setNoParser (boolean flag)
    {
      noParser = flag;
    }

  /* ******************************************************************* */

  /**
   * When set, this generates the grammar's documentation (default: <tt>false</tt>).
   */  
     
  private static boolean docOnly = false;

  public static boolean getDocOnly ()
    {
      return docOnly;
    }

  public final static void setDocOnly (boolean flag)
    {
      docOnly = flag;
    }

  /* ******************************************************************* */

  /**
   * When set, this copies the resources files (default: <tt>true</tt>).
   */  
     
  private static boolean copyResourceFiles = true;

  public static boolean copyResourceFiles ()
    {
      return copyResourceFiles;
    }

  public final static void setCopyResourceFiles (boolean flag)
    {
      copyResourceFiles = flag;
    }

  /* ******************************************************************* */

  /**
   * When set, allows an incomplete grammar (default: <tt>false</tt>).
   */  
     
  private static boolean permissible = false;

  public final static boolean getPermissible ()
    {
      return permissible;
    }

  public final static void setPermissible (boolean p)
    {
      permissible = p;
    }

  /* ******************************************************************* */

  /**
   * When <tt>true</tt>, R/R conflicts are resolved using rule precedence (namely,
   * choose the rule with higher tag precedence, or if they are equal, choose
   * the rule that comes first in the grammmar. When <tt>false</tt>, pick the
   * rule that comes first in the grammar; (default: <tt>false</tt>).
   */  
     
  private static boolean resolveRRsWithPrecedence = false;

  public final static boolean resolveRRsWithPrecedence ()
    {
      return resolveRRsWithPrecedence;
    }

  public final static void setResolveRRsWithPrecedence (boolean p)
    {
      resolveRRsWithPrecedence = p;
    }

  /* ******************************************************************* */

  /**
   * When <tt>true</tt>, a <tt>DynamicParser</tt> will bundle unresolved
   * conflicts into a choice action such that each option in the choice
   * action are tried in turn, backtracking upon failure, up to the size
   * of the sizes of the choice and trail stacks (<i>i.e.</i>, the values
   * of <tt>CHOICE_HISTORY</tt> and/or <tt>TRAIL_HISTORY</tt>.
   */  
     
  private static boolean allowChoiceActions = false;

  public final static boolean allowChoiceActions ()
    {
      return allowChoiceActions;
    }

  public final static void setAllowChoiceActions (boolean p)
    {
      allowChoiceActions = p;
    }

  /* ******************************************************************* */

  /**
   * How many choice points to keep by a dynamic parser (default: 10).
   */
     
  private static int CHOICE_HISTORY = 10;

  public final static void setChoiceHistory (int size)
    {
      if (size>=0) CHOICE_HISTORY = size;
    }

  public final static int getChoiceHistory ()
    {
      return CHOICE_HISTORY;
    }

  /* ******************************************************************* */

  /**
   * How much history may be undone by a dynamic parser (default: 100).
   */
     
  private static int TRAIL_HISTORY = 100;

  public final static void setTrailHistory (int size)
    {
      if (size>=0) TRAIL_HISTORY = size;
    }

  public final static int getTrailHistory ()
    {
      return TRAIL_HISTORY;
    }

}
