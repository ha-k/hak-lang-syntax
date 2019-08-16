//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\
// PLEASE DO NOT EDIT WITHOUT THE EXPLICIT CONSENT OF THE AUTHOR! \\
//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\

package hlt.language.syntax;

import hlt.language.tools.Misc;

/**
 * Signals that something is wrong with the grammar.
 *
 * @see         Grammar
 * @version     Last modified on Fri Apr 13 19:55:10 2012 by hak
 * @author      <a href="mailto:hak@acm.org">Hassan A&iuml;t-Kaci</a>
 * @copyright   &copy; <a href="http://www.hassan-ait-kaci.net/">by the author</a>
 */

public class BadGrammarException extends Exception
{
  /**
   * Constructs a new BadGrammarException with no message
   */
  public BadGrammarException ()
    {
      this("");
    }

  /**
   * Constructs a new BadGrammarException with a message.
   */
  public BadGrammarException (String msg)
    {
      Options.getErrStream().println("*** Bad grammar: "+msg);
      Options.getErrStream().println("*** Aborting grammar analysis!");
    }
}
